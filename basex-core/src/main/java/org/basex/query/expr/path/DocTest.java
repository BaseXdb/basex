package org.basex.query.expr.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Document kind test.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends Test {
  /** Child test. */
  private final Test child;

  /**
   * Constructor.
   * @param test child test (node test or element kind test)
   */
  public DocTest(final Test test) {
    super(NodeType.DOC);
    child = test;
  }

  @Override
  public Test copy() {
    return new DocTest(child);
  }

  @Override
  public boolean matches(final ANode node) {
    if(node.type != NodeType.DOC) return false;
    final BasicNodeIter iter = node.childIter();
    boolean found = false;
    for(ANode n; (n = iter.next()) != null;) {
      if(n.type == NodeType.COM || n.type == NodeType.PI) continue;
      if(found || !child.matches(n)) return false;
      found = true;
    }
    return true;
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof UnionTest) {
      return test.intersect(this);
    }
    if(test instanceof DocTest) {
      final DocTest dt = (DocTest) test;
      if(child == null || dt.child == null || child.equals(dt.child))
        return child != null ? this : test;
      final Test tp = child.intersect(dt.child);
      return tp == null ? null : new DocTest(tp);
    }
    if(test instanceof KindTest) return NodeType.DOC.instanceOf(test.type) ? this : null;
    if(test instanceof InvDocTest) return this;
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof DocTest && obj.equals(((DocTest) obj).child);
  }

  @Override
  public String toString() {
    return Strings.concat(type.string(), '(', child, ')');
  }
}
