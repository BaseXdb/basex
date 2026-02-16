package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple node kind test.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class NodeTest extends Test {
  /** Generic document node test. */
  public static final NodeTest DOCUMENT_NODE = new NodeTest(NodeType.DOCUMENT);
  /** Generic element node test. */
  public static final NodeTest ELEMENT = new NodeTest(NodeType.ELEMENT) {
    @Override
    public String toString(final boolean full) { return full ? type.toString() : "*"; }
  };
  /** Generic attribute node test. */
  public static final NodeTest ATTRIBUTE = new NodeTest(NodeType.ATTRIBUTE);
  /** Generic PI node test. */
  public static final NodeTest PROCESSING_INSTRUCTION =
      new NodeTest(NodeType.PROCESSING_INSTRUCTION);
  /** Generic text node test. No other {@link NodeType#TEXT} tests exist. */
  public static final NodeTest TEXT = new NodeTest(NodeType.TEXT);
  /** Generic comment node test. No other {@link NodeType#COMMENT} tests exist. */
  public static final NodeTest COMMENT = new NodeTest(NodeType.COMMENT);
  /** Generic namespace node test. No other {@link NodeType#COMMENT} tests exist. */
  public static final NodeTest NAMESPACE_NODE = new NodeTest(NodeType.NAMESPACE);
  /** Generic node test. No other {@link NodeType#NODE} tests exist. */
  public static final NodeTest NODE = new NodeTest(NodeType.NODE) {
    @Override
    public boolean matches(final XNode node) { return true; }
    @Override
    public boolean instanceOf(final Test test) { return false; }
    @Override
    public Test intersect(final Test test) { return test; }
  };
  /** Node test that always yields false. */
  public static final NodeTest FALSE = new NodeTest(NodeType.NODE) {
    @Override
    public boolean matches(final XNode node) { return false; }
    @Override
    public boolean instanceOf(final Test test) { return false; }
    @Override
    public Test intersect(final Test test) { return null; }
  };

  /**
   * Constructor.
   * @param type node type
   */
  NodeTest(final NodeType type) {
    super(type);
  }

  /**
   * Returns a test instance.
   * @param type node type
   * @return kind test
   */
  public static NodeTest get(final NodeType type) {
    return switch(type.kind) {
      case TEXT                   -> TEXT;
      case PROCESSING_INSTRUCTION -> PROCESSING_INSTRUCTION;
      case ELEMENT                -> ELEMENT;
      case DOCUMENT               -> DOCUMENT_NODE;
      case ATTRIBUTE              -> ATTRIBUTE;
      case COMMENT                -> COMMENT;
      case NODE                   -> NODE;
      case NAMESPACE              -> NAMESPACE_NODE;
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
    return node.kind() == type.kind;
  }

  @Override
  public Boolean matches(final SeqType seqType) {
    final Type tp = seqType.type;
    if(tp.intersect(type) == null) return Boolean.FALSE;
    return tp.instanceOf(type) ? Boolean.TRUE : null;
  }

  @Override
  public boolean instanceOf(final Test test) {
    return (test instanceof NodeTest || test instanceof UnionTest) && super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof NodeTest)
      return instanceOf(test) ? this : test.instanceOf(this) ? test : null;
    if(test instanceof NameTest || test instanceof DocTest)
      return test.instanceOf(this) ? test : null;
    if(test instanceof UnionTest)
      return test.intersect(this);
    // InvDocTest
    return null;
  }

  @Override
  public final boolean equals(final Object obj) {
    return obj == this;
  }

  @Override
  public String toString(final boolean full) {
    return type.toString();
  }
}
