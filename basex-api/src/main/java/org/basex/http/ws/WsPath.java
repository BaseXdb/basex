package org.basex.http.ws;

import org.basex.http.web.*;

/**
 * This class represents the Path of a WebSocket endpoint.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class WsPath extends WebPath implements Comparable<WsPath> {
  /**
   * Constructor.
   * @param path String the Path
   */
  public WsPath(final String path) {
    super(path);
  }

  @Override
  public int compareTo(final WsPath o) {
    return path.compareTo(o.path);
  }
}
