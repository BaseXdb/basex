package org.basex.query.path;

import org.basex.query.QueryContext;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;

/**
 * Kind test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class KindTest extends Test {
  /** Type name. */
  public Type extype;

  /**
   * Constructor.
   * @param t node type
   */
  KindTest(final NodeType t) {
    this(t, null, null);
  }

  /**
   * Constructor.
   * @param t node type
   * @param ext type extension
   * @param et type name extension
   */
  public KindTest(final NodeType t, final QNm ext, final Type et) {
    type = t;
    name = ext;
    extype = et;
  }

  @Override
  public boolean comp(final QueryContext ctx) {
    return extype == null || type == extype;
  }

  @Override
  public boolean eval(final ANode node) {
    return node.type == type && (name == null || node.update(tmpq).eq(name));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(type.toString());
    // ...
    return sb.toString();
  }
}
