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

package com.alibaba.cobar.manager.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.log4j.Logger;

/**
 * @author haiqing.zhuhq 2012-2-21
 */
public class EncryptUtil {
    private static final Logger logger = Logger.getLogger(EncryptUtil.class);

    /**
     * ???????????????8
     */
    private static final String PASSWORD_CRYPT_KEY = "cobar-manager";
    private final static String DES = "DES";

    /**
     * ????
     * 
     * @param src ????
     * @param key ???????????????8
     * @return ????????????
     * @throws Exception
     */
    public static byte[] encrypt(byte[] src, byte[] key) throws Exception {
        //DES???????????????¦Å??????? 
        SecureRandom sr = new SecureRandom();
        // ?????????????DESKeySpec???? 
        DESKeySpec dks = new DESKeySpec(key);
        // ???????????????????????DESKeySpec????? 
        // ???SecretKey???? 
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher???????????????? 
        Cipher cipher = Cipher.getInstance(DES);
        // ?????????Cipher???? 
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
        // ???????????????? 
        // ?????§Þ?????? 
        return cipher.doFinal(src);
    }

    /**
     * ????
     * 
     * @param src ????
     * @param key ??????????????8?????
     * @return ??????????????
     * @throws Exception
     */
    public static byte[] decrypt(byte[] src, byte[] key) throws Exception {
        // DES???????????????¦Å??????? 
        SecureRandom sr = new SecureRandom();
        // ????????????????DESKeySpec???? 
        DESKeySpec dks = new DESKeySpec(key);
        // ???????????????????????DESKeySpec????????? 
        // ???SecretKey???? 
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher???????????????? 
        Cipher cipher = Cipher.getInstance(DES);
        // ?????????Cipher???? 
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
        // ???????????????? 
        // ?????§ß?????? 
        return cipher.doFinal(src);
    }

    /**
     * ???????
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public final static String decrypt(String data) {
        try {
            return new String(decrypt(hex2byte(data.getBytes()), PASSWORD_CRYPT_KEY.getBytes()));
        } catch (Exception e) {
            logger.error("decrypt error!!");
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * ???????
     * 
     * @param password
     * @return
     * @throws Exception
     */
    public final static String encrypt(String password) {
        try {
            return byte2hex(encrypt(password.getBytes(), PASSWORD_CRYPT_KEY.getBytes()));
        } catch (Exception e) {
            logger.error("encrypt error!!");
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * ??????????
     * 
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) hs = hs + "0" + stmp;
            else hs = hs + stmp;
        }
        return hs.toUpperCase();
    }

    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) throw new IllegalArgumentException("??????????");
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
}
