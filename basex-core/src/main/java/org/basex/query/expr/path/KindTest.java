package org.basex.query.expr.path;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple node kind test.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class KindTest extends Test {
  /** Static text node test. */
  public static final KindTest TXT = new KindTest(NodeType.TXT);
  /** Static PI node test. */
  public static final KindTest PI = new KindTest(NodeType.PI);
  /** Static element node test. */
  public static final KindTest ELM = new KindTest(NodeType.ELM);
  /** Static document node test. */
  public static final KindTest DOC = new KindTest(NodeType.DOC);
  /** Static attribute node test. */
  public static final KindTest ATT = new KindTest(NodeType.ATT);
  /** Static comment node test. */
  public static final KindTest COM = new KindTest(NodeType.COM);
  /** Static comment node test. */
  public static final KindTest NSP = new KindTest(NodeType.NSP);
  /** Static node test. */
  public static final KindTest NOD = new KindTest(NodeType.NOD) {
    @Override
    public boolean eq(final ANode it) { return true; }
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
   * @param t node type
   * @return kind test
   */
  public static KindTest get(final NodeType t) {
    switch(t) {
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
  public KindTest copy() {
    return get(type);
  }

  @Override
  public boolean eq(final ANode node) {
    return node.type == type;
  }

  @Override
  public Test intersect(final Test other) {
    if(other instanceof NodeTest || other instanceof DocTest) {
      return other.type.instanceOf(type) ? other : null;
    }
    if(other instanceof KindTest) {
      return type.instanceOf(other.type) ? this :
        other.type.instanceOf(type) ? other : null;
    }
    if(other instanceof NameTest || other instanceof InvDocTest) {
      throw Util.notExpected(other);
    }
    return null;
  }

  @Override
  public String toString() {
    return String.valueOf(type);
  }
}
