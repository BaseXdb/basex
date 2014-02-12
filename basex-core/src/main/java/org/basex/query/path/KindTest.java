package org.basex.query.path;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple node kind test.
 *
 * @author BaseX Team 2005-13, BSD License
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
  public Test copy() {
    return Test.get(type);
  }

  @Override
  public boolean eq(final ANode node) {
    return node.type == type;
  }

  @Override
  public String toString() {
    return String.valueOf(type);
  }

  @Override
  public Test intersect(final Test other) {
    if(other instanceof NodeTest || other instanceof DocTest) {
      return other.type.instanceOf(type) ? other : null;
    }
    if(other instanceof KindTest) {
      return type.instanceOf(other.type) ? this :
        other.type.instanceOf(type) ? other : null;
    }
    if(other instanceof NameTest || other instanceof InvDocTest) {
      throw Util.notExpected(other);
    }
    return null;
  }
}
