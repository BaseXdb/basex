package org.basex.http.ws;

/**
 * This class represents the Path of a Websocket-Endpoint.
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class WsPath implements Comparable<WsPath> {

  /**
   * The Path.
   * */
  private final String path;

  /**
   * The Constructor.
   * @param path String the Path
   */
  public WsPath(final String path) {
    this.path = path;
  }

  @Override
  public int compareTo(final WsPath o) {
    return this.path.compareTo(o.getPath());
  }

  /**
   * Returns the Path.
   * @return String the path
   */
  public String getPath() {
    return path;
  }
}
