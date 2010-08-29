package org.basex.query.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
  /** Non-grouping variables. */
  final Var[] ngv;
  /** Group Partitioning. */
  final ArrayList<GroupNode> partitions;
  /** Resulting Sequence for non grouping variables. */
  final ArrayList<HashMap<Var, ItemIter>> items;
  /** HashValue, Position. */
  private final HashMap<Integer, IntList> hashes =
    new HashMap<Integer, IntList>();
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
    items = new ArrayList<HashMap<Var, ItemIter>>();
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
    final Item[] its = new Item[gv.length];
    for(int i = 0; i < gv.length; ++i) {
      final Value val = ctx.vars.get(gv[i]).value(ctx);
      if(val.item()) its[i] = (Item) val;
      else Err.or(input, QueryText.XGRP);
    }
    boolean found = false;
    int p = 0;
    final GroupNode cand = new GroupNode(its, gv.length);
    final Integer chash = cand.hashCode();

    if(hashes.containsKey(chash)) {
      final IntList ps = hashes.get(cand.hash);
      for(final int pp : ps.toArray()) {
        if(cand.eq(partitions.get(pp))) {
          found = true;
          p = pp;
          break;
        }
      }
    }
     if(!found) {
      if(order != null) order.add(ctx);
      p = partitions.size();
      partitions.add(cand);
      final IntList pos = hashes.get(chash) != null ?
          hashes.get(chash)
          : new IntList(8);
      pos.add(p);
      hashes.put(chash, pos);
    }
    addNonGrpIts(ctx, p);
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

    if(!cachedVars) cacheVars(ctx);
    if(items.size() <= p) items.add(new HashMap<Var, ItemIter>());
    HashMap<Var, ItemIter> sq = items.get(p);

    for(int i = 0; i < ngv.length; ++i) {
      if(sq == null) sq = new HashMap<Var, ItemIter>();
      if(sq.get(ngv[i]) == null) sq.put(ngv[i], new ItemIter());
      final Value v = ngv[i].value(ctx);
      if(v.item()) {
        sq.get(ngv[i]).add((Item) v);
      }
    }
  }

  /**
   * Caches the non grouping variables to avoid calls to vars.get.
   * @param ctx query context
   */
  private void cacheVars(final QueryContext ctx) {
    for(int i = 0; i < ngv.length; ++i) {
      ngv[i] = ctx.vars.get(ngv[i]);
    }
    cachedVars = true;

  }
  /**
   * GroupNode defines one valid partitioning setting.
   * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
   * @author Michael Seiferle
   */
  static final class GroupNode {
    /** List of grouping var items. */
    final Item[] its;
    /** Length of grouping variables. */
    final int varlen;
    /** Hashes for the group representative values.
     *  N.B. long instead of int */
    final int hash;

    /**
     * Creates a group node.
     * @param is grouping var items
     * @param vl # of grouping vars
     */
    GroupNode(final Item[] is, final int vl) {
      its = is;
      varlen = vl;

      final long[] hhs = new long[is.length];
      for(int i = 0; i < varlen; ++i) {
        if(is[i].empty()) {
          // Add long.max_value to denote empty sequence in item
          hhs[i] = Long.MAX_VALUE;
        } else {
          hhs[i] = is[i].hashCode();
        }
      }
      hash = java.util.Arrays.hashCode(hhs);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    /* for debugging (should be removed later) */
    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(" ");
      sb.append(" with grouping var ");
      sb.append(Arrays.toString(its));
      return sb.toString();
    }

    /**
     * Checks the nodes for equality.
     * @param c second group node
     * @return result of check
     * @throws QueryException query exception
     */
    boolean eq(final GroupNode c) throws QueryException {
      if(its.length != c.its.length || varlen != c.its.length) return false;
      for(int i = 0; i < its.length; ++i) {
        if(!its[i].equiv(null, c.its[i])) return false;
      }
      return true;
    }
  }

}
