package org.basex.query.expr;

import java.util.ArrayList;
import java.util.HashMap;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.util.ItemList;
import org.basex.query.util.Var;
import org.basex.util.IntList;

/**
 * Stores the grouping for a group by clause.
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
final class GroupPartition {
  /** Grouping variables. */
  final Var[] gv;
  /** Non-grouping variables. */
  private final Var[] ngv;
  /** Group Partitioning. */
  final ArrayList<GroupNode> partitions;
  /** Resulting Sequence for non grouping variables. */
  final ArrayList<HashMap<Var, ItemList>> items;
  /** HashValue, Position. */
  private final HashMap<Integer, IntList> hashes =
    new HashMap<Integer, IntList>();
  

  /**
   * Sets up an empty partitioning.
   * @param gv1 Grouping vars
   * @param fl1 ForLet Variables
   */
  GroupPartition(final Var[] gv1, final Var[] fl1) {
    gv = gv1;
    ngv = new Var[fl1.length - gv.length];
    int i = 0;
    for(final Var v : fl1) {
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
    items = new ArrayList<HashMap<Var, ItemList>>();
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
    for(int i = 0; i < gv.length; i++) {
      its[i] = ctx.vars.get(gv[i]).item;
    }
    
    boolean found = false;
    int p = 0;
    final GroupNode cand = new GroupNode(its, gv.length);
    final Integer chash = cand.hashCode();
    
    if(hashes.containsKey(chash)) {
      final IntList ps = hashes.get(cand.hash);
      for(int pp : ps.toArray()) {
        if(cand.eq(partitions.get(pp))) {
          found = true;
          p = pp;
          break;
        }
      }    
    }
     if(!found) {
      p = partitions.size();
      partitions.add(cand);
      final IntList pos = hashes.get(chash) != null ?
          hashes.get(chash)
          : new IntList(2);
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
   */
  private void addNonGrpIts(final QueryContext ctx, final int p) {
    if(items.size() <= p) items.add(new HashMap<Var, ItemList>());
    HashMap<Var, ItemList> sq = items.get(p);

    for(final Var v : ngv) {
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
    /** Length of grouping variables. */
    final int varlen;
    /** Hashes for the group representative values.
     *  N.B. long instead of int */
    final int hash;
    

    /**
     * Creates a group node.
     * @param is grouping var values
     * @param vl number of grouping variables
     */
    public GroupNode(final Item[] is, final int vl) {
      its = is;
      varlen = vl;
      final long[] hhs = new long[is.length];
      for(int i = 0; i < varlen; i++) {
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

    /**
     * Checks the nodes for equality.
     * @param c second group node
     * @return result of check
     * @throws QueryException query exception
     */
    boolean eq(final GroupNode c) throws QueryException {
      if(its.length != c.its.length ||
          varlen != c.its.length) return false;
      for(int i = 0; i < its.length; i++) {
          if(its[i].empty() && c.its[i].empty())
            continue;
          if(!its[i].equiv(null, c.its[i]))
            return false;
      }
      return true;
    }
  }
}
