package org.basex.query.expr.path;

import org.basex.index.name.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

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

  /** Local name; assigned if URI can be ignored at runtime (can be {@code null}). */
  public byte[] name;
  /** Database name; assigned if the test matches a single name (can be {@code null}). */
  private byte[] dbName;

  /** JNode key; assigned if the test can select a single JNode key (can be {@code null}). */
  private final Item jkey;

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
    jkey = scope == Scope.FLEXIBLE && kind == Kind.NODE ? JNodeTest.key(qname) : null;
  }

  @Override
  public Item key() {
    return jkey;
  }

  @Override
  public Test optimize(final Kind kn, final Data data) {
    // create more specific test
    if(kn != null && kind == Kind.NODE) {
      final Kind k = kn == Kind.JNODE ? Kind.JNODE : kn.instanceOf(Kind.XNODE) ? Kind.ELEMENT :
        null;
      if(k != null) return get(k, qname, scope, ns).optimize(kn, data);
    }

    // skip optimizations if the database is unknown or incomplete, or if names are not indexed
    final boolean elem = kind == Kind.ELEMENT;
    if(data == null || !data.meta.complete || !elem && kind != Kind.ATTRIBUTE ||
        !scope.oneOf(Scope.LOCAL, Scope.FLEXIBLE, Scope.FULL)) return this;

    // resolve the namespaces of all database names with the same local name
    final Names names = elem ? data.elemNames : data.attrNames;
    final TokenList all = names.lexical(qname.local(), !data.nspaces.isEmpty());
    final TokenList matches = matches(all, data);
    dbName = matches != null && matches.size() == 1 ? matches.get(0) : null;
    if(matches == null) return this;
    // no matching name: no results
    if(matches.isEmpty()) return null;
    // all names match: namespace can be ignored at runtime
    if(matches.size() == all.size()) name = qname.local();
    return this;
  }

  /**
   * Returns the database name that is matched by this test.
   * @return name, or {@code null} if no or several names are matched, or if a name cannot be
   *   resolved
   */
  public byte[] dbName() {
    return dbName;
  }

  /**
   * Returns the database names that are matched by this test.
   * @param names names with the local name of this test
   * @param data data reference
   * @return matching names, or {@code null} if the namespace of a name cannot be resolved
   */
  private TokenList matches(final TokenList names, final Data data) {
    if(scope == Scope.LOCAL) return names;

    final boolean elem = kind == Kind.ELEMENT;
    final byte[] uri = qname.hasURI() ? qname.uri() : elem ? ns : Token.EMPTY;
    final TokenList list = new TokenList(names.size());
    for(final byte[] nm : names) {
      final byte[] u = data.nsUri(nm, elem);
      if(u == null) return null;
      if(Token.eq(u, uri)) list.add(nm);
    }
    return list;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean matches(final GNode node) {
    if(kind != Kind.NODE && kind != node.kind()) return false;

    if(node instanceof final JNode jnode) {
      // JNodes
      if(scope == Scope.ALL) return true;
      if(jnode.isRoot()) return false;
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
    if(kind == Kind.NODE) return null;
    // (<who/>, [ 'knows' ])/self::no-one
    final Kind kn = type.kind();
    if(kn == null || kn.oneOf(Kind.NODE, Kind.XNODE)) return null;
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
      return kind.instanceOf(nt.kind) && switch(nt.scope) {
        case LOCAL -> scope.oneOf(Scope.LOCAL, Scope.FLEXIBLE, Scope.FULL) &&
          Token.eq(qname.local(), nt.qname.local());
        case URI -> (scope == Scope.URI || scope == Scope.FULL) &&
          Token.eq(qname.uri(), nt.qname.uri());
        case FLEXIBLE, FULL -> scope.oneOf(Scope.FLEXIBLE, Scope.FULL) && qname.eq(nt.qname);
        case ALL -> true;
      };
    }
    return super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test == NodeTest.NODE || test == this) return this;
    if(test instanceof NodeTest || test instanceof UnionTest) return test.intersect(this);

    if(kind != test.kind) {
      // node(local) = element(local) → element(local) = element(local)
      if(kind == Kind.NODE && test.kind.oneOf(Kind.ELEMENT, Kind.ATTRIBUTE, Kind.JNODE,
          Kind.PROCESSING_INSTRUCTION)) return get(test.kind, qname, scope, ns).intersect(test);
      if(test.kind == Kind.NODE) return test.intersect(this);
    } else if(test instanceof final NameTest nt) {
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
    return type && kind == Kind.NODE ? "(jnode(" + string + ")|element(" + string + "))" :
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
