package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This index class retrieves range values from the index.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RangeAccess extends IndexAccess {
  /** Index type. */
  final NumericRange ind;

  /**
   * Constructor.
   * @param ii input info
   * @param t index reference
   * @param d data reference
   * @param it flag for iterative evaluation
   */
  RangeAccess(final InputInfo ii, final NumericRange t, final Data d, final boolean it) {
    super(d, it, ii);
    ind = t;
  }

  @Override
  public AxisIter iter(final QueryContext ctx) {
    final byte kind = ind.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;

    return new AxisIter() {
      final IndexIterator it = data.iter(ind);
      @Override
      public ANode next() {
        return it.more() ? new DBNode(data, it.next(), kind) : null;
      }
    };
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new RangeAccess(info, ind, data, iterable);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DATA, data.meta.name,
        MIN, ind.min, MAX, ind.max, TYP, ind.type));
  }

  @Override
  public String toString() {
    return new TokenBuilder(DB).add(':').
      add(ind.type().toString().toLowerCase(Locale.ENGLISH)).add("-range(").
      addExt(ind.min).add(SEP).addExt(ind.max).add(')').toString();
  }
}
