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
import org.basex.util.*;

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
   * @param ic index context
   */
  RangeAccess(final InputInfo ii, final NumericRange t, final IndexContext ic) {
    super(ic, ii);
    ind = t;
  }

  @Override
  public AxisIter iter(final QueryContext ctx) {
    final Data data = ictx.data;
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
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DATA, ictx.data.meta.name,
        MIN, ind.min, MAX, ind.max, TYP, ind.type));
  }

  @Override
  public String toString() {
    return new TokenBuilder(DB).add(':').
      add(ind.type().toString().toLowerCase(Locale.ENGLISH)).add("-range(").
      addExt(ind.min).add(SEP).addExt(ind.max).add(')').toString();
  }
}
