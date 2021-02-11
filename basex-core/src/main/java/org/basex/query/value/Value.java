package org.basex.query.value;

import static org.basex.query.QueryError.*;

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
 * @author BaseX Team 2005-21, BSD License
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
   * Returns a materialized, context-independent version of this value.
   * @param qc query context (if {@code null}, process cannot be interrupted)
   * @param error query error
   * @param info input info
   * @return item copy, or {@code null}) if the value cannot be materialized
   * @throws QueryException query exception
   */
  public Value materialize(final QueryContext qc, final QueryError error, final InputInfo info)
      throws QueryException {

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : this) {
      final Item it = item.materialize(qc, item.persistent());
      if(it == null) throw error.get(info, item);
      vb.add(it);
    }
    return vb.value();
  }

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
   * @param ii input info
   * @throws QueryException query exception
   */
  public abstract void cache(boolean lazy, InputInfo ii) throws QueryException;

  /**
   * Computes the number of atomized items.
   * @return atomized item
   */
  public abstract long atomSize();

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
   * @param ii input info
   * @return hash code
   * @throws QueryException if atomization can't be applied (e.g. function item)
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
   * @param options serialization parameters (may be {@code null})
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
    return data == null || visitor.lock(data.meta.name, false);
  }

  @Override
  public final int exprSize() {
    return 1;
  }
}
