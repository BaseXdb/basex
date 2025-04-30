package org.basex.query.value.seq;

import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Singleton value sequence.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SingletonSeq extends Seq {
  /** Singleton value. */
  private final Value value;

  /**
   * Constructor.
   * @param size size of resulting sequence (multiple of value size)
   * @param value singleton value
   */
  private SingletonSeq(final long size, final Value value) {
    super(size, value.type);
    this.value = value;
  }

  /**
   * Creates a value from the input stream. Called from {@link Store#read(DataInput, QueryContext)}.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException, QueryException {
    final long count = in.readLong();
    final Value value = Store.read(in, qc);
    return get(value, count);
  }

  @Override
  public void write(final DataOutput out) throws IOException, QueryException {
    out.writeLong(count());
    Store.write(out, value);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    value.cache(lazy, ii);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return get(value.atomValue(qc, ii), size);
  }

  @Override
  protected Value subSeq(final long pos, final long length, final QueryContext qc) {
    return singleItem() ? get(value, length) : super.subSeq(pos, length, qc);
  }

  @Override
  public Value insertValue(final long pos, final Value val, final QueryContext qc) {
    return val.equals(value) ? get(value, size + 1) : super.insertValue(pos, val, qc);
  }

  @Override
  public Value removeItem(final long pos, final QueryContext qc) {
    return singleItem() ? get(value, size - 1) : super.removeItem(pos, qc);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    return singleItem() ? this : get(value.reverse(qc), count());
  }

  @Override
  public Item itemAt(final long index) {
    return value.itemAt(index % value.size());
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.DISTINCT || mode == Simplify.PREDICATE && type.isNumber()) {
      expr = value;
    } else if(type instanceof NodeType && mode.oneOf(Simplify.DATA, Simplify.NUMBER,
        Simplify.STRING)) {
      expr = get((Value) value.simplifyFor(mode, cc), count());
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Value materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    return materialized(test, ii) ? this : get(value.materialize(test, ii, qc), size);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return value.materialized(test, ii);
  }

  @Override
  public String description() {
    return "singleton " + super.description();
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), value);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(REPLICATE, value, count());
  }

  /**
   * Indicates if the sequence is based on a single item.
   * @return result of check
   */
  public boolean singleItem() {
    return value.size() == 1;
  }

  /**
   * Value count.
   * @return count
   */
  public long count() {
    return size / value.size();
  }

  @Override
  public void refineType() {
    refineType(value);
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a singleton sequence with the specified value.
   * @param value value
   * @param count number of repetitions
   * @return value
   */
  public static Value get(final Value value, final long count) {
    // single count: return value itself
    if(count == 1) return value;
    // zero results: return empty sequence
    final long vs = value.size(), size = vs * count;
    if(size == 0) return Empty.VALUE;
    if(!Util.inBounds(vs, count)) throw new OutOfMemoryError("Memory limit reached.");

    // if all items are equal, reduce value to single item
    Value val = value;
    if(val instanceof SingletonSeq) {
      val = ((SingletonSeq) val).value;
    } else if(vs > 1) {
      final Item item = val.itemAt(0);
      int v = 0;
      while(++v < vs && item.equals(val.itemAt(v)));
      if(v == vs) val = item;
    }
    return size == 1 ? val : new SingletonSeq(size, val);
  }
}
