package org.basex.query.path;

import static org.basex.util.Token.*;
import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;

/**
 * XQuery name test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Local name. */
  public final byte[] ln;

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
    ln = nm != null ? nm.ln() : null;
    name = nm;
    kind = t;
  }

  @Override
  public boolean eval(final Nod node) throws QueryException {
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
  public boolean comp(final QueryContext ctx) throws QueryException {
    // check namespace context
    if(ctx.ns.size() != 0) {
      if(name != null && name.uri == Uri.EMPTY) {
        name.uri = Uri.uri(ctx.ns.uri(name.pref(), false));
      }
    } else {
      final Data data = ctx.data();
      if(data == null) return true;

      // check existence of namespaces in input document
      if(ctx.nsElem.length == 0 && data.ns.size() == 0) {
        // no prefix - check only name
        if(kind == Kind.STD && !name.ns()) kind = Kind.NAME;

        // pre-evaluate unknown tag/attribute names
        if(kind == Kind.NAME && (type == Type.ELM ? data.tagID(ln) :
          data.attNameID(ln)) == 0) {
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
