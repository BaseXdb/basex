package org.basex.query.xquery.path;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;

/**
 * XQuery Node Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Test {
  /** Static node test. */
  public static final Test NODE = new Test() {
    @Override
    public boolean eval(final Nod node) { return true; }
    @Override
    public String toString() { return Type.NOD + "()"; }
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
  protected QNm name;
  /** Temporary QName instance. */
  protected QNm tmpq = new QNm();
  
  /**
   * Optimizes and compiles the expression.
   * @param ctx query context
   * @return false if test always returns false
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused") 
  public boolean comp(final XQContext ctx) throws XQException {
    return true;
  }
  
  /**
   * Tests the specified node.
   * @param nod temporary node
   * @return result of check
   * @throws XQException evaluation exception
   */
  public abstract boolean eval(final Nod nod) throws XQException;

  /**
   * Checks the current and specified test for equality.
   * @param t test to be compared
   * @return result of check
   */
  public boolean sameAs(final Test t) {
    return kind == t.kind && name == t.name && type == t.type;
  }
}
