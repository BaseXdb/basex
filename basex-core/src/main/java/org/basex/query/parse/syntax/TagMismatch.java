package org.basex.query.parse.syntax;

import org.basex.query.parse.gen.*;

/**
 * Raised instead of a syntax error if an element with different start and end tags was parsed
 * before: the hand-written parser reports that first.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class TagMismatch extends RuntimeException {
  /** Element. */
  private final transient Nodes.DirElemConstructor element;

  /**
   * Constructor.
   * @param element element with different start and end tags
   */
  TagMismatch(final Nodes.DirElemConstructor element) {
    super("Different start and end tag.", null, false, false);
    this.element = element;
  }

  /**
   * Returns the element.
   * @return element
   */
  public Nodes.DirElemConstructor element() {
    return element;
  }
}
