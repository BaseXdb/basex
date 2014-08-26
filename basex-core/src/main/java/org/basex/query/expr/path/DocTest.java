package org.basex.query.expr.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Document kind test.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends Test {
  /** Optional child test. */
  private final Test test;

  /**
   * Constructor.
   * @param test child test (may be {@code null})
   */
  public DocTest(final Test test) {
    this.test = test;
    type = NodeType.DOC;
  }

  @Override
  public Test copy() {
    return new DocTest(test);
  }

  @Override
  public boolean eq(final ANode node) {
    if(node.type != NodeType.DOC) return false;
    final AxisMoreIter ai = node.children();
    return ai.more() && test.eq(ai.next()) && !ai.more();
  }

  @Override
  public boolean nsSensitive() {
    return test != null && test.nsSensitive();
  }

  @Override
  public Test intersect(final Test other) {
    if(other instanceof DocTest) {
      final DocTest o = (DocTest) other;
      if(test == null || o.test == null || test.sameAs(o.test)) return test != null ? this : other;
      final Test t = test.intersect(o.test);
      return t == null ? null : new DocTest(t);
    }
    if(other instanceof KindTest) return NodeType.DOC.instanceOf(other.type) ? this : null;
    if(other instanceof InvDocTest) return this;
    return null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append(type).append('(');
    if(test != null) sb.append(test);
    return sb.append(')').toString();
  }
}
