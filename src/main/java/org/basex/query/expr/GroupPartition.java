package org.basex.query.expr;

import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.util.ValueList;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.hash.IntMap;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.IntList;
import org.basex.util.list.ObjList;

/**
 * Stores the grouping for a group by clause.
 *
 * @author BaseX Team 2005-11, BSD License
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
  private final ObjList<GroupNode> part = new ObjList<GroupNode>();
  /** Resulting sequence for non-grouping variables. */
  private final ObjList<ItemCache[]> items;
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
      for(final Var g : gv) ng &= !v.is(g);
      if(ng) ngv[i++] = v;
    }

    items = ngv.length != 0 ? new ObjList<ItemCache[]>() : null;
    input = ii;
  }

  /**
   * Calculates the number of unique non-grouping variables.
   * This is #ForLet - #GroupBy
   * <p>Returns 0 for <br />
   * {@code for $a in 1 for $a in 2 group by $a return $a}</p>
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
      for(final Var v : fls) f |= v.is(g);
      if(!f) GVARNOTDEFINED.thrw(g.input, g);
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

    final GroupNode gn = new GroupNode(input, vals);
    final int h = gn.hash();
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

    // adds the current non-grouping variable bindings to the p-th partition.
    if(p == items.size()) items.add(new ItemCache[ngl]);
    final ItemCache[] sq = items.get(p);

    for(int i = 0; i < ngl; ++i) {
      ItemCache ic = sq[i];
      final Value result = ngv[i].value(ctx);
      if(ic == null) {
        ic = new ItemCache();
        sq[i] = ic;
      }
      ic.add(result);
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
   * @param ctx query context
   * @param ret return expression
   * @param ks key list
   * @param vs value list
   * @return iterator on the result set
   * @throws QueryException query exception
   */
  Iter ret(final QueryContext ctx, final Expr ret, final ObjList<Item[]> ks,
      final ValueList vs) throws QueryException {
    final ItemCache ic = new ItemCache();
    if(pggv == null) cacheRet(ctx);

    for(int i = 0; i < part.size(); ++i) {
      final GroupNode gn = part.get(i);
      for(int j = 0; j < pggv.length; ++j) pggv[j].bind(gn.vals[j], ctx);

      if(items != null) {
        final ItemCache[] ii = items.get(i);
        for(int j = 0; j < ii.length; ++j) {
          pgngv[j].bind(ii[j].finish(), ctx);
        }
      }
      if(order != null) {
        order.add(ctx, ret, ks, vs);
      } else ic.add(ctx.value(ret));
    }
    return order != null ? ctx.iter(order.set(ks, vs)) : ic;
  }

  /**
   * Caches the return variables.
   * @param ctx query context.
   */
  private void cacheRet(final QueryContext ctx) {
    pggv = new Var[gv.length];
    pgngv = new Var[ngv.length];
    for(int j = 0; j < gv.length; ++j) pggv[j] = ctx.vars.get(gv[j]);
    for(int j = 0; j < ngv.length; ++j) pgngv[j] = ctx.vars.get(ngv[j]);
  }
}
