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

import org.jivesoftware.smack.XMPPException;

import java.net.Socket;

public class Tester {

    public static void main(String...args) {

        JmDNSService jmDNSService = new JmDNSService();
        XMPPLLPresence xmppllPresence = new XMPPLLPresence("tom@macbook15");
        xmppllPresence.setFirstName("Tom");
        xmppllPresence.setLastName("Taylor");
        xmppllPresence.setEmail("tom@ishankhanna.in");
        xmppllPresence.setPort(5562);
        Socket socket = new Socket();
        XMPPLLConnectionConfiguration xmppllConnectionConfiguration = new XMPPLLConnectionConfiguration(xmppllPresence, socket);
        XMPPLLConnection xmppllConnection = new XMPPLLConnection(jmDNSService, xmppllConnectionConfiguration);
        try {
            xmppllConnection.announcePresence(xmppllPresence);
        }
        catch (XMPPException e) {
            e.printStackTrace();
        }
    }
}
