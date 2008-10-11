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
public abstract class Test {
  /** Static node test. */
  public static final Test NODE = new Test() {
    @Override
    public boolean e(final Nod tmp) {
      return true;
    }
    @Override
    public String toString() {
      return type + "()";
    }
  };
  /** Name test. */
  public QNm name;
  /** Node test. */
  public Type type;
  /** Temporary QName instance. */
  protected QNm qname = new QNm();
  
  /**
   * Tests the specified node.
   * @param tmp temporary node
   * @return result of check
   * @throws XQException evaluation exception
   */
  public abstract boolean e(final Nod tmp)
      throws XQException;
}
