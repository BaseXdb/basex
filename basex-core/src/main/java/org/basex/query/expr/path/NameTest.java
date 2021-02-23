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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** QName test. */
  public final QNm qname;
  /** Part of name to be tested. */
  public final NamePart part;
  /** Local name. */
  public final byte[] local;
  /** Default element namespace. */
  private final byte[] defaultNs;

  /** Perform only local check at runtime. */
  private boolean simple;

  /**
   * Convenience constructor for element tests.
   * @param name node name
   */
  public NameTest(final QNm name) {
    this(name, NamePart.FULL, NodeType.ELEMENT, null);
  }

  /**
   * Constructor.
   * @param qname name
   * @param part part of name to be tested
   * @param type node type
   * @param defaultNs default element namespace (used for optimizations, can be {@code null})
   */
  public NameTest(final QNm qname, final NamePart part, final NodeType type,
      final byte[] defaultNs) {

    super(type);
    this.qname = qname;
    this.part = part;
    this.defaultNs = defaultNs != null ? defaultNs : Token.EMPTY;
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
      // element and db default namespaces are different: no results
      if(type != NodeType.ATTRIBUTE && !Token.eq(dataNs, defaultNs)) return true;
      // namespace is irrelevant/identical: only check local name
      simple = true;
    }

    // check existence of local element/attribute names
    return !(type == NodeType.PROCESSING_INSTRUCTION || part() != NamePart.LOCAL ||
      (type == NodeType.ELEMENT ? data.elemNames : data.attrNames).contains(local));
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean matches(final ANode node) {
    if(node.type != type) return false;
    switch(part()) {
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
    switch(part()) {
      // namespaces wildcard: only check local name
      case LOCAL: return Token.eq(local, qName.local());
      // name wildcard: only check namespace
      case URI: return Token.eq(qname.uri(), qName.uri());
      // check everything
      default: return qname.eq(qName);
    }
  }

  /**
   * Returns the name part relevant at runtime.
   * @return name part
   */
  public NamePart part() {
    return simple ? NamePart.LOCAL : part;
  }


  @Override
  public boolean instanceOf(final Test test) {
    if(test instanceof NameTest) {
      final NameTest nt = (NameTest) test;
      return type == nt.type && part == nt.part && qname.eq(nt.qname);
    }
    return super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof NameTest) {
      final NameTest nt = (NameTest) test;
      return type == nt.type && qname.eq(nt.qname) ? this : null;
    }
    if(test instanceof KindTest) return type.instanceOf(test.type) ? this : null;
    if(test instanceof UnionTest) return test.intersect(this);
    // DocTest, InvDocTest
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof NameTest)) return false;
    final NameTest nt = (NameTest) obj;
    return type == nt.type && part == nt.part && qname.eq(nt.qname);
  }

  @Override
  public String toString(final boolean full) {
    final boolean pi = type == NodeType.PROCESSING_INSTRUCTION;
    final TokenBuilder tb = new TokenBuilder();

    // add URI part
    final byte[] prefix = qname.prefix(), uri = qname.uri();
    if(part == NamePart.LOCAL && !pi) {
      if(!(full && type == NodeType.ATTRIBUTE)) tb.add("*:");
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
    final String test = tb.toString();
    return full || pi ? type.toString(test) : test;
  }
}
