package org.basex.ws.response.stomp;

import java.util.*;

/**
 * Class for a Error Frame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class ErrorFrame extends StompFrame {

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public ErrorFrame(final Commands cmd, final Map<String, String> header, final String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    // Nothing required
    return true;
  }

}
