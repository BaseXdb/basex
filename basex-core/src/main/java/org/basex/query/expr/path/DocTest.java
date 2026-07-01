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
  public boolean matches(final GNode node) {
    boolean found = false;
    if(node.kind() == Kind.DOCUMENT) {
      for(final GNode n : node.childIter()) {
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
      final Test isect = child.intersect(dt.child);
      return isect != null ? new DocTest(isect) : null;
    }
    if(test instanceof NodeTest || test instanceof UnionTest) {
      return test.intersect(this);
    }
    return null;
  }

  @Override
  public boolean instanceOf(final Test test) {
    if(this == test) return true;
    // distribute a union child: document-node(a|b) is document-node(a)|document-node(b)
    if(child instanceof final UnionTest ut) {
      for(final Test t : ut.tests) {
        if(!new DocTest(t).instanceOf(test)) return false;
      }
      return true;
    }
    // compare the child tests of two document tests
    if(test instanceof final DocTest dt) return child.instanceOf(dt.child);
    // union on the right-hand side, generic node tests
    return super.instanceOf(test);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final DocTest dt && child.equals(dt.child);
  }

  @Override
  public String toString(final boolean type) {
    return kind.toString(child.toString());
  }
}
