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
  /** Node test. */
  public static final NodeTest NODE = new NodeTest(Kind.NODE) {
    @Override
    public boolean matches(final XNode node) { return true; }
    @Override
    public Boolean subsumes(final Type type) { return Boolean.TRUE; }
    @Override
    public boolean instanceOf(final Test test) { return false; }
    @Override
    public Test intersect(final Test test) { return test; }
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
  public Test optimize(final Data data) {
    return this;
  }

  @Override
  public boolean matches(final XNode node) {
    return node.kind() == kind;
  }

  @Override
  public Boolean subsumes(final Type type) {
    // (<who/>, text { 'knows' })/self::*)
    if(type.oneOf(BasicType.ITEM, NodeType.NODE)) return null;
    // <yes/>/self::element()
    return type.kind() == kind;
  }

  @Override
  public boolean instanceOf(final Test test) {
    if(this == test) return true;
    return (test instanceof NodeTest || test instanceof UnionTest) && super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof NodeTest) {
      // a intersect * → a
      if(instanceOf(test)) return this;
      if(test.instanceOf(this)) return test;
    } else if(test instanceof UnionTest) {
      return test.intersect(this);
    } else {
      // a intersect element() → a
      if(kind == test.kind || kind == Kind.NODE) return test;
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
