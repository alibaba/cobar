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
 * (created at 2011-10-19)
 */
package com.alibaba.cobar.route.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class PermutationUtil {

    public static Set<String> permutateSQL(String delimiter, String... frag) {
        return new PermutationGenerator(frag).setDelimiter(delimiter).permutateSQL();
    }

    public static final class PermutationGenerator {
        private String delimiter = ", ";
        private List<String> fragments;

        public PermutationGenerator(String... frag) {
            if (frag == null || frag.length <= 0)
                throw new IllegalArgumentException();
            List<String> list = new ArrayList<String>(frag.length);
            for (String f : frag) {
                list.add(f);
            }
            this.fragments = list;
        }

        public PermutationGenerator setDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Set<String> permutateSQL() {
            return gen(fragments);
        }

        private Set<String> gen(List<String> frag) {
            if (frag.size() == 1)
                return new HashSet<String>(frag);
            Set<String> rst = new HashSet<String>();
            for (int i = 0; i < frag.size(); ++i) {
                String prefix = frag.get(i) + delimiter;
                List<String> fragnew = new ArrayList<String>();
                for (int j = 0; j < frag.size(); ++j) {
                    if (j != i) {
                        fragnew.add(frag.get(j));
                    }
                }
                Set<String> smallP = gen(fragnew);
                for (String s : smallP) {
                    rst.add(prefix + s);
                }
            }
            return rst;
        }
    }

}
