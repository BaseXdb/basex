package org.basex.query.path;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.util.Util;

/**
 * Node test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Test {
  /** Static text node test. */
  public static final Test TXT = new KindTest(NodeType.TXT);
  /** Static PI node test. */
  public static final Test PI = new KindTest(NodeType.PI);
  /** Static element node test. */
  private static final Test ELM = new KindTest(NodeType.ELM);
  /** Static document node test. */
  public static final Test DOC = new KindTest(NodeType.DOC);
  /** Static attribute node test. */
  private static final Test ATT = new KindTest(NodeType.ATT);
  /** Static comment node test. */
  public static final Test COM = new KindTest(NodeType.COM);
  /** Static node test. */
  public static final Test NOD = new Test() {
    @Override
    public boolean eval(final ANode node) { return true; }
    @Override
    public String toString() { return NodeType.NOD.toString(); }
  };

  /** Name test types. */
  public enum Name {
    /** Accept all nodes (*).     */ ALL,
    /** Test names (*:tag).       */ NAME,
    /** Test namespaces (pre:*).  */ NS,
    /** Test all nodes (pre:tag). */ STD
  }

  /** Type of node test. */
  public NodeType type;
  /** Type of name test. Set to {@code null} for other kind tests. */
  public Name test;
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
      default:  throw Util.notexpected();
    }
  }

  /**
   * Optimizes and compiles the expression.
   * @param ctx query context
   * @return false if test always returns false
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public boolean comp(final QueryContext ctx) throws QueryException {
    return true;
  }

  /**
   * Tests the specified node.
   * @param node temporary node
   * @return result of check
   */
  public abstract boolean eval(final ANode node);

  /**
   * Checks the current and specified test for equality.
   * @param t test to be compared
   * @return result of check
   */
  public final boolean sameAs(final Test t) {
    return test == t.test && type == t.type &&
      (name == t.name || name.eq(t.name));
  }
}
