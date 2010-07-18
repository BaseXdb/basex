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

/**
 * Single group expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class GroupBy extends Expr {
  /** Grouping expression. */
  Expr expr;

  /**
   * Constructor.
   * @param e expression
   */
  public GroupBy(final Expr e) {
    //[MS] change type of expr to varcall.
    assert e instanceof VarCall : "Grouping Argument must be a VarCall";
    new SeqIter();
    expr = e;
  }
  /**
   * Returns the Grouping var.
   * @return grouping var
   */
  Var getVar() {
    return ((VarCall) expr).var;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(null == ctx.vars.get(getVar())) Err.or(GVARNOTDEFINED, getVar());
    expr = expr.comp(ctx);
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
//      if(it.size(null) > 1) Err.or(XGRP);
//      seq.add(it);
//      return;
//    }
//    throw new QueryException("Todo check ITEM for conformance"); // [MS]
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
  public GroupBy remove(final Var v) {
    if(expr != null) expr = expr.remove(v);
    return this;
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
