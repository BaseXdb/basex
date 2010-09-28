package org.basex.query.path;

import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;

/**
 * Kind test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class KindTest extends Test {
  /**
   * Constructor.
   * @param t node type
   */
  KindTest(final Type t) {
    this(t, null);
  }

  /**
   * Constructor.
   * @param t node type
   * @param ext type extension
   */
  KindTest(final Type t, final QNm ext) {
    type = t;
    name = ext;
  }

  @Override
  public boolean eval(final Nod n) throws QueryException {
    return n.type != type ? false : name == null || n.qname(tmpq).eq(name);
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
