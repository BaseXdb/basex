package org.basex.query.path;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.util.Util;

/**
 * Node test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Test {
  /** Static text node test. */
  public static final Test TXT = new KindTest(Type.TXT);
  /** Static PI node test. */
  public static final Test PI = new KindTest(Type.PI);
  /** Static element node test. */
  public static final Test ELM = new KindTest(Type.ELM);
  /** Static document node test. */
  public static final Test DOC = new KindTest(Type.DOC);
  /** Static attribute node test. */
  public static final Test ATT = new KindTest(Type.ATT);
  /** Static comment node test. */
  public static final Test COM = new KindTest(Type.COM);
  /** Static node test. */
  public static final Test NOD = new Test() {
    @Override
    public boolean eval(final Nod node) { return true; }
    @Override
    public String toString() { return Type.NOD.toString(); }
  };

  /** Name test types. */
  public enum Name {
    /** Accept all nodes (*).     */ ALL,
    /** Test names (*:tag).       */ NAME,
    /** Test namespaces (pre:*).  */ NS,
    /** Test all nodes (pre:tag). */ STD
  }

  /** Type of node test. */
  public Type type;
  /** Type of name test. Set to {@code null} for other kind tests. */
  public Name test;
  /** Name test. Set to {@code null} for other kind tests. */
  public QNm name;

  /** Cached QName instance. */
  protected final QNm tmpq = new QNm();

  /**
   * Returns a test instance.
   * @param t node type
   * @return kind test
   */
  public static Test get(final Type t) {
    switch(t) {
      case TXT: return TXT;
      case PI:  return PI;
      case ELM: return ELM;
      case DOC: return DOC;
      case ATT: return ATT;
      case COM: return COM;
      case NOD: return NOD;
      default:  Util.notexpected(); return null;
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
   * @param nod temporary node
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean eval(final Nod nod) throws QueryException;

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
