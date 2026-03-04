package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
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
    /** Full test (Q{uri}local). */ FULL,
    /** Accept all (*).          */ ALL,
    /** Flexible (JNode/XNode).  */ FLEXIBLE;

    /**
     * Checks if this is one of the specified scopes.
     * @param scopes scopes
     * @return result of check
     */
    public boolean oneOf(final Scope... scopes) {
      for(final Scope scope : scopes) {
        if(this == scope) return true;
      }
      return false;
    }
  }

  /** QName test. */
  public final QNm qname;
  /** Test scope. */
  public final Scope scope;
  /** Default element namespace. */
  public final byte[] ns;

  /** Local name; assigned if URI can be ignored at runtime. */
  public byte[] name;

  /**
   * Returns a named element test.
   * @param qname node name
   * @return test
   */
  public static Test get(final QNm qname) {
    return get(qname, Kind.ELEMENT);
  }

  /**
   * Returns a name test.
   * @param qname node name
   * @param kind node kind
   * @return test
   */
  public static Test get(final QNm qname, final Kind kind) {
    return get(kind, qname, null, null);
  }

  /**
   * Constructor.
   * @param qname name
   * @param scope scope
   * @param kind node kind
   * @param ns default element namespace (used for optimizations, can be {@code null})
   */
  NameTest(final QNm qname, final Scope scope, final Kind kind, final byte[] ns) {
    super(kind);
    this.qname = qname;
    this.scope = scope;
    this.ns = ns != null ? ns : Token.EMPTY;
    if(scope == Scope.LOCAL) name = qname.local();
  }

  @Override
  public Test optimize(final Kind kn, final Data data) {
    // create more specific test
    if(kn != null && kind == Kind.GNODE) {
      final Kind k = kn == Kind.JNODE ? Kind.JNODE : kn.instanceOf(Kind.NODE) ? Kind.ELEMENT : null;
      if(k != null) return get(k, qname, scope, ns).optimize(kn, data);
    }

    // skip optimizations if data reference is not known at compile time
    if(data == null) return this;

    // skip optimizations if more than one namespace is defined in the database
    final byte[] dataNs = data.defaultNs();
    if(dataNs == null) return this;

    // check if test may yield results
    if(scope.oneOf(Scope.FLEXIBLE, Scope.FULL) && !qname.hasURI()) {
      // element and db default namespaces are different: no results
      if(!kind.oneOf(Kind.GNODE, Kind.ATTRIBUTE) && !Token.eq(dataNs, ns)) return null;
      // namespace is irrelevant/identical: only check local name
      name = qname.local();
    }

    // check if local element/attribute names occur in database
    if(!kind.oneOf(Kind.GNODE, Kind.PROCESSING_INSTRUCTION) && name != null &&
        !(kind == Kind.ELEMENT ? data.elemNames : data.attrNames).contains(name)) {
      return null;
    }
    return this;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean matches(final GNode node) {
    if(kind != Kind.GNODE && kind != node.kind()) return false;

    if(node instanceof final JNode jnode) {
      // JNodes
      if(scope == Scope.ALL) return true;
      if(jnode.key == Empty.VALUE) return false;
      if(scope == Scope.FLEXIBLE) {
        try {
          return Token.eq(qname.string(), jnode.key.string(null));
        } catch(final QueryException ex) {
          throw Util.notExpected(ex);
        }
      }
    }
    final QNm qnm = node.qname();
    return qnm != null && matches(qnm);
  }

  /**
   * Checks if the specified name matches the test.
   * @param qName name
   * @return result of check
   */
  public boolean matches(final QNm qName) {
    return scope == Scope.ALL || (
      // namespace wildcard: only check local name
      name != null ? Token.eq(name, qName.local()) :
      // name wildcard: only check namespace
      scope == Scope.URI ? Token.eq(qname.uri(), qName.uri()) :
      // check everything
      qname.eq(qName)
    );
  }

  @Override
  public Boolean subsumes(final Type type) {
    // specific type unknown at compile time
    if(kind == Kind.GNODE) return null;
    // (<who/>, [ 'knows' ])/self::no-one
    final Kind kn = type.kind();
    if(kn == null || kn.oneOf(Kind.GNODE, Kind.NODE)) return null;
    // text { 'no' }/self::no, [ 'no' ]/self::no
    if(kn != kind) return Boolean.FALSE;
    // <yes/>/self::yes, <maybe/>/self::no
    if(type instanceof final NodeType ntype && ntype.test instanceof final NameTest nt) {
      if(nt.scope.oneOf(scope, Scope.FLEXIBLE, Scope.FULL)) return matches(nt.qname);
    }
    // (<who/>, <knows/>)/self::no-one)
    return null;
  }

  @Override
  public boolean instanceOf(final Test test) {
    if(this == test) return true;
    if(test instanceof final NameTest nt) {
      return kind == nt.kind && switch(nt.scope) {
        case LOCAL -> scope.oneOf(Scope.LOCAL, Scope.FLEXIBLE, Scope.FULL) &&
          Token.eq(qname.local(), nt.qname.local());
        case URI -> (scope == Scope.URI || scope == Scope.FULL) &&
          Token.eq(qname.uri(), nt.qname.uri());
        case FLEXIBLE, FULL -> scope.oneOf(Scope.FLEXIBLE, Scope.FULL) && qname.eq(nt.qname);
        case ALL -> scope == Scope.ALL;
      };
    }
    return super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test == NodeTest.GNODE || test == this) return this;
    if(test instanceof final NameTest nt) {
      if(kind == nt.kind) {
        if(scope == Scope.ALL) {
          // *
          return test;
        } else if(scope.oneOf(nt.scope, Scope.FLEXIBLE, Scope.FULL)) {
          // *:local1 = *:local2, Q{uri1}* = Q{uri2}*, Q{uri1}local1 = Q{uri2}local2
          // Q{uri1}local1 = *:local2, Q{uri1}local1 = Q{uri2}*
          if(nt.matches(qname)) return this;
        } else if(nt.scope == Scope.URI) {
          // *:local1 = Q{uri2}* → Q{uri2}local1
          return get(kind, new QNm(name, nt.qname.uri()), Scope.FULL, ns);
        } else {
          // *:local1 = Q{uri2}local2, Q{uri1}* = Q{uri2}local2, Q{uri1}* = *:local2
          return test.intersect(this);
        }
      }
    } else if(test instanceof NodeTest || test instanceof UnionTest) {
      return test.intersect(this);
    }
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final NameTest nt &&
      kind == nt.kind && scope == nt.scope && qname.eq(nt.qname);
  }

  @Override
  public String toString(final boolean type) {
    final String string = nameString();
    return type && kind == Kind.GNODE ? "(jnode(" + string + ")|element(" + string + "))" :
      type || kind.oneOf(Kind.PROCESSING_INSTRUCTION, Kind.ATTRIBUTE) ? kind.toString(string) :
      string;
  }

  /**
   * Returns a string representation of the name.
   * @return string
   */
  public String nameString() {
    final TokenBuilder tb = new TokenBuilder();
    // add URI part
    final byte[] prefix = qname.prefix(), uri = qname.uri();
    if(scope == Scope.ALL) {
      tb.add('*');
    } else if(scope == Scope.LOCAL && kind != Kind.PROCESSING_INSTRUCTION) {
      tb.add("*:");
    } else if(prefix.length > 0) {
      tb.add(prefix).add(':');
    } else if(uri.length != 0) {
      tb.add("Q{").add(uri).add('}');
    }
    // add local part
    if(scope == Scope.URI) {
      tb.add('*');
    } else {
      tb.add(qname.local());
    }
    return tb.toString();
  }
}
