package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.SeqType;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Single group expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class GroupBy extends ParseExpr {
  /** Grouping expression. */
  Var expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  public GroupBy(final InputInfo ii, final Var e) {
    super(ii);
    new SeqIter();
    expr = e;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // [MS] might be moved to parser?
    if(ctx.vars.get(expr) == null) Err.or(input, GVARNOTDEFINED, expr);
    return this;
  }

  // [MS] moved to GroupPartition
//  /**
//   * Adds an item for the membership check.
//   * @param it item
//   * @throws QueryException query exception * [MS] dynamic error is raised
//   *           [err:XQDY0095] iff grouping var contains more than 1 value.
//   */
//  void adds(final Item it) throws QueryException {
//    if(seq != null) {
//      if(it.size(null) > 1) Err.or(input, XGRP);
//      seq.add(it);
//      return;
//    }
//    throw new QueryException("[MS] check ITEM for conformance"); // [MS]
//
//  }

//  /**
//   * Resets the built sequence.
//   */
//  void finish() {
//    seq = new SeqIter();
//  }

//  /**
//   * Returns the specified item.
//   * @param i item index
//   * @return item
//   */
//  Item item(final int i) {
//    return seq.item[i];
//  }

  @Override
  public boolean uses(final Use use, final QueryContext ctx) {
    return expr.uses(use, ctx);
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    return !v.eq(expr);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return expr.returned(ctx);
  }

  @Override
  public String color() {
    return "66FF66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    expr.plan(ser);
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
