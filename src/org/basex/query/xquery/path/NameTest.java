package org.basex.query.xquery.path;

import static org.basex.util.Token.*;
import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.DBNode;
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
   * @param att attribute flag
   */
  public NameTest(final boolean att) {
    this(null, Kind.ALL, att);
  }

  /**
   * Constructor.
   * @param nm name
   * @param t test type
   * @param att attribute flag
   */
  public NameTest(final QNm nm, final Kind t, final boolean att) {
    type = att ? Type.ATT : Type.ELM;
    if(nm != null) ln = nm.ln();
    name = nm;
    kind = t;
  }
  
  @Override
  public boolean eval(final Nod node) throws XQException {
    // only elements and attributes will yield results
    if(node.type != type) return false;
    
    // wildcard - accept all nodes
    if(kind == Kind.ALL) return true;
    // namespaces wildcard - check only name
    if(kind == Kind.NAME) return eq(ln, ln(node.nname()));
    
    final QNm nm = node.qname(tmpq);
    
    // name wildcard - check only namespace
    if(kind == Kind.NS) return name.uri.eq(nm.uri);
    // check everything
    return name.eq(nm);
  }
  
  @Override 
  public boolean comp(final XQContext ctx) throws XQException {
    // check namespace context
    if(ctx.ns.size() != 0) {
      if(name != null && name.uri == Uri.EMPTY) {
        name.uri = Uri.uri(ctx.ns.uri(name.pre()));
      }
    } else {
      final DBNode db = ctx.dbroot();
      if(db == null) return true;
      
      // check existence of namespaces in input document
      if(ctx.nsElem.length == 0 && db.data.ns.size() == 0) {
        // no namespaces - check only name
        if(kind == Kind.STD && !name.ns()) kind = Kind.NAME;
        
        // pre-evaluate unknown tag/attribute names
        if(kind == Kind.NAME && (type == Type.ELM ? db.data.tagID(ln) :
          db.data.attNameID(ln)) == 0) {
            ctx.compInfo(OPTNAME, ln);
            return false;
        }
      }
    }
    return true;
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
