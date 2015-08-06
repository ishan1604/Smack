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

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.serverless.packet.XMPPLLStreamOpen;
import org.jxmpp.jid.BareJid;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class XMPPLLService {

    static final int DEFAULT_MIN_PORT = 2300;
    static final int DEFAULT_MAX_PORT = 2400;
    private static XMPPLLService service = null;

    static {
        SmackConfiguration.getVersion();
    }

    protected XMPPLLPresence presence;
    private boolean done = false;
    private boolean initiated = false;
    private Thread listenerThread;
    private ServerSocket listeningSocket;
    private Map<String, XMPPLLConnection> incoming = new ConcurrentHashMap<String, XMPPLLConnection>();
    private Map<String, XMPPLLConnection> outgoing = new ConcurrentHashMap<String, XMPPLLConnection>();

    public static XMPPLLService getInstance() {

        if (service == null) {
            service = new JmDNSService();
        }

        return service;
    }

    /**
     * Bind one listeningSocket to any port within a given range.
     *
     * @param min the minimum port number allowed
     * @param max hte maximum port number allowed
     * @throws XMPPException if binding failed on all allowed ports.
     */
    private static ServerSocket bindRange(int min, int max) throws XMPPException {
        // TODO this method exists also for the local socks5 proxy code and should be factored out into a util
        int port = 0;
        for (int try_port = min; try_port <= max; try_port++) {
            try {
                ServerSocket socket = new ServerSocket(try_port);
                return socket;
            }
            catch (IOException e) {
                // failed to bind, try next
            }
        }
        throw new XMPPException.XMPPErrorException("Unable to bind port, no ports available.",
                        new XMPPError(XMPPError.Condition.resource_constraint));
    }

    /**
     * Fetch list of all the users who are present on the Link Local network.
     *
     * @return List<BareJid> list of BareJid(s) of all available clients
     */
    public abstract List<BareJid> getAllClientsPresentOnLLNetwork();

    /**
     * Marks presence of a client on the Link Local Network. Marking presence
     * on the network does not mean client is available for chat. It might so
     * happen that client is present on the network but is Away, Busy or
     * Unavailable for chat.
     * @throws XMPPException
     * @param presence
     */
    public abstract void announcePresence(XMPPLLPresence presence) throws XMPPException;

    /**
     * Marks absence of a client on the Link Local Network. Marking absence
     * on the network means client is no longer connected to the Link Local
     * Network, in simpler terms client has logged out.
     *
     */
    public abstract void concealPresence();

    public void init(int port) throws XMPPException, IOException {

        Selector selector = Selector.open();

        // allocate a new port for remote clients to connect to
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Listen for connections
        while (!done) {
            try {
                while (true) {

                    int readyChannels = selector.select();

                    if(readyChannels == 0) continue;

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();

                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while(keyIterator.hasNext()) {

                        SelectionKey key = keyIterator.next();

                        if(key.isAcceptable()) {
                            // a connection was accepted by a ServerSocketChannel.
                            Socket s = serverSocketChannel.accept().socket();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            System.out.println(reader.readLine());
                            if (!initiated) {
                                DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
                                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                XMPPLLStreamOpen xmppllStreamOpen = new XMPPLLStreamOpen("ubuntu@ubuntu",
                                                presence.getServiceName());
                                System.out.println(xmppllStreamOpen.toXML().toString());
                                bufferedWriter.write(xmppllStreamOpen.toXML().toString());
                                initiated = true;
                            }
                        } else if (key.isConnectable()) {
                            // a connection was established with a remote server.

                        } else if (key.isReadable()) {
                            // a channel is ready for reading

                        } else if (key.isWritable()) {
                            // a channel is ready for writing
                        }

                        keyIterator.remove();
                    }
                }
            }
            catch (SocketException se) {
                se.printStackTrace();
                // If we are closing down, it's probably closed listeningSocket exception.
                if (!done) {
                    throw new XMPPException.XMPPErrorException("Link-local service unexpectedly closed down.",
                                    new XMPPError(XMPPError.Condition.undefined_condition), se);
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                throw new XMPPException.XMPPErrorException("Link-local service unexpectedly closed down.",
                                new XMPPError(XMPPError.Condition.undefined_condition), ioe);
            }
        }
    }

    /**
     * Returns a connection to a given service name.
     * First checks for an outgoing connection, if noone exists,
     * try ingoing.
     *
     * @param serviceName the service name
     * @return a connection associated with the service name or null if no
     * connection is available.
     */
    XMPPLLConnection getConnectionTo(String serviceName) {
        XMPPLLConnection connection = outgoing.get(serviceName);
        if (connection != null)
            return connection;
        return incoming.get(serviceName);
    }

    protected void serviceNameChanged(String newName, String oldName) {
        // update our own presence with the new name, for future connections
        presence.setServiceName(newName);

        // clean up connections
        XMPPLLConnection c;
        c = getConnectionTo(oldName);
        if (c != null)
            c.disconnect();
        c = getConnectionTo(newName);
        if (c != null)
            c.disconnect();

    }
}
