package org.basex.http.ws.stomp.frames;

import java.util.*;

import org.basex.http.ws.stomp.*;

/**
 * Class for a Receipt Frame.
 * @author BaseX Team 2005-18, BSD License
 * */
public class ReceiptFrame  extends StompFrame{

  /**
   * Constructor.
   * @param cmd The Command.
   * @param header The header map
   * @param body the body
   */
  public ReceiptFrame(Commands cmd, Map<String, String> header, String body) {
    super(cmd, header, body);
  }

  @Override
  public boolean checkValidity() {
    Map<String, String> headers = this.getHeaders();
    if(headers.get("receipt-id") == null) {
      return false;
    }
    return true;
  }

}
