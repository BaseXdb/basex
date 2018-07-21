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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Empty extends Value {
  /** Single instance. */
  public static final Empty SEQ = new Empty();
  /** Empty iterator. */
  public static final BasicIter<Item> ITER = new BasicIter<Item>(0) {
    @Override
    public Item next() {
      return null;
    }
    @Override
    public Item get(final long i) {
      return null;
    }
    @Override
    public Value value() {
      return SEQ;
    }
    @Override
    public Value value(final QueryContext qc) {
      return SEQ;
    }
  };

  /**
   * Private constructor.
   */
  private Empty() {
    super(AtomType.ITEM);
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
  public BasicIter<Item> iter() {
    return ITER;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo info) {
    return null;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo info) {
    return Bln.FALSE;
  }

  @Override
  public Item test(final QueryContext qc, final InputInfo info) {
    return null;
  }

  @Override
  public Empty subSequence(final long start, final long length, final QueryContext qc) {
    return this;
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
  public int hash(final InputInfo info) {
    return 0;
  }

  @Override
  public Item itemAt(final long pos) {
    throw Util.notExpected();
  }

  @Override
  public Value reverse(final QueryContext qc) {
    return this;
  }

  @Override
  public boolean homogeneous() {
    return true;
  }

  @Override
  public void cache(final InputInfo info) { }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo info) {
    return this;
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo info) {
    return null;
  }

  @Override
  public long atomSize() {
    return 0;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == SEQ;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(SIZE, 0, TYPE, seqType()));
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
