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

import org.jivesoftware.smack.serverless.JmDNSService;
import org.jivesoftware.smack.serverless.XMPPLLConnection;
import org.jivesoftware.smack.serverless.XMPPLLConnectionConfiguration;
import org.jivesoftware.smack.serverless.XMPPLLPresence;
import org.jxmpp.jid.BareJid;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;

public class Tester {

    public static void main(String...args) throws UnknownHostException, InterruptedException {

//        XMPPLLPresence xmppllPresence = new XMPPLLPresence("ishan@intellij");
//        xmppllPresence.setPort(5562);
//        xmppllPresence.setFirstName("Deepali");
//        xmppllPresence.setLastName("Kishnani");
//        xmppllPresence.setNick("deeps");
//        xmppllPresence.setStatus(XMPPLLPresence.Mode.avail);
//        xmppllPresence.setHost(InetAddress.getLocalHost().toString());
//        xmppllPresence.setHash("sha-1");
//        xmppllPresence.setVer("1");
//        xmppllPresence.setMsg("Hanging out down");
//        xmppllPresence.setNode("www.ishankhanna.in");
//        xmppllPresence.setJid("ishan1604@jabber.org");
//
//        JmDNSService jmDNSService = new JmDNSService();
//
//        try {
//            jmDNSService.announcePresence(xmppllPresence);
//
//        }
//        catch (XMPPException e) {
//            e.printStackTrace();
//        }

        XMPPLLConnectionConfiguration xmppllConnectionConfiguration = new XMPPLLConnectionConfiguration.Builder()
                        .setServiceName("ishan@mbp")
                        .build();
        XMPPLLConnection xmppllConnection = new XMPPLLConnection(xmppllConnectionConfiguration);
        try {
            System.out.println("Going Online");
            xmppllConnection.announcePresence();
        }
        catch (XMPPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
