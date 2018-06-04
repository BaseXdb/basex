package org.basex.http.ws.stomp;

import java.util.*;

/**
 * Class for a Connect Frame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class ConnectFrame extends StompFrame {

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public ConnectFrame(final Commands cmd, final Map<String, String> header, final String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    Map<String, String> headers = this.getHeaders();
    System.out.println(headers.toString());
    if((headers.get("accept-version") == null) /*||
        (headers.get("host") == null)
        Host not neccessasry in stomp v.10 but in v1.1 and v1.2
        */
        ) {
      return false;
    }
    return true;
  }
}
