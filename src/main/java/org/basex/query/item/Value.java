package org.basex.query.item;

import java.util.*;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ValueBuilder;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Abstract value.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for (for-each) loops. Note, however, that
 * using the default {@link #iter()} method will give you better performance.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Value extends Expr implements Iterable<Item> {
  /** Data type. */
  public Type type;

  /**
   * Constructor.
   * @param t data type
   */
  Value(final Type t) {
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

  @Override
  public final Iterator<Item> iterator() {
    return iter().iterator();
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
  public final boolean isValue() {
    return true;
  }

  @Override
  public abstract long size();

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
  public String description() {
    return info();
  }

  @Override
  public final String info() {
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
   * Creates an {@link ValueBuilder}, containing all items of this value.
   * Use with care, as compressed Values are expanded, creating many objects.
   * @return cached items
   */
  public final ValueBuilder cache() {
    final ValueBuilder vb = new ValueBuilder((int) size());
    vb.size(writeTo(vb.item, 0));
    return vb;
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
