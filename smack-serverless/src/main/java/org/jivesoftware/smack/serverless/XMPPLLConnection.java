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
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.serverless.packet.XMPPLLStreamOpen;
import org.jivesoftware.smack.util.Async;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Resourcepart;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class XMPPLLConnection extends AbstractXMPPConnection {

    private JmDNSService service;
    private boolean done = false;
    private boolean initiated = false;
    private final XMPPLLConnectionConfiguration xmppllConnectionConfiguration;
    private Map<String,XMPPLLStreamOpen> streamOpenMap = new HashMap<>();

    static ByteBuffer buffer = ByteBuffer.allocate(512);

    /**
     * Initialize a new Link-Local Connection.  
     * @param configuration specifications about the connection to be established
     */
    public XMPPLLConnection(XMPPLLConnectionConfiguration configuration) {
        super(configuration);
        xmppllConnectionConfiguration = configuration;
        service = new JmDNSService();
    }

    public void announcePresence() throws XMPPException, IOException, InterruptedException {
        service.announcePresence(xmppllConnectionConfiguration.getLocalPresence());
        init(xmppllConnectionConfiguration.getLocalPresence().getPort());
    }

    public void concealPresence() {
        service.concealPresence();
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
                            String packetReceived = new String(bytes);
                            System.out.println(packetReceived);
                            buffer.clear();

                            if (packetReceived.contains("stream")) {
                                System.out.println("This is a stream packet and I am closing the stream for now");
                                String msg = "</stream:stream>";
                                ByteBuffer bb = ByteBuffer.wrap(msg.getBytes("utf-8"));
                                client.write(bb);
                                bb.clear();
                            } else {
                                System.out.println("This is not a stream packet");
                            }

//                            if (!streamOpenMap.containsKey(presence.getJid())) {
//                                openStream(client);
//                            } else {
//                                String msg = "<message xmlns=\"jabber:client\" to=\"ubuntu@ubuntu\" type=\"chat\" id=\"106\" from=\"ish@macbookpro/local\"><body>Hello Bro "+ new Random(System.currentTimeMillis()).nextInt() +"</body></message>\n";
//                                ByteBuffer bb = ByteBuffer.wrap(msg.getBytes("utf-8"));
//                                client.write(bb);
//                                bb.clear();
//                            }
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

    private void openStream(SocketChannel client, XMPPLLPresence remotePresence, XMPPLLPresence localPresence) throws IOException {

        XMPPLLStreamOpen xmppllStreamOpen = new XMPPLLStreamOpen(remotePresence.getServiceName(),
                        localPresence.getServiceName());
        ByteBuffer bb = ByteBuffer.wrap(xmppllStreamOpen.toXML().toString().getBytes("utf-8"));
        client.write(bb);
        bb.clear();
        streamOpenMap.put(remotePresence.getJid(), xmppllStreamOpen);
    }


    protected class PacketReader {

        XmlPullParser parser;

        private volatile boolean done;

        /**
         * Initializes the reader in order to be used. The reader is initialized during the
         * first connection and when reconnecting due to an abruptly disconnection.
         */
        void init() {
            done = false;

            Async.go(new Runnable() {
                public void run() {
                    parsePackets();
                }
            }, "Smack Packet Reader (" + getConnectionCounter() + ")");
        }

        /**
         * Shuts the stanza(/packet) reader down. This method simply sets the 'done' flag to true.
         */
        void shutdown() {
            done = true;
        }

        private void parsePackets() {

            try {

                int eventType = parser.getEventType();
                while(!done) {
                    switch (eventType) {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }



}
