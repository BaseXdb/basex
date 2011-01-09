package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Abstract container for order by clauses.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public abstract class OrderBy extends ParseExpr {
  /** Ascending/descending order. */
  boolean desc;
  /** Order for empty expressions. */
  boolean lst;

  /**
   * Empty constructor for stable sorting.
   * @param ii input info
   */
  protected OrderBy(final InputInfo ii) {
    super(ii);
  }

  /**
   * Initializes the sequence builder.
   * @param s expected number of entries
   */
  abstract void init(final int s);

  /**
   * Adds an item to be sorted.
   * @param ctx query context
   * @throws QueryException query exception
   */
  abstract void add(final QueryContext ctx) throws QueryException;

  /**
   * Returns the specified item.
   * @param i item index
   * @return item
   */
  abstract Item get(final int i);

  @Override
  public OrderBy remove(final Var v) {
    return this;
  }
}
