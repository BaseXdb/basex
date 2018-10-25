package org.basex.http.ws.stomp.frames;

import java.util.*;

import org.eclipse.jetty.websocket.api.*;

/**
 * A HeartBeat.
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class HeartBeat extends TimerTask{
  /** The RemoteEndpoint.*/
  RemoteEndpoint re;
  /**
   * Constructor.
   * @param re Remote Endpoint
   * */
  public HeartBeat( RemoteEndpoint re) {
    this.re = re;
  }
  @Override
  public void run() {
    re.sendStringByFuture("\n");
  }
}
