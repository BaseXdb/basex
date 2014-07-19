package org.basex.util.options;

/**
 * Comment.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Comment extends Option<Object> {
  /**
   * Constructor without default value.
   * @param name name
   */
  public Comment(final String name) {
    super(name);
  }

  @Override
  public Object value() {
    return null;
  }
}
