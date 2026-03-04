package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple XML node test.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class NodeTest extends Test {
  /** GNode test. */
  public static final NodeTest GNODE = new NodeTest(Kind.GNODE) {
    @Override
    public boolean matches(final GNode node) { return true; }
    @Override
    public Boolean subsumes(final Type type) { return Boolean.TRUE; }
    @Override
    public boolean instanceOf(final Test test) { return test == this; }
    @Override
    public Test intersect(final Test test) { return test; }
  };
  /** JNode test. */
  public static final NodeTest JNODE = new NodeTest(Kind.JNODE) {
    @Override
    public boolean matches(final GNode node) { return node.kind() == Kind.JNODE; }
  };
  /** XNode test. */
  public static final NodeTest NODE = new NodeTest(Kind.NODE) {
    @Override
    public boolean matches(final GNode node) { return node.kind() != Kind.JNODE; }
  };
  /** Generic document test. */
  public static final NodeTest DOCUMENT = new NodeTest(Kind.DOCUMENT);
  /** Generic element test. */
  public static final NodeTest ELEMENT = new NodeTest(Kind.ELEMENT) {
    @Override
    public String toString(final boolean type) { return type ? kind.toString() : "*"; }
  };
  /** Generic attribute test. */
  public static final NodeTest ATTRIBUTE = new NodeTest(Kind.ATTRIBUTE);
  /** Generic PI test. */
  public static final NodeTest PROCESSING_INSTRUCTION = new NodeTest(Kind.PROCESSING_INSTRUCTION);
  /** Generic text test. */
  public static final NodeTest TEXT = new NodeTest(Kind.TEXT);
  /** Generic comment test. */
  public static final NodeTest COMMENT = new NodeTest(Kind.COMMENT);
  /** Generic namespace test. */
  public static final NodeTest NAMESPACE = new NodeTest(Kind.NAMESPACE);

  /**
   * Constructor.
   * @param kind node kind
   */
  private NodeTest(final Kind kind) {
    super(kind);
  }

  /**
   * Returns a test instance.
   * @param kind node kind
   * @return node test
   */
  public static NodeTest get(final Kind kind) {
    return switch(kind) {
      case GNODE                  -> GNODE;
      case JNODE                  -> JNODE;
      case NODE                   -> NODE;
      case DOCUMENT               -> DOCUMENT;
      case ELEMENT                -> ELEMENT;
      case ATTRIBUTE              -> ATTRIBUTE;
      case PROCESSING_INSTRUCTION -> PROCESSING_INSTRUCTION;
      case TEXT                   -> TEXT;
      case COMMENT                -> COMMENT;
      case NAMESPACE              -> NAMESPACE;
      default                     -> throw Util.notExpected();
    };
  }

  @Override
  public final NodeTest copy() {
    return this;
  }

  @Override
  public Test optimize(final Kind kn, final Data data) {
    if(kind == Kind.GNODE && kn != null && kn != kind) {
      return get(kn == Kind.JNODE  ? Kind.JNODE : Kind.NODE);
    }
    return this;
  }

  @Override
  public boolean matches(final GNode node) {
    return node.kind() == kind;
  }

  @Override
  public Boolean subsumes(final Type type) {
    // <yes/>/self::gnode();
    if(kind == Kind.GNODE) return Boolean.TRUE;
    // (<who/>, [ 'knows' ])/self::node()
    final Kind kn = type.kind();
    if(kn == null) return null;
    // <yes/>/self::yes, [ 'yes' ]/self::jnode()
    if(kn == kind) return Boolean.TRUE;
    // <no/>/self::jnode()
    if(kind == Kind.JNODE) return kn != Kind.GNODE ? Boolean.FALSE : null;
    // [ 'no' ]/self::node(), <yes/>/self::node()
    if(kind == Kind.NODE)
      return kn == Kind.JNODE ? Boolean.FALSE : kn != Kind.GNODE ? Boolean.TRUE : null;
    // <no/>/self::text()
    return !kn.oneOf(Kind.NODE, Kind.GNODE) ? Boolean.FALSE : null;
  }

  @Override
  public boolean instanceOf(final Test test) {
    if(this == test) return true;
    return (test instanceof NodeTest || test instanceof UnionTest) && super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof NodeTest) {
      // a intersect * ? a
      if(instanceOf(test)) return this;
      if(test.instanceOf(this)) return test;
    } else if(test instanceof UnionTest) {
      return test.intersect(this);
    } else {
      // a intersect element() ? a
      if(kind == test.kind || kind == Kind.GNODE) return test;
      if(kind == Kind.NODE && test.kind != Kind.JNODE) return test;
    }
    return null;
  }

  @Override
  public final boolean equals(final Object obj) {
    return obj == this;
  }

  @Override
  public String toString(final boolean type) {
    return kind.toString();
  }
}
