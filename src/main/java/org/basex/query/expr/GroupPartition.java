package org.basex.query.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.util.ItemList;
import org.basex.query.util.Var;

/**
 * Stores the grouping for a group by clause.
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
final class GroupPartition {
  /** Grouping variables. */
  protected static Var[] gv;
  /** Non grouping variables. */
  private final Var[] fl;
  /** Group Partitioning. */
  final ArrayList<GroupNode> partitions;
  /** Resulting Sequence. */
  final ArrayList<HashMap<Var, ItemList>> items;
  /** HashValue, Position. */
  private final HashMap<Integer, Integer> hashes =
    new HashMap<Integer, Integer>();

  /**
   * Sets up an empty partitioning.
   * @param gv1 Grouping vars
   * @param fl1 Non grouping vars
   */
  GroupPartition(final Var[] gv1, final Var[] fl1) {
    gv = gv1;
    fl = fl1;
    partitions = new ArrayList<GroupNode>();
    items = new ArrayList<HashMap<Var, ItemList>>();
  }

  /**
   * Adds the current variable binding to the partitioning scheme.
   * @param ctx QueryContext
   */
  void add(final QueryContext ctx)  {
    final Item[] its = new Item[gv.length];
    for(int i = 0; i < gv.length; i++) {
      its[i] = ctx.vars.get(gv[i]).item;
    }
    final GroupNode cand = new GroupNode(its);
    boolean found = false;
    int p = 0;
    final int chash = cand.hashCode();
    if(hashes.containsKey(chash)) {
      p = hashes.get(cand.hash);
      if(cand.equals(partitions.get(p))) {
        found = true;
      } else {
        System.out.println("Possible collision cand" + cand + " and "
            + partitions.get(p));
      }
        
    }
    if(!found) {
      partitions.add(cand);
      p = partitions.size() - 1;
      hashes.put(chash, hashes.size());
    }
    if(items.size() <= p) items.add(new HashMap<Var, ItemList>());
    HashMap<Var, ItemList> sq = items.get(p);

    for(final Var v : fl) {
      boolean skip = false;
      for(final Var g : gv)
        if(v.eq(g)) {
          skip = true;
          break;
        }
      if(skip) continue;
      if(sq == null) sq = new HashMap<Var, ItemList>();
      if(sq.get(v) == null) sq.put(v, new ItemList());
      sq.get(v).add(ctx.vars.get(v).item);
    }

  }

  /**
   * GroupNode defines one valid partitioning setting.
   * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
   * @author Michael Seiferle
   */
  static final class GroupNode {
    /** List of grouping var values. */
    final Item[] its;
    /** Hashes for the group representative values.
     *  N.B. long instead of int */
    final int hash;
    
    /**
     * Creates a group node.
     * @param is grouping var values
     */
    GroupNode(final Item[] is) {
      its = is;
      final long[] hhs = new long[is.length];
      for(int i = 0; i < gv.length; i++) {
        if(is[i].empty()) {
          // Add long.max_value to denote empty sequence in item
          hhs[i] = Long.MAX_VALUE;
        } else {
          hhs[i] = is[i].hashCode();
        }
      }
      hash = Arrays.hashCode(hhs);
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
      sb.append(Arrays.toString(gv));
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
      if(its.length != c.its.length ||
          gv.length != c.its.length) return false;
      for(int i = 0; i < its.length; i++) {
        // [CG] calling expression should be passed on
        // (might be skipped again)
        if(!its[i].equive(null, c.its[i])) return false;
      }
      return true;
    }
  }

}
