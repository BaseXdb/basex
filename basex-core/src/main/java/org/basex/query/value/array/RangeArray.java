package org.basex.query.value.array;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Range array, containing at least two integers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RangeArray extends XQArray {
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
  public RangeArray(final long start, final long size, final boolean ascending) {
    super(size, ArrayType.get(SeqType.INTEGER_O));
    this.start = start;
    this.ascending = ascending;
  }

  @Override
  public Value memberAt(final long pos) {
    return Int.get(get(pos));
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final QueryContext qc) {
    return new RangeArray(get(pos), length, ascending);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return new RangeArray(get(size - 1), structSize(), !ascending);
  }

  @Override
  public Iter items() throws QueryException {
    return RangeSeq.get(start, size, ascending).iter();
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
  public String description() {
    return "range " + ARRAY;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, FROM, get(0), TO, get(size - 1)));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(ARRAY).token("{");
    final long min = ascending ? start : start - size + 1;
    final long max = ascending ? start + size - 1 : start;
    final String arg = new QueryString().token(min).token(TO).token(max).toString();
    if(ascending) {
      qs.paren(arg);
    } else {
      qs.function(Function.REVERSE, ' ' + arg);
    }
    qs.token("}");
  }
}
