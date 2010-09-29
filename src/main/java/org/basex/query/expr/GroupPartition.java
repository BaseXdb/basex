package org.basex.query.expr;

import java.util.ArrayList;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import static org.basex.query.util.Err.*;
import org.basex.query.util.ValueList;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.IntList;
import org.basex.util.IntMap;
import org.basex.util.TokenSet;

/**
 * Stores the grouping for a group by clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
final class GroupPartition {
  /** Input information. */
  private final InputInfo input;
  /** Order by specifier. */
  private final Order order;

  /** Grouping variables. */
  private final Var[] gv;
  /** Non-grouping variables. */
  private final Var[] ngv;
  /** Cached post-grouping grouping variables. */
  private Var[] pggv;
  /** Cached post-grouping non-grouping variables. */
  private Var[] pgngv;

  /** Group partitioning. */
  private final ArrayList<GroupNode> part = new ArrayList<GroupNode>();
  /** Resulting sequence for non-grouping variables. */
  private final ArrayList<ItemIter[]> items;
  /** HashValue, position (with overflow bucket). */
  private final IntMap<IntList> hashes = new IntMap<IntList>();

  /** flag indicates variable caching. */
  private boolean cachedVars;

  /**
   * Sets up an empty partitioning.
   * Sets up the ordering scheme.
   * @param gvs grouping variables
   * @param fls ForLet variables
   * @param ob order by specifier
   * @param ii input info
   * @throws QueryException exception
   */
  GroupPartition(final Var[] gvs, final Var[] fls, final Order ob,
      final InputInfo ii) throws QueryException {

    gv = gvs;
    ngv = new Var[ngvSize(gvs, fls)];
    order = ob;

    int i = 0;
    for(final Var v : fls) {
      boolean ng = true;
      for(final Var g : gv) ng &= !v.eq(g);
      if(ng) ngv[i++] = v;
    }

    items = ngv.length != 0 ? new ArrayList<ItemIter[]>() : null;
    input = ii;
  }

  /**
   * Calculates the number of unique non-grouping variables.
   * This is #ForLet - #GroupBy
   * <p>Returns 0 for <br />
   * <code>for $a in 1 for $a in 2 group by $a return $a</code></p>
   * @param gvs grouping vars
   * @param fls forlet vars
   * @return size of non-grouping variables container.
   * @throws QueryException var not found.
   */
  private int ngvSize(final Var[] gvs, final Var[] fls) throws QueryException {
    final TokenSet fc = new TokenSet();
    final TokenSet gc = new TokenSet();

    for(final Var v : fls) fc.add(v.name.atom());
    for(final Var g : gvs) gc.add(g.name.atom());
    for(final Var g : gvs) {
      boolean f = false;
      for(final Var v : fls) f |= v.eq(g);
      if(!f) GVARNOTDEFINED.thrw(null, g);
    }
    return fc.size() - gc.size();
  }

  /**
   * Adds the current grouping variable binding to the partitioning scheme.
   * Then the resulting non-grouping variable item sequence is built for each
   * candidate.
   * Searches the known partition hashes {@link GroupPartition#hashes} for
   * potential matches and checks them for equivalence.
   * The GroupNode candidate is ignored if it exists otherwise added to the
   * partitioning scheme.
   * @param ctx QueryContext
   * @throws QueryException exception
   */
  void add(final QueryContext ctx) throws QueryException  {
    if(!cachedVars) cacheVars(ctx);

    final int gl = gv.length;
    final Value[] vals = new Value[gl];
    for(int i = 0; i < gl; ++i) {
      final Value val = gv[i].value(ctx);
      if(val.size() > 1) XGRP.thrw(input);
      vals[i] = val;
    }

    final GroupNode gn = new GroupNode(vals);
    final int h = gn.hashCode();
    final IntList ps = hashes.get(h);
    int p = -1;
    if(ps != null) {
      for(int i = 0; i < ps.size(); ++i) {
        final int pp = ps.get(i);
        if(gn.eq(part.get(pp))) {
          p = pp;
          break;
        }
      }
    }
    if(p < 0) {
      p = part.size();
      part.add(gn);

      IntList pos = hashes.get(h);
      if(pos == null) {
        pos = new IntList(1);
        hashes.add(h, pos);
      }
      pos.add(p);
    }

    // no non-grouping variables exist
    final int ngl = ngv.length;
    if(ngl == 0) return;

    // Adds the current non-grouping variable bindings to the p-th partition.
    if(p == items.size()) items.add(new ItemIter[ngl]);
    final ItemIter[] sq = items.get(p);

    for(int i = 0; i < ngl; ++i) {
      ItemIter ir = sq[i];
      final Iter iter = ngv[i].iter(ctx);
      if(ir == null) {
        ir = new ItemIter();
        sq[i] = ir;
      }
      ir.add(iter);
    }
  }

  /**
   * Caches the variables to avoid calls to vars.get.
   * @param ctx query context
   */
  private void cacheVars(final QueryContext ctx) {
    for(int i = 0; i < ngv.length; ++i) ngv[i] = ctx.vars.get(ngv[i]);
    for(int i = 0; i < gv.length; ++i) gv[i] = ctx.vars.get(gv[i]);
    cachedVars = true;
  }

  /**
   * Returns grouped variables.
   * @param ctx context
   * @param ret return expression
   * @return iterator on the result set
   * @throws QueryException query exception
   */
  Iter ret(final QueryContext ctx, final Expr ret) throws QueryException {
    final ItemIter ir = new ItemIter();
    final ValueList vl = new ValueList();
    if(pggv == null) cacheRet(ctx);
    if(order != null) order.init(vl);

    for(int i = 0; i < part.size(); ++i) {
      final GroupNode gn = part.get(i);
      for(int j = 0; j < pggv.length; ++j) pggv[j].bind(gn.vals[j], ctx);

      if(items != null) {
        final ItemIter[] ii = items.get(i);
        for(int j = 0; j < ii.length; ++j) {
          pgngv[j].bind(ii[j].finish(), ctx);
        }
      }
      if(order != null) {
        order.add(ctx);
        vl.add(ret.value(ctx));
      } else ir.add(ctx.iter(ret));
    }
    return order != null ? ctx.iter(order) : ir;
  }

  /**
   * Caches the return variables.
   * @param ctx query context.
   */
  private void cacheRet(final QueryContext ctx) {
    // [MS] wondering why the references differ... but, hm, it's true
    pggv = new Var[gv.length];
    pgngv = new Var[ngv.length];
    for(int j = 0; j < gv.length; ++j) pggv[j] = ctx.vars.get(gv[j]);
    for(int j = 0; j < ngv.length; ++j) pgngv[j] = ctx.vars.get(ngv[j]);
  }
}
