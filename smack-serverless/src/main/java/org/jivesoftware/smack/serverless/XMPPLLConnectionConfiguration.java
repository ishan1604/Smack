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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.net.SocketFactory;
import java.net.InetAddress;
import java.net.Socket;

public class XMPPLLConnectionConfiguration extends ConnectionConfiguration{

    private XMPPLLPresence remotePresence;
    private XMPPLLPresence localPresence;
    private Socket socket;

    /**
     * Holds the socket factory that is used to generate the socket in the connection.
     */
    private SocketFactory socketFactory;

    public XMPPLLConnectionConfiguration(Builder builder) {
        super(builder);
        localPresence = new XMPPLLPresence(builder.serviceName);
    }

    /**
     * This tells if this is a host connection or not.
     * @return true if this is a host connection and false otherwise
     */
    public boolean isInitiator() {
        return socket == null;
    }

    /**
     * Return the remote client's link-local presence information.
     * @return the remote client's link-local presence information
     */
    public XMPPLLPresence getRemotePresence() {
        return remotePresence;
    }

    /**
     * Return this client's link-local presence information.
     * @return this client's link-local presence information
     */
    public XMPPLLPresence getLocalPresence() {
        return localPresence;
    }

    /**
     * Return the socket which was instantiated when a connection to a client
     * was established.
     * @return the socket established when the remote client connected
     */
    public Socket getSocket() {
        return socket;
    }


    public static final class Builder extends ConnectionConfiguration.Builder<Builder, XMPPLLConnectionConfiguration> {

        String serviceName;
        int port;

        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override public XMPPLLConnectionConfiguration build() {

            try {
                this.setXmppDomain(JidCreate.domainBareFrom("linklocal.com"));
            }
            catch (XmppStringprepException e) {
                e.printStackTrace();
            }

            return new XMPPLLConnectionConfiguration(this);
        }

        @Override protected Builder getThis() {
            return this;
        }
    }
}
