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
import org.basex.util.InputInfo;

/**
 * Name test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Input information. */
  public final InputInfo input;
  /** Local name. */
  public final byte[] ln;

  /**
   * Empty constructor ('*').
   * @param att attribute flag
   * @param ii input info
   */
  public NameTest(final boolean att, final InputInfo ii) {
    this(null, Name.ALL, att, ii);
  }

  /**
   * Constructor.
   * @param nm name
   * @param t type of name test
   * @param att attribute flag
   * @param ii input info
   */
  public NameTest(final QNm nm, final Name t, final boolean att,
      final InputInfo ii) {
    type = att ? Type.ATT : Type.ELM;
    ln = nm != null ? nm.ln() : null;
    name = nm;
    test = t;
    input = ii;
  }

  @Override
  public boolean comp(final QueryContext ctx) throws QueryException {
    // check namespace context
    if(ctx.ns.size() != 0) {
      if(name != null && !name.hasUri()) {
        name.uri(ctx.ns.uri(name.pref(), false, input));
      }
    }

    // retrieve current data reference
    final Data data = ctx.data();
    if(data == null) return true;

    final byte[] ns = data.ns.globalNS();
    // skip optimizations if several default namespaces are defined in the
    // database, or if default and database namespace are different
    if(ns == null || !eq(ns, ctx.nsElem)) return true;

    // no prefix: check only local name (faster)
    if(test == Name.STD && !name.ns()) {
      ctx.compInfo(OPTPREF, ln);
      test = Name.NAME;
    }

    // pre-evaluate unknown tag/attribute names
    if(test == Name.NAME && (type == Type.ELM ?
        data.tags : data.atts).id(ln) == 0) {
      ctx.compInfo(OPTNAME, ln);
      return false;
    }
    return true;
  }

  @Override
  public boolean eval(final Nod nod) throws QueryException {
    // only elements and attributes will yield results
    if(nod.type != type) return false;

    switch(test) {
      // wildcard - accept all nodes
      case ALL:
        return true;
      // namespaces wildcard - check only name
      case NAME:
        return eq(ln, ln(nod.nname()));
      // name wildcard - check only namespace
      case NS:
        return name.uri().eq(nod.qname(tmpq).uri());
      default:
        // check attributes, or check everything
        return type == Type.ATT && !name.ns() ? eq(ln, nod.nname()) :
          name.eq(nod.qname(tmpq));
    }
  }

  @Override
  public String toString() {
    if(test == Name.ALL) return "*";
    if(test == Name.NAME) return "*:" + string(name.atom());
    final String uri = name.uri() == Uri.EMPTY || name.ns() ? "" :
      "{" + string(name.uri().atom()) + "}";
    return uri + (test == Name.NS ? "*" : string(name.atom()));
  }
}
