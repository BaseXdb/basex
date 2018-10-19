package org.basex.query.expr.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Document kind test.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends Test {
  /** Child test. */
  private final Test test;

  /**
   * Constructor.
   * @param test child test (node test or element kind test)
   */
  public DocTest(final Test test) {
    super(NodeType.DOC);
    this.test = test;
  }

  @Override
  public Test copy() {
    return new DocTest(test);
  }

  @Override
  public boolean eq(final ANode node) {
    if(node.type != NodeType.DOC) return false;
    final BasicNodeIter iter = node.children();
    boolean found = false;
    for(ANode n; (n = iter.next()) != null;) {
      if(n.type == NodeType.COM || n.type == NodeType.PI) continue;
      if(found || !test.eq(n)) return false;
      found = true;
    }
    return true;
  }

  @Override
  public boolean nsSensitive() {
    return test != null && test.nsSensitive();
  }

  @Override
  public Test intersect(final Test other) {
    if(other instanceof DocTest) {
      final DocTest dt = (DocTest) other;
      if(test == null || dt.test == null || test.equals(dt.test))
        return test != null ? this : other;
      final Test tp = test.intersect(dt.test);
      return tp == null ? null : new DocTest(tp);
    }
    if(other instanceof KindTest) return NodeType.DOC.instanceOf(other.type) ? this : null;
    if(other instanceof InvDocTest) return this;
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof DocTest && obj.equals(((DocTest) obj).test);
  }

  @Override
  public String toString() {
    return Strings.concat(type.string(), '(', test, ')');
  }
}
