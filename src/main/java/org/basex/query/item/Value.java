package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Abstract value.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public final ValueIter iter(final QueryContext ctx) {
    return iter();
  }

  /**
   * Returns an iterator.
   * @return iterator
   */
  public abstract ValueIter iter();

  @Override
  public Value value(final QueryContext ctx) {
    return this;
  }

  @Override
  public final boolean value() {
    return true;
  }

  @Override
  public abstract long size();

  /**
   * Checks if this is a single numeric item.
   * @return result of check
   */
  public final boolean num() {
    return type.num();
  }

  /**
   * Checks if this is a single function item.
   * @return result of check
   */
  public final boolean func() {
    return type.func();
  }

  /**
   * Checks if this is a single untyped item.
   * @return result of check
   */
  public final boolean unt() {
    return type.unt();
  }

  /**
   * Checks if this is a single string item.
   * @return result of check
   */
  public final boolean str() {
    return type.str();
  }

  /**
   * Checks if this is a single duration item.
   * @return result of check
   */
  public final boolean dur() {
    return type.dur();
  }

  /**
   * Checks if this is a single date item.
   * @return result of check
   */
  public final boolean date() {
    return type.dat();
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
  public boolean uses(final Use u) {
    return false;
  }

  @Override
  public int count(final Var v) {
    return 0;
  }

  @Override
  public final boolean removable(final Var v) {
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return this;
  }

  @Override
  public final String desc() {
    return name();
  }

  @Override
  public final String name() {
    return type.toString();
  }

  /**
   * Returns a hash code for this value.
   * @param ii input info
   * @return hash code
   * @throws QueryException if atomization can't be applied (e.g. function item)
   */
  public abstract int hash(final InputInfo ii) throws QueryException;
}
