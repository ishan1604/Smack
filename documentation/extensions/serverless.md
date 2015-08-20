Smack: Server Less Messaging API
======================

[Back](index.md)

This document will introduce you to the Smack Server Less Messaging API and provide an overview of
important classes and concepts. This is added to smack as an extension described in [XEP-174](http://www.xmpp.org/extensions/xep-0174.html).


Establishing a Connection
-------------------------

The `XMPPLLConnection` class is used to create a connection. The `XMPPLLConnectionConfiguration` class is used to configure the connection.
Below are code examples for making a connection:

```
// Configure the Link Local Connection
XMPPLLConnectionConfiguration xmppllConnectionConfiguration = new XMPPLLConnectionConfiguration.Builder()
                .setServiceName("tester@macbookpro")
                .setPort(5524)
                .build();

// Create a connection with the configuration created above
XMPPLLConnection xmppllConnection = new XMPPLLConnection(xmppllConnectionConfiguration);

try {
    // Announce your presence over the local network
    xmppllConnection.announcePresence();
}
catch (XMPPException e) {
    e.printStackTrace();
}
catch (IOException e) {
    e.printStackTrace();
}

```
