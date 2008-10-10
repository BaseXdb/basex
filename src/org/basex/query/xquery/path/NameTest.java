package org.basex.query.xquery.path;

import static org.basex.util.Token.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;

/**
 * XQuery Name Test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Local name. */
  private byte[] ln;
  /** Prefix flag. */
  private boolean pre;
  /** Namespace wildcard flag. */
  private boolean wild;

  /**
   * Empty Constructor ("*" test).
   */
  public NameTest() { }

  /**
   * Constructor.
   * @param n name
   * @param ctx xquery context
   * @param w wildcard flag
   * @throws XQException evaluation exception
   */
  public NameTest(final byte[] n, final boolean w, final XQContext ctx)
      throws XQException {
    
    name = new QNm(n, ctx);
    ln = name.ln();
    pre = !eq(ln, name.str());
    wild = w;
  }
  
  @Override
  public boolean e(final Nod tmp, final XQContext ctx) throws XQException {
    if(tmp.type != Type.ELM && tmp.type != Type.ATT) return false;

    // wildcard - accepting all names
    if(name == null) return true;

    // namespace wildcard
    if(wild) return eq(ln, ln(tmp.nname()));

    // namespace and name
    final QNm nm = tmp.qname(qname);
    return !pre && !nm.ns() ? eq(ln, nm.str()) : name.eq(nm);
  }

  @Override
  public String toString() {
    return name != null ? string(name.str()) : "*";
  }
}
