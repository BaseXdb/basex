package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.util.HashMap;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.query.util.ValueList;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Implementation of the group by clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class Group extends ParseExpr {
  /** Group by specification. */
  final Var[] groupby;
  /** Grouping partition. **/
  GroupPartition gp;

  /**
   * Constructor.
   * @param ii input info
   * @param gb group by expression
   */
  public Group(final InputInfo ii, final Var[] gb) {
    super(ii);
    groupby = gb;
  }

  /**
   * Initializes the grouping partition.
   * @param fl ForLet
   * @param ob order by spec
   */
  public void initgroup(final ForLet[] fl, final Order ob) {
    final Var[] vs = new Var[groupby.length];
    final Var[] fs = new Var[fl.length];
    for(int i = 0; i < groupby.length; ++i) vs[i] = groupby[i];
    for(int i = 0; i < fl.length; ++i) fs[i] = fl[i].var;
    gp = new GroupPartition(vs, fs, ob, input);

  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Var g : groupby) g.comp(ctx);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() { // group is blocking => no iterator
      @Override
      public Item next() {
        Main.notexpected(this);
        return null;
      }
    };
  }

  @Override
  public boolean uses(final Use use) {
    for(final Var v : groupby) if(v.uses(use)) return true;
    return false;
  }

  @Override
  public boolean uses(final Var v) {
    for(final Var g : groupby) if(v.eq(g)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Var g : groupby) if(g.eq(v)) return false;
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int o = 0; o != groupby.length; ++o) groupby[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(" " + GROUP + " " + BY + " ").
    add(groupby, ", ").toString();
  }

  /**
   * Adds the current context to the grouping partition.
   * @param ctx context
   * @throws QueryException ex
   */
  public void add(final QueryContext ctx) throws QueryException {
    gp.add(ctx);
  }

  /**
   * Returns grouped variables.
   * @param ctx context.
   * @param ret return expression
   * @return iterator on the result set
   * @throws QueryException on error.
   */
  Iter ret(final QueryContext ctx, final Expr ret) throws QueryException {
    final ItemIter ir = new ItemIter();
    final ValueList vl = new ValueList();
    final Var[] pgvars = new Var[gp.gv.length];
    final Var[] pgngvar = new Var[gp.ngv.length];
    for(int j = 0; j < gp.gv.length; ++j)
      pgvars[j] = ctx.vars.get(gp.gv[j]);
    for(int j = 0; j < gp.ngv.length; ++j)
      pgngvar[j] = ctx.vars.get(gp.ngv[j]);

    for(int i = 0; i < gp.partitions.size(); ++i) { // bind grouping var
      collectValues(ctx, pgvars, pgngvar, i);

      if(gp.order != null) vl.add(ctx.iter(ret).finish());
      else ir.add(ctx.iter(ret));
    }
    if(gp.order != null) {
      gp.order.vl = vl;
      return ctx.iter(gp.order);
    }
    return ir;
  }

  /**
   * Extracts the current variable binding.
   * @param ctx QueryContext
   * @param pgvars grouping variables
   * @param pgngvar non grouping variables
   * @param i position
   * @throws QueryException exception
   */
  private void collectValues(final QueryContext ctx, final Var[] pgvars,
      final Var[] pgngvar, final int i) throws QueryException {
    final HashMap<Var, ItemIter> ngvars = gp.items.get(i);
    final GroupNode gn = gp.partitions.get(i);
    for(int j = 0; j < gp.gv.length; ++j)
      pgvars[j].bind(gn.its[j], ctx);

    for(int j = 0; j < gp.ngv.length; ++j) {
      final ItemIter its = ngvars.get(gp.ngv[j]);
      if(its != null) pgngvar[j].bind(its.finish(), ctx);
      else pgngvar[j].bind(Empty.SEQ, ctx);
    }
  }
}
