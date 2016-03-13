package org.basex.query.expr.index;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This index class retrieves range values from the index.
 *
 * @author BaseX Team 2005-16, BSD License
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
  public RangeAccess(final InputInfo info, final NumericRange index, final IndexContext ictx) {
    super(ictx, info);
    this.index = index;
  }

  @Override
  public BasicNodeIter iter(final QueryContext qc) {
    final byte kind = index.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;

    return new BasicNodeIter() {
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
    final TokenBuilder tb = new TokenBuilder();
    return tb.addExt((index.type() == IndexType.TEXT ? Function._DB_TEXT_RANGE :
      Function._DB_ATTRIBUTE_RANGE).get(null, info, Str.get(ictx.data.meta.name),
        Dbl.get(index.min), Dbl.get(index.max))).toString();
  }
}
