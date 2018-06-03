package org.basex.ws;

import org.basex.ws.response.stomp.*;

/**
 * Describes a WebSocketMessage.
 *  @author BaseX Team 2005-18, BSD License
 */
public class WebsocketMessage {

  /**
   * Represents the Messagetype.
   * */
  public enum MESSAGETYPE {
    /**
     * Binary Message type.
     */
    BINARY,
    /**
     * String Message type.
     */
    STRING,
    /**
     * STOMP Frame.
     * */
    STOMP
  }
  /**
   * Represents the MessageType.
   * */
  private MESSAGETYPE msgType;
  /**
   * Represents a String message.
   * */
  private String strMessage;
  /**
   * Represents a Binary message.
   * */
  private byte[] binMessage;
  /**
   * Represents a STOMP Message.
   * */
  private StompFrame stompMessage;

  /**
   * Constructor for String-messages.
   * @param message String the string-message
   * */
  public WebsocketMessage(final String message) {
    this.strMessage = message;
    this.msgType = MESSAGETYPE.STRING;
  }

  /**
   * Constructor for binary-messages.
   * @param message byte[] the binary-message
   * */
  public WebsocketMessage(final byte[] message) {
    this.binMessage = message;
    this.msgType = MESSAGETYPE.BINARY;
  }

  /**
   * Constructor for STOMP-Messages.
   * @param message STOMPFrame
   * */
  public WebsocketMessage(final StompFrame message) {
    this.stompMessage = message;
    this.msgType = MESSAGETYPE.STOMP;
  }

  /**
   * Returns the StringMessage.
   * @return String message
   */
  public String getStringMessage() {
    return this.strMessage;
  }

  /**
   * Returns the BinaryMessage.
   * @return Byte[] Message
   */
  public byte[] getBinMessage() {
    return this.binMessage;
  }

  /**
   * Returns the StompMessage.
   * @return StompFrame message
   * */
  public StompFrame getStompMessage() {
    return this.stompMessage;
  }

  /**
   * Returns the Messagetype.
   * @return MESSAGETYPE the messagetype
   */
  public MESSAGETYPE getMsgType() {
    return msgType;
  }
}

