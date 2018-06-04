package org.basex.http.ws;

import org.basex.http.util.*;

/**
 * This class represents the Path of a Websocket-Endpoint.
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class WsPath extends WebPath implements Comparable<WsPath> {

  /**
   * The Constructor.
   * @param path String the Path
   */
  public WsPath(final String path) {
    super(path);
  }

  @Override
  public int compareTo(final WsPath o) {
    return this.path.compareTo(o.toString());
  }
}
