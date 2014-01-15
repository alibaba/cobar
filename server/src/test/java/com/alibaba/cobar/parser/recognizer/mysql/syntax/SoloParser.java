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
 * (created at 2011-5-9)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_AS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_JOIN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_SELECT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_UNION;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_LEFT_PAREN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class SoloParser extends MySQLParser {
    public SoloParser(MySQLLexer lexer) {
        super(lexer);
    }

    public Refs refs() throws SQLSyntaxErrorException {
        Refs refs = new Refs();
        for (;;) {
            Ref ref = ref();
            refs.addRef(ref);
            if (lexer.token() == PUNC_COMMA) {
                lexer.nextToken();
            } else {
                return refs;
            }
        }
    }

    public Ref buildRef(Ref first) throws SQLSyntaxErrorException {
        for (; lexer.token() == KW_JOIN;) {
            lexer.nextToken();
            Ref temp = factor();
            first = new Join(first, temp);
        }
        return first;
    }

    public Ref ref() throws SQLSyntaxErrorException {
        return buildRef(factor());
    }

    public Ref factor() throws SQLSyntaxErrorException {
        String alias;
        if (lexer.token() == PUNC_LEFT_PAREN) {
            lexer.nextToken();
            Ref queryRefs = refsOrQuery();
            match(PUNC_RIGHT_PAREN);
            if (queryRefs instanceof Query) {
                match(KW_AS);
                alias = lexer.stringValue();
                lexer.nextToken();
                return new SubQuery((Query) queryRefs, alias);
            }
            return queryRefs;
        }
        String tableName = lexer.stringValue();
        lexer.nextToken();
        if (lexer.token() == KW_AS) {
            lexer.nextToken();
            alias = lexer.stringValue();
            lexer.nextToken();
            return new Factor(tableName, alias);
        }
        return new Factor(tableName, null);
    }

    /**
     * first <code>(</code> has been consumed
     */
    public Ref refsOrQuery() throws SQLSyntaxErrorException {
        Ref temp;
        Refs rst;
        Union u;
        switch (lexer.token()) {
        case KW_SELECT:
            u = new Union();
            for (;;) {
                Select s = selectPrimary();
                u.addSelect(s);
                if (lexer.token() == KW_UNION) {
                    lexer.nextToken();
                } else {
                    break;
                }
            }
            if (u.selects.size() == 1) {
                return u.selects.get(0);
            }
            return u;
        case PUNC_LEFT_PAREN:
            lexer.nextToken();
            temp = refsOrQuery();
            match(PUNC_RIGHT_PAREN);
            if (temp instanceof Query) {
                if (temp instanceof Select) {
                    if (lexer.token() == KW_UNION) {
                        u = new Union();
                        u.addSelect((Select) temp);
                        while (lexer.token() == KW_UNION) {
                            lexer.nextToken();
                            temp = selectPrimary();
                            u.addSelect((Select) temp);
                        }
                        return u;
                    }
                }
                if (lexer.token() == KW_AS) {
                    lexer.nextToken();
                    String alias = lexer.stringValue();
                    temp = new SubQuery((Query) temp, alias);
                    lexer.nextToken();
                } else {
                    return temp;
                }
            }
            // ---- build factor complete---------------

            temp = buildRef(temp);
            // ---- build ref complete---------------
            break;
        default:
            temp = ref();
        }

        if (lexer.token() == PUNC_COMMA) {
            rst = new Refs();
            rst.addRef(temp);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                temp = ref();
                rst.addRef(temp);
            }
            return rst;
        }
        return temp;
    }

    /**
     * first <code>SELECT</code> or <code>(</code> has not been consumed
     */
    private Select selectPrimary() throws SQLSyntaxErrorException {
        Select s = null;
        if (lexer.token() == PUNC_LEFT_PAREN) {
            lexer.nextToken();
            s = selectPrimary();
            match(PUNC_RIGHT_PAREN);
            return s;
        }
        match(KW_SELECT);
        return new Select();
    }

    public static void main(String[] args) throws SQLSyntaxErrorException {
        String sql = "   ( ( select union select union select)  as j join    (((select union (select)) as t    )   join t2 ) ,(select)as d), t3)";
        // String sql =
        // "((select) as s1, ((((   select  union select          ) as t2)) join (((t2),t4 as t))) ), t1 aS T1";
        // String sql =
        // "  (( select union select union select)  as j  ,(select)as d), t3";
        System.out.println(sql);
        MySQLLexer lexer = new MySQLLexer(sql);
        lexer.nextToken();
        SoloParser p = new SoloParser(lexer);
        Refs refs = p.refs();
        System.out.println(refs);
    }

}

interface Ref {
}

class Factor implements Ref {
    String tableName;
    String alias;

    public Factor(String tableName, String alias) {
        super();
        this.tableName = tableName;
        this.alias = alias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tableName);
        sb.append(" AS ");
        sb.append(alias);
        return sb.toString();
    }
}

class SubQuery implements Ref {
    Query u;
    String alias;

    public SubQuery(Query u, String alias) {
        super();
        this.u = u;
        this.alias = alias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(u);
        sb.append(") AS ");
        sb.append(alias);
        return sb.toString();
    }

}

class Join implements Ref {
    Ref left;
    Ref right;

    public Join(Ref left, Ref right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(left.toString());
        sb.append(" JOIN ");
        sb.append(right.toString());
        sb.append(">");
        return sb.toString();
    }
}

class Refs implements Ref {
    List<Ref> refs = new ArrayList<Ref>();

    public void addRef(Ref ref) {
        refs.add(ref);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < refs.size(); ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(refs.get(i).toString());
        }
        sb.append("]");
        return sb.toString();
    }
}

interface Query {
}

class Union implements Query, Ref {
    List<Select> selects = new ArrayList<Select>();

    public void addSelect(Select select) {
        selects.add(select);
    }

    @SuppressWarnings("unused")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Select s : selects) {
            sb.append(" UNION SELECT");
        }
        String rst = sb.toString();
        int i = rst.indexOf("UNION");
        if (i >= 0) {
            rst = rst.substring(i + "UNION".length());
        }
        return rst;
    }
}

class Select implements Query, Ref {
    @Override
    public String toString() {
        return "SELECT";
    }
}
