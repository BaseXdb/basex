package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Empty sequence.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Empty extends Item {
  /** Single instance. */
  public static final Empty VALUE = new Empty();
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
    public Value iterValue() {
      return VALUE;
    }
    @Override
    public Value value(final QueryContext qc, final Expr expr) {
      return VALUE;
    }
  };

  /**
   * Private constructor.
   */
  private Empty() {
    super(AtomType.ITEM);
  }

  @Override
  public boolean vacuous() {
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
  public Item ebv(final QueryContext qc, final InputInfo ii) {
    return Bln.FALSE;
  }

  @Override
  public Item test(final QueryContext qc, final InputInfo ii) {
    return null;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public Empty subsequence(final long start, final long length, final QueryContext qc) {
    return this;
  }

  @Override
  public SeqType seqType() {
    return SeqType.EMPTY_SEQUENCE_Z;
  }

  @Override
  public int hash(final InputInfo ii) {
    return 0;
  }

  @Override
  public Iter atomIter(final QueryContext qc, final InputInfo ii) {
    return ITER;
  }

  @Override
  public long atomSize() {
    return 0;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) {
    return (mode == Simplify.EBV || mode == Simplify.PREDICATE) ?
      cc.simplify(this, Bln.FALSE) : this;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == VALUE;
  }

  @Override
  public String description() {
    return EMPTYY + ' ' + SEQUENCE;
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this));
  }

  @Override
  public void plan(final QueryString qs) {
    qs.paren("");
  }
}
