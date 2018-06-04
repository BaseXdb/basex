package org.basex.http.ws.stomp;

import java.util.*;

/**
 * Class for a Disconnect Frame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class DisconnectFrame extends StompFrame {

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public DisconnectFrame(final Commands cmd, final Map<String, String> header, final String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    return true;
  }

}
