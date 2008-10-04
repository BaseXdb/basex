package org.basex.query.xquery.path;

import static org.basex.util.Token.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;

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
    
    name = new QNm(n);
    name.check(ctx);
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
    if(wild) {
      final byte[] nm = tmp.nname();
      return eq(ln, substring(nm, indexOf(nm, ':') + 1));
    }

    // namespace and name
    final QNm nm = tmp.qname(qname);
    if(!pre && !nm.ns()) return eq(nm.str(), ln);
    
    ctx.ns.uri(nm);
    return eq(ln, nm.ln()) && (name.uri.eq(nm.uri) ||
      name.uri == Uri.EMPTY && nm.uri == Uri.XMLNS);
  }

  @Override
  public String toString() {
    return name != null ? string(name.str()) : "*";
  }
}
