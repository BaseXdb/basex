package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Sequence of a single item.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class SingletonSeq extends Seq {
  /** Singleton value. */
  public final Value value;

  /**
   * Constructor.
   * @param size size of resulting sequence (multiple of value size)
   * @param value singleton value
   */
  private SingletonSeq(final long size, final Value value) {
    super(size, value.type);
    this.value = value;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value.ebv(qc, ii);
  }

  @Override
  public int writeTo(final Item[] arr, final int index) {
    final int w = Math.min((int) size, arr.length - index);
    final long vs = value.size();
    for(int i = 0; i < w; i++) arr[index + i] = value.itemAt(i % vs);
    return w;
  }

  @Override
  public boolean homogeneous() {
    return value.homogeneous();
  }

  @Override
  public void materialize(final InputInfo ii) throws QueryException {
    value.materialize(ii);
  }

  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    return get(value.atomValue(ii), size);
  }

  @Override
  public long atomSize() {
    return value.atomSize();
  }

  @Override
  protected Seq subSeq(final long offset, final long length) {
    return value.size() == 1 ? new SingletonSeq(length, value) : super.subSeq(offset, length);
  }

  @Override
  public Value insert(final long pos, final Item item) {
    return item.equals(value) ? get(value, size + 1) : copyInsert(pos, item);
  }

  @Override
  public Value remove(final long pos) {
    return value.size() == 1 ? get(value, size - 1) : copyRemove(pos);
  }

  @Override
  public Value reverse() {
    if(value.size() == 1) return this;
    final long n = size;
    final ValueBuilder vb = new ValueBuilder();
    for(long i = 0; i < n; i++) vb.add(itemAt(n - i - 1));
    return vb.value(type);
  }

  @Override
  public Item itemAt(final long pos) {
    return value.itemAt(pos % value.size());
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(SIZE, size, TYPE, seqType());
    addPlan(plan, el);
    value.plan(el);
  }

  @Override
  public String description() {
    return "singleton " + super.description();
  }

  @Override
  public String toString() {
    return _UTIL_REPLICATE.args(value, size / value.size()).substring(1);
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified value.
   * @param value value
   * @param size number of repetitions
   * @return value
   */
  public static Value get(final Value value, final long size) {
    final long sz = size * value.size();
    return sz == 0 ? Empty.SEQ : size == 1 ? value : new SingletonSeq(sz, value);
  }
}
