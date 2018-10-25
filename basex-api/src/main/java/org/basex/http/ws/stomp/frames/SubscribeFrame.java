package org.basex.http.ws.stomp.frames;

import java.util.*;

import org.basex.http.ws.stomp.*;

/**
 * Class for a Subscribe Frame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class SubscribeFrame extends StompFrame {

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public SubscribeFrame(final Commands cmd, final Map<String, String> header, final String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    Map<String, String> headers = this.getHeaders();
    if((headers.get("destination") == null) ||
        (headers.get("id") == null)
        ) {
      return false;
    }
    return true;
  }

}