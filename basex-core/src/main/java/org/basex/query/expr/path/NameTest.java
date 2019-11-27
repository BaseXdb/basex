package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Name test.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Default element namespace. */
  private final byte[] defaultNs;
  /** Local name. */
  public final byte[] local;
  /** QName test. */
  public final QNm name;
  /** Part of name to be tested. */
  private NamePart part;

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
   * @param name name
   * @param part part of name to be tested
   * @param defaultNs default element namespace (required for optimizations, can be {@code null})
   */
  public NameTest(final NodeType type, final QNm name, final NamePart part,
      final byte[] defaultNs) {

    super(type);
    this.defaultNs = defaultNs != null ? defaultNs : Token.EMPTY;
    this.part = part;
    this.name = name;
    local = name.local();
  }

  @Override
  public boolean optimize(final Value value) {
    // skip optimizations if context value has no data reference
    if(value == null) return true;
    final Data data = value.data();
    if(data == null) return true;

    // skip optimizations if more than one namespace is defined in the database
    final byte[] dataNs = data.nspaces.globalUri();
    if(dataNs == null) return true;

    // check if test may yield results
    if(part == NamePart.FULL && !name.hasURI()) {
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
  public NamePart part() {
    return part;
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
      case URI: return Token.eq(name.uri(), node.qname().uri());
      // check attributes, or check everything
      default: return name.eq(node.qname());
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
      case URI: return Token.eq(name.uri(), qName.uri());
      // check everything
      default: return name.eq(qName);
    }
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof NameTest) {
      final NameTest nt = (NameTest) test;
      return type == nt.type && name.eq(nt.name) ? this : null;
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
    return type == nt.type && name.eq(nt.name);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(type.string()).add('(');
    if(part == NamePart.LOCAL) {
      tb.add('*').add(':');
    } else if(name.hasPrefix()) {
      tb.add(name.prefix()).add(':');
    } else if(name.uri().length != 0) {
      tb.add('{').add(name.uri()).add('}');
    }
    if(part == NamePart.URI) {
      tb.add('*');
    } else {
      tb.add(name.local());
    }
    return tb.add(')').toString();
  }
}
