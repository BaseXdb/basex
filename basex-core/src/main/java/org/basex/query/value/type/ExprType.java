package org.basex.query.value.type;

import org.basex.query.expr.*;
import org.basex.util.*;

/**
 * Expression type, including a sequence type and result size.
 *
 * @author BaseX Team 2005-17, BSD License
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
   * Assigns the specified sequence type and result size.
   * @param st sequence type
   * @param sz result size
   */
  public void assign(final SeqType st, final long sz) {
    seqType = st;
    size = sz;
  }

  /**
   * Assigns the specified type, updates the result size.
   * @param st sequence type
   */
  public void assign(final SeqType st) {
    assign(st, st.zero() ? 0 : st.one() ? 1 : -1);
  }

  /**
   * Assigns type information of the specified expression.
   * @param expr expression
   */
  public void assign(final Expr expr) {
    assign(expr.seqType(), expr.size());
  }

  /**
   * Assigns the specified expression type.
   * @param et expression type
   */
  public void assign(final ExprType et) {
    assign(et.seqType, et.size);
  }

  /**
   * Assigns the specified type.
   * @param type type
   */
  public void assign(final Type type) {
    seqType = seqType.with(type);
  }

  /**
   * Assigns the specified type and occurrence indicator, updates the result size.
   * @param type type
   * @param occ occurrence indicator
   */
  public void assign(final Type type, final Occ occ) {
    assign(seqType.with(type, occ));
  }

  /**
   * Assigns the specified occurrence indicator, updates the result size.
   * @param occ occurrence indicator
   */
  public void assign(final Occ occ) {
    assign(seqType.with(occ));
  }

  /**
   * Assigns the specified type, and result size or occurrence indicator.
   * @param type type
   * @param sz result size. If {@code -1}, the occurrence indicator will be assigned.
   * @param occ occurrence indicator
   */
  public void assign(final Type type, final Occ occ, final long sz) {
    if(sz >= 0) {
      final Occ oc = sz == 0 ? Occ.ZERO : sz == 1 ? Occ.ONE : sz > 1 ? Occ.ONE_MORE : Occ.ZERO_MORE;
      assign(seqType.with(type, oc), sz);
    } else {
      assign(type, occ);
    }
  }

  /**
   * Assigns the type and result size, based on the specified min/max occurrences.
   * @param type type
   * @param minMax min/max values (min: 0 or more, max: -1 or more)
   */
  public void assign(final Type type, final long[] minMax) {
    final long min = minMax[0], max = minMax[1], sz = min == max ? min : -1;
    final Occ occ = min == 0 ? max == 1 ? Occ.ZERO_ONE : Occ.ZERO_MORE : Occ.ONE_MORE;
    assign(type, occ, sz);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
    sb.append(seqType).append(", ").append(size);
    return sb.append(']').toString();
  }
}
