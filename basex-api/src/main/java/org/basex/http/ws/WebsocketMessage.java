package org.basex.http.ws;

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
    STRING
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
   * Returns the Messagetype.
   * @return MESSAGETYPE the messagetype
   */
  public MESSAGETYPE getMsgType() {
    return msgType;
  }
}

