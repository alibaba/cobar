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
 * (created at 2011-7-20)
 */
package com.alibaba.cobar.parser.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public interface BinaryOperandCalculator {
    Number calculate(Integer integer1, Integer integer2);

    Number calculate(Long long1, Long long2);

    Number calculate(BigInteger bigint1, BigInteger bigint2);

    Number calculate(BigDecimal bigDecimal1, BigDecimal bigDecimal2);
}
