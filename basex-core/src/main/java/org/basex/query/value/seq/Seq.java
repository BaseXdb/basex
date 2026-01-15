package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Seq extends Value {
  /** Length. */
  protected long size;

  /**
   * Constructor, specifying a type.
   * @param size size
   * @param type exact type, {@code item()*} otherwise
   */
  protected Seq(final long size, final Type type) {
    super(type);
    this.size = size;
  }

  @Override
  public final long size() {
    return size;
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw typeError(this, AtomType.ITEM, ii);
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {

    final Item first = itemAt(0);
    if(first instanceof ANode) return true;
    if(pos == 0 || !(first instanceof ANum)) throw testError(this, false, ii);

    for(final Item item : this) {
      if(!(item instanceof ANum)) throw testError(this, true, ii);
      if(item.test(qc, ii, pos)) return true;
    }
    return false;
  }

  @Override
  public BasicIter<Item> iter() {
    return new BasicIter<>(size) {
      @Override
      public Item get(final long i) {
        return itemAt(i);
      }
      @Override
      public boolean valueIter() {
        return true;
      }
      @Override
      public Seq value(final QueryContext qc, final Expr expr) {
        return Seq.this;
      }
    };
  }

  @Override
  public boolean ddo() {
    return false;
  }

  @Override
  protected Value subSeq(final long pos, final long length, final Job job) {
    job.checkStop();
    return new SubSeq(this, pos, length);
  }

  @Override
  public Value insertValue(final long pos, final Value value, final Job job) {
    return toTree(job).insertValue(pos, value, job);
  }

  @Override
  public Value removeItem(final long pos, final Job job) {
    return toTree(job).removeItem(pos, job);
  }

  /**
   * Creates a tree-based version of this sequence.
   * @param job interruptible job
   * @return value
   */
  private Value toTree(final Job job) {
    final ValueBuilder vb = new ValueBuilder(job, size).tree(true);
    for(final Item item : this) vb.add(item);
    return vb.value(type);
  }

  @Override
  public Value reverse(final Job job) {
    final ValueBuilder vb = new ValueBuilder(job, size);
    for(long i = size - 1; i >= 0; i--) vb.add(itemAt(i));
    return vb.value(type);
  }

  @Override
  public final void refineType(final Expr expr) {
    final Type tp = expr.seqType().type.intersect(type);
    if(tp != null) type = tp;
  }

  @Override
  public final SeqType seqType() {
    return type.seqType(Occ.ONE_OR_MORE);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc, size);
    for(final Item item : this) vb.add(item.atomValue(qc, ii));
    return vb.value(AtomType.ANY_ATOMIC_TYPE);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(type instanceof NodeType && mode.oneOf(Simplify.DATA, Simplify.NUMBER, Simplify.STRING)) {
      if(mode == Simplify.STRING) {
        final TokenList list = new TokenList(size);
        for(int i = 0; i < size; i++) list.add(itemAt(i).string(null));
        expr = StrSeq.get(list);
      } else {
        final Item[] items = new Item[(int) size];
        for(int i = 0; i < size; i++) items[i] = Atm.get(itemAt(i).string(null));
        expr = ItemSeq.get(items, (int) size, AtomType.UNTYPED_ATOMIC);
      }
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(final Item item : this) item.cache(lazy, ii);
  }

  @Override
  public Value materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {

    if(materialized(test, ii)) return this;

    final ValueBuilder vb = new ValueBuilder(qc, size);
    for(final Item item : this) vb.add(item.materialize(test, ii, qc));
    return vb.value(type);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    if(!type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
      for(final Item item : this) {
        if(!item.materialized(test, ii)) return false;
      }
    }
    return true;
  }

  @Override
  public boolean refineType() {
    boolean same = true;
    if(type.refinable()) {
      Type refined = null;
      for(final Item item : this) {
        final Type tp = item.type;
        if(refined == null) {
          refined = tp;
        } else {
          same &= refined.eq(tp);
          refined = refined.union(tp);
          if(refined.eq(type)) return same;
        }
      }
      type = refined;
    }
    return same;
  }

  @Override
  protected final Value rebuild(final Job job) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(job, size());
    for(final Item item : this) vb.add(item.shrink(job));
    return vb.value(type);
  }

  @Override
  public Object toJava() throws QueryException {
    // determine type (static or exact)
    refineType();

    // shortcut for strings (avoid intermediate token representation)
    final int sz = (int) size;
    if(type == AtomType.STRING) {
      final StringList list = new StringList(sz);
      for(final Item item : this) list.add(item.string(null));
      return list.finish();
    }
    int a = 0;
    final Object[] array = new Object[sz];
    for(final Item item : this) array[a++] = item.toJava();
    return array;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof final Seq s) || size != s.size) return false;
    final BasicIter<Item> iter1 = iter(), iter2 = s.iter();
    for(Item item1; (item1 = iter1.next()) != null;) {
      if(!item1.equals(iter2.next())) return false;
    }
    return true;
  }

  @Override
  public String description() {
    return type == AtomType.ITEM ? SEQUENCE : type + " " + SEQUENCE;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    final int max = (int) Math.min(size, 5);
    final ExprList list = new ExprList(max);
    for(long i = 0; i < max; i++) list.add(itemAt(i));
    plan.add(plan.create(this), list.finish());
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add('(');
    for(int i = 0; i < size && tb.moreInfo(); ++i) {
      if(i > 0) tb.add(SEP);
      final Item item = itemAt(i);
      tb.add(qs.error() ? item.toErrorString() : item.toString());
    }
    qs.token(tb.add(')').finish());
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a value from the input stream.
   * Called from {@link Stores#read(DataInput, QueryContext)}.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException, QueryException {
    throw Util.notExpected();
  }

  /**
   * Returns an initial array capacity for the expected result size.
   * Throws an exception if the requested size will take too much memory.
   * @param size expected result size
   * @return capacity
   * @throws QueryException query exception
   */
  public static int initialCapacity(final long size) throws QueryException {
    if(size > Array.MAX_SIZE) throw MAX_SIZE_X_X.get(null, Array.MAX_SIZE, size);
    return Array.initialCapacity(size);
  }
}
