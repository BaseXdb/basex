package org.basex.http;

/**
 * Single HTTP Accept header.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class HTTPAccept {
  /** Media type. */
  public String type;
  /** Quality factor (default: {@code 1}). */
  public double qf = 1;
}
