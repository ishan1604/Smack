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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jxmpp.jid.BareJid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class Tester extends Thread{

    private ServerSocket listeningSocket;
    private SocketAddress previousClientsSocketAddress;

    public Tester(int port) throws IOException
    {
        listeningSocket = new ServerSocket(port);
        listeningSocket.setSoTimeout(100000);
    }

    @Override public void run() {

        while (true) {
            try {
                System.out.println("Waiting for client on port " +
                                listeningSocket.getLocalPort() + "...");
                Socket client = listeningSocket.accept();
                System.out.println("Remote Socket Address : " + client.getRemoteSocketAddress().toString());
                if (previousClientsSocketAddress == null || !client.getRemoteSocketAddress().equals(previousClientsSocketAddress)) {
                    System.out.println("Just connected to "
                                    + client.getRemoteSocketAddress());
                    previousClientsSocketAddress = client.getRemoteSocketAddress();
                    Runnable runnable = new Runnable() {
                        @Override public void run() {
                            try {
                                while(true) {
                                    DataInputStream in =
                                                new DataInputStream(client.getInputStream());
                                    System.out.println("Message in Thread with ID : " + currentThread().getId() + "," + in.readUTF());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    Thread thread = new Thread(runnable);
                    System.out.println("Spawned new thread with ID : " + thread.getId());
                    thread.start();
                }
            } catch(SocketTimeoutException s)
            {
                System.out.println("Socket timed out!");
                break;
            }catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String...args) {

        try {
            Tester tester = new Tester(1337);
            tester.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
