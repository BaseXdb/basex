package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Empty sequence.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Empty extends Item {
  /** Singleton instance. */
  public static final Empty VALUE = new Empty();
  /** Placeholder for an undefined function argument. */
  public static final Empty UNDEFINED = new Empty();

  /** Empty iterator. */
  public static final BasicIter<Item> ITER = new BasicIter<>(0) {
    @Override
    public Item next() {
      return null;
    }
    @Override
    public Item get(final long i) {
      return null;
    }
    @Override
    public boolean valueIter() {
      return true;
    }
    @Override
    public Empty value(final QueryContext qc, final Expr expr) {
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
    return null;
  }

  @Override
  public BasicIter<Item> iter() {
    return ITER;
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final boolean pred) {
    return false;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public boolean bool(final InputInfo ii) throws QueryException {
    return false;
  }

  @Override
  public boolean equal(final Item item, final Collation coll, final StaticContext sc,
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
  public Expr optimizePos(final OpV op, final CompileContext cc) {
    return Bln.FALSE;
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
  public boolean equals(final Object obj) {
    return obj == VALUE;
  }

  @Override
  public String description() {
    return EMPTYY + ' ' + SEQUENCE;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.paren("");
  }
}
