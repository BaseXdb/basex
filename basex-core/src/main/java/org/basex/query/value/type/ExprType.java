package org.basex.query.value.type;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.util.*;

/**
 * Expression type, including a sequence type and result size.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ExprType {
  /** Sequence type. */
  private SeqType seqType;
  /** Result size ({@code -1} if unknown). */
  private long size;
  /** Data reference. */
  private Data data;

  /**
   * Constructor.
   * @param seqType sequence type
   */
  public ExprType(final SeqType seqType) {
    assign(seqType);
  }

  /**
   * Copy constructor.
   * @param et expression type
   */
  public ExprType(final ExprType et) {
    seqType = et.seqType;
    size = et.size;
    data = et.data;
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
   * Returns the data reference.
   * @return data reference
   */
  public Data data() {
    return data;
  }

  /**
   * If available, assigns the specified data reference.
   * @param dt data reference (can be {@code null})
   */
  public void data(final Data dt) {
    if(dt != null) data = dt;
  }

  /**
   * If available, determines and assigns the common data reference of the specified expressions.
   * @param exprs expressions ({@code null} references and
   *   expressions yielding empty sequences are ignored)
   */
  public void data(final Expr... exprs) {
    Data dt = null;
    for(final Expr expr : exprs) {
      if(expr != null && !expr.seqType().zero()) {
        final Data d = expr.data();
        if(dt == null) dt = d;
        if(dt == null || dt != d) return;
      }
    }
    data(dt);
  }

  /**
   * If available, assigns the data reference of the specified expression.
   * @param expr expression (can be {@code null})
   */
  public void data(final Expr expr) {
    if(expr != null) data(expr.data());
  }

  /**
   * Assigns type information of the specified expression.
   * @param expr expression
   */
  public void assign(final Expr expr) {
    asg(expr.seqType(), expr.size());
    data(expr);
  }

  /**
   * Assigns the specified sequence type and updates the result size.
   * @param st sequence type
   * @return self reference
   */
  public ExprType assign(final SeqType st) {
    if(st != seqType) asg(st, st.zero() ? 0 : st.one() ? 1 : -1);
    return this;
  }

  /**
   * Assigns the specified type.
   * @param type type
   */
  public void assign(final Type type) {
    seqType = SeqType.get(type, seqType.occ);
  }

  /**
   * Assigns the specified type and occurrence indicator and updates the result size.
   * @param type type
   * @param occ occurrence indicator
   * @return self reference
   */
  public ExprType assign(final Type type, final Occ occ) {
    return assign(SeqType.get(type, occ));
  }

  /**
   * Assigns the specified occurrence indicator and updates the result size.
   * @param occ occurrence indicator
   * @return self reference
   */
  public ExprType assign(final Occ occ) {
    return assign(seqType.with(occ));
  }

  /**
   * Assigns the specified sequence type and result size.
   * The occurrence indicator of the sequence type is ignored if the result size is known.
   * @param st sequence type
   * @param sz result size ({@code -1} if unknown)
   * @return self reference
   */
  public ExprType assign(final SeqType st, final long sz) {
    if(sz >= 0) {
      asg(st.with(sz == 0 ? Occ.ZERO : sz == 1 ? Occ.EXACTLY_ONE : Occ.ONE_OR_MORE), sz);
    } else {
      assign(st);
    }
    return this;
  }

  /**
   * Assigns the specified type and result size.
   * The specified occurrence indicator is ignored if the result size is known.
   * @param st sequence type (the occurrence indicator is ignored)
   * @param occ occurrence indicator
   * @param sz result size ({@code -1} if unknown)
   * @return self reference
   */
  public ExprType assign(final SeqType st, final Occ occ, final long sz) {
    return assign(sz >= 0 ? st : st.with(occ), sz);
  }

  /**
   * Assigns the type, the result size, based on the specified min/max occurrences, and
   * the data reference.
   * @param expr expression
   * @param minMax min/max values (min: 0 or more, max: -1 or more)
   */
  public void assign(final Expr expr, final long[] minMax) {
    final long min = minMax[0], max = minMax[1], sz = min == max ? min : -1;
    final Occ occ = min > 0 ? Occ.ONE_OR_MORE : max == 1 ? Occ.ZERO_OR_ONE : Occ.ZERO_OR_MORE;
    assign(expr.seqType(), occ, sz);
    data(expr);
  }

  /**
   * Refines the type with type information from the specified expression.
   * @param expr expression with original type
   */
  public void refine(final Expr expr) {
    final SeqType st = seqType, est = expr.seqType();
    if(!st.instanceOf(est)) {
      final long es = expr.size();
      final SeqType ist = st.intersect(est);
      if(ist != null) {
        asg(ist, es == size || size == -1 ? es : es == -1 ? size : -1);
      } else {
        Util.errln("Expression type % cannot be refined for % (%): %",
            st, Util.className(expr), est, expr);
      }
    }
    data(expr);
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
