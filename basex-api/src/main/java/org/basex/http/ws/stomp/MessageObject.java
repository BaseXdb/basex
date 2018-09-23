package org.basex.http.ws.stomp;

import java.util.*;

/**
 * A MessageObject represents a Message send by the Server to the Client.
 * */
public class MessageObject implements Comparable<MessageObject> {
  /** The messageId. */
  private String messageId;
  /** The time. */
  private Date time;
  /** The message. */
  private String message;
  /** The Websocket Id of the client that gets the message*/
  private String wsid;

  /**
   * Constructor.
   * @param messageId The messageid
   * @param message The message string
   * @param wsid The websocketid
   */
  public MessageObject(final String messageId, final String message, final String wsid) {
    this.messageId = messageId;
    this.message = message;
    this.wsid = wsid;
    this.time = new Date();
  }

  @Override
  public int compareTo(MessageObject o) {
    return this.time.compareTo(o.time);
  }

  /**
   * Returns the WebSocket-Id
   * @return String websocketid
   * */
  public String getWebSocketId() {
    return wsid;
  }

  /**
   * Returns the messageId.
   * @return String messageId
   * */
  public String getMessageId() {
    return messageId;
  }

  /**
   * Returns the message.
   * @return String message
   * */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the time.
   * @return Date time
   * */
  public Date getTime() {
    return time;
  }

}
