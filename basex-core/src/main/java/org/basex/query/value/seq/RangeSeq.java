package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.function.*;

import org.basex.core.*;
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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RangeSeq extends Seq {
  /** Start value. */
  private final long start;
  /** Ascending/descending order. */
  private final boolean ascending;

  /**
   * Constructor.
   * @param start start value
   * @param size size
   * @param ascending ascending order
   */
  private RangeSeq(final long start, final long size, final boolean ascending) {
    super(size, AtomType.INTEGER);
    this.start = start;
    this.ascending = ascending;
  }

  /**
   * Returns a value representation of the specified items.
   * @param start start value
   * @param size size
   * @param ascending ascending order
   * @return resulting item or sequence
   */
  public static Value get(final long start, final long size, final boolean ascending) {
    return size < 1 ? Empty.VALUE : size == 1 ? Int.get(start) :
      new RangeSeq(start, size, ascending);
  }

  /**
   * Creates a value from the input stream. Called from {@link Store#read(DataInput, QueryContext)}.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   */
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException {
    final long size = in.readLong(), start = in.readLong();
    final boolean ascending = in.readBool();
    return get(start, size, ascending);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeLong(size);
    out.writeLong(start);
    out.writeBool(ascending);
  }

  /**
   * Returns whether the order is ascending or descending.
   * @return order
   */
  public boolean ascending() {
    return ascending;
  }

  /**
   * Returns the minimum value, ignoring the order.
   * @return minimum value
   */
  public long min() {
    return ascending ? start : start - size + 1;
  }

  /**
   * Returns the maximum value, ignoring the order.
   * @return maximum value
   */
  public long max() {
    return ascending ? start + size - 1 : start;
  }

  /**
   * Returns the specified value.
   * @param pos position
   * @return minimum value
   */
  private long get(final long pos) {
    return start + (ascending ? pos : -pos);
  }

  @Override
  public Object toJava() {
    final long[] obj = new long[(int) size];
    for(int s = 0; s < size; ++s) obj[s] = start + (ascending ? s : -s);
    return obj;
  }

  @Override
  public Int itemAt(final long pos) {
    return Int.get(get(pos));
  }

  @Override
  protected Seq subSeq(final long pos, final long length, final QueryContext qc) {
    return new RangeSeq(get(pos), length, ascending);
  }

  @Override
  public Value insertBefore(final long pos, final Item item, final QueryContext qc) {
    return copyInsert(pos, item, qc);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    return pos == 0 || pos == size - 1 ? subSeq(pos == 0 ? 0 : 1, size - 1, qc) :
      copyRemove(pos, qc);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    return get(get(size - 1), size(), !ascending);
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
  public Expr optimizePos(final OpV op, final CompileContext cc) {
    final long min = min(), max = max();
    switch(op) {
      case LE:
        // position() <= (-5 to 0)  ->  false()
        // position() <= (3 to 5)   ->  position() <= 5
        return max <= 0 ? Bln.FALSE : Int.get(max);
      case LT:
        // position() < (-5 to 1)  ->  false()
        // position() < (3 to 5)   ->  position() < 5
        return max <= 1 ? Bln.FALSE : Int.get(max);
      case GE:
        // position() >= (1 to 5)  ->  true()
        // position() >= (3 to 5)  ->  position() >= 3
        return min <= 1 ? Bln.TRUE : Int.get(min);
      case GT:
        // position() > (0 to 5)  ->  true()
        // position() > (3 to 5)  ->  position() > 3
        return min <= 0 ? Bln.TRUE : Int.get(min);
      case EQ:
        // position() = (-5 to 0x7FFFFFFFFFFFFFFF)  ->  true()
        // position() = (-5 to 0)                   ->  false()
        if(min <= 1 && max == Long.MAX_VALUE) return Bln.TRUE;
        if(max <= 0) return Bln.FALSE;
        break;
      case NE:
        // position() != (-5 to 0x7FFFFFFFFFFFFFFF)  ->  false()
        // position() != (-5 to 0)                   ->  true()
        if(min <= 1 && max == Long.MAX_VALUE) return Bln.FALSE;
        if(max <= 0) return Bln.TRUE;
    }
    // op: =/!=, max >= 1
    // position() = (-3 to 1)  ->  position() = 1
    // position() = (-3 to 5)  ->  position() = (1 to 5)
    return min >= 1 ? this : get(1, max, true);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof RangeSeq)) return super.equals(obj);
    final RangeSeq rs = (RangeSeq) obj;
    return start == rs.start && size == rs.size && ascending == rs.ascending;
  }

  @Override
  public String description() {
    return "range " + SEQUENCE;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, FROM, get(0), TO, get(size - 1)));
  }

  @Override
  public void toString(final QueryString qs) {
    final String arg = new QueryString().token(min()).token(TO).token(max()).toString();
    if(ascending) {
      qs.paren(arg);
    } else {
      qs.function(Function.REVERSE, ' ' + arg);
    }
  }
}
