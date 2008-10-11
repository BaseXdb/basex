package org.basex.query.xquery.path;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;

/**
 * XQuery Node Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class KindTest extends Test {
  /**
   * Constructor.
   * @param t node type
   */
  public KindTest(final Type t) {
    type = t;
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
  public boolean e(final Nod tmp) throws XQException {
    return tmp.type != type ? false : name == null || tmp.qname(qname).eq(name);
  }

  @Override
  public String toString() {
    return type + "()";
  }
}
