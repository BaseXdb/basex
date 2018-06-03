package org.basex.ws.stomp.copy;

/**
 * Possible Command-Strings in a Stomp Message from Client to Server or Server to Client.
 * @author BaseX Team 2005-18, BSD License
 */
public enum Commands {
  //Client -> Server
    /**
     * Send a Message to the Server.
     * Required headers: destination
     * Optional headers: transaction
     * */
    SEND,
    /**
     * Subscribe to a Channel of the Server.
     * Required headers: destination, id
     * Optional headers: ack
     * */
    SUBSCRIBE,
    /**
     * Unsuscribe from a Channel of the Server.
     * Required headers: id
     * Optional headers: none
     * */
    UNSUBSCRIBE,
    /**
     * Start a Transaction.
     * Required headers: transaction
     * Optional headers: none
     * */
    BEGIN,
    /**
     * Commit a Transaction in Progress.
     * Required headers: transaction
     * Optional headers: none
     * */
    COMMIT,
    /**
     * Rollback a Transaction in Progress.
     * Required headers: transaction
     * Optional headers: none
     * */
    ABORT,
    /**
     * Acknowledge consumption of a message from a subscription
     * using client or client-individual acknowledgement.
     * Required headers: id
     * Optional headers: transaction
     * */
    ACK,
    /**
     * Opposite of ACK: Client doesnt consume the Message.
     * Required headers: id
     * Optional headers: transaction
     * */
    NACK,
    /**
     * Disconnect from the Server.
     * Required headers: none
     * Optional headers: receipt
     * */
    DISCONNECT,
    /**
     * Connect to the Server.
     * Required headers: accept-version, host
     * Optional headers: login, passcode, heart-beat
     */
    CONNECT,
    /**
     * Like Connect but just for STOMP V. 1.2
     * Required headers: accept-version, host
     * Optional headers: login, passcode, heart-beat
     * */
    STOMP,
    // Server -> Client
    /**
     * Client and Server connected.
     * Required headers: version
     * Optional headers: session, server, heart-beat
     * */
    CONNECTED,
    /**
     * Message from Server to Client.
     * Required headers: destination, message-id, subscription
     * Optional headers: ack
     * */
    MESSAGE,
    /**
     * Sent once a server has successfully processed a client frame that
     * requests a receipt.
     * Required headers: receipt-id
     * Optional headers: none
     * */
    RECEIPT,
    /**
     * If an Error happens.
     * Required headers: none
     * Optional headers: message
     * */
    ERROR
}
