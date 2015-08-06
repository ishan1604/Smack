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
package org.jivesoftware.smack;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Nonza;
import org.jivesoftware.smack.serverless.JmDNSService;
import org.jivesoftware.smack.serverless.XMPPLLConnection;
import org.jivesoftware.smack.serverless.XMPPLLConnectionConfiguration;
import org.jivesoftware.smack.serverless.XMPPLLPresence;
import org.jivesoftware.smack.serverless.packet.XMPPLLStreamOpen;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;

public class Tester {

    public static void main(String...args) throws UnknownHostException, InterruptedException {

        XMPPLLConnectionConfiguration xmppllConnectionConfiguration = new XMPPLLConnectionConfiguration.Builder()
                        .setServiceName("ishan@macbookpro")
                        .setPort(5522)
                        .build();
        XMPPLLConnection xmppllConnection = new XMPPLLConnection(xmppllConnectionConfiguration);

//        XMPPLLConnectionConfiguration xmppllConnectionConfiguration1 = new XMPPLLConnectionConfiguration.Builder()
//                        .setServiceName("ishan@macbookpro")
//                        .setPort(5524)
//                        .build();
//        XMPPLLConnection xmppllConnection1 = new XMPPLLConnection(xmppllConnectionConfiguration);

        try {
            System.out.println("Going Online");
            xmppllConnection.announcePresence();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    System.out.println("In shutdown hook");
                    xmppllConnection.concealPresence();
                }
            }, "Shutdown-thread"));
        }
        catch (XMPPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
