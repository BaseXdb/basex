package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Name test.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NameTest extends Test {
  /** Test scope. */
  public enum Scope {
    /** Local test (*:local).    */ LOCAL,
    /** URI test (Q{uri}*).      */ URI,
    /** Full test (Q{uri}local). */ FULL
  }

  /** QName test. */
  public final QNm qname;
  /** Test scope. */
  public final Scope scope;
  /** Default element namespace. */
  public final byte[] defaultNs;

  /** Local name; assigned if URI can be ignored at runtime. */
  public byte[] local;

  /**
   * Returns a name test.
   * @param type node type (must be element, attribute, or processing instruction)
   * @param qname node name
   * @return test
   */
  public static NameTest get(final NodeType type, final QNm qname) {
    final Scope scope = type.kind == Kind.PROCESSING_INSTRUCTION ? Scope.LOCAL : Scope.FULL;
    return new NameTest(qname, scope, type, null);
  }

  /**
   * Returns a named element test.
   * @param qname node name
   * @return test
   */
  public static NameTest get(final QNm qname) {
    return new NameTest(qname, Scope.FULL, NodeType.ELEMENT, null);
  }

  /**
   * Constructor.
   * @param qname name
   * @param scope scope
   * @param type node type (must be element, attribute, or processing instruction)
   * @param defaultNs default element namespace (used for optimizations, can be {@code null})
   */
  public NameTest(final QNm qname, final Scope scope, final NodeType type, final byte[] defaultNs) {
    super(type);
    this.qname = qname;
    this.scope = scope;
    this.defaultNs = defaultNs != null ? defaultNs : Token.EMPTY;
    if(scope == Scope.LOCAL) local = qname.local();
  }

  @Override
  public Test optimize(final Data data) {
    // skip optimizations if data reference is not known at compile time
    if(data == null) return this;

    // skip optimizations if more than one namespace is defined in the database
    final byte[] dataNs = data.defaultNs();
    if(dataNs == null) return this;

    // check if test may yield results
    if(scope == Scope.FULL && !qname.hasURI()) {
      // element and db default namespaces are different: no results
      if(type.kind != Kind.ATTRIBUTE && !Token.eq(dataNs, defaultNs)) return null;
      // namespace is irrelevant/identical: only check local name
      local = qname.local();
    }

    // check if local element/attribute names occur in database
    if(type.kind != Kind.PROCESSING_INSTRUCTION && local != null &&
      !(type.kind == Kind.ELEMENT ? data.elemNames : data.attrNames).contains(local)) {
      return null;
    }
    return this;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean matches(final XNode node) {
    return node.kind() == type.kind && (
      // namespace wildcard: only check local name
      local != null ? Token.eq(local, Token.local(node.name())) :
      // name wildcard: only check namespace
      scope == Scope.URI ? Token.eq(qname.uri(), node.qname().uri()) :
      // check attributes, or check everything
      qname.eq(node.qname())
    );
  }

  /**
   * Checks if the specified name matches the test.
   * @param qName name
   * @return result of check
   */
  public boolean matches(final QNm qName) {
    return
      // namespace wildcard: only check local name
      local != null ? Token.eq(local, qName.local()) :
      // name wildcard: only check namespace
      scope == Scope.URI ? Token.eq(qname.uri(), qName.uri()) :
      // check attributes, or check everything
      qname.eq(qName);
  }

  @Override
  public Boolean matches(final SeqType seqType) {
    final Type tp = seqType.type;
    if(tp.intersect(type) == null) return Boolean.FALSE;
    if(tp instanceof final NodeType nt && nt.kind == type.kind &&
        seqType.test() instanceof final NameTest name) {
      if(name.scope == scope || name.scope == Scope.FULL) return matches(name.qname);
    }
    return null;
  }

  @Override
  public boolean instanceOf(final Test test) {
    if(test instanceof final NameTest nt) {
      return type.kind == nt.type.kind && switch(nt.scope) {
        case LOCAL -> (scope == Scope.LOCAL || scope == Scope.FULL) &&
          Token.eq(qname.local(), nt.qname.local());
        case URI -> (scope == Scope.URI || scope == Scope.FULL) &&
          Token.eq(qname.uri(), nt.qname.uri());
        case FULL -> scope == Scope.FULL && qname.eq(nt.qname);
      };
    }
    return super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof final NameTest nt) {
      if(type.kind == nt.type.kind) {
        if(scope == nt.scope || scope == Scope.FULL) {
          // *:local1 = *:local2, Q{uri1}* = Q{uri2}*, Q{uri1}local1 = Q{uri2}local2
          // Q{uri1}local1 = *:local2, Q{uri1}local1 = Q{uri2}*
          if(nt.matches(qname)) return this;
        } else if(nt.scope == Scope.URI) {
          // *:local1 = Q{uri2}* → Q{uri2}local1
          return new NameTest(new QNm(local, nt.qname.uri()), Scope.FULL, type, defaultNs);
        } else {
          // *:local1 = Q{uri2}local2, Q{uri1}* = Q{uri2}local2, Q{uri1}* = *:local2
          return test.intersect(this);
        }
      }
    } else if(test instanceof NodeTest) {
      if(type.instanceOf(test.type)) return this;
    } else if(test instanceof UnionTest) {
      return test.intersect(this);
    }
    // no intersection possible (failed match; DocTest; InvDocTest)
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final NameTest nt &&
      type.kind == nt.type.kind && scope == nt.scope && qname.eq(nt.qname);
  }

  @Override
  public String toString(final boolean full) {
    final boolean pi = type.kind == Kind.PROCESSING_INSTRUCTION;
    final TokenBuilder tb = new TokenBuilder();

    // add URI part
    final byte[] p = qname.prefix(), u = qname.uri(), l = qname.local();
    if(scope == Scope.LOCAL && !pi) {
      if(!(full && type.kind == Kind.ATTRIBUTE)) tb.add("*:");
    } else if(p.length > 0) {
      tb.add(p).add(':');
    } else if(u.length != 0) {
      tb.add("Q{").add(u).add('}');
    }
    // add local part
    if(scope == Scope.URI) {
      tb.add('*');
    } else {
      tb.add(l);
    }
    final String test = tb.toString();
    return full || pi ? type.toString(test) : test;
  }
}
