package org.basex.query.expr.path;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Document with single child test.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends Test {
  /** Child test. */
  private final Test child;

  /**
   * Constructor.
   * @param child child element test
   */
  public DocTest(final Test child) {
    super(Kind.DOCUMENT);
    this.child = child;
  }

  @Override
  public Test copy() {
    return new DocTest(child);
  }

  @Override
  public boolean matches(final XNode node) {
    boolean found = false;
    if(node.kind() == Kind.DOCUMENT) {
      for(final XNode n : node.childIter()) {
        if(n.kind().oneOf(Kind.COMMENT, Kind.PROCESSING_INSTRUCTION)) continue;
        if(found || !child.matches(n)) return false;
        found = true;
      }
    }
    return found;
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof final DocTest dt) {
      if(equals(dt)) return this;
    } else if(test instanceof NodeTest || test instanceof UnionTest) {
      return test.intersect(this);
    }
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof final DocTest dt && obj.equals(dt.child);
  }

  @Override
  public String toString(final boolean full) {
    return kind.toString(child.toString());
  }
}
