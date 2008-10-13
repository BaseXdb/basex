package org.basex.query.xquery.path;

import static org.basex.util.Token.*;
import org.basex.query.xquery.XQException;
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
  public byte[] ln;

  /**
   * Empty Constructor ('*').
   */
  public NameTest() {
    kind = Kind.ALL;
  }

  /**
   * Constructor.
   * @param nm name
   * @param t test type
   */
  public NameTest(final QNm nm, final Kind t) {
    name = nm;
    ln = name.ln();
    kind = t;
  }
  
  @Override
  public boolean e(final Nod tmp) throws XQException {
    // only elements and attributes will yield results
    if(tmp.type != Type.ELM && tmp.type != Type.ATT) return false;
    
    // wildcard - accept all nodes
    if(kind == Kind.ALL) return true;
    // namespaces wildcard - check only name
    if(kind == Kind.NAME) return eq(ln, ln(tmp.nname()));
    
    final QNm nm = tmp.qname(qname);
    // name wildcard - check only namespace
    if(kind == Kind.NS) return nm.uri.eq(name.uri);
    // check everything
    return name.eq(nm);
  }

  @Override
  public String toString() {
    if(kind == Kind.ALL) return "*";
    if(kind == Kind.NAME) return "*:" + string(name.str());
    final String uri = name.uri == Uri.EMPTY || name.ns() ? "" :
      "{" + string(name.uri.str()) + "}";
    return uri + (kind == Kind.NS ? "*" : string(name.str()));
  }
}
