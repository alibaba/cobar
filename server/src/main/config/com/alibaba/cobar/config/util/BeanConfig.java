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
package com.alibaba.cobar.config.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.util.ObjectUtil;

public class BeanConfig implements Cloneable {
    private static final ReflectionProvider refProvider = new ReflectionProvider();

    private String name;
    private String className;
    private Map<String, Object> params = new HashMap<String, Object>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String beanObject) {
        this.className = beanObject;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object create(boolean initEarly) throws IllegalAccessException, InvocationTargetException {
        Object obj = null;
        try {
            obj = refProvider.newInstance(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new ConfigException(e);
        }
        ParameterMapping.mapping(obj, params);
        if (initEarly && (obj instanceof Initializable)) {
            ((Initializable) obj).init();
        }
        return obj;
    }

    @Override
    public Object clone() {
        BeanConfig bc = null;
        try {
            bc = getClass().newInstance();
        } catch (InstantiationException e) {
            throw new ConfigException(e);
        } catch (IllegalAccessException e) {
            throw new ConfigException(e);
        }
        if (bc == null) {
            return null;
        }
        bc.className = className;
        bc.name = name;
        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(params);
        return bc;
    }

    @Override
    public int hashCode() {
        int hashcode = 37;
        hashcode += (name == null ? 0 : name.hashCode());
        hashcode += (className == null ? 0 : className.hashCode());
        hashcode += (params == null ? 0 : params.hashCode());
        return hashcode;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof BeanConfig) {
            BeanConfig entity = (BeanConfig) object;
            boolean isEquals = equals(name, entity.name);
            isEquals = isEquals && equals(className, entity.getClassName());
            isEquals = isEquals && (ObjectUtil.equals(params, entity.params));
            return isEquals;
        }
        return false;
    }

    private static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

}
