package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.Function;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Range sequence, containing at least two integers.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class RangeSeq extends Seq {
  /** Start value. */
  private final long start;
  /** Ascending/descending order. */
  public final boolean asc;

  /**
   * Constructor.
   * @param start start value
   * @param size size
   * @param asc ascending order
   */
  private RangeSeq(final long start, final long size, final boolean asc) {
    super(size, AtomType.INTEGER);
    this.start = start;
    this.asc = asc;
  }

  /**
   * Returns a value representation of the specified items.
   * @param start start value
   * @param size size
   * @param asc ascending order
   * @return resulting item or sequence
   */
  public static Value get(final long start, final long size, final boolean asc) {
    return size < 1 ? Empty.VALUE : size == 1 ? Int.get(start) : new RangeSeq(start, size, asc);
  }

  /**
   * Creates a value from the input stream.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   */
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException {
    final long size = in.readLong(), start = in.readLong();
    final boolean asc = in.readBool();
    return get(start, size, asc);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeLong(size);
    out.writeLong(start);
    out.writeBool(asc);
  }

  /**
   * Returns the range as long values.
   * @param order respect ascending/descending order
   * @return minimum and maximum value (inclusive)
   */
  public long[] range(final boolean order) {
    final long end = asc ? start + size - 1 : start - size + 1;
    return new long[] { order || asc ? start : end, order || asc ? end : start };
  }

  @Override
  public Object toJava() {
    final long[] obj = new long[(int) size];
    for(int s = 0; s < size; ++s) obj[s] = start + (asc ? s : -s);
    return obj;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw ebvError(this, ii);
  }

  @Override
  public Int itemAt(final long pos) {
    return Int.get(start + (asc ? pos : -pos));
  }

  @Override
  protected Seq subSeq(final long pos, final long length, final QueryContext qc) {
    return new RangeSeq(start + (asc ? pos : -pos), length, asc);
  }

  @Override
  public Value insert(final long pos, final Item item, final QueryContext qc) {
    return copyInsert(pos, item, qc);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    return pos == 0 || pos == size - 1 ? subSeq(pos == 0 ? 0 : 1, size - 1, qc) :
      copyRemove(pos, qc);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    return get(range(true)[1], size(), !asc);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) { }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) {
    return this;
  }

  @Override
  public boolean sameType() {
    return true;
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii) {
    return true;
  }

  @Override
  public Expr optimizePos(final OpV op, final CompileContext cc) throws QueryException {
    final long[] range = range(false);
    final long min = range[0], max = range[1];
    range[0] = Math.max(range[0], 1);
    range[1] = Math.max(range[1], 1);
    switch(op) {
      case EQ:
        if(max < 1) return Bln.FALSE;
        if(min < 2 && max == Long.MAX_VALUE) return Bln.TRUE;
        break;
      case NE:
        if(max < 1) return Bln.TRUE;
        if(min < 2 && max == Long.MAX_VALUE) return Bln.FALSE;
        break;
      case LE:
        return max < 1 ? Bln.FALSE : Int.get(range[1]);
      case LT:
        return max < 2 ? Bln.FALSE : Int.get(range[1]);
      case GT:
        return max < 1 ? Bln.TRUE : Int.get(range[0]);
      case GE:
        return max < 2 ? Bln.TRUE : Int.get(range[0]);
    }
    if(min == range[0] && max == range[1]) return this;
    final Expr ex = RangeSeq.get(range[0], range[1], true);
    return ex == Empty.VALUE ? Bln.FALSE : ex;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof RangeSeq)) return super.equals(obj);
    final RangeSeq seq = (RangeSeq) obj;
    return start == seq.start && size == seq.size && asc == seq.asc;
  }

  @Override
  public String description() {
    return "range " + SEQUENCE;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    final long[] range = range(true);
    plan.add(plan.create(this, FROM, range[0], TO, range[1]));
  }

  @Override
  public void toString(final QueryString qs) {
    final long[] range = range(false);
    final String arg = new QueryString().token(range[0]).token(TO).token(range[1]).toString();
    if(asc) {
      qs.paren(arg);
    } else {
      qs.function(Function.REVERSE, ' ' + arg);
    }
  }
}
