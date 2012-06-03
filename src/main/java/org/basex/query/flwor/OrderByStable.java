package org.basex.query.flwor;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Stable order specifier.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class OrderByStable extends OrderBy {
  /**
   * Empty constructor for stable sorting.
   * @param ii input info
   */
  public OrderByStable(final InputInfo ii) {
    super(ii);
  }

  @Override
  public void checkUp() {
  }

  @Override
  public OrderByStable analyze(final QueryContext ctx) throws QueryException {
    return this;
  }

  @Override
  public OrderByStable compile(final QueryContext ctx) throws QueryException {
    type = SeqType.ITEM_ZM;
    return this;
  }

  @Override
  Item key(final QueryContext ctx, final int i) {
    return Int.get(i);
  }

  @Override
  public boolean uses(final Use u) {
    return false;
  }

  @Override
  public int count(final Var v) {
    return 0;
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem());
  }

  @Override
  public String toString() {
    return "";
  }
}
