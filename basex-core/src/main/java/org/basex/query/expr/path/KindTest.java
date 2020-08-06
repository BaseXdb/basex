package org.basex.query.expr.path;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple node kind test.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class KindTest extends Test {
  /** Generic document node test. */
  public static final KindTest DOC = new KindTest(NodeType.DOC);
  /** Generic element node test. */
  public static final KindTest ELM = new KindTest(NodeType.ELM) {
    @Override
    public String toString(final boolean full) { return full ? type.toString() : "*"; }
  };
  /** Generic attribute node test. */
  public static final KindTest ATT = new KindTest(NodeType.ATT);
  /** Generic PI node test. */
  public static final KindTest PI = new KindTest(NodeType.PI);
  /** Generic text node test. No other {@link NodeType#TXT} tests exist. */
  public static final KindTest TXT = new KindTest(NodeType.TXT);
  /** Generic comment node test. No other {@link NodeType#COM} tests exist. */
  public static final KindTest COM = new KindTest(NodeType.COM);
  /** Generic namespace node test. No other {@link NodeType#COM} tests exist. */
  public static final KindTest NSP = new KindTest(NodeType.NSP);
  /** Generic node test. No other {@link NodeType#NOD} tests exist. */
  public static final KindTest NOD = new KindTest(NodeType.NOD) {
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
      case TXT: return TXT;
      case PI:  return PI;
      case ELM: return ELM;
      case DOC: return DOC;
      case ATT: return ATT;
      case COM: return COM;
      case NOD: return NOD;
      case NSP: return NSP;
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
