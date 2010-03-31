package org.basex.query.path;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;

/**
 * XQuery node tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Test {
  /** Static node test. */
  public static final Test NODE = new Test() {
    @Override
    public boolean eval(final Nod node) { return true; }
    @Override
    public String toString() { return Type.NOD.toString(); }
  };

  /** Test types. */
  public enum Kind {
    /** Accept all nodes (*).     */ ALL,
    /** Test names (*:tag).       */ NAME,
    /** Test namespaces (pre:*).  */ NS,
    /** Test all nodes (pre:tag). */ STD
  };
  /** Test type. */
  public Kind kind;
  /** Node test. */
  public Type type;
  /** Name test. */
  public QNm name;
  /** Temporary QName instance. */
  final QNm tmpq = new QNm();

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
    return kind == t.kind && type == t.type &&
      (name == t.name || name.eq(t.name));
  }
}
