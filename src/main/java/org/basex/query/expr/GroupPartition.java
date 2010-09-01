package org.basex.query.expr;

import java.util.ArrayList;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
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
  at least one non grouping variable. */
  final boolean among;
  /** flag indicates variable caching. */
  private boolean cachedVars;
  /** flag indicates return variable caching.*/
  private boolean cachedRet;
  /** Cached postgrouping grouping variables. */
  private Var[] pgvars;
  /** Cached postgrouping non grouping variables. */
  private Var[] pgngvars;

  /**
   * Sets up an empty partitioning.
   * Sets up the ordering scheme.
   * @param gvs grouping vars
   * @param fls ForLet Variables
   * @param ob OrderBy specifier
   * @param ii input info
   * @throws QueryException exception
   */
  GroupPartition(final Var[] gvs, final Var[] fls, final Order ob,
      final InputInfo ii) throws QueryException {

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
      for (boolean am1 : ams) am &= am1;
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
   * @throws QueryException var not found.
   */
  private int ngvSize(final Var[] gvs, final Var[] fls) throws QueryException {
    final TokenSet flshelp = new TokenSet();
    final TokenSet glshelp = new TokenSet();

    for(final Var v : fls)
      flshelp.add(v.name.atom());
    for(final Var g : gvs)
      glshelp.add(g.name.atom());
    for(final Var g : gvs) {
      boolean found = false;
      for(final Var f : fls) {
        found |= f.eq(g);
      }
      if(!found) Err.or(null, QueryText.GVARNOTDEFINED, g);
    }
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

    final Value[] its = new Value[gv.length];
    for(int i = 0; i < gv.length; ++i) {
      final Value val = cgv[i].value(ctx);
      if(val.item() || val.empty()) its[i] = val;
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
    if(!cachedRet) cacheRet(ctx);

    for(int i = 0; i < partitions.size(); ++i) {
      final GroupNode gn = partitions.get(i);
      for(int j = 0; j < pgvars.length; ++j)
        pgvars[j].bind(gn.its[j], ctx);

      if(items != null) {
        final ItemIter[] ngvars = items.get(i);
        for(int j = 0; j < ngvars.length; ++j) {
          final ItemIter its = ngvars[j];
          pgngvars[j].bind(its.finish(), ctx);
        }
      }

      if(order != null) vl.add(ret.value(ctx));
      else ir.add(ctx.iter(ret));
    }
    if(order != null) {
      order.vl = vl;
      return ctx.iter(order);
    }
    return ir;
  }

  /**
   * Caches the return variables.
   * @param ctx query context.
   */
  private void cacheRet(final QueryContext ctx) {
    pgvars = new Var[gv.length];
    pgngvars = new Var[ngv.length];
    for(int j = 0; j < gv.length; ++j)
      pgvars[j] = ctx.vars.get(gv[j]);
    for(int j = 0; j < ngv.length; ++j)
      pgngvars[j] = ctx.vars.get(ngv[j]);
    cachedRet = true;
  }
}
