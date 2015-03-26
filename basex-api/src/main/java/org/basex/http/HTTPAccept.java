package org.basex.http;

import org.basex.util.http.*;

/**
 * Single HTTP Accept header.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class HTTPAccept {
  /** Media type. */
  public MediaType type;
  /** Quality factor (default: {@code 1}). */
  public double qf = 1;
}
