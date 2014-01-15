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
package com.alibaba.cobar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author xianmao.hexm
 */
public class EchoBioServer implements Runnable {

    private static final byte[] FIRST_BYTES = "Welcome to Cobar Server.".getBytes();

    private final ServerSocket serverSocket;

    public EchoBioServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(new BioConnection(socket)).start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class BioConnection implements Runnable {

        private Socket socket;
        private InputStream input;
        private OutputStream output;
        private byte[] readBuffer;
        private byte[] writeBuffer;

        private BioConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
            this.readBuffer = new byte[4096];
            this.writeBuffer = new byte[4096];
        }

        @Override
        public void run() {
            try {
                output.write(FIRST_BYTES);
                output.flush();
                while (true) {
                    int got = input.read(readBuffer);
                    output.write(writeBuffer, 0, got);
                    // output.flush();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if (socket != null)
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Thread(new EchoBioServer(8066)).start();
    }

}
