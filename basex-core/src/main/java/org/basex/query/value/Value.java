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
import org.basex.query.value.seq.*;
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
 * @author BaseX Team, BSD License
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

  /**
   * Tests if this is an empty sequence.
   * @return result of check
   */
  public boolean isEmpty() {
    return false;
  }

  /**
   * Returns the item at the given position.
   * The specified value must be lie within the valid bounds.
   * @param index index position
   * @return item
   */
  public abstract Item itemAt(long index);

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
   * Returns a subsequence with the given start and length.
   * The following properties must hold:
   * <ul>
   *   <li>{@code pos >= 0},
   *   <li>{@code length >= 0},
   *   <li>{@code length <= size() - pos}
   * </ul>
   * @param pos starting position
   * @param length number of items
   * @param qc query context
   * @return new subsequence
   */
  public final Value subsequence(final long pos, final long length, final QueryContext qc) {
    return length == 0 ? Empty.VALUE :
           length == 1 ? itemAt(pos) :
           length == size() ? this :
           subSeq(pos, length, qc);
  }

  /**
   * Returns a subsequence with the given start and length.
   * @param pos position of first item (>= 0)
   * @param length number of items (1 < length < size())
   * @param qc query context
   * @return new subsequence
   */
  protected abstract Value subSeq(long pos, long length, QueryContext qc);


  /**
   * Appends a value.
   * @param value value to append
   * @param qc query context
   * @return new value
   */
  public final Value append(final Value value, final QueryContext qc) {
    return insert(size(), value, qc);
  }

  /**
   * Inserts a value at the given position.
   * @param pos insertion position, must be between 0 and {@link #size()}
   * @param value value to insert
   * @param qc query context
   * @return new value
   */
  public final Value insert(final long pos, final Value value, final QueryContext qc) {
    final long size = size(), vsize = value.size();
    return size == 0 ? value :
           vsize == 0 ? this :
           pos == size && size < vsize ? value.insertValue(0, this, qc) :
           insertValue(pos, value, qc);
  }

  /**
   * Inserts a value at the given position.
   * @param pos insertion position, must be between 0 and {@link #size()}
   * @param value value to insert
   * @param qc query context
   * @return new value
   */
  public abstract Value insertValue(long pos, Value value, QueryContext qc);

  /**
   * Removes an item at the given position.
   * @param pos deletion position, must be between 0 and {@link #size()} - 1
   * @param qc query context
   * @return new sequence
   */
  public abstract Value removeItem(long pos, QueryContext qc);

  /**
   * Caches data of lazy items (i.e., those implementing the {@link Lazy} interface).
   * By calling this method, the streaming feature of lazy items will be disabled.
   * @param lazy if {@code false}, cache immediately. if {@code true}, caching is deferred
   *   until the data is actually requested
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
  public Value copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return this;
  }

  /**
   * Returns a materialized version of this value without dependencies to persistent data.
   * Raises an error if the value contains function items.
   * @param test test to check if a node can be adopted unchanged
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
   * @param test test to check if a node can be adopted unchanged
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean materialized(Predicate<Data> test, InputInfo ii) throws QueryException;

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
      throw SERPARAM_X.getIO(ex);
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
   * Returns all items of this value in reverse order.
   * @param qc query context
   * @return items in reverse order
   */
  public abstract Value reverse(QueryContext qc);

  /**
   * Refines the type of a value.
   * @return if the sequence is homogeneous, i.e., if all items are of the same type
   * @throws QueryException query exception
   */
  public abstract boolean refineType() throws QueryException;

  /**
   * If possible, returns a compactified version of this value.
   * Note that the memory consumption may increase during the reconstruction of a data structure.
   * @param qc query context
   * @return compactified value or self reference
   * @throws QueryException query exception
   */
  public abstract Value shrink(QueryContext qc) throws QueryException;

  /**
   * Saves memory by recursively rebuilding the data structure.
   * Called by {@link #shrink(QueryContext)} and implemented for sequences, maps and arrays.
   * @param qc query exception
   * @return rebuilt data structure
   * @throws QueryException query exception
   */
  protected abstract Value rebuild(QueryContext qc) throws QueryException;

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
