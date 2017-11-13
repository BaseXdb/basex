package org.basex.query.value.type;

import org.basex.query.expr.*;
import org.basex.query.value.type.SeqType.*;
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
   * Indicates if the expression result will be empty.
   * @return result of check
   */
  public boolean isEmpty() {
    return size == 0;
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
   * Assigns the specified type and result size.
   * @param st type
   * @param sz result size
   */
  public void assign(final Type st, final long sz) {
    final Occ occ = sz == 0 ? Occ.ZERO : sz == 1 ? Occ.ONE : sz > 1 ? Occ.ONE_MORE : Occ.ZERO_MORE;
    assign(SeqType.get(st, occ), sz);
  }

  /**
   * Assigns the specified type.
   * @param type type
   */
  public void assign(final Type type) {
    seqType = SeqType.get(type, seqType.occ);
  }

  /**
   * Assigns the specified result size.
   * @param sz result size
   */
  public void assign(final long sz) {
    assign(seqType.type, sz);
  }

  /**
   * Assigns the specified type, updates the result size.
   * @param st sequence type
   */
  public void assign(final SeqType st) {
    assign(st, st.zero() ? 0 : st.one() ? 1 : -1);
  }

  /**
   * Assigns the specified type and occurrence, updates the result size.
   * @param type type
   * @param occ occurrence
   */
  public void assign(final Type type, final Occ occ) {
    assign(SeqType.get(type, occ));
  }

  /**
   * Assigns the specified occurrence, updates the result size.
   * @param occ occurrence
   */
  public void assign(final Occ occ) {
    assign(seqType.type, occ);
  }

  /**
   * Assigns the result size and sequence type, based on the specified min/max occurrences.
   * @param st original sequence type
   * @param minMax min/max values (min: 0 or more, max: -1 or more)
   */
  public void assign(final SeqType st, final long[] minMax) {
    final long min = minMax[0], max = minMax[1], sz = min == max ? min : -1;
    assign(sz == -1 ? st.withMinMax(min, max) : st.withOcc(occ(sz)), sz);
  }

  /**
   * Returns an occurrence for the specified result size.
   * @param sz result size
   * @return occurrence
   */
  public Occ occ(final long sz) {
    return sz == 0 ? Occ.ZERO : sz == 1 ? Occ.ONE : sz > 1 ? Occ.ONE_MORE : Occ.ZERO_MORE;
  }

  @Override
  public String toString() {
    return Util.className(this) + "[" + seqType + ": " + size + "]";
  }
}
