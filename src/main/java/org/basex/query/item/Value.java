package org.basex.query.item;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ItemCache;
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
  public final Value value(final QueryContext ctx) {
    return this;
  }

  /**
   * Returns the data reference (if) attached to this value.
   * @return data reference
   */
  public Data data() {
    return null;
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
   * Checks if this is a single function item.
   * @return result of check
   */
  public final boolean map() {
    return type.map();
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
   * @throws QueryException query exception
   */
  public abstract Object toJava() throws QueryException;

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
  public final Expr remove(final Var v) {
    return this;
  }

  @Override
  public String desc() {
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

  /**
   * Writes this value's items out to the given array.
   * @param arr array to write to
   * @param start start position
   * @return number of written items
   */
  public abstract int writeTo(final Item[] arr, final int start);

  /**
   * Creates an {@link ItemCache}, containing all items of this value.
   * Use with care, as compressed Values are expanded, creating many objects.
   * @return cached items
   */
  public final ItemCache cache() {
    final ItemCache ic = new ItemCache((int) size());
    ic.size(writeTo(ic.item, 0));
    return ic;
  }

  /**
   * Gets the item at the given position in the value.
   * @param pos position
   * @return item
   */
  public abstract Item itemAt(final long pos);

  /**
   * Checks if all items of this value share the same type.
   * @return result of check
   */
  public abstract boolean homogenous();
}
