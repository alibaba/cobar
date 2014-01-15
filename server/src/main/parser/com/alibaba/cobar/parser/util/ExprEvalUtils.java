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
 * (created at 2011-7-19)
 */
package com.alibaba.cobar.parser.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBoolean;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * adapt Java's expression rule into MySQL's
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ExprEvalUtils {
    private static final int CLASS_MAP_DOUBLE = 1;
    private static final int CLASS_MAP_FLOAT = 2;
    private static final int CLASS_MAP_BIG_ING = 3;
    private static final int CLASS_MAP_BIG_DECIMAL = 4;
    private static final int CLASS_MAP_LONG = 5;
    private static final Map<Class<? extends Number>, Integer> classMap = new HashMap<Class<? extends Number>, Integer>(
            5);
    static {
        classMap.put(Double.class, CLASS_MAP_DOUBLE);
        classMap.put(Float.class, CLASS_MAP_FLOAT);
        classMap.put(BigInteger.class, CLASS_MAP_BIG_ING);
        classMap.put(BigDecimal.class, CLASS_MAP_BIG_DECIMAL);
        classMap.put(Long.class, CLASS_MAP_LONG);
    }

    public static boolean obj2bool(Object obj) {
        if (obj == LiteralBoolean.TRUE)
            return true;
        if (obj == LiteralBoolean.FALSE)
            return false;

        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        Number num;
        if (obj instanceof String) {
            num = ExprEvalUtils.string2Number((String) obj);
        } else {
            num = (Number) obj;
        }
        Integer classType = classMap.get(num.getClass());
        if (classType == null) {
            return num.intValue() != 0;
        }
        switch (classType) {
        case CLASS_MAP_BIG_DECIMAL:
            return BigDecimal.ZERO.compareTo((BigDecimal) num) != 0;
        case CLASS_MAP_BIG_ING:
            return BigInteger.ZERO.compareTo((BigInteger) num) != 0;
        case CLASS_MAP_DOUBLE:
            return ((Double) num).doubleValue() != 0d;
        case CLASS_MAP_FLOAT:
            return ((Float) num).floatValue() != 0f;
        case CLASS_MAP_LONG:
            return ((Long) num).longValue() != 0L;
        default:
            throw new IllegalArgumentException("unsupported number type: " + num.getClass());
        }
    }

    private static final int NUM_INT = 1;
    private static final int NUM_LONG = 2;
    private static final int NUM_BIG_INTEGER = 3;
    private static final int NUM_BIG_DECIMAL = 4;

    public static Number calculate(UnaryOperandCalculator cal, Number num) {
        if (num == null)
            return null;
        if (num instanceof Integer)
            return cal.calculate((Integer) num);
        if (num instanceof Long)
            return cal.calculate((Long) num);
        if (num instanceof BigDecimal)
            return cal.calculate((BigDecimal) num);
        if (num instanceof BigInteger)
            return cal.calculate((BigInteger) num);
        throw new IllegalArgumentException("unsupported add calculate: " + num.getClass());
    }

    public static Number calculate(BinaryOperandCalculator cal, Number n1, Number n2) {
        if (n1 == null || n2 == null)
            return null;
        if (n1 instanceof Integer)
            return cal.calculate((Integer) n1, (Integer) n2);
        if (n1 instanceof Long)
            return cal.calculate((Long) n1, (Long) n2);
        if (n1 instanceof BigDecimal)
            return cal.calculate((BigDecimal) n1, (BigDecimal) n2);
        if (n1 instanceof BigInteger)
            return cal.calculate((BigInteger) n1, (BigInteger) n2);
        throw new IllegalArgumentException("unsupported add calculate: " + n1.getClass());
    }

    /**
     * @param obj1 class of String or Number
     */
    public static Pair<Number, Number> convertNum2SameLevel(Object obj1, Object obj2) {
        Number n1, n2;
        if (obj1 instanceof String) {
            n1 = string2Number((String) obj1);
        } else {
            n1 = (Number) obj1;
        }
        if (obj2 instanceof String) {
            n2 = string2Number((String) obj2);
        } else {
            n2 = (Number) obj2;
        }
        if (n1 == null || n2 == null) {
            return new Pair<Number, Number>(n1, n2);
        }
        int l1 = getNumberLevel(n1.getClass());
        int l2 = getNumberLevel(n2.getClass());
        if (l1 > l2) {
            n2 = upTolevel(n2, l1);
        } else if (l1 < l2) {
            n1 = upTolevel(n1, l2);
        }
        return new Pair<Number, Number>(n1, n2);
    }

    private static Number upTolevel(Number num, int level) {
        switch (level) {
        case NUM_INT:
            if (num instanceof Integer)
                return num;
            return num.intValue();
        case NUM_LONG:
            if (num instanceof Long)
                return num;
            return num.longValue();
        case NUM_BIG_INTEGER:
            if (num instanceof BigInteger)
                return num;
            return new BigInteger(num.toString());
        case NUM_BIG_DECIMAL:
            if (num instanceof BigDecimal)
                return num;
            return new BigDecimal(num.toString());
        default:
            throw new IllegalArgumentException("unsupported number level: " + level);
        }
    }

    private static int getNumberLevel(Class<?> clz) {
        if (Integer.class.isAssignableFrom(clz)) {
            return NUM_INT;
        }
        if (Long.class.isAssignableFrom(clz)) {
            return NUM_LONG;
        }
        if (BigInteger.class.isAssignableFrom(clz)) {
            return NUM_BIG_INTEGER;
        }
        if (BigDecimal.class.isAssignableFrom(clz)) {
            return NUM_BIG_DECIMAL;
        }
        throw new IllegalArgumentException("unsupported number class: " + clz);
    }

    public static Number string2Number(String str) {
        if (str == null)
            return null;
        try {
            return new Integer(str);
        } catch (Exception e) {
        }
        try {
            return new Long(str);
        } catch (Exception e) {
        }
        try {
            MySQLLexer lexer = new MySQLLexer(str);
            switch (lexer.token()) {
            case LITERAL_NUM_PURE_DIGIT:
                return lexer.integerValue();
            case LITERAL_NUM_MIX_DIGIT:
                return lexer.decimalValue();
            default:
                throw new IllegalArgumentException("unrecognized number: " + str);
            }
        } catch (SQLSyntaxErrorException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
