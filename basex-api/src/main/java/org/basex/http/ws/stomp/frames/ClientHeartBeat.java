package org.basex.http.ws.stomp.frames;

import java.util.*;

import org.eclipse.jetty.websocket.api.*;

/**
 * Checks if the Client is still connected and sends heartbeats.
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class ClientHeartBeat extends TimerTask{
  /** The Session.*/
  Session se;
  /** The lastActivity */
  LastActivity lastActivity;
  /** The interval between heartbeats from the client */
  Long interval;

  /**
   * Constructor.
   * @param se Session
   * @param lastActivity the last client activity
   * @param interval the interval
   * */
  public ClientHeartBeat( Session se, LastActivity lastActivity, long interval) {
    this.se = se;
    this.lastActivity = lastActivity;
    this.interval = interval;
  }

  @Override
  public void run() {
    if((System.currentTimeMillis() - lastActivity.getLastActivity()) > (interval * 2)) {
      se.close(StatusCode.ABNORMAL, "Heartbeat failed");
      cancel();
    }
  }
}
