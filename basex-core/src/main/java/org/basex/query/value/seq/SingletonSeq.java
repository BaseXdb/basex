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
 * @author BaseX Team 2005-18, BSD License
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
  public Item ebv(final QueryContext qc, final InputInfo info) throws QueryException {
    return value.ebv(qc, info);
  }

  @Override
  public boolean homogeneous() {
    return value.homogeneous();
  }

  @Override
  public void cache(final InputInfo info) throws QueryException {
    value.cache(info);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo info) throws QueryException {
    return get(value.atomValue(qc, info), size);
  }

  @Override
  public long atomSize() {
    return value.atomSize();
  }

  @Override
  protected Seq subSeq(final long offset, final long length, final QueryContext qc) {
    return value.size() == 1 ? new SingletonSeq(length, value) : super.subSeq(offset, length, qc);
  }

  @Override
  public Value insert(final long pos, final Item item, final QueryContext qc) {
    return item.equals(value) ? get(value, size + 1) : copyInsert(pos, item, qc);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    return value.size() == 1 ? get(value, size - 1) : copyRemove(pos, qc);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    if(value.size() == 1) return this;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < size; i++) vb.add(itemAt(size - i - 1));
    return vb.value(type);
  }

  @Override
  public Item itemAt(final long pos) {
    return value.itemAt(pos % value.size());
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem(SIZE, size, TYPE, seqType());
    addPlan(plan, elem);
    value.plan(elem);
  }

  @Override
  public String description() {
    return "singleton " + super.description();
  }

  @Override
  public String toString() {
    return _UTIL_REPLICATE.args(value, size / value.size()).substring(1);
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
    if(size == 0) return Empty.SEQ;

    // if all items are equal, reduce value to single item
    Value val = value;
    if(val instanceof SingletonSeq) {
      val = ((SingletonSeq) val).value;
    } else if(vs > 1 && val.homogeneous()) {
      final Item it = val.itemAt(0);
      int v = 0;
      while(++v < vs && it.equals(val.itemAt(v)));
      if(v == vs) val = it;
    }
    return size == 1 ? val : new SingletonSeq(size, val);
  }
}
