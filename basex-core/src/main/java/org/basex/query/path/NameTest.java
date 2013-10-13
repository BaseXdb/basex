package org.basex.query.path;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Name test.
 *
 * @author BaseX Team 2005-13, BSD License
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
  public boolean compile(final QueryContext ctx) {
    // skip optimizations if data reference cannot be determined statically
    final Data data = ctx.data();
    if(data == null) return true;

    // skip optimizations if more than one namespace is defined in the database
    final byte[] dataNS = data.nspaces.globalNS();
    if(dataNS == null) return true;

    // check if test may yield results
    boolean results = true;
    if(mode == Mode.STD && !name.hasURI()) {
      final byte[] elemNS = ctx.sc.elemNS != null ? ctx.sc.elemNS : Token.EMPTY;
      if(type == NodeType.ATT || Token.eq(dataNS, elemNS)) {
        // namespace is irrelevant/identical: only check local name
        mode = Mode.LN;
      } else {
        // element and db default namespaces are different: no results
        results = false;
      }
    }

    // check existence of tag/attribute names
    if(results) results = mode != Mode.LN ||
        (type == NodeType.ELM ? data.tagindex : data.atnindex).contains(ln);

    if(!results) ctx.compInfo(OPTNAME, name);
    return results;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean eq(final ANode node) {
    // only elements and attributes will yield results
    if(node.type != type) return false;

    switch(mode) {
      // wildcard: accept all nodes
      case ALL: return true;
      // namespaces wildcard: only check local name
      case LN: return Token.eq(ln, Token.local(node.name()));
      // name wildcard: only check namespace
      case NS: return Token.eq(name.uri(), node.qname(tmpq).uri());
      // check attributes, or check everything
      default: return type == NodeType.ATT && !name.hasURI() ?
        Token.eq(ln, node.name()) : name.eq(node.qname(tmpq));
    }
  }

  /**
   * Checks if the specified name matches the test.
   * @param nm name
   * @return result of check
   */
  public boolean eq(final QNm nm) {
    switch(mode) {
      // wildcard: accept all nodes
      case ALL:  return true;
      // namespaces wildcard: only check local name
      case LN: return Token.eq(ln, nm.local());
      // name wildcard: only check namespace
      case NS: return Token.eq(name.uri(), nm.uri());
      // check everything
      default: return name.eq(nm);
    }
  }

  @Override
  public String toString() {
    if(mode == Mode.ALL) return "*";
    if(mode == Mode.LN) return "*:" + Token.string(name.string());
    final String uri = name.uri().length == 0 || name.hasPrefix() ? "" :
      '{' + Token.string(name.uri()) + '}';
    return uri + (mode == Mode.NS ? "*" : Token.string(name.string()));
  }

  @Override
  public Test intersect(final Test other) {
    throw Util.notexpected(other);
  }
}
