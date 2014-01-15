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
package com.alibaba.cobar.route.config;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.cobar.config.loader.SchemaLoader;
import com.alibaba.cobar.config.model.rule.RuleAlgorithm;
import com.alibaba.cobar.config.model.rule.RuleConfig;
import com.alibaba.cobar.config.util.ConfigException;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLFunctionManager;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;
import com.alibaba.cobar.parser.recognizer.mysql.syntax.MySQLExprParser;
import com.alibaba.cobar.parser.recognizer.mysql.syntax.MySQLParser;
import com.alibaba.cobar.route.function.ExpressionAdapter;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class RouteRuleInitializer {
    public static void initRouteRule(SchemaLoader loader) throws SQLSyntaxErrorException {
        Map<String, RuleAlgorithm> functions = loader.getFunctions();
        MySQLFunctionManager functionManager = new MySQLFunctionManager(true);
        buildFuncManager(functionManager, functions);
        for (RuleConfig conf : loader.listRuleConfig()) {
            String algorithmString = conf.getAlgorithm();
            MySQLLexer lexer = new MySQLLexer(algorithmString);
            MySQLExprParser parser = new MySQLExprParser(lexer, functionManager, false, MySQLParser.DEFAULT_CHARSET);
            Expression expression = parser.expression();
            if (lexer.token() != MySQLToken.EOF) {
                throw new ConfigException("route algorithm not end with EOF: " + algorithmString);
            }
            RuleAlgorithm algorithm;
            if (expression instanceof RuleAlgorithm) {
                algorithm = (RuleAlgorithm) expression;
            } else {
                algorithm = new ExpressionAdapter(expression);
            }
            conf.setRuleAlgorithm(algorithm);
        }
    }

    private static void buildFuncManager(MySQLFunctionManager functionManager, Map<String, RuleAlgorithm> functions) {
        Map<String, FunctionExpression> extFuncPrototypeMap = new HashMap<String, FunctionExpression>(functions.size());
        for (Entry<String, RuleAlgorithm> en : functions.entrySet()) {
            extFuncPrototypeMap.put(en.getKey(), (FunctionExpression) en.getValue());
        }
        functionManager.addExtendFunction(extFuncPrototypeMap);
    }
}
