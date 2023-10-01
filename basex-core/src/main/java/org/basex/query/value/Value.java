package org.basex.query.value;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.out.DataOutput;
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
 * values can also be retrieved via enhanced for (for-each) loops. The default
 * {@link #iter()} method will provide better performance.
 *
 * @author BaseX Team 2005-23, BSD License
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

  /**
   * Writes the data structure to disk.
   * @param out data output
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public void write(final DataOutput out) throws IOException, QueryException {
    throw Util.notExpected();
  }

  @Override
  public final void checkUp() {
  }

  @Override
  public final Value compile(final CompileContext cc) {
    return this;
  }

  @Override
  public final BasicIter<Item> iter(final QueryContext qc) {
    return iter();
  }

  @Override
  public Iterator<Item> iterator() {
    return iter().iterator();
  }

  /**
   * Returns an iterator.
   * @return iterator
   */
  public abstract BasicIter<Item> iter();

  @Override
  public final Value value(final QueryContext qc) {
    return this;
  }

  /**
   * Returns a materialized version of this value without dependencies to persistent data.
   * Raises an error if the value contains function items
   * @param test check if a contained node can be adopted unchanged
   * @param ii input info (can be {@code null})
   * @param qc query context
   * @return materialized value
   * @throws QueryException query exception
   */
  public abstract Value materialize(Predicate<Data> test, InputInfo ii, QueryContext qc)
      throws QueryException;

  /**
   * Checks if this value is materialized, i.e., contains no persistent database nodes or
   * function items.
   * @param test check if a contained node can be adopted unchanged
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean materialized(Predicate<Data> test, InputInfo ii) throws QueryException;

  /**
   * Tests if this is an empty sequence.
   * @return result of check
   */
  public final boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Tests if this is an item.
   * @return result of check
   */
  public final boolean isItem() {
    return size() == 1;
  }

  /**
   * Returns a subsequence of this value with the given start and length.
   * The following properties must hold:
   * <ul>
   *   <li>{@code start >= 0},
   *   <li>{@code length >= 0},
   *   <li>{@code length <= size() - start}
   * </ul>
   * @param start starting position
   * @param length number of items
   * @param qc query context
   * @return sub sequence
   */
  public abstract Value subsequence(long start, long length, QueryContext qc);

  /**
   * Caches lazy values.
   * @param lazy lazy caching
   * @param ii input info (can be {@code null})
   * @throws QueryException query exception
   */
  public abstract void cache(boolean lazy, InputInfo ii) throws QueryException;

  /**
   * Returns a Java representation of the value.
   * @return Java object
   * @throws QueryException query exception
   */
  public abstract Object toJava() throws QueryException;

  @Override
  public final boolean has(final Flag... flags) {
    return false;
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    return true;
  }

  @Override
  public final VarUsage count(final Var var) {
    return VarUsage.NEVER;
  }

  @Override
  public final Expr inline(final InlineContext ic) {
    return null;
  }

  @Override
  public Value copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return this;
  }

  /**
   * Returns a hash code for this value.
   * @param ii input info (can be {@code null})
   * @return hash code
   * @throws QueryException if atomization cannot be applied (e.g. function item)
   */
  public abstract int hash(InputInfo ii) throws QueryException;

  /**
   * Serializes the value, using the standard XML serializer,
   * and returns the cached result.
   * @return serialized value
   * @throws QueryIOException query I/O exception
   */
  public final ArrayOutput serialize() throws QueryIOException {
    return serialize((SerializerOptions) null);
  }

  /**
   * Serializes the value with the specified serialization parameters and returns the cached result.
   * @param options serialization parameters (can be {@code null})
   * @return serialized value
   * @throws QueryIOException query I/O exception
   */
  public final ArrayOutput serialize(final SerializerOptions options) throws QueryIOException {
    final ArrayOutput ao = new ArrayOutput();
    try {
      serialize(Serializer.get(ao, options));
    } catch(final QueryIOException ex) {
      throw ex;
    } catch(final IOException ex) {
      throw SER_X.getIO(ex);
    } catch(final ArrayIndexOutOfBoundsException ex) {
      // might occur if serialized result is too large
      Util.debug(ex);
      throw BASEX_ERROR_X.getIO(ex.getLocalizedMessage());
    }
    return ao;
  }

  /**
   * Serializes the value with the specified serializer.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public final void serialize(final Serializer ser) throws IOException {
    for(final Item item : this) {
      if(ser.finished()) break;
      ser.serialize(item);
    }
  }

  /**
   * Checks if all items of the sequence are of the same type.
   * @return result of check
   */
  public abstract boolean sameType();

  /**
   * Returns the item at the given position in the value.
   * The specified value must be lie within the valid bounds.
   * @param pos position
   * @return item
   */
  public abstract Item itemAt(long pos);

  /**
   * Returns all items of this value in reverse order.
   * @param qc query context
   * @return items in reverse order
   */
  public abstract Value reverse(QueryContext qc);

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
