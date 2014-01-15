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
 * (created at 2011-4-11)
 */
package com.alibaba.cobar.parser;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public interface Performance {
    String SQL_BENCHMARK_SELECT = " seLEcT id, member_id , image_path  \t , image_size , STATUS,   gmt_modified from    wp_image wheRe \t\t\n id =  ? AND member_id\t=\t-123.456";
    // String SQL_BENCHMARK_SELECT =
    // "select ID, GMT_CREATE, GMT_MODIFIED, INBOX_FOLDER_ID, MESSAGE_ID,             FEEDBACK_TYPE, TARGET_ID,               TRADE_ID, SUBJECT, SENDER_ID, SENDER_TYPE,              S_DISPLAY_NAME, SENDER_STATUS, RECEIVER_ID, RECEIVER_TYPE,              R_DISPLAY_NAME, RECEIVER_STATUS, SPAM_STATUS, REPLY_STATUS,             ATTACHMENT_STATUS,              SENDER_COUNTRY,                 RECEIVER_COUNTRY,APP_FROM,APP_TO,APP_SOURCE,SENDER_VACOUNT,RECEIVER_VACOUNT,            DISTRIBUTE_STATUS,ORG_RECEIVER_ID,CUSTOMER_ID,OPERATOR_ID,OPERATOR_NAME,FOLLOW_STATUS,DELETE_STATUS,FOLLOW_TIME,BATCH_COUNT             from MESSAGE_REC_RECORD                 where RECEIVER_VACOUNT          =? and ID = ?";
    String SQL_BENCHMARK_EXPR_SELECT = "( seLect id, member_id , image_path  \t , image_size , STATUS,   gmt_modified from    wp_image where \t\t\n id =  ? and member_id\t=\t?)";
}
