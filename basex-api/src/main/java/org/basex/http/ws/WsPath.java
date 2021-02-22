package org.basex.http.ws;

import org.basex.http.web.*;

/**
 * This class represents the Path of a WebSocket endpoint.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public final class WsPath extends WebPath implements Comparable<WsPath> {
  /**
   * Constructor.
   * @param path WebSocket path
   */
  WsPath(final String path) {
    super(path);
  }

  @Override
  public int compareTo(final WsPath o) {
    return path.compareTo(o.path);
  }
}
