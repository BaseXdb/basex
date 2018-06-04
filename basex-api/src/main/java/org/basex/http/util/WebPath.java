package org.basex.http.util;


/**
 * This abstract class represents the path of a Web function.
 *
 * @author BaseX Team 2005-18, BSD License
 */
public abstract class WebPath {
  /**
   * Path.
   * */
  protected final String path;

  /**
   * Constructor.
   * @param path The Path
   */
  protected WebPath(final String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return path;
  }
}
