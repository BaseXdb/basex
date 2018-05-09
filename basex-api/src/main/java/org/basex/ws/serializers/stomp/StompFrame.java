package org.basex.ws.serializers.stomp;

import java.util.*;

/**
 * Represents the a Frame in the Stomp Protocol.
 * @author BaseX Team 2005-18, BSD License
 * TODO: Body kann auch Binary sein -> neue klasse: StompBody ?
 */
public class StompFrame {
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
   */
  public static StompFrame parse(final String message) {

    String cmdhead = message.split("\n\n")[0];
    String[] headers = cmdhead.split("\n");

    Map<String, String> header = new HashMap<>();

    for (int i = 1; i < headers.length; i++) {
      String head = headers[i].split(":")[0];
      header.put(head, headers[i].substring(head.length() + 1));
    }

    return new StompFrame(
        Commands.valueOf(headers[0]),
        header,
        message.substring(cmdhead.length() + 2)
        );
  }

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
   * Returns the Body.
   * @return String body
   * */
  public String getBody() {
    return body;
  }

  @Override
  public String toString() {
    return "COMMAND: " + this.command + " HEADERS: " +
           this.header.toString() + " BODY: " + this.body;
  }
}
