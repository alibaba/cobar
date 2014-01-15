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
 * (created at 2011-8-4)
 */
package com.alibaba.cobar.route.hint;

import java.sql.SQLSyntaxErrorException;

import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.route.RouteResultsetNode;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class DataNodeHintParser extends HintParser {

    @Override
    public void process(CobarHint hint, String hintName, String sql) throws SQLSyntaxErrorException {
        if (currentChar(hint, sql) == '[') {
            for (;;) {
                nextChar(hint, sql);
                Pair<Integer, Integer> pair = parseDataNode(hint, sql);
                hint.addDataNode(pair.getKey(), pair.getValue());
                switch (currentChar(hint, sql)) {
                case ',':
                    continue;
                case ']':
                    nextChar(hint, sql);
                    return;
                default:
                    throw new SQLSyntaxErrorException("err for dataNodeId: " + sql);
                }
            }
        } else {
            Pair<Integer, Integer> pair = parseDataNode(hint, sql);
            hint.addDataNode(pair.getKey(), pair.getValue());
        }
    }

    /**
     * first char is not separator
     */
    private Pair<Integer, Integer> parseDataNode(CobarHint hint, String sql) {
        int start = hint.getCurrentIndex();
        int ci = start;
        for (; isDigit(sql.charAt(ci)); ++ci) {
        }
        Integer nodeIndex = Integer.parseInt(sql.substring(start, ci));
        Integer replica = RouteResultsetNode.DEFAULT_REPLICA_INDEX;
        hint.setCurrentIndex(ci);
        if (currentChar(hint, sql) == '.') {
            nextChar(hint, sql);
            start = hint.getCurrentIndex();
            ci = start;
            for (; isDigit(sql.charAt(ci)); ++ci) {
            }
            replica = Integer.parseInt(sql.substring(start, ci));
            hint.setCurrentIndex(ci);
        }
        return new Pair<Integer, Integer>(nodeIndex, replica);
    }
}
