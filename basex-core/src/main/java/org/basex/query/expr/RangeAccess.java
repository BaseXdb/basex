package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This index class retrieves range values from the index.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RangeAccess extends IndexAccess {
  /** Index type. */
  private final NumericRange index;

  /**
   * Constructor.
   * @param info input info
   * @param index index reference
   * @param ictx index context
   */
  RangeAccess(final InputInfo info, final NumericRange index, final IndexContext ictx) {
    super(ictx, info);
    this.index = index;
  }

  @Override
  public AxisIter iter(final QueryContext qc) {
    final byte kind = index.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;

    return new AxisIter() {
      final IndexIterator it = ictx.data.iter(index);
      @Override
      public ANode next() {
        return it.more() ? new DBNode(ictx.data, it.pre(), kind) : null;
      }
    };
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new RangeAccess(info, index, ictx);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DATA, ictx.data.meta.name, MIN, index.min, MAX, index.max,
        TYPE, index.type()));
  }

  @Override
  public String toString() {
    return new TokenBuilder(DB_PREFIX).add(':').
      add(index.type().toString().toLowerCase(Locale.ENGLISH)).add("-range(").
      addExt(index.min).add(SEP).addExt(index.max).add(')').toString();
  }
}
