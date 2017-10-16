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
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This index class retrieves range values from the index.
 *
 * @author BaseX Team 2005-17, BSD License
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
    seqType = SeqType.NOD_ZM;
  }

  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final IndexType type = index.type();
    final Data data = ictx.data(qc, type, info);
    return new DBNodeIter(data) {
      final byte kind = type == IndexType.TEXT ? Data.TEXT : Data.ATTR;
      final IndexIterator ii = data.iter(index);
      @Override
      public DBNode next() {
        return ii.more() ? new DBNode(data, ii.pre(), kind) : null;
      }
    };
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new RangeAccess(info, index, ictx);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof RangeAccess && index.equals(((RangeAccess) obj).index) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, index.min, MAX, index.max, IDX, index.type()), ictx.expr());
  }

  @Override
  public String toString() {
    final Function func = index.type() == IndexType.TEXT ? Function._DB_TEXT_RANGE :
      Function._DB_ATTRIBUTE_RANGE;
    return func.toString(ictx.expr(), Dbl.get(index.min), Dbl.get(index.max));
  }
}
