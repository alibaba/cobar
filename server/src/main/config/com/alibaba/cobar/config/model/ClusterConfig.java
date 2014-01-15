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
package com.alibaba.cobar.config.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.cobar.config.util.ConfigException;
import com.alibaba.cobar.config.util.ConfigUtil;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ClusterConfig {
    private final Map<String, CobarNodeConfig> nodes;
    private final Map<String, List<String>> groups;

    public ClusterConfig(Element root, int port) {
        nodes = Collections.unmodifiableMap(loadNode(root, port));
        groups = Collections.unmodifiableMap(loadGroup(root, nodes));
    }

    public Map<String, CobarNodeConfig> getNodes() {
        return nodes;
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

    private static Map<String, CobarNodeConfig> loadNode(Element root, int port) {
        Map<String, CobarNodeConfig> nodes = new HashMap<String, CobarNodeConfig>();
        NodeList list = root.getElementsByTagName("node");
        Set<String> hostSet = new HashSet<String>();
        for (int i = 0, n = list.getLength(); i < n; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                String name = element.getAttribute("name").trim();
                if (nodes.containsKey(name)) {
                    throw new ConfigException("node name duplicated :" + name);
                }

                Map<String, Object> props = ConfigUtil.loadElements(element);
                String host = (String) props.get("host");
                if (null == host || "".equals(host)) {
                    throw new ConfigException("host empty in node: " + name);
                }
                if (hostSet.contains(host)) {
                    throw new ConfigException("node host duplicated :" + host);
                }

                String wei = (String) props.get("weight");
                if (null == wei || "".equals(wei)) {
                    throw new ConfigException("weight should not be null in host:" + host);
                }
                int weight = Integer.valueOf(wei);
                if (weight <= 0) {
                    throw new ConfigException("weight should be > 0 in host:" + host + " weight:" + weight);
                }

                CobarNodeConfig conf = new CobarNodeConfig(name, host, port, weight);
                nodes.put(name, conf);
                hostSet.add(host);
            }
        }
        return nodes;
    }

    private static Map<String, List<String>> loadGroup(Element root, Map<String, CobarNodeConfig> nodes) {
        Map<String, List<String>> groups = new HashMap<String, List<String>>();
        NodeList list = root.getElementsByTagName("group");
        for (int i = 0, n = list.getLength(); i < n; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                String groupName = e.getAttribute("name").trim();
                if (groups.containsKey(groupName)) {
                    throw new ConfigException("group duplicated : " + groupName);
                }

                Map<String, Object> props = ConfigUtil.loadElements(e);
                String value = (String) props.get("nodeList");
                if (null == value || "".equals(value)) {
                    throw new ConfigException("group should contain 'nodeList'");
                }

                String[] sList = SplitUtil.split(value, ',', true);

                if (null == sList || sList.length == 0) {
                    throw new ConfigException("group should contain 'nodeList'");
                }

                for (String s : sList) {
                    if (!nodes.containsKey(s)) {
                        throw new ConfigException("[ node :" + s + "] in [ group:" + groupName + "] doesn't exist!");
                    }
                }
                List<String> nodeList = Arrays.asList(sList);
                groups.put(groupName, nodeList);
            }
        }
        if (!groups.containsKey("default")) {
            List<String> nodeList = new ArrayList<String>(nodes.keySet());
            groups.put("default", nodeList);
        }
        return groups;
    }
}
