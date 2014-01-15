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
 * (created at 2012-6-13)
 */
package com.alibaba.cobar.config.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.cobar.config.model.rule.RuleAlgorithm;
import com.alibaba.cobar.config.model.rule.RuleConfig;
import com.alibaba.cobar.config.model.rule.TableRuleConfig;
import com.alibaba.cobar.config.util.ConfigException;
import com.alibaba.cobar.config.util.ConfigUtil;
import com.alibaba.cobar.config.util.ParameterMapping;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
@SuppressWarnings("unchecked")
public class XMLRuleLoader {
    private final static String DEFAULT_DTD = "/rule.dtd";
    private final static String DEFAULT_XML = "/rule.xml";

    private final Map<String, TableRuleConfig> tableRules;
    private final Set<RuleConfig> rules;
    private final Map<String, RuleAlgorithm> functions;

    public XMLRuleLoader(String ruleFile) {
        this.rules = new HashSet<RuleConfig>();
        this.tableRules = new HashMap<String, TableRuleConfig>();
        this.functions = new HashMap<String, RuleAlgorithm>();
        load(DEFAULT_DTD, ruleFile == null ? DEFAULT_XML : ruleFile);
    }

    public XMLRuleLoader() {
        this(null);
    }

    public Map<String, TableRuleConfig> getTableRules() {
        return (Map<String, TableRuleConfig>) (tableRules.isEmpty() ? Collections.emptyMap() : tableRules);
    }

    public Set<RuleConfig> listRuleConfig() {
        return (Set<RuleConfig>) ((rules == null || rules.isEmpty()) ? Collections.emptySet() : rules);
    }

    public Map<String, RuleAlgorithm> getFunctions() {
        return (Map<String, RuleAlgorithm>) (functions.isEmpty() ? Collections.emptyMap() : functions);
    }

    private void load(String dtdFile, String xmlFile) {
        InputStream dtd = null;
        InputStream xml = null;
        try {
            dtd = XMLRuleLoader.class.getResourceAsStream(dtdFile);
            xml = XMLRuleLoader.class.getResourceAsStream(xmlFile);
            Element root = ConfigUtil.getDocument(dtd, xml).getDocumentElement();
            loadFunctions(root);
            loadTableRules(root);
        } catch (ConfigException e) {
            throw e;
        } catch (Exception e) {
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

    private void loadTableRules(Element root) throws SQLSyntaxErrorException {
        NodeList list = root.getElementsByTagName("tableRule");
        for (int i = 0, n = list.getLength(); i < n; ++i) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                String name = e.getAttribute("name");
                if (tableRules.containsKey(name)) {
                    throw new ConfigException("table rule " + name + " duplicated!");
                }
                NodeList ruleNodes = e.getElementsByTagName("rule");
                int length = ruleNodes.getLength();
                List<RuleConfig> ruleList = new ArrayList<RuleConfig>(length);
                for (int j = 0; j < length; ++j) {
                    RuleConfig rule = loadRule((Element) ruleNodes.item(j));
                    ruleList.add(rule);
                    rules.add(rule);
                }
                tableRules.put(name, new TableRuleConfig(name, ruleList));
            }
        }
    }

    private RuleConfig loadRule(Element element) throws SQLSyntaxErrorException {
        Element columnsEle = ConfigUtil.loadElement(element, "columns");
        String[] columns = SplitUtil.split(columnsEle.getTextContent(), ',', true);
        for (int i = 0; i < columns.length; ++i) {
            columns[i] = columns[i].toUpperCase();
        }
        Element algorithmEle = ConfigUtil.loadElement(element, "algorithm");
        String algorithm = algorithmEle.getTextContent();
        return new RuleConfig(columns, algorithm);
    }

    private void loadFunctions(Element root) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        NodeList list = root.getElementsByTagName("function");
        for (int i = 0, n = list.getLength(); i < n; ++i) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                String name = e.getAttribute("name");
                if (functions.containsKey(name)) {
                    throw new ConfigException("rule function " + name + " duplicated!");
                }
                String clazz = e.getAttribute("class");
                RuleAlgorithm function = createFunction(name, clazz);
                ParameterMapping.mapping(function, ConfigUtil.loadElements(e));
                functions.put(name, function);
            }
        }
    }

    private RuleAlgorithm createFunction(String name, String clazz) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> clz = Class.forName(clazz);
        if (!RuleAlgorithm.class.isAssignableFrom(clz)) {
            throw new IllegalArgumentException("rule function must implements " + RuleAlgorithm.class.getName()
                    + ", name=" + name);
        }
        Constructor<?> constructor = null;
        for (Constructor<?> cons : clz.getConstructors()) {
            Class<?>[] paraClzs = cons.getParameterTypes();
            if (paraClzs != null && paraClzs.length == 1) {
                Class<?> paraClzs1 = paraClzs[0];
                if (String.class.isAssignableFrom(paraClzs1)) {
                    constructor = cons;
                    break;
                }
            }
        }
        if (constructor == null) {
            throw new ConfigException("function " + name + " with class of " + clazz
                    + " must have a constructor with one parameter: String funcName");
        }
        return (RuleAlgorithm) constructor.newInstance(name);
    }

}
