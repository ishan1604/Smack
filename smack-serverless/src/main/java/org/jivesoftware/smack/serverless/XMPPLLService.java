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
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class XMPPLLService {

    static final int DEFAULT_MIN_PORT = 2300;
    static final int DEFAULT_MAX_PORT = 2400;
    private static XMPPLLService service = null;
    private Map<String,XMPPLLStreamOpen> streamOpenMap = new HashMap<>();

    static ByteBuffer buffer = ByteBuffer.allocate(512);

    static {
        SmackConfiguration.getVersion();
    }

    protected XMPPLLPresence presence;
    private boolean done = false;
    private boolean initiated = false;
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

    public void init(int port) throws XMPPException, IOException, InterruptedException {

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
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            SelectionKey clientKey2 = socketChannel.register(selector, SelectionKey.OP_WRITE);
                            clientKey2.attach(new Integer(1));
                            SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
                            clientKey.attach(new Integer(0));

                        } else if (key.isReadable()) {
                            // a channel is ready for reading
                            SocketChannel client = (SocketChannel) key.channel();
                            if (!key.isReadable())
                                continue;
                            int bytesread = client.read(buffer);
                            if (bytesread == -1) {
                                key.cancel();
                                client.close();
                                continue;
                            }
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            System.out.println(new String(bytes));
                            buffer.clear();

                            if (!streamOpenMap.containsKey(presence.getJid())) {
                                openStream(client);
                            } else {
                                String msg = "<message xmlns=\"jabber:client\" to=\"ubuntu@ubuntu\" type=\"chat\" id=\"106\" from=\"ish@macbookpro/local\"><body>Hello Bro "+ new Random(System.currentTimeMillis()).nextInt() +"</body></message>\n";
                                ByteBuffer bb = ByteBuffer.wrap(msg.getBytes("utf-8"));
                                client.write(bb);
                                bb.clear();
                            }
                        } else if (key.isWritable()) {
                            // a channel is ready for writing

                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            if (!key.isWritable())
                                continue;
                            /*

                                if (MESSAGE QUEUE != empty) {
                                    String msg = Get message from MESSAGE QUEUE
                                    ByteBuffer bb = ByteBuffer.wrap(msg.getBytes("utf-8"));
                                    socketChannel.write(bb);
                                }


                             */
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

    private void openStream(SocketChannel client) throws IOException {

        XMPPLLStreamOpen xmppllStreamOpen = new XMPPLLStreamOpen("ubuntu@ubuntu",
                        presence.getServiceName());
        ByteBuffer bb = ByteBuffer.wrap(xmppllStreamOpen.toXML().toString().getBytes("utf-8"));
        client.write(bb);
        bb.clear();
        streamOpenMap.put(presence.getJid(), xmppllStreamOpen);
    }
}
