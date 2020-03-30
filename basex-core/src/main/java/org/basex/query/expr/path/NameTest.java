package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Name test.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Default element namespace. */
  private final byte[] defaultNs;
  /** Local name. */
  public final byte[] local;
  /** QName test. */
  public final QNm qname;
  /** Part of name to be tested. */
  public NamePart part;

  /**
   * Convenience constructor for element tests.
   * @param name node name
   */
  public NameTest(final QNm name) {
    this(NodeType.ELM, name, NamePart.FULL, null);
  }

  /**
   * Constructor.
   * @param type node type
   * @param qname name
   * @param part part of name to be tested
   * @param defaultNs default element namespace (used for optimizations, can be {@code null})
   */
  public NameTest(final NodeType type, final QNm qname, final NamePart part,
      final byte[] defaultNs) {

    super(type);
    this.defaultNs = defaultNs != null ? defaultNs : Token.EMPTY;
    this.part = part;
    this.qname = qname;
    local = qname.local();
  }

  @Override
  public boolean optimize(final Expr expr) {
    // skip optimizations if context value has no data reference
    if(expr == null) return true;
    final Data data = expr.data();
    if(data == null) return true;

    // skip optimizations if more than one namespace is defined in the database
    final byte[] dataNs = data.defaultNs();
    if(dataNs == null) return true;

    // check if test may yield results
    if(part == NamePart.FULL && !qname.hasURI()) {
      if(type == NodeType.ATT || Token.eq(dataNs, defaultNs)) {
        // namespace is irrelevant/identical: only check local name
        part = NamePart.LOCAL;
      } else {
        // element and db default namespaces are different: no results
        return false;
      }
    }

    // check existence of local element/attribute names
    return type == NodeType.PI || part != NamePart.LOCAL ||
      (type == NodeType.ELM ? data.elemNames : data.attrNames).contains(local);
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean matches(final ANode node) {
    if(node.type != type) return false;

    switch(part) {
      // namespaces wildcard: only check local name
      case LOCAL: return Token.eq(local, Token.local(node.name()));
      // name wildcard: only check namespace
      case URI: return Token.eq(qname.uri(), node.qname().uri());
      // check attributes, or check everything
      default: return qname.eq(node.qname());
    }
  }

  /**
   * Checks if the specified name matches the test.
   * @param qName name
   * @return result of check
   */
  public boolean matches(final QNm qName) {
    switch(part) {
      // namespaces wildcard: only check local name
      case LOCAL: return Token.eq(local, qName.local());
      // name wildcard: only check namespace
      case URI: return Token.eq(qname.uri(), qName.uri());
      // check everything
      default: return qname.eq(qName);
    }
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof UnionTest) {
      return test.intersect(this);
    }
    if(test instanceof NameTest) {
      final NameTest nt = (NameTest) test;
      return type == nt.type && qname.eq(nt.qname) ? this : null;
    }
    if(test instanceof KindTest) {
      return type.instanceOf(test.type) ? this : null;
    }
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof NameTest)) return false;
    final NameTest nt = (NameTest) obj;
    return type == nt.type && qname.eq(nt.qname);
  }

  /**
   * Returns a string representation of the name test.
   * @param full include node type
   * @return string
   */
  public String toString(final boolean full) {
    final TokenBuilder tb = new TokenBuilder();
    if(full) tb.add(type.string()).add('(');
    if(part == NamePart.LOCAL) {
      tb.add('*').add(':');
    } else if(qname.hasPrefix()) {
      tb.add(qname.prefix()).add(':');
    } else if(qname.uri().length != 0) {
      tb.add('{').add(qname.uri()).add('}');
    }
    if(part == NamePart.URI) {
      tb.add('*');
    } else {
      tb.add(qname.local());
    }
    if(full) tb.add(')');
    return tb.toString();
  }

  @Override
  public String toString() {
    return toString(true);
  }
}
