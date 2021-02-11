package org.basex.query.value.type;

import org.basex.query.expr.*;
import org.basex.util.*;

/**
 * Expression type, including a sequence type and result size.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ExprType {
  /** Sequence type. */
  private SeqType seqType;
  /** Result size. */
  private long size = -1;

  /**
   * Constructor.
   * @param seqType sequence type
   */
  public ExprType(final SeqType seqType) {
    assign(seqType);
  }

  /**
   * Returns the sequence type.
   * @return sequence type
   */
  public SeqType seqType() {
    return seqType;
  }

  /**
   * Returns the result size.
   * @return result size
   */
  public long size() {
    return size;
  }

  /**
   * Assigns the specified sequence type, updates the result size.
   * @param st sequence type
   */
  public void assign(final SeqType st) {
    asg(st, st.zero() ? 0 : st.one() ? 1 : -1);
  }

  /**
   * Assigns type information of the specified expression.
   * @param expr expression
   */
  public void assign(final Expr expr) {
    asg(expr.seqType(), expr.size());
  }

  /**
   * Assigns the specified expression type.
   * @param et expression type
   */
  public void assign(final ExprType et) {
    asg(et.seqType, et.size);
  }

  /**
   * Assigns the specified type.
   * @param type type
   */
  public void assign(final Type type) {
    seqType = SeqType.get(type, seqType.occ);
  }

  /**
   * Assigns the specified type and occurrence indicator, updates the result size.
   * @param type type
   * @param occ occurrence indicator
   */
  public void assign(final Type type, final Occ occ) {
    assign(SeqType.get(type, occ));
  }

  /**
   * Assigns the specified occurrence indicator, updates the result size.
   * @param occ occurrence indicator
   */
  public void assign(final Occ occ) {
    assign(seqType.with(occ));
  }

  /**
   * Assigns the specified sequence type and result size.
   * The occurrence indicator of the sequence type is ignored if the result size is known.
   * @param st sequence type
   * @param sz result size (unknown if negative)
   */
  public void assign(final SeqType st, final long sz) {
    if(sz >= 0) {
      asg(st.with(sz == 0 ? Occ.ZERO : sz == 1 ? Occ.EXACTLY_ONE : Occ.ONE_OR_MORE), sz);
    } else {
      assign(st);
    }
  }

  /**
   * Assigns the specified type and result size.
   * The specified occurrence indicator is ignored if the result size is known.
   * @param st sequence type (the occurrence indicator is ignored)
   * @param occ occurrence indicator
   * @param sz result size (unknown if negative)
   */
  public void assign(final SeqType st, final Occ occ, final long sz) {
    assign(sz >= 0 ? st : st.with(occ), sz);
  }

  /**
   * Assigns the type and result size, based on the specified min/max occurrences.
   * @param st sequence type (the occurrence indicator is ignored)
   * @param minMax min/max values (min: 0 or more, max: -1 or more)
   */
  public void assign(final SeqType st, final long[] minMax) {
    final long min = minMax[0], max = minMax[1], sz = min == max ? min : -1;
    final Occ occ = min > 0 ? Occ.ONE_OR_MORE : max == 1 ? Occ.ZERO_OR_ONE : Occ.ZERO_OR_MORE;
    assign(st, occ, sz);
  }

  /**
   * Refines the type with type information from the specified expression.
   * @param expr expression with original type
   */
  public void refine(final Expr expr) {
    final SeqType st = seqType.intersect(expr.seqType());
    if(st != null) {
      final long es = expr.size();
      asg(st, es == size || size == -1 ? es : es == -1 ? size : -1);
    }
  }

  /**
   * Assigns the specified sequence type and result size.
   * @param st sequence type
   * @param sz result size
   */
  private void asg(final SeqType st, final long sz) {
    seqType = st;
    size = sz;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + seqType + ", " + size + ']';
  }
}
