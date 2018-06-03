package org.basex.ws.stomp.copy;

import java.util.*;

/**
 * Class for a Message Frame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class MessageFrame extends StompFrame {

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public MessageFrame(final Commands cmd, final Map<String, String> header, final String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    Map<String, String> headers = this.getHeaders();
    System.out.println(headers.toString());
    if((headers.get("destination") == null) ||
        (headers.get("message-id") == null) ||
        (headers.get("subscription") == null)) {
      return false;
    }
    return true;
  }

}
