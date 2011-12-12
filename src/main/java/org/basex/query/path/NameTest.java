package org.basex.query.path;

import static org.basex.util.Token.*;
import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.util.InputInfo;

/**
 * Name test.
 *
 * @author BaseX Team 2005-11, BSD License
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
    type = att ? NodeType.ATT : NodeType.ELM;
    ln = nm != null ? nm.local() : null;
    name = nm;
    test = t;
    input = ii;
  }

  @Override
  public boolean comp(final QueryContext ctx) throws QueryException {
    // retrieve current data reference
    final Data data = ctx.data();
    if(data == null) return true;

    // skip optimizations if several namespaces are defined in the database
    final byte[] ns = data.nspaces.globalNS();
    if(ns == null) return true;

    // true if results can be expected
    boolean ok = true;

    if(test == Name.STD && !name.hasPrefix()) {
      // no results if default and database namespaces of elements are different
      ok = type == NodeType.ATT || ctx.sc.nsElem == null ||
          eq(ns, ctx.sc.nsElem);
      if(ok) {
        // namespace is irrelevant or identical: ignore prefix to speed up test
        if(ns.length != 0) ctx.compInfo(OPTPREF, ln);
        test = Name.NAME;
      }
    }

    // check existence of tag/attribute names
    ok = ok && (test != Name.NAME || (type == NodeType.ELM ?
        data.tagindex : data.atnindex).id(ln) != 0);

    if(!ok) ctx.compInfo(OPTNAME, name);
    return ok;
  }

  @Override
  public boolean eval(final ANode node) {
    // only elements and attributes will yield results
    if(node.type != type) return false;

    switch(test) {
      // wildcard - accept all nodes
      case ALL:
        return true;
      // namespaces wildcard - check only name
      case NAME:
        return eq(ln, local(node.name()));
      // name wildcard - check only namespace
      case NS:
        return eq(name.uri(), node.update(tmpq).uri());
      default:
        // check attributes, or check everything
        return type == NodeType.ATT && !name.hasPrefix() ?
            eq(ln, node.name()) : name.eq(node.update(tmpq));
    }
  }

  @Override
  public String toString() {
    if(test == Name.ALL) return "*";
    if(test == Name.NAME) return "*:" + string(name.string());
    final String uri = name.uri().length == 0 || name.hasPrefix() ? "" :
      "{" + string(name.uri()) + "}";
    return uri + (test == Name.NS ? "*" : string(name.string()));
  }
}
