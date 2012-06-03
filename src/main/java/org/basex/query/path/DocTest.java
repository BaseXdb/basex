package org.basex.query.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Document kind test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends Test {
  /** Optional child test. */
  private final Test test;

  /**
   * Constructor.
   * @param t child test (may be {@code null})
   */
  public DocTest(final Test t) {
    type = NodeType.DOC;
    test = t;
  }

  @Override
  public boolean eq(final ANode node) {
    if(node.type != NodeType.DOC) return false;
    final AxisMoreIter ai = node.children();
    return ai.more() && test.eq(ai.next()) && !ai.more();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append(type).append('(');
    if(test != null) sb.append(test);
    return sb.append(')').toString();
  }
}
