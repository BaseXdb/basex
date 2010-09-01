package org.basex.query.expr;

import java.util.ArrayList;
import java.util.HashSet;

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
 *
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
  /** ordering among groups. Set to false if order by contains
  at leasst one non grouping varibale. */
  final boolean among;
  /** flag indicates variable caching. */
  private boolean cachedVars;

  /**
   * Sets up an empty partitioning.
   * Sets up the ordering scheme.
   * @param gvs grouping vars
   * @param fls ForLet Variables
   * @param ob OrderBy specifier
   * @param ii input info
   */
  GroupPartition(final Var[] gvs, final Var[] fls, final Order ob,
      final InputInfo ii) {

    gv = gvs;
    cgv = gvs;
    final int ns = ngvSize(gvs, fls);
    ngv = new Var[ns];
    int i = 0;
    order = ob;

    for(final Var v : fls) {
      boolean skip = false;
      for(final Var g : gv) {
        if(v.eq(g)) {
          skip = true;
          break;
        }
      }
      if(skip) continue;
      
      ngv[i++] = v;
    }
    if(order != null) {
      boolean[] ams = new boolean[order.ob.length - 1];
      for(int ol = 0; ol < ams.length; ++ol) {
        for(final Var g : gv) {
          if(order.ob[ol].uses(g)) ams[ol] = true;
        }
      }
      boolean am = true;
      for(int j = 0; j < ams.length; ++j)
        am &= ams[j];
      among = am;
    } else among = true;
    
    partitions = new ArrayList<GroupNode>();
    items = ngv.length != 0 ? new ArrayList<ItemIter[]>() : null;
    input = ii;
  }

  /**
   * Calculates the number of unique non grouping variables.
   * This is #ForLet - #GroupBy
   * <p>Returns 0 for <br />
   * <code>for $a in 1 for $a in 2 group by $a return $a</code></p>
   * @param gvs grouping vars
   * @param fls forlet vars
   * @return size of non grouping variables container.
   */
  private int ngvSize(final Var[] gvs, final Var[] fls) {
    final HashSet<String> flshelp = new HashSet<String>();
    final HashSet<String> glshelp = new HashSet<String>();

    for(final Var v : fls)
      flshelp.add(v.toString());
    for(final Var g : gvs)
      glshelp.add(g.toString());
    final int vl = flshelp.size();
    final int gl = glshelp.size();
    return vl - gl;
  }

  /**
   * Adds the current grouping variable binding to the partitioning scheme.
   * Then the resulting non grouping variable item sequence is built for each
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

    final Item[] its = new Item[gv.length];
    for(int i = 0; i < gv.length; ++i) {
      final Value val = cgv[i].value(ctx);
      if(val.item()) its[i] = (Item) val;
      else Err.or(input, QueryText.XGRP);
    }
    int p = -1;
    final GroupNode cand = new GroupNode(its);
    final int chash = cand.hashCode();
    IntList ps = hashes.get(chash);
    if(ps != null) {
      for(int i = 0; i < ps.size(); ++i) {
        final int pp = ps.get(i);
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
        pos = new IntList(1);
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
    final ItemIter[] sq = items.get(p);

    for(int i = 0; i < ngv.length; ++i) {
      ItemIter ir = sq[i];
      if(ir == null) {
        ir = new ItemIter();
        sq[i] = ir;
      } else if(!among && order.uses(ngv[i])) Err.or(input, QueryText.XPSORT);
      
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
