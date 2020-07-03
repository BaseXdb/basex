package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.*;
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
  public boolean noMatches(final Data data) {
    // skip optimizations if data reference is not known at compile time
    if(data == null) return false;

    // skip optimizations if more than one namespace is defined in the database
    final byte[] dataNs = data.defaultNs();
    if(dataNs == null) return false;

    // check if test may yield results
    if(part == NamePart.FULL && !qname.hasURI()) {
      if(type == NodeType.ATT || Token.eq(dataNs, defaultNs)) {
        // namespace is irrelevant/identical: only check local name
        part = NamePart.LOCAL;
      } else {
        // element and db default namespaces are different: no results
        return true;
      }
    }

    // check existence of local element/attribute names
    return type != NodeType.PI && part == NamePart.LOCAL &&
      !(type == NodeType.ELM ? data.elemNames : data.attrNames).contains(local);
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

  @Override
  public String toString(final boolean full) {
    final TokenBuilder tb = new TokenBuilder();
    final boolean pi = type == NodeType.PI;
    if(full || pi) tb.add(type.name).add('(');

    // add URI part
    final byte[] prefix = qname.prefix(), uri = qname.uri();
    if(part == NamePart.LOCAL && !pi) {
      tb.add("*:");
    } else if(prefix.length > 0) {
      tb.add(prefix).add(':');
    } else if(uri.length != 0) {
      tb.add(QueryText.EQNAME).add(uri).add('}');
    }
    // add local part
    if(part == NamePart.URI) {
      tb.add('*');
    } else {
      tb.add(qname.local());
    }

    if(full || pi) tb.add(')');
    return tb.toString();
  }
}
