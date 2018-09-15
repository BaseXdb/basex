package org.basex.http.ws.stomp;

import java.util.*;

/**
 * Class for a UnsubscribeFrame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class UnsubscribeFrame extends StompFrame {

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public UnsubscribeFrame(final Commands cmd, final Map<String, String> header, final String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    Map<String, String> headers = this.getHeaders();
    if(headers.get("id") == null) {
      return false;
    }
    return true;
  }

}