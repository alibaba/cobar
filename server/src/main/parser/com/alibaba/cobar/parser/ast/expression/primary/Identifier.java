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
 * (created at 2011-1-23)
 */
package com.alibaba.cobar.parser.ast.expression.primary;

import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class Identifier extends PrimaryExpression {
    public static String unescapeName(String name) {
        return unescapeName(name, false);
    }

    public static String unescapeName(String name, boolean toUppercase) {
        if (name == null || name.length() <= 0) {
            return name;
        }
        if (name.charAt(0) != '`') {
            return toUppercase ? name.toUpperCase() : name;
        }
        if (name.charAt(name.length() - 1) != '`') {
            throw new IllegalArgumentException("id start with a '`' must end with a '`', id: " + name);
        }
        StringBuilder sb = new StringBuilder(name.length() - 2);
        final int endIndex = name.length() - 1;
        boolean hold = false;
        for (int i = 1; i < endIndex; ++i) {
            char c = name.charAt(i);
            if (c == '`' && !hold) {
                hold = true;
                continue;
            }
            hold = false;
            if (toUppercase && c >= 'a' && c <= 'z') {
                c -= 32;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** null if no parent */
    protected Identifier parent;
    /** e.g. "id1", "`id1`" */
    protected final String idText;
    protected final String idTextUpUnescape;

    public Identifier(Identifier parent, String idText) {
        this(parent, idText, idText.toUpperCase());
    }

    public Identifier(Identifier parent, String idText, String idTextUp) {
        this.parent = parent;
        this.idText = idText;
        this.idTextUpUnescape = unescapeName(idTextUp);
    }

    public String getLevelUnescapeUpName(int level) {
        Identifier id = this;
        for (int i = level; i > 1 && id != null; --i) {
            id = id.parent;
        }
        if (id != null) {
            return id.idTextUpUnescape;
        }
        return null;
    }

    /** trim not happen because parent in given level is not exist */
    public static final int PARENT_ABSENT = 0;
    /** trim happen */
    public static final int PARENT_TRIMED = 1;
    /** trim not happen because parent in given not equals to given name */
    public static final int PARENT_IGNORED = 2;

    /**
     * @param level At most how many levels left after trim, must be a positive
     *            integer. e.g. level = 2 for "schema1.tb1.c1", "tb1.c1" is left
     * @param trimSchema upper-case. Assumed that top trimmed parent is schema,
     *            if that equals given schema, do not trim
     * @return {@link #PARENT_ABSENT} or {@link #PARENT_TRIMED}or
     *         {@link #PARENT_IGNORED}
     */
    public int trimParent(int level, String trimSchema) {
        Identifier id = this;
        for (int i = 1; i < level; ++i) {
            if (id.parent == null) {
                return PARENT_ABSENT;
            }
            id = id.parent;
        }
        if (id.parent == null) {
            return PARENT_ABSENT;
        }
        if (trimSchema != null && !trimSchema.equals(id.parent.idTextUpUnescape)) {
            return PARENT_IGNORED;
        } else {
            id.parent = null;
            return PARENT_TRIMED;
        }
    }

    public void setParent(Identifier parent) {
        this.parent = parent;
    }

    public Identifier getParent() {
        return parent;
    }

    public String getIdText() {
        return idText;
    }

    public String getIdTextUpUnescape() {
        return idTextUpUnescape;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ID:");
        if (parent != null) {
            sb.append(parent).append('.');
        }
        return sb.append(idText).toString();
    }

    @Override
    public int hashCode() {
        final int constant = 37;
        int hash = 17;
        if (parent == null) {
            hash += constant;
        } else {
            hash = hash * constant + parent.hashCode();
        }
        if (idText == null) {
            hash += constant;
        } else {
            hash = hash * constant + idText.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof Identifier) {
            Identifier that = (Identifier) obj;
            return objEquals(this.parent, that.parent) && objEquals(this.idText, that.idText);
        }
        return false;
    }

    private static boolean objEquals(Object obj, Object obj2) {
        if (obj == obj2)
            return true;
        if (obj == null)
            return obj2 == null;
        return obj.equals(obj2);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
