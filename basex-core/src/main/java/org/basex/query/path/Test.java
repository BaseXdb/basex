package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract node test.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Test {
  /** Static text node test. */
  public static final KindTest TXT = new KindTest(NodeType.TXT);
  /** Static PI node test. */
  public static final KindTest PI = new KindTest(NodeType.PI);
  /** Static element node test. */
  public static final KindTest ELM = new KindTest(NodeType.ELM);
  /** Static document node test. */
  public static final KindTest DOC = new KindTest(NodeType.DOC);
  /** Static attribute node test. */
  private static final KindTest ATT = new KindTest(NodeType.ATT);
  /** Static comment node test. */
  public static final KindTest COM = new KindTest(NodeType.COM);
  /** Static comment node test. */
  public static final KindTest NSP = new KindTest(NodeType.NSP);
  /** Static node test. */
  public static final Test NOD = new KindTest(NodeType.NOD) {
    @Override
    public boolean eq(final ANode it) { return true; }
  };

  /** Name test types. */
  public enum Mode {
    /** Accept all nodes (*).     */ ALL,
    /** Test names (*:tag).       */ LN,
    /** Test namespaces (pre:*).  */ NS,
    /** Test all nodes (pre:tag). */ STD
  }

  /** Type of node test. */
  public NodeType type;
  /** Type of name test. Set to {@code null} for other kind tests. */
  public Mode mode;
  /** Name test. Set to {@code null} for other kind tests. */
  public QNm name;

  /** Mutable QName instance. */
  final QNm tmpq = new QNm();

  /**
   * Returns a test instance.
   * @param t node type
   * @return kind test
   */
  public static Test get(final NodeType t) {
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

  /**
   * Optimizes and compiles the expression.
   * @param ctx query context
   * @return false if test always returns false
   */
  @SuppressWarnings("unused")
  public boolean compile(final QueryContext ctx) {
    return true;
  }

  /**
   * Tests if the test yields true.
   * @param node node to be checked
   * @return result of check
   */
  public abstract boolean eq(final ANode node);

  /**
   * Tests if the test yields true.
   * @param item item to be checked
   * @return result of check
   */
  public boolean eq(final Item item) {
    return item instanceof ANode && eq((ANode) item);
  }

  /**
   * Checks the current and specified test for equality.
   * @param t test to be compared
   * @return result of check
   */
  public final boolean sameAs(final Test t) {
    return mode == t.mode && type == t.type && (name == t.name || name.eq(t.name));
  }

  /**
   * Copies this test.
   * @return deep copy
   */
  public abstract Test copy();

  /**
   * Checks if this test is namespace-sensitive.
   * @return result of check
   */
  boolean nsSensitive() {
    return name != null;
  }

  /**
   * Computes the intersection between two tests.
   * @param other other test
   * @return intersection if it exists, {@code null} otherwise
   */
  public abstract Test intersect(final Test other);
}
