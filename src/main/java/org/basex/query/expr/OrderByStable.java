package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Stable order specifier.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  @SuppressWarnings("unused")
  public Expr comp(final QueryContext ctx) throws QueryException {
    type = SeqType.ITEM_ZM;
    return this;
  }

  @Override
  void init(final int s) { }

  @Override
  void add(final QueryContext ctx) { }

  @Override
  Item get(final int i) {
    return Itr.get(i);
  }

  @Override
  public boolean uses(final Use u) {
    return false;
  }

  @Override
  public boolean uses(final Var v) {
    return false;
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
