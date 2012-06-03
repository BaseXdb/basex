package org.basex.query.path;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Simple node kind test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
class KindTest extends Test {
  /**
   * Constructor.
   * @param t node type
   */
  KindTest(final NodeType t) {
    type = t;
  }

  @Override
  public boolean eq(final ANode node) {
    return node.type == type;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(type).toString();
  }
}
