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
  /** Test types. */
  public enum TYPE {
    /** Accept all nodes (*).     */ ALL,
    /** Test names (*:tag).       */ NAME,
    /** Test namespaces (pre:*).  */ NS,
    /** Test all nodes (pre:tag). */ STD
  };
  /** Local name. */
  private byte[] ln;
  /** Test type. */
  private TYPE test;

  /**
   * Empty Constructor ('*').
   */
  public NameTest() {
    test = TYPE.ALL;
  }

  /**
   * Constructor.
   * @param nm name
   * @param t test type
   */
  public NameTest(final QNm nm, final TYPE t) {
    name = nm;
    ln = name.ln();
    test = t;
  }
  
  @Override
  public boolean e(final Nod tmp) throws XQException {
    // only elements and attributes will yield results
    if(tmp.type != Type.ELM && tmp.type != Type.ATT) return false;
    
    if(test == TYPE.ALL) return true;
    if(test == TYPE.NAME) return eq(ln, ln(tmp.nname()));
    
    final QNm nm = tmp.qname(qname);
    return test == TYPE.NS ? nm.uri.eq(name.uri) : name.eq(nm);
  }

  @Override
  public String toString() {
    if(test == TYPE.ALL) return "*";
    if(test == TYPE.NAME) return "*:" + string(name.str());
    final String uri = name.uri == Uri.EMPTY || name.ns() ? "" :
      "{" + string(name.uri.str()) + "}";
    return uri + (test == TYPE.NS ? "*" : string(name.str()));
  }
}
