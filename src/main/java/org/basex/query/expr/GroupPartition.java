package org.basex.query.expr;

import java.util.ArrayList;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.IntList;
import org.basex.util.IntMap;

/**
 * Stores the grouping for a group by clause.
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
final class GroupPartition {
  /** Input information. */
  final InputInfo input;
  /** Grouping variables. */
  final Var[] gv;
  /** Cached Grouping variables. Needed to allow return iteration. */
  final Var[] cgv;
  /** Non-grouping variables. */
  final Var[] ngv;
  /** Group Partitioning. */
  final ArrayList<GroupNode> partitions;
  /** Resulting Sequence for non grouping variables. */
  final ArrayList<ItemIter[]> items;
  /** HashValue, Position (with overflow bucket). */
  private final IntMap<IntList> hashes = new IntMap<IntList>();
  /** Order by specifier. */
  final Order order;
  /** ordering among groups. *TODO* */
  final boolean among;
  /** flag indicates variable caching. */
  private boolean cachedVars;

  /**
   * Sets up an empty partitioning.
   * Sets up the ordering scheme.
   * [MS] *TODO* Order by so far only orders
   * by grouping variables, non grouping variables are silently ignored
   * in most cases where an XPTY0004 (no sequences allowed as sort keys) should
   * be thrown.
   * @param gvs grouping vars
   * @param fls ForLet Variables
   * @param ob OrderBy specifier.
   * @param ii input info
   */
  GroupPartition(final Var[] gvs, final Var[] fls, final Order ob,
      final InputInfo ii) {

    gv = gvs;
    cgv = gvs;
    ngv = new Var[fls.length - gv.length];
    int i = 0;
    for(final Var v : fls) {
      boolean skip = false;
      for(final Var g : gv)
        if(v.eq(g)) {
          skip = true;
          break;
        }
      if(skip) continue;
      ngv[i++] = v;
    }
    partitions = new ArrayList<GroupNode>();
    
    items = ngv.length != 0 ? new ArrayList<ItemIter[]>() : null;
    order = ob;
    among = false;
    input = ii;
  }

  /**
   * Adds the current grouping variable binding to the partitioning scheme.
   * Then the resulting non grouping variable item sequence is built for each
   * candidate.
   * Searches the known partition hashes {@link GroupPartition#hashes} for
   * potential matches and checks them for equivalence.
   * The GroupNode candidate is ignored if it exists otherwise added to the
   * partitioning scheme.
   *
   * @param ctx QueryContext
   * @throws QueryException exception
   */
  void add(final QueryContext ctx) throws QueryException  {
    if(!cachedVars) cacheVars(ctx);

    final Item[] its = new Item[gv.length];
    for(int i = 0; i < gv.length; ++i) {
      final Value val = cgv[i].value(ctx);
      if(val.item()) its[i] = (Item) val;
      else Err.or(input, QueryText.XGRP);
    }
    int p = -1;
    final GroupNode cand = new GroupNode(its);
    final int chash = cand.hashCode();
    IntList ps;
    if(null != (ps = hashes.get(chash))) {
      for(int i = 0; i <= ps.size(); ++i) {
        final int pp  = ps.get(i);
        if(cand.eq(partitions.get(pp))) {
          p = pp;
          break;
        }
      }
    }
    if(p < 0) {
      if(order != null) order.add(ctx);
      p = partitions.size();
      partitions.add(cand);
      
      IntList pos = hashes.get(chash);
      if(pos == null) {
        pos = new IntList(2);
        hashes.add(chash, pos);
      }
      pos.add(p);
    }
    if(ngv.length != 0) addNonGrpIts(ctx, p);
  }

  /**
   * Adds the current non grouping variable bindings to the
   * {@code p-th} partition.
   * @param ctx query context
   * @param p partition position
   * @throws QueryException query exception
   */
  private void addNonGrpIts(final QueryContext ctx, final int p)
      throws QueryException {
    
    if(p == items.size()) items.add(new ItemIter[ngv.length]);
    ItemIter[] sq = items.get(p);

    for(int i = 0; i < ngv.length; ++i) {
      ItemIter ir = sq[i];
      if(ir == null) {
        ir = new ItemIter();
        sq[i] = ir;
      }
        ir.add(ngv[i].iter(ctx));
    }
  }

  /**
   * Caches the variables to avoid calls to vars.get.
   * @param ctx query context
   */
  private void cacheVars(final QueryContext ctx) {
    for(int i = 0; i < ngv.length; ++i) ngv[i] = ctx.vars.get(ngv[i]);
    
    for(int i = 0; i < gv.length; ++i) cgv[i] = ctx.vars.get(gv[i]);
    cachedVars = true;

  }

}
