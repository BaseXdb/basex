package org.basex.http.ws.stomp.frames;

import java.util.*;

import org.basex.http.ws.stomp.*;

/**
 * Represents the a Frame in the Stomp Protocol.
 * @author BaseX Team 2005-18, BSD License
 */
public abstract class StompFrame {
  /**
   * The Stomp-Command, e.g. CONNECT.
   * */
  private Commands command;

  /**
   * The headers, e.g. destination.
   * */
  private Map<String, String> header = new HashMap<>();

  /**
   * The Message-Body.
   * */
  private String body;

  /**
   * Constructor.
   * @param cmd the Command
   * @param header The Headers-map
   * @param body The Body
   */
  public StompFrame(final Commands cmd, final Map<String, String> header,
                    final String body) {
    this.command = cmd;
    this.header = header;
    this.body = body;
  }

  /**
   * Parses a MessageString to a StompFrame.
   * @param message The MessageString
   * @return StompFrame
   * @throws HeadersException if not all headers are set
   */
  public static StompFrame parse(final String message) throws HeadersException {

    String cmdhead = message.split("\n\n")[0];
    String[] headers = cmdhead.split("\n");

    Map<String, String> header = new HashMap<>();
    for (int i = 1; i < headers.length; i++) {
      String head = headers[i].split(":")[0];
      header.put(head, headers[i].substring(head.length() + 1));
    }

    // Read the Command
    Commands cmd = Commands.valueOf(headers[0]);
    StompFrame returnFrame = null;

    // Check witch type of Frame we got
    switch(cmd) {
      case SEND:
        returnFrame = new SendFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case SUBSCRIBE:
        returnFrame = new SubscribeFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case UNSUBSCRIBE:
        returnFrame = new UnsubscribeFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
        // Transaction stuff
      case BEGIN:
        returnFrame = new BeginFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case COMMIT:
        returnFrame = new CommitFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case ABORT:
        returnFrame = new AbortFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case ACK:
        returnFrame = new AckFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case NACK:
        returnFrame = new NackFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case DISCONNECT:
        returnFrame = new DisconnectFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      case CONNECT:
      case STOMP:
        returnFrame = new ConnectFrame(cmd, header, message.substring(cmdhead.length() + 2));
        break;
      default:
        returnFrame = new SendFrame(cmd, header, message.substring(cmdhead.length() + 2));
    }

    // Check if the Frame has all Headers set
    if(!returnFrame.checkValidity()) {
       throw new HeadersException();
    }

    return returnFrame;
  }

  /**
   * Checks if all required Parameters are set.
   * @return boolean true if anything is set
   */

  public abstract boolean checkValidity();
  /**
   * Returns the Command.
   * @return Command
   * */
  public Commands getCommand() {
    return command;
  }

  /**
   * Returns the headers.
   * @return Map<String,String> headers
   * */
  public Map<String, String> getHeaders() {
    return header;
  }

  /**
   * Appends the found STOMP-Headers to the given Header-Map.
   * @param pHeaderMap Map<String,String>
   * */
  public void appendHeader(final Map<String, String> pHeaderMap) {
    header.forEach((k, v) -> {
      if(k.equals("id")) {
        pHeaderMap.put("stompId", v);
      } else {
        pHeaderMap.put(k, v);
      }
    });
  }
  /**
   * Returns the Body.
   * @return String body
   * */
  public String getBody() {
    return body;
  }

  /**
   * Returns a Serialized Frame.
   * @return String frame
   * */
  public String serializedFrame() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.command);
    sb.append("\n");
    // Add Headers
    if(this.header != null && this.header.size() >= 1) {
      this.header.forEach(
          (k, v) -> {
                      sb.append(k + ":" + v);
                      sb.append("\n");
                    });
    }
    sb.append("\n");
    sb.append(this.body);
    sb.append('\u0000');
    return sb.toString();
  }

  @Override
  public String toString() {
    if(this.header == null || this.header.size() < 1) {
      return "COMMAND: " + this.command + " HEADERS: " +
          "" + " BODY: " + this.body;
    }
    return "COMMAND: " + this.command + " HEADERS: " +
           this.header.toString() + " BODY: " + this.body;
  }
}