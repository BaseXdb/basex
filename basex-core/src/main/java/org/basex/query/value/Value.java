package org.basex.query.value;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

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
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract value.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for(for-each) loops. The default
 * {@link #iter()} method will provide better performance.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Value extends Expr implements Iterable<Item> {
  /** Item type. */
  public Type type;

  /**
   * Constructor.
   * @param type item type
   */
  protected Value(final Type type) {
    this.type = type;
  }

  @Override
  public final void checkUp() {
  }

  @Override
  public final Value compile(final QueryContext qc, final VarScope scp) {
    return this;
  }

  @Override
  public final ValueIter iter(final QueryContext qc) {
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
  public final Value value(final QueryContext qc) {
    return this;
  }

  /**
   * Materializes streamable values, or returns a self reference.
   * @param ii input info
   * @return materialized item
   * @throws QueryException query exception
   */
  public abstract Value materialize(final InputInfo ii) throws QueryException;

  /**
   * Evaluates the expression and returns the atomized items.
   * @param ii input info
   * @return materialized item
   * @throws QueryException query exception
   */
  public abstract Value atomValue(final InputInfo ii) throws QueryException;

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return atomValue(ii);
  }

  /**
   * Computes the number of atomized items.
   * @return atomized item
   */
  public abstract long atomSize();

  @Override
  public final boolean isValue() {
    return true;
  }

  /**
   * Returns a Java representation of the value.
   * @return Java object
   * @throws QueryException query exception
   */
  public abstract Object toJava() throws QueryException;

  @Override
  public boolean has(final Flag flag) {
    return false;
  }

  @Override
  public final boolean removable(final Var var) {
    return true;
  }

  @Override
  public final VarUsage count(final Var var) {
    return VarUsage.NEVER;
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    // values do not contain variable references
    return null;
  }

  @Override
  public Value copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return this;
  }

  @Override
  public String description() {
    return type + " " + SEQUENCE;
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
   * @param index start position
   * @return number of written items
   */
  public abstract int writeTo(final Item[] arr, final int index);

  /**
   * Creates an {@link ValueBuilder}, containing all items of this value.
   * Use with care, as compressed Values are expanded, creating many objects.
   * @return cached items
   */
  public final ValueBuilder cache() {
    final ValueBuilder vb = new ValueBuilder((int) size());
    vb.size(writeTo(vb.items(), 0));
    return vb;
  }

  /**
   * Serializes the value, using the standard XML serializer,
   * and returns the cached result.
   * @return serialized value
   * @throws QueryIOException query I/O exception
   */
  public final ArrayOutput serialize() throws QueryIOException {
    return serialize(null);
  }

  /**
   * Serializes the value with the specified serialization parameters and returns the cached result.
   * @param options serialization parameters (may be {@code null})
   * @return serialized value
   * @throws QueryIOException query I/O exception
   */
  public final ArrayOutput serialize(final SerializerOptions options) throws QueryIOException {
    final ArrayOutput ao = new ArrayOutput();
    try {
      final Serializer ser = Serializer.get(ao, options);
      final ValueIter vi = iter();
      for(Item it; (it = vi.next()) != null;) ser.serialize(it);
      ser.close();
    } catch(final QueryIOException ex) {
      throw ex;
    } catch(final IOException ex) {
      throw SER_X.getIO(ex);
    }
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
  public abstract boolean homogeneous();

  @Override
  public boolean accept(final ASTVisitor visitor) {
    final Data data = data();
    return data == null || visitor.lock(data.meta.name);
  }

  @Override
  public final int exprSize() {
    return 1;
  }
}
