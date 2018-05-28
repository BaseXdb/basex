package org.basex.ws.serializers.stomp;

import java.util.*;

/**
 * Class for a Connected Frame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class ConnectedFrame extends StompFrame {

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public ConnectedFrame(final Commands cmd, final Map<String, String> header, final String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    Map<String, String> headers = this.getHeaders();
    System.out.println(headers.toString());
    if(headers.get("version") == null) {
      return false;
    }
    return true;
  }

}
