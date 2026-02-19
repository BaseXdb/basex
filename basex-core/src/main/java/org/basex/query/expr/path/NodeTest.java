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
    public Boolean matches(final Type tp) { return Boolean.TRUE; }
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
    public String toString(final boolean full) { return full ? kind.toString() : "*"; }
  };
  /** Generic attribute test. */
  public static final NodeTest ATTRIBUTE = new NodeTest(Kind.ATTRIBUTE);
  /** Generic PI test. */
  public static final NodeTest PROCESSING_INSTRUCTION = new NodeTest(Kind.PROCESSING_INSTRUCTION);
  /** Generic text test. No other {@link Kind#TEXT} tests exist. */
  public static final NodeTest TEXT = new NodeTest(Kind.TEXT);
  /** Generic comment test. No other {@link Kind#COMMENT} tests exist. */
  public static final NodeTest COMMENT = new NodeTest(Kind.COMMENT);
  /** Generic namespace test. No other {@link Kind#COMMENT} tests exist. */
  public static final NodeTest NAMESPACE = new NodeTest(Kind.NAMESPACE);
  /** Test that always returns false. */
  public static final NodeTest FALSE = new NodeTest(Kind.NODE) {
    @Override
    public boolean matches(final XNode node) { return false; }
    @Override
    public boolean instanceOf(final Test test) { return false; }
    @Override
    public Test intersect(final Test test) { return null; }
  };

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
      case TEXT                   -> TEXT;
      case PROCESSING_INSTRUCTION -> PROCESSING_INSTRUCTION;
      case ELEMENT                -> ELEMENT;
      case DOCUMENT               -> DOCUMENT;
      case ATTRIBUTE              -> ATTRIBUTE;
      case COMMENT                -> COMMENT;
      case NODE                   -> NODE;
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
    return this == FALSE ? null : this;
  }

  @Override
  public boolean matches(final XNode node) {
    return node.kind() == kind;
  }

  @Override
  public Boolean matches(final Type tp) {
    // (<who/>, text { 'knows' })/self::*)
    if(tp.oneOf(BasicType.ITEM, NodeType.NODE)) return null;
    // <yes/>/self::element()
    return tp.kind() == kind;
  }

  @Override
  public boolean instanceOf(final Test test) {
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
  public String toString(final boolean full) {
    return kind.toString();
  }
}
