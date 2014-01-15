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

package com.alibaba.cobar.manager.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wenfeng.cenwf 2011-2-18
 */
public class MixTest {
    final static int SIZE = 1024;

    static void xxx(List<Integer> list1, List<Integer> list2) {
        list2 = new ArrayList<Integer>(list1);
        list1.clear();
        return;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        //        CobarNodeClusterDO d = new CobarNodeClusterDO();
        //        d.setId(new Long(123));
        //        d.setName("test");
        //        d.setBUId(new Long(3));
        //        d.setStatus(new Byte((byte) 'a'));
        //        String str = d.toString();
        //        System.out.println(str);
        //
        //        long st = 1293690711302L;
        //        Date date = new Date(st);
        //        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //        String dStr = format.format(date);
        //        System.out.println("date:" + dStr);

        //        System.out.println("ceil of 10/3 : " + Math.ceil(10 / 3.0));
        //        Integer i = new Integer(3);
        //        System.out.println(i instanceof Number);
        //        String s = "cobarserver10";
        //        System.out.println(Long.parseLong(s.substring(11)));
        //
        //        System.out.println(System.currentTimeMillis());

        //        List<Integer> list = new ArrayList<Integer>();
        //        list.add(1);
        //        list.add(2);
        //        list.add(3);
        //        List<Integer> list2 = new ArrayList<Integer>();
        //        
        //        xxx(list,list2);
        //
        //        int index = 0;
        //        for (Integer i : list) {
        //            if(i%2==0)
        //                list.remove(i);
        //            System.out.println("e:" + i + " index: " + index);
        //            index++;
        //        }
        //        System.out.println();
        //
        //        String a = "abd";
        //        String b = null;
        //        try {
        ////            a = URLEncoder.encode(a, "UTF-8");
        //            b = URLDecoder.decode(a, "UTF-8");
        //            System.out.println(b);
        //        } catch (UnsupportedEncodingException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        63, 63, -95, -29, -88, -90, 63, 63, 63, 63, -95, -24
        //        byte[] b = {63, 63, -95, -29, -88, -90, 63, 63, 63, 63, -95, -24};
        //        String a = b.toString();
        //        String c = "abc";
        //        try {
        //            System.out.println(new String(b,"utf8"));
        //        } catch (UnsupportedEncodingException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //
        //        String str = "?a";
        //        System.out.println(str + "'s length = " + str.getBytes().length);
        //
        //        Set<String> set = new HashSet<String>();
        //        set.add("aaa");
        //        set.add("bbb");
        //
        //        String e = "bbb";
        //        System.out.println("contain " + e + " ? " + set.contains(e));
        //        ByteBuffer bb = ByteBuffer.allocate(SIZE);
        //        System.out.println(bb.toString());
        //
        //        bb.put("cobar".getBytes());
        //        System.out.println(bb.toString());
        //
        //        String sql = "show @@sql.detail where id= -234434 ";
        //        String[] arr = StringUtil.split(sql, "=");
        //        for (int i = 0; i < arr.length; i++)
        //            System.out.println(arr[i].trim() + ":" + arr[i].trim().length());
        //        long id = -12323232;
        //        String ID = new Long(id).toString();
        //        System.out.print(ID.length());

        //        String s1 = "mysql1223";
        //        String s2 = "mysql_2_3";
        //        System.out.print(s1.lastIndexOf('_'));
        //        System.out.print(10 / 3 + 1);

        //        String query = "= -1233434244 -";
        //        String id = query.substring(1).trim();
        //        Long sql = Long.parseLong(id);
        //        System.out.print(sql);
        String[] src = { "a", "b", "c" };
        String[] dest = src;
        if (src == dest) System.out.printf("???");
    }

}
