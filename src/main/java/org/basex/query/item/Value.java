package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.expr.Expr;
import org.basex.query.util.Var;

/**
 * Abstract value.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Value extends Expr {
  /** Data type. */
  public Type type;

  /**
   * Constructor.
   * @param t data type
   */
  protected Value(final Type t) {
    type = t;
  }

  @Override
  public final Value comp(final QueryContext ctx) {
    return this;
  }
  
  @Override
  public final boolean value() {
    return true;
  }
  
  @Override
  public abstract long size(final QueryContext ctx);

  /**
   * Checks if this is a single numeric item.
   * @return result of check
   */
  public final boolean num() {
    return type.num;
  }

  /**
   * Checks if this is a single untyped item.
   * @return result of check
   */
  public final boolean unt() {
    return type.unt;
  }

  /**
   * Checks if this is a single string item.
   * @return result of check
   */
  public final boolean str() {
    return type.str;
  }

  /**
   * Checks if this is a single duration item.
   * @return result of check
   */
  public final boolean dur() {
    return type.dur;
  }

  /**
   * Checks if this is a single date item.
   * @return result of check
   */
  public final boolean date() {
    return this instanceof Date;
  }

  /**
   * Checks if this is a single node.
   * @return result of check
   */
  public final boolean node() {
    return type.node();
  }

  /**
   * Returns a Java representation of the value.
   * @return Java object
   */
  public abstract Object toJava();

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    return false;
  }

  @Override
  public final boolean removable(final Var v, final QueryContext ctx) {
    return true;
  }

  @Override
  public abstract boolean duplicates(final QueryContext ctx);

  @Override
  public final String color() {
    return "9999FF";
  }

  @Override
  public final String name() {
    return type.name;
  }
}
