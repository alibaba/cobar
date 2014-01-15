/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * (created at 2012-6-15)
 */
package com.alibaba.cobar.config.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.cobar.config.model.ClusterConfig;
import com.alibaba.cobar.config.model.QuarantineConfig;
import com.alibaba.cobar.config.model.SystemConfig;
import com.alibaba.cobar.config.model.UserConfig;
import com.alibaba.cobar.config.util.ConfigException;
import com.alibaba.cobar.config.util.ConfigUtil;
import com.alibaba.cobar.config.util.ParameterMapping;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
@SuppressWarnings("unchecked")
public class XMLServerLoader {
    private final SystemConfig system;
    private final Map<String, UserConfig> users;
    private final QuarantineConfig quarantine;
    private ClusterConfig cluster;

    public XMLServerLoader() {
        this.system = new SystemConfig();
        this.users = new HashMap<String, UserConfig>();
        this.quarantine = new QuarantineConfig();
        this.load();
    }

    public SystemConfig getSystem() {
        return system;
    }

    public Map<String, UserConfig> getUsers() {
        return (Map<String, UserConfig>) (users.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(users));
    }

    public QuarantineConfig getQuarantine() {
        return quarantine;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }

    private void load() {
        InputStream dtd = null;
        InputStream xml = null;
        try {
            dtd = XMLServerLoader.class.getResourceAsStream("/server.dtd");
            xml = XMLServerLoader.class.getResourceAsStream("/server.xml");
            Element root = ConfigUtil.getDocument(dtd, xml).getDocumentElement();
            loadSystem(root);
            loadUsers(root);
            this.cluster = new ClusterConfig(root, system.getServerPort());
            loadQuarantine(root);
        } catch (ConfigException e) {
            throw e;
        } catch (Throwable e) {
            throw new ConfigException(e);
        } finally {
            if (dtd != null) {
                try {
                    dtd.close();
                } catch (IOException e) {
                }
            }
            if (xml != null) {
                try {
                    xml.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void loadQuarantine(Element root) {
        NodeList list = root.getElementsByTagName("host");
        for (int i = 0, n = list.getLength(); i < n; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                String host = e.getAttribute("name").trim();
                if (quarantine.getHosts().containsKey(host)) {
                    throw new ConfigException("host duplicated : " + host);
                }

                Map<String, Object> props = ConfigUtil.loadElements(e);
                String[] users = SplitUtil.split((String) props.get("user"), ',', true);
                HashSet<String> set = new HashSet<String>();
                if (null != users) {
                    for (String user : users) {
                        UserConfig uc = this.users.get(user);
                        if (null == uc) {
                            throw new ConfigException("[user: " + user + "] doesn't exist in [host: " + host + "]");
                        }

                        if (null == uc.getSchemas() || uc.getSchemas().size() == 0) {
                            throw new ConfigException("[host: " + host + "] contains one root privileges user: " + user);
                        }
                        if (set.contains(user)) {
                            throw new ConfigException("[host: " + host + "] contains duplicate user: " + user);
                        } else {
                            set.add(user);
                        }
                    }
                }
                quarantine.getHosts().put(host, set);
            }
        }
    }

    private void loadUsers(Element root) {
        NodeList list = root.getElementsByTagName("user");
        for (int i = 0, n = list.getLength(); i < n; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                String name = e.getAttribute("name");
                UserConfig user = new UserConfig();
                user.setName(name);
                Map<String, Object> props = ConfigUtil.loadElements(e);
                user.setPassword((String) props.get("password"));
                String schemas = (String) props.get("schemas");
                if (schemas != null) {
                    String[] strArray = SplitUtil.split(schemas, ',', true);
                    user.setSchemas(new HashSet<String>(Arrays.asList(strArray)));
                }
                if (users.containsKey(name)) {
                    throw new ConfigException("user " + name + " duplicated!");
                }
                users.put(name, user);
            }
        }
    }

    private void loadSystem(Element root) throws IllegalAccessException, InvocationTargetException {
        NodeList list = root.getElementsByTagName("system");
        for (int i = 0, n = list.getLength(); i < n; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Map<String, Object> props = ConfigUtil.loadElements((Element) node);
                ParameterMapping.mapping(system, props);
            }
        }
    }

}
