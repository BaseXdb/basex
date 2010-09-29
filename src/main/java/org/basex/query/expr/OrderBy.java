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
public class OrderBy extends ParseExpr {
  /** Ascending/descending order. */
  boolean desc;
  /** Order for empty expressions. */
  boolean lst;

  /**
   * Empty constructor for stable sorting.
   * @param ii input info
   */
  public OrderBy(final InputInfo ii) {
    super(ii);
  }

  @Override
  @SuppressWarnings("unused")
  public Expr comp(final QueryContext ctx) throws QueryException {
    type = SeqType.ITEM_ZM;
    return this;
  }

  /**
   * Initializes the sequence builder.
   */
  void init() { }

  /**
   * Adds an item to be sorted.
   * @param ctx query context
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  void add(final QueryContext ctx) throws QueryException { }

  /**
   * Returns the specified item.
   * @param i item index
   * @return item
   */
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
  public OrderBy remove(final Var v) {
    return this;
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
