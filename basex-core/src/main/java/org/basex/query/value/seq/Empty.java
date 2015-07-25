package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Empty sequence.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Empty extends Value {
  /** Single instance. */
  public static final Empty SEQ = new Empty();
  /** Empty iterator. */
  public static final ValueIter ITER = new ValueIter() {
    @Override public Item next() { return null; }
    @Override public Item get(final long i) { return null; }
    @Override public Value value() { return SEQ; }
    @Override public long size() { return 0; }
  };

  /**
   * Private constructor.
   */
  private Empty() {
    super(AtomType.ITEM);
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isVacuous() {
    return true;
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public Object toJava() {
    return new Object[0];
  }

  @Override
  public ValueIter iter() {
    return ITER;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    return null;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) {
    return Bln.FALSE;
  }

  @Override
  public Item test(final QueryContext qc, final InputInfo ii) {
    return null;
  }

  @Override
  public SeqType seqType() {
    return SeqType.EMP;
  }

  @Override
  public boolean iterable() {
    return true;
  }

  @Override
  public int hash(final InputInfo ii) {
    return 0;
  }

  @Override
  public int writeTo(final Item[] arr, final int index) {
    return 0;
  }

  @Override
  public Item itemAt(final long pos) {
    throw Util.notExpected();
  }

  @Override
  public Value reverse() {
    return this;
  }

  @Override
  public boolean homogeneous() {
    return true;
  }

  @Override
  public Value subSeq(final long start, final long len) {
    return this;
  }

  @Override
  public Value materialize(final InputInfo ii) {
    return this;
  }

  @Override
  public Value atomValue(final InputInfo ii) {
    return this;
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) {
    return null;
  }

  @Override
  public long atomSize() {
    return 0;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(SIZE, 0));
  }

  @Override
  public String description() {
    return EMPTY_SEQUENCE + "()";
  }

  @Override
  public String toString() {
    return "()";
  }
}
