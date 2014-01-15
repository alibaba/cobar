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
 * (created at 2011-1-21)
 */
package com.alibaba.cobar.parser.recognizer.mysql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Abs;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Acos;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Asin;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Atan;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Atan2;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Ceiling;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Conv;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Cos;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Cot;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Crc32;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Degrees;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Exp;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Floor;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Log;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Log10;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Log2;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Oct;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Pi;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Pow;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Radians;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Rand;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Round;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Sign;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Sin;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Sqrt;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Tan;
import com.alibaba.cobar.parser.ast.expression.primary.function.arithmetic.Truncate;
import com.alibaba.cobar.parser.ast.expression.primary.function.bit.BitCount;
import com.alibaba.cobar.parser.ast.expression.primary.function.comparison.Coalesce;
import com.alibaba.cobar.parser.ast.expression.primary.function.comparison.Greatest;
import com.alibaba.cobar.parser.ast.expression.primary.function.comparison.Interval;
import com.alibaba.cobar.parser.ast.expression.primary.function.comparison.Isnull;
import com.alibaba.cobar.parser.ast.expression.primary.function.comparison.Least;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Adddate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Addtime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.ConvertTz;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Curdate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Curtime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Date;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.DateAdd;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.DateFormat;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.DateSub;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Datediff;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Dayname;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Dayofmonth;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Dayofweek;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Dayofyear;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.FromDays;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.FromUnixtime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Hour;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.LastDay;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Makedate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Maketime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Microsecond;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Minute;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Month;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Monthname;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Now;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.PeriodAdd;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.PeriodDiff;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Quarter;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.SecToTime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Second;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.StrToDate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Subdate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Subtime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Sysdate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Time;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.TimeFormat;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.TimeToSec;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Timediff;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Timestamp;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.ToDays;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.ToSeconds;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.UnixTimestamp;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.UtcDate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.UtcTime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.UtcTimestamp;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Week;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Weekday;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Weekofyear;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Year;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Yearweek;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.AesDecrypt;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.AesEncrypt;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Compress;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Decode;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.DesDecrypt;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.DesEncrypt;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Encode;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Encrypt;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Md5;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.OldPassword;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Password;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Sha1;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Sha2;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.Uncompress;
import com.alibaba.cobar.parser.ast.expression.primary.function.encryption.UncompressedLength;
import com.alibaba.cobar.parser.ast.expression.primary.function.flowctrl.If;
import com.alibaba.cobar.parser.ast.expression.primary.function.flowctrl.Ifnull;
import com.alibaba.cobar.parser.ast.expression.primary.function.flowctrl.Nullif;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.BitAnd;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.BitOr;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.BitXor;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Std;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Stddev;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.StddevPop;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.StddevSamp;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.VarPop;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.VarSamp;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Variance;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.Benchmark;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.Charset;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.Coercibility;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.Collation;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.ConnectionId;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.CurrentUser;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.Database;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.FoundRows;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.LastInsertId;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.RowCount;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.User;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.Version;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.Analyse;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.Default;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.GetLock;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.InetAton;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.InetNtoa;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.IsFreeLock;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.IsUsedLock;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.MasterPosWait;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.NameConst;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.ReleaseLock;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.Sleep;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.Uuid;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.UuidShort;
import com.alibaba.cobar.parser.ast.expression.primary.function.misc.Values;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Ascii;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Bin;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.BitLength;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.CharLength;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Concat;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.ConcatWs;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Elt;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.ExportSet;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Field;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.FindInSet;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Format;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Hex;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Insert;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Instr;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Left;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Length;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.LoadFile;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Locate;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Lower;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Lpad;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Ltrim;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.MakeSet;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Ord;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Quote;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Repeat;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Replace;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Reverse;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Right;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Rpad;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Rtrim;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Soundex;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Space;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Strcmp;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Substring;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.SubstringIndex;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Unhex;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Upper;
import com.alibaba.cobar.parser.ast.expression.primary.function.xml.Extractvalue;
import com.alibaba.cobar.parser.ast.expression.primary.function.xml.Updatexml;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLFunctionManager {
    public static enum FunctionParsingStrategy {
        /** not a function */
        _DEFAULT,
        /** ordinary function */
        _ORDINARY,
        CAST,
        POSITION,
        SUBSTRING,
        TRIM,
        AVG,
        COUNT,
        GROUP_CONCAT,
        MAX,
        MIN,
        SUM,
        ROW,
        CHAR,
        CONVERT,
        EXTRACT,
        TIMESTAMPADD,
        TIMESTAMPDIFF,
        GET_FORMAT
    }

    public static final MySQLFunctionManager INSTANCE_MYSQL_DEFAULT = new MySQLFunctionManager(false);
    private final boolean allowFuncDefChange;

    /** non-reserved word named special syntax function */
    private final HashMap<String, FunctionParsingStrategy> parsingStrateg = new HashMap<String, FunctionParsingStrategy>();
    /** non-reserved word named ordinary syntax function */
    private Map<String, FunctionExpression> functionPrototype = new HashMap<String, FunctionExpression>();

    public MySQLFunctionManager(boolean allowFuncDefChange) {
        this.allowFuncDefChange = allowFuncDefChange;
        parsingStrateg.put("CAST", FunctionParsingStrategy.CAST);
        parsingStrateg.put("POSITION", FunctionParsingStrategy.POSITION);
        parsingStrateg.put("SUBSTR", FunctionParsingStrategy.SUBSTRING);
        parsingStrateg.put("SUBSTRING", FunctionParsingStrategy.SUBSTRING);
        parsingStrateg.put("TRIM", FunctionParsingStrategy.TRIM);
        parsingStrateg.put("AVG", FunctionParsingStrategy.AVG);
        parsingStrateg.put("COUNT", FunctionParsingStrategy.COUNT);
        parsingStrateg.put("GROUP_CONCAT", FunctionParsingStrategy.GROUP_CONCAT);
        parsingStrateg.put("MAX", FunctionParsingStrategy.MAX);
        parsingStrateg.put("MIN", FunctionParsingStrategy.MIN);
        parsingStrateg.put("SUM", FunctionParsingStrategy.SUM);
        parsingStrateg.put("ROW", FunctionParsingStrategy.ROW);
        parsingStrateg.put("CHAR", FunctionParsingStrategy.CHAR);
        parsingStrateg.put("CONVERT", FunctionParsingStrategy.CONVERT);
        parsingStrateg.put("EXTRACT", FunctionParsingStrategy.EXTRACT);
        parsingStrateg.put("TIMESTAMPADD", FunctionParsingStrategy.TIMESTAMPADD);
        parsingStrateg.put("TIMESTAMPDIFF", FunctionParsingStrategy.TIMESTAMPDIFF);
        parsingStrateg.put("GET_FORMAT", FunctionParsingStrategy.GET_FORMAT);
        functionPrototype.put("ABS", new Abs(null));
        functionPrototype.put("ACOS", new Acos(null));
        functionPrototype.put("ADDDATE", new Adddate(null));
        functionPrototype.put("ADDTIME", new Addtime(null));
        functionPrototype.put("AES_DECRYPT", new AesDecrypt(null));
        functionPrototype.put("AES_ENCRYPT", new AesEncrypt(null));
        functionPrototype.put("ANALYSE", new Analyse(null));
        functionPrototype.put("ASCII", new Ascii(null));
        functionPrototype.put("ASIN", new Asin(null));
        functionPrototype.put("ATAN2", new Atan2(null));
        functionPrototype.put("ATAN", new Atan(null));
        functionPrototype.put("BENCHMARK", new Benchmark(null));
        functionPrototype.put("BIN", new Bin(null));
        functionPrototype.put("BIT_AND", new BitAnd(null));
        functionPrototype.put("BIT_COUNT", new BitCount(null));
        functionPrototype.put("BIT_LENGTH", new BitLength(null));
        functionPrototype.put("BIT_OR", new BitOr(null));
        functionPrototype.put("BIT_XOR", new BitXor(null));
        functionPrototype.put("CEIL", new Ceiling(null));
        functionPrototype.put("CEILING", new Ceiling(null));
        functionPrototype.put("CHAR_LENGTH", new CharLength(null));
        functionPrototype.put("CHARACTER_LENGTH", new CharLength(null));
        functionPrototype.put("CHARSET", new Charset(null));
        functionPrototype.put("COALESCE", new Coalesce(null));
        functionPrototype.put("COERCIBILITY", new Coercibility(null));
        functionPrototype.put("COLLATION", new Collation(null));
        functionPrototype.put("COMPRESS", new Compress(null));
        functionPrototype.put("CONCAT_WS", new ConcatWs(null));
        functionPrototype.put("CONCAT", new Concat(null));
        functionPrototype.put("CONNECTION_ID", new ConnectionId(null));
        functionPrototype.put("CONV", new Conv(null));
        functionPrototype.put("CONVERT_TZ", new ConvertTz(null));
        functionPrototype.put("COS", new Cos(null));
        functionPrototype.put("COT", new Cot(null));
        functionPrototype.put("CRC32", new Crc32(null));
        functionPrototype.put("CURDATE", new Curdate());
        functionPrototype.put("CURRENT_DATE", new Curdate());
        functionPrototype.put("CURRENT_TIME", new Curtime());
        functionPrototype.put("CURTIME", new Curtime());
        functionPrototype.put("CURRENT_TIMESTAMP", new Now());
        functionPrototype.put("CURRENT_USER", new CurrentUser());
        functionPrototype.put("CURTIME", new Curtime());
        functionPrototype.put("DATABASE", new Database(null));
        functionPrototype.put("DATE_ADD", new DateAdd(null));
        functionPrototype.put("DATE_FORMAT", new DateFormat(null));
        functionPrototype.put("DATE_SUB", new DateSub(null));
        functionPrototype.put("DATE", new Date(null));
        functionPrototype.put("DATEDIFF", new Datediff(null));
        functionPrototype.put("DAY", new Dayofmonth(null));
        functionPrototype.put("DAYOFMONTH", new Dayofmonth(null));
        functionPrototype.put("DAYNAME", new Dayname(null));
        functionPrototype.put("DAYOFWEEK", new Dayofweek(null));
        functionPrototype.put("DAYOFYEAR", new Dayofyear(null));
        functionPrototype.put("DECODE", new Decode(null));
        functionPrototype.put("DEFAULT", new Default(null));
        functionPrototype.put("DEGREES", new Degrees(null));
        functionPrototype.put("DES_DECRYPT", new DesDecrypt(null));
        functionPrototype.put("DES_ENCRYPT", new DesEncrypt(null));
        functionPrototype.put("ELT", new Elt(null));
        functionPrototype.put("ENCODE", new Encode(null));
        functionPrototype.put("ENCRYPT", new Encrypt(null));
        functionPrototype.put("EXP", new Exp(null));
        functionPrototype.put("EXPORT_SET", new ExportSet(null));
        // functionPrototype.put("EXTRACT", new Extract(null));
        functionPrototype.put("EXTRACTVALUE", new Extractvalue(null));
        functionPrototype.put("FIELD", new Field(null));
        functionPrototype.put("FIND_IN_SET", new FindInSet(null));
        functionPrototype.put("FLOOR", new Floor(null));
        functionPrototype.put("FORMAT", new Format(null));
        functionPrototype.put("FOUND_ROWS", new FoundRows(null));
        functionPrototype.put("FROM_DAYS", new FromDays(null));
        functionPrototype.put("FROM_UNIXTIME", new FromUnixtime(null));
        // functionPrototype.put("GET_FORMAT", new GetFormat(null));
        functionPrototype.put("GET_LOCK", new GetLock(null));
        functionPrototype.put("GREATEST", new Greatest(null));
        functionPrototype.put("HEX", new Hex(null));
        functionPrototype.put("HOUR", new Hour(null));
        functionPrototype.put("IF", new If(null));
        functionPrototype.put("IFNULL", new Ifnull(null));
        functionPrototype.put("INET_ATON", new InetAton(null));
        functionPrototype.put("INET_NTOA", new InetNtoa(null));
        functionPrototype.put("INSERT", new Insert(null));
        functionPrototype.put("INSTR", new Instr(null));
        functionPrototype.put("INTERVAL", new Interval(null));
        functionPrototype.put("IS_FREE_LOCK", new IsFreeLock(null));
        functionPrototype.put("IS_USED_LOCK", new IsUsedLock(null));
        functionPrototype.put("ISNULL", new Isnull(null));
        functionPrototype.put("LAST_DAY", new LastDay(null));
        functionPrototype.put("LAST_INSERT_ID", new LastInsertId(null));
        functionPrototype.put("LCASE", new Lower(null));
        functionPrototype.put("LEAST", new Least(null));
        functionPrototype.put("LEFT", new Left(null));
        functionPrototype.put("LENGTH", new Length(null));
        functionPrototype.put("LN", new Log(null)); // Ln(X) equals Log(X)
        functionPrototype.put("LOAD_FILE", new LoadFile(null));
        functionPrototype.put("LOCALTIME", new Now());
        functionPrototype.put("LOCALTIMESTAMP", new Now());
        functionPrototype.put("LOCATE", new Locate(null));
        functionPrototype.put("LOG10", new Log10(null));
        functionPrototype.put("LOG2", new Log2(null));
        functionPrototype.put("LOG", new Log(null));
        functionPrototype.put("LOWER", new Lower(null));
        functionPrototype.put("LPAD", new Lpad(null));
        functionPrototype.put("LTRIM", new Ltrim(null));
        functionPrototype.put("MAKE_SET", new MakeSet(null));
        functionPrototype.put("MAKEDATE", new Makedate(null));
        functionPrototype.put("MAKETIME", new Maketime(null));
        functionPrototype.put("MASTER_POS_WAIT", new MasterPosWait(null));
        functionPrototype.put("MD5", new Md5(null));
        functionPrototype.put("MICROSECOND", new Microsecond(null));
        functionPrototype.put("MID", new Substring(null));
        functionPrototype.put("MINUTE", new Minute(null));
        functionPrototype.put("MONTH", new Month(null));
        functionPrototype.put("MONTHNAME", new Monthname(null));
        functionPrototype.put("NAME_CONST", new NameConst(null));
        functionPrototype.put("NOW", new Now());
        functionPrototype.put("NULLIF", new Nullif(null));
        functionPrototype.put("OCT", new Oct(null));
        functionPrototype.put("OCTET_LENGTH", new Length(null));
        functionPrototype.put("OLD_PASSWORD", new OldPassword(null));
        functionPrototype.put("ORD", new Ord(null));
        functionPrototype.put("PASSWORD", new Password(null));
        functionPrototype.put("PERIOD_ADD", new PeriodAdd(null));
        functionPrototype.put("PERIOD_DIFF", new PeriodDiff(null));
        functionPrototype.put("PI", new Pi(null));
        functionPrototype.put("POW", new Pow(null));
        functionPrototype.put("POWER", new Pow(null));
        functionPrototype.put("QUARTER", new Quarter(null));
        functionPrototype.put("QUOTE", new Quote(null));
        functionPrototype.put("RADIANS", new Radians(null));
        functionPrototype.put("RAND", new Rand(null));
        functionPrototype.put("RELEASE_LOCK", new ReleaseLock(null));
        functionPrototype.put("REPEAT", new Repeat(null));
        functionPrototype.put("REPLACE", new Replace(null));
        functionPrototype.put("REVERSE", new Reverse(null));
        functionPrototype.put("RIGHT", new Right(null));
        functionPrototype.put("ROUND", new Round(null));
        functionPrototype.put("ROW_COUNT", new RowCount(null));
        functionPrototype.put("RPAD", new Rpad(null));
        functionPrototype.put("RTRIM", new Rtrim(null));
        functionPrototype.put("SCHEMA", new Database(null));
        functionPrototype.put("SEC_TO_TIME", new SecToTime(null));
        functionPrototype.put("SECOND", new Second(null));
        functionPrototype.put("SESSION_USER", new User(null));
        functionPrototype.put("SHA1", new Sha1(null));
        functionPrototype.put("SHA", new Sha1(null));
        functionPrototype.put("SHA2", new Sha2(null));
        functionPrototype.put("SIGN", new Sign(null));
        functionPrototype.put("SIN", new Sin(null));
        functionPrototype.put("SLEEP", new Sleep(null));
        functionPrototype.put("SOUNDEX", new Soundex(null));
        functionPrototype.put("SPACE", new Space(null));
        functionPrototype.put("SQRT", new Sqrt(null));
        functionPrototype.put("STD", new Std(null));
        functionPrototype.put("STDDEV_POP", new StddevPop(null));
        functionPrototype.put("STDDEV_SAMP", new StddevSamp(null));
        functionPrototype.put("STDDEV", new Stddev(null));
        functionPrototype.put("STR_TO_DATE", new StrToDate(null));
        functionPrototype.put("STRCMP", new Strcmp(null));
        functionPrototype.put("SUBDATE", new Subdate(null));
        functionPrototype.put("SUBSTRING_INDEX", new SubstringIndex(null));
        functionPrototype.put("SUBTIME", new Subtime(null));
        functionPrototype.put("SYSDATE", new Sysdate(null));
        functionPrototype.put("SYSTEM_USER", new User(null));
        functionPrototype.put("TAN", new Tan(null));
        functionPrototype.put("TIME_FORMAT", new TimeFormat(null));
        functionPrototype.put("TIME_TO_SEC", new TimeToSec(null));
        functionPrototype.put("TIME", new Time(null));
        functionPrototype.put("TIMEDIFF", new Timediff(null));
        functionPrototype.put("TIMESTAMP", new Timestamp(null));
        // functionPrototype.put("TIMESTAMPADD", new Timestampadd(null));
        // functionPrototype.put("TIMESTAMPDIFF", new Timestampdiff(null));
        functionPrototype.put("TO_DAYS", new ToDays(null));
        functionPrototype.put("TO_SECONDS", new ToSeconds(null));
        functionPrototype.put("TRUNCATE", new Truncate(null));
        functionPrototype.put("UCASE", new Upper(null));
        functionPrototype.put("UNCOMPRESS", new Uncompress(null));
        functionPrototype.put("UNCOMPRESSED_LENGTH", new UncompressedLength(null));
        functionPrototype.put("UNHEX", new Unhex(null));
        functionPrototype.put("UNIX_TIMESTAMP", new UnixTimestamp(null));
        functionPrototype.put("UPDATEXML", new Updatexml(null));
        functionPrototype.put("UPPER", new Upper(null));
        functionPrototype.put("USER", new User(null));
        functionPrototype.put("UTC_DATE", new UtcDate(null));
        functionPrototype.put("UTC_TIME", new UtcTime(null));
        functionPrototype.put("UTC_TIMESTAMP", new UtcTimestamp(null));
        functionPrototype.put("UUID_SHORT", new UuidShort(null));
        functionPrototype.put("UUID", new Uuid(null));
        functionPrototype.put("VALUES", new Values(null));
        functionPrototype.put("VAR_POP", new VarPop(null));
        functionPrototype.put("VAR_SAMP", new VarSamp(null));
        functionPrototype.put("VARIANCE", new Variance(null));
        functionPrototype.put("VERSION", new Version(null));
        functionPrototype.put("WEEK", new Week(null));
        functionPrototype.put("WEEKDAY", new Weekday(null));
        functionPrototype.put("WEEKOFYEAR", new Weekofyear(null));
        functionPrototype.put("YEAR", new Year(null));
        functionPrototype.put("YEARWEEK", new Yearweek(null));
    }

    /**
     * @param extFuncPrototypeMap funcName -&gt; extFunctionPrototype. funcName
     *            MUST NOT be the same as predefined function of MySQL 5.5
     * @throws IllegalArgumentException
     */
    public synchronized void addExtendFunction(Map<String, FunctionExpression> extFuncPrototypeMap) {
        if (extFuncPrototypeMap == null || extFuncPrototypeMap.isEmpty()) {
            return;
        }
        if (!allowFuncDefChange) {
            throw new UnsupportedOperationException("function define is not allowed to be changed");
        }

        Map<String, FunctionExpression> toPut = new HashMap<String, FunctionExpression>();
        // check extFuncPrototypeMap
        for (Entry<String, FunctionExpression> en : extFuncPrototypeMap.entrySet()) {
            String funcName = en.getKey();
            if (funcName == null)
                continue;
            String funcNameUp = funcName.toUpperCase();
            if (functionPrototype.containsKey(funcNameUp)) {
                throw new IllegalArgumentException("ext-function '" + funcName + "' is MySQL's predefined function!");
            }
            FunctionExpression func = en.getValue();
            if (func == null) {
                throw new IllegalArgumentException("ext-function '" + funcName + "' is null!");
            }
            toPut.put(funcNameUp, func);
        }

        functionPrototype.putAll(toPut);
    }

    /**
     * @return null if
     */
    public FunctionExpression createFunctionExpression(String funcNameUpcase, List<Expression> arguments) {
        FunctionExpression prototype = functionPrototype.get(funcNameUpcase);
        if (prototype == null)
            return null;
        FunctionExpression func = prototype.constructFunction(arguments);
        func.init();
        return func;
    }

    public FunctionParsingStrategy getParsingStrategy(String funcNameUpcase) {
        FunctionParsingStrategy s = parsingStrateg.get(funcNameUpcase);
        if (s == null) {
            if (functionPrototype.containsKey(funcNameUpcase)) {
                return FunctionParsingStrategy._ORDINARY;
            }
            return FunctionParsingStrategy._DEFAULT;
        }
        return s;
    }

}
