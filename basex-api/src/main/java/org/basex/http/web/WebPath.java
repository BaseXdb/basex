package org.basex.http.web;

/**
 * This abstract class represents the path of a Web function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public abstract class WebPath {
  /** Path. */
  protected final String path;

  /**
   * Constructor.
   * @param path path
   */
  protected WebPath(final String path) {
    this.path = path;
  }

  @Override
  public final String toString() {
    return path;
  }
}
