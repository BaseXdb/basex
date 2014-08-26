package org.basex.query.expr.path;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Local name. */
  public final byte[] local;
  /** Default element namespace. */
  private final byte[] defElemNS;

  /**
   * Empty constructor ('*').
   * @param attr attribute flag
   */
  public NameTest(final boolean attr) {
    this(null, Kind.WILDCARD, attr, null);
  }

  /**
   * Constructor.
   * @param name name (may be {@code null})
   * @param mode type of name test
   * @param attr attribute flag
   * @param elemNS default element namespace (may be {@code null})
   */
  public NameTest(final QNm name, final Kind mode, final boolean attr, final byte[] elemNS) {
    type = attr ? NodeType.ATT : NodeType.ELM;
    local = name != null ? name.local() : null;
    defElemNS = elemNS != null ? elemNS : Token.EMPTY;
    this.name = name;
    kind = mode;
  }

  @Override
  public boolean optimize(final QueryContext qc) {
    // skip optimizations if data reference cannot be determined statically
    final Data data = qc.data();
    if(data == null) return true;

    // skip optimizations if more than one namespace is defined in the database
    final byte[] dataNS = data.nspaces.globalNS();
    if(dataNS == null) return true;

    // check if test may yield results
    boolean results = true;
    if(kind == Kind.URI_NAME && !name.hasURI()) {
      if(type == NodeType.ATT || Token.eq(dataNS, defElemNS)) {
        // namespace is irrelevant/identical: only check local name
        kind = Kind.NAME;
      } else {
        // element and db default namespaces are different: no results
        results = false;
      }
    }

    // check existence of element/attribute names
    if(results) results = kind != Kind.NAME ||
        (type == NodeType.ELM ? data.elemNames : data.attrNames).contains(local);

    if(!results) qc.compInfo(OPTNAME, name);
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

    switch(kind) {
      // wildcard: accept all nodes
      case WILDCARD: return true;
      // namespaces wildcard: only check local name
      case NAME: return Token.eq(local, Token.local(node.name()));
      // name wildcard: only check namespace
      case URI: return Token.eq(name.uri(), node.qname(tmpq).uri());
      // check attributes, or check everything
      default: return type == NodeType.ATT && !name.hasURI() ?
        Token.eq(local, node.name()) : name.eq(node.qname(tmpq));
    }
  }

  /**
   * Checks if the specified name matches the test.
   * @param nm name
   * @return result of check
   */
  public boolean eq(final QNm nm) {
    switch(kind) {
      // wildcard: accept all nodes
      case WILDCARD: return true;
      // namespaces wildcard: only check local name
      case NAME: return Token.eq(local, nm.local());
      // name wildcard: only check namespace
      case URI: return Token.eq(name.uri(), nm.uri());
      // check everything
      default: return name.eq(nm);
    }
  }

  /**
   * Checks if the specified test is equals to this test.
   * @param test test to be compared
   * @return result of check
   */
  public boolean eq(final NameTest test) {
    if(kind != test.kind) return false;
    switch(kind) {
      // wildcard: accept all nodes
      case WILDCARD: return true;
      // namespaces wildcard: only check local name
      case NAME: return Token.eq(local, test.local);
      // name wildcard: only check namespace
      case URI: return Token.eq(name.uri(), test.name.uri());
      // check everything
      default: return name.eq(test.name);
    }
  }

  @Override
  public Test intersect(final Test other) {
    throw Util.notExpected(other);
  }

  @Override
  public String toString() {
    if(kind == Kind.WILDCARD) return "*";
    if(kind == Kind.NAME) return "*:" + Token.string(name.string());
    final String uri = name.uri().length == 0 || name.hasPrefix() ? "" :
      '{' + Token.string(name.uri()) + '}';
    return uri + (kind == Kind.URI ? "*" : Token.string(name.string()));
  }
}
