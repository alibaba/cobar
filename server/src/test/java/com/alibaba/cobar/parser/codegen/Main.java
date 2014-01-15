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
package com.alibaba.cobar.parser.codegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * (created at 2010-9-26)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.dir(new File(
                "/media/BC4CF85E4CF814BC/work/cobar/cobar-1.1.0-dev/cobar-parser/src/main/java/com/alibaba/cobar/parser/ast/expression"));
    }

    private void dir(File dir) throws Exception {
        if (dir.getName().equals(".svn"))
            return;
        File[] fl = dir.listFiles();
        if (fl == null)
            return;
        for (File file : fl) {
            if (file.isDirectory()) {
                dir(file);
            } else {
                handleFile(file);
            }
        }
    }

    protected static final String srcString = "<a href=\"mailto:QiuShuo1985@gmail.com\">";
    protected static final String targetString = "<a href=\"mailto:shuo.qius@alibaba-inc.com\">";

    // void visit(ASTNode groupBy);
    private void handleFile(File file) throws Exception {
        if (file == null || !file.getName().endsWith(".java")) {
            return;
        }
        InputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            BufferedReader fin = new BufferedReader(new InputStreamReader(fileInputStream, "gbk"));
            File tmp = getTmpFile(file);
            PrintWriter tout = null;
            try {
                tout = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tmp), "gbk"));
                StringBuilder sb = new StringBuilder();
                for (String line = null; (line = fin.readLine()) != null;) {
                    sb.append(line).append("\r\n");
                }
                int index = sb.lastIndexOf("}");
                tout.print(sb.substring(0, index));
                tout.print("@Override\r\n");
                tout.print("public void accept(SQLASTVisitor visitor){visitor.visit(this);}}");
                tout.flush();
            } finally {
                try {
                    tout.close();
                } catch (Exception e2) {
                }
            }
            String opath = file.getAbsolutePath();
            if (file.delete() == false) {
                System.out.println(file.getAbsolutePath());
                tmp.delete();
                return;
            }
            tmp.renameTo(new File(opath));
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception e) {
            }
        }
    }

    private File getTmpFile(File file) throws Exception {
        File[] sub = file.getParentFile().listFiles();
        String newName = file.getName() + ".temp";
        loop1: while (true) {
            for (File s : sub) {
                if (newName.equalsIgnoreCase(s.getName())) {
                    newName = newName + "1";
                    continue loop1;
                }
            }
            break;
        }
        return new File(file.getParent(), newName);
    }
}
