package org.basex.http.ws.stomp.frames;

/**
 * Wraps the last activity of the client.
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class LastActivity {
  /** The time of the last activity of the client*/
  private long time;

  /** Constructor */
  public LastActivity() {
    time = System.currentTimeMillis();
  }

  /** Returns the last Activity
   * @return time long*/
  public long getLastActivity() {
    return time;
  }

  /**
   * Sets the time of the last activity to the current time.
   * */
  public void setLastActivity() {
    this.time = System.currentTimeMillis() ;
  }
}
