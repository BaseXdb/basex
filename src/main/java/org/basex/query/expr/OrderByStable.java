package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.SeqType;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Stable order specifier.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public Expr comp(final QueryContext ctx) throws QueryException {
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
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, DIR);
  }

  @Override
  public String toString() {
    return "";
  }
}
