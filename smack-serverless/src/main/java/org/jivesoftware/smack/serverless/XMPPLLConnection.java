/**
 *
 * Copyright 2015 Ishan Khanna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack.serverless;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Nonza;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.parts.Resourcepart;

import java.io.IOException;

public class XMPPLLConnection extends AbstractXMPPConnection {

    private JmDNSService service;
    private final XMPPLLConnectionConfiguration xmppllConnectionConfiguration;
    /**
     * Initialize a new Link-Local Connection.  
     * @param configuration specifications about the connection to be established
     */
    public XMPPLLConnection(XMPPLLConnectionConfiguration configuration) {
        super(configuration);
        xmppllConnectionConfiguration = configuration;
        service = new JmDNSService();
    }

    public void announcePresence() throws XMPPException {
        service.announcePresence(xmppllConnectionConfiguration.getLocalPresence());
    }

    @Override public boolean isSecureConnection() {
        return false;
    }

    @Override protected void sendStanzaInternal(Stanza packet)
                    throws SmackException.NotConnectedException, InterruptedException {

    }

    @Override public void sendNonza(Nonza element) throws SmackException.NotConnectedException, InterruptedException {

    }

    @Override public boolean isUsingCompression() {
        return false;
    }

    @Override protected void connectInternal() throws SmackException, IOException, XMPPException, InterruptedException {

    }

    @Override protected void loginInternal(String username, String password, Resourcepart resource)
                    throws XMPPException, SmackException, IOException, InterruptedException {

    }

    @Override protected void shutdown() {

    }
}
