/**
 * Copyright 2015 ishan
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack.serverless;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class TestingClient {

    public static void main(String...args) throws InterruptedException {
        String serverName = "localhost";
        int port = 1337;
        try
        {
            System.out.println("Connecting to " + serverName
                            + " on port " + port);
            Socket client = new Socket(serverName, port);
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out =
                            new DataOutputStream(outToServer);
            out.writeUTF("Hello from "
                            + client.getLocalSocketAddress());
            while(true) {
                out.writeUTF(""+(new Random(System.currentTimeMillis()).nextInt()));
                Thread.sleep(1000);
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }

    }
}
