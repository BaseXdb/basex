package org.basex.query.expr.path;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple node kind test.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class KindTest extends Test {
  /** Generic document node test. */
  public static final KindTest DOC = new KindTest(NodeType.DOCUMENT_NODE);
  /** Generic element node test. */
  public static final KindTest ELM = new KindTest(NodeType.ELEMENT) {
    @Override
    public String toString(final boolean full) { return full ? type.toString() : "*"; }
  };
  /** Generic attribute node test. */
  public static final KindTest ATT = new KindTest(NodeType.ATTRIBUTE);
  /** Generic PI node test. */
  public static final KindTest PI = new KindTest(NodeType.PROCESSING_INSTRUCTION);
  /** Generic text node test. No other {@link NodeType#TEXT} tests exist. */
  public static final KindTest TXT = new KindTest(NodeType.TEXT);
  /** Generic comment node test. No other {@link NodeType#COMMENT} tests exist. */
  public static final KindTest COM = new KindTest(NodeType.COMMENT);
  /** Generic namespace node test. No other {@link NodeType#COMMENT} tests exist. */
  public static final KindTest NSP = new KindTest(NodeType.NAMESPACE_NODE);
  /** Generic node test. No other {@link NodeType#NODE} tests exist. */
  public static final KindTest NOD = new KindTest(NodeType.NODE) {
    @Override
    public boolean matches(final ANode node) { return true; }
    @Override
    public boolean instanceOf(final Test test) { return false; }
    @Override
    public Test intersect(final Test test) { return test; }
  };

  /**
   * Constructor.
   * @param type node type
   */
  KindTest(final NodeType type) {
    super(type);
  }

  /**
   * Returns a test instance.
   * @param type node type
   * @return kind test
   */
  public static KindTest get(final NodeType type) {
    switch(type) {
      case TEXT: return TXT;
      case PROCESSING_INSTRUCTION:  return PI;
      case ELEMENT: return ELM;
      case DOCUMENT_NODE: return DOC;
      case ATTRIBUTE: return ATT;
      case COMMENT: return COM;
      case NODE: return NOD;
      case NAMESPACE_NODE: return NSP;
      default: throw Util.notExpected();
    }
  }

  @Override
  public final KindTest copy() {
    return this;
  }

  @Override
  public boolean matches(final ANode node) {
    return node.type == type;
  }

  @Override
  public boolean instanceOf(final Test test) {
    return (test instanceof KindTest || test instanceof UnionTest) && super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test instanceof KindTest)
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
