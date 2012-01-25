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
import org.basex.util.list.IntList;
import org.basex.util.list.ObjList;

/**
 * Stores the grouping for a group by clause.
 *
 * @author BaseX Team 2005-12, BSD License
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
  private final Var[][] ngv;

  /** Group partitioning. */
  private final ObjList<GroupNode> part = new ObjList<GroupNode>();
  /** Resulting sequence for non-grouping variables. */
  private final ObjList<ItemCache[]> items;
  /** HashValue, position (with overflow bucket). */
  private final IntMap<IntList> hashes = new IntMap<IntList>();

  /**
   * Sets up an empty partitioning.
   * Sets up the ordering scheme.
   * @param g grouping variables
   * @param ng non-grouping variables
   * @param ob order by specifier
   * @param ii input info
   */
  GroupPartition(final Var[] g, final Var[][] ng, final Order ob,
      final InputInfo ii) {
    gv = g;
    ngv = ng;
    order = ob;
    items = ngv[0].length != 0 ? new ObjList<ItemCache[]>() : null;
    input = ii;
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
    final int gl = gv.length;
    final Value[] vals = new Value[gl];
    for(int i = 0; i < gl; i++) {
      final Value val = ctx.value(ctx.vars.get(gv[i]));
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

    final int ngl = ngv[0].length;

    // no non-grouping variables exist
    if(ngl == 0) return;

    // adds the current non-grouping variable bindings to the p-th partition.
    if(p == items.size()) items.add(new ItemCache[ngl]);
    final ItemCache[] sq = items.get(p);

    for(int i = 0; i < ngl; ++i) {
      ItemCache ic = sq[i];
      final Value result = ctx.value(ctx.vars.get(ngv[0][i]));
      if(ic == null) {
        ic = new ItemCache();
        sq[i] = ic;
      }
      ic.add(result);
    }
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

    for(int i = 0; i < part.size(); ++i) {
      final GroupNode gn = part.get(i);
      for(int j = 0; j < gv.length; ++j)
        ctx.vars.add(gv[j].copy().bind(gn.vals[j], ctx));

      if(items != null) {
        final ItemCache[] ii = items.get(i);
        for(int j = 0; j < ii.length; ++j) {
          ctx.vars.add(ngv[1][j].copy().bind(ii[j].value(), ctx));
        }
      }
      if(order != null) {
        order.add(ctx, ret, ks, vs);
      } else ic.add(ctx.value(ret));
    }
    return order != null ? ctx.iter(order.set(ks, vs)) : ic;
  }
}
