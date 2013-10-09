package org.basex.util.options;

/**
 * Comment.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Comment extends Option {
  /**
   * Constructor without default value.
   * @param n name
   */
  public Comment(final String n) {
    super(n);
  }

  @Override
  public Object value() {
    return null;
  }
}
