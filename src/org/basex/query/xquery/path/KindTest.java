package org.basex.query.xquery.path;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;

/**
 * XQuery Kind Test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class KindTest extends Test {
  /**
   * Constructor.
   * @param t node type
   */
  public KindTest(final Type t) {
    this(t, null);
  }

  /**
   * Constructor.
   * @param t node type
   * @param ext type extension
   */
  public KindTest(final Type t, final QNm ext) {
    type = t;
    name = ext;
  }
  
  @Override
  public boolean eval(final Nod n) throws XQException {
    return n.type != type ? false : name == null || n.qname(tmpq).eq(name);
  }

  @Override
  public String toString() {
    return type + "()";
  }
}
