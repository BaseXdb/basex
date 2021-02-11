package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Sequence of a single item.
 *
 * @author BaseX Team 2005-21, BSD License
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

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item head = value.itemAt(0);
    if(head instanceof ANode) return head;
    throw EBV_X.get(ii, this);
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
    return get(value.reverse(qc), size / value.size());
  }

  @Override
  public Item itemAt(final long pos) {
    return value.itemAt(pos % value.size());
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return mode == Simplify.DISTINCT ? cc.replaceWith(this, value) : super.simplifyFor(mode, cc);
  }

  @Override
  public String description() {
    return "singleton " + super.description();
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), value);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.function(_UTIL_REPLICATE, value, size / value.size());
  }

  /**
   * Indicates if the sequence is based on a single item.
   * @return result of check
   */
  public boolean singleItem() {
    return value instanceof Item;
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
