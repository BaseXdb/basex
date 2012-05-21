package org.basex.query.path;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Name test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Local name. */
  public final byte[] ln;

  /**
   * Empty constructor ('*').
   * @param att attribute flag
   */
  public NameTest(final boolean att) {
    this(null, Mode.ALL, att);
  }

  /**
   * Constructor.
   * @param nm name
   * @param t type of name test
   * @param att attribute flag
   */
  public NameTest(final QNm nm, final Mode t, final boolean att) {
    type = att ? NodeType.ATT : NodeType.ELM;
    ln = nm != null ? nm.local() : null;
    name = nm;
    mode = t;
  }

  @Override
  public boolean comp(final QueryContext ctx) {
    // retrieve current data reference
    final Data data = ctx.data();
    if(data == null) return true;

    // skip optimizations if several namespaces are defined in the database
    final byte[] ns = data.nspaces.globalNS();
    if(ns == null) return true;

    // true if results can be expected
    boolean ok = true;

    if(mode == Mode.STD && !name.hasPrefix()) {
      // no results if default and database namespaces of elements are different
      final byte[] nse = ctx.sc.nsElem != null ? ctx.sc.nsElem : Token.EMPTY;
      ok = type == NodeType.ATT || Token.eq(ns, nse);
      if(ok) {
        // namespace is irrelevant or identical: ignore prefix to speed up test
        ctx.compInfo(OPTPREF, this);
        mode = Mode.NAME;
      }
    }

    // check existence of tag/attribute names
    ok = ok && (mode != Mode.NAME || (type == NodeType.ELM ?
        data.tagindex : data.atnindex).contains(ln));

    if(!ok) ctx.compInfo(OPTNAME, name);
    return ok;
  }

  @Override
  public boolean eq(final ANode node) {
    // only elements and attributes will yield results
    if(node.type != type) return false;

    switch(mode) {
      // wildcard - accept all nodes
      case ALL:
        return true;
      // namespaces wildcard - check only name
      case NAME:
        return Token.eq(ln, Token.local(node.name()));
      // name wildcard - check only namespace
      case NS:
        return Token.eq(name.uri(), node.qname(tmpq).uri());
      default:
        // check attributes, or check everything
        return type == NodeType.ATT && !name.hasPrefix() ?
            Token.eq(ln, node.name()) : name.eq(node.qname(tmpq));
    }
  }

  @Override
  public String toString() {
    if(mode == Mode.ALL) return "*";
    if(mode == Mode.NAME) return "*:" + Token.string(name.string());
    final String uri = name.uri().length == 0 || name.hasPrefix() ? "" :
            '{' + Token.string(name.uri()) + '}';
    return uri + (mode == Mode.NS ? "*" : Token.string(name.string()));
  }
}
