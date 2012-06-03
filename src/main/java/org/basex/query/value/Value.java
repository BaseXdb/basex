package org.basex.query.value;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract value.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for (for-each) loops. The default
 * {@link #iter()} method will provide better performance.
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
  public Value(final Type t) {
    type = t;
  }

  @Override
  public final void checkUp() {
  }

  @Override
  public final Value analyze(final QueryContext ctx) {
    return this;
  }

  @Override
  public final Value compile(final QueryContext ctx) {
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
   * Returns the data reference (if) attached to this value. This method is overwritten
   * by {@link DBNode} and {@link DBNodeSeq}.
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

  /**
   * Materializes streamable values, or returns a self reference.
   * @param ii input info
   * @return materialized item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Value materialize(final InputInfo ii) throws QueryException {
    return this;
  }

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
   * Serializes the value, using the standard XML serializer,
   * and returns the cached result.
   * @return serialized value
   * @throws IOException I/O exception
   */
  public final ArrayOutput serialize() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    final Serializer ser = Serializer.get(ao);
    final ValueIter vi = iter();
    for(Item it; (it = vi.next()) != null;) ser.serialize(it);
    ser.close();
    return ao;
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
