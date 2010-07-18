package org.basex.query.expr;

import java.util.ArrayList;
import java.util.HashMap;

import org.basex.query.QueryContext;
import org.basex.query.item.Item;
import org.basex.query.util.ItemList;
import org.basex.query.util.Var;

/**
 * Stores the grouping for a group by clause.
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public class GroupPartition {
  /** Grouping variables. */
  private final Var[] gv;
  /** Non grouping variables. */
  private final Var[] fl;
  /** Group Partitioning. */
  final ArrayList<GroupNode> partitions;
  /** Resulting Sequence. */
  final ArrayList<HashMap<Var, ItemList>> items;
  /** HashValue, Position. */
  private final HashMap<Integer, Integer> hashes = 
    new HashMap<Integer, Integer>();
  /** Items map. */

  /**
   * Sets up an empty partitioning.
   * @param gv1 Grouping vars
   * @param fl1 Non grouping vars
   */
  public GroupPartition(final Var[] gv1, final Var[] fl1) {
    this.gv = gv1;
    this.fl = fl1;
    partitions = new ArrayList<GroupNode>();
    items = new ArrayList<HashMap<Var, ItemList>>();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for(GroupNode gp : partitions) {
      sb.append(gp);
      sb.append("\n");
      sb.append(" ").append(items.get(i));
      sb.append("\n\n");
      i++;
    }
    return sb.toString();
  }

  /**
   * Adds the current variable binding to the partitioning scheme.
   * @param ctx QueryContext
   */
  public void add(final QueryContext ctx)  {
    Item[] its = new Item[gv.length];
    for(int i = 0; i < gv.length; i++) {
      its[i] = ctx.vars.get(gv[i]).item;
    }
    final GroupNode cand = new GroupNode(gv, its);
    boolean found = false;
    int p = 0;
    final int chash = cand.hashCode();
    if(hashes.containsKey(chash)) {
      p = hashes.get(chash);
      found = true;
    }
    if(!found) {
      partitions.add(cand);
      p = partitions.size() - 1;
      hashes.put(chash, hashes.size());
    }
    if(items.size() <= p) items.add(new HashMap<Var, ItemList>());
    HashMap<Var, ItemList> sq = items.get(p);

    for(Var v : fl) {
      boolean skip = false;
      for(Var g : gv)
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
   *
   */
  public class GroupNode {
    /** List of grouping vars. */
    final Var[] vars;
    /** List of grouping var values. */
    final ItemList its;
    /** Hashes for the group representative values.
     *  N.B. long instead of int */
    final int hash;

    /**
     * Creates a group node.
     * @param vs grouping vars
     * @param is grouping var values
     */
    public GroupNode(final Var[] vs, final Item[] is) {
      this.vars = vs;
      this.its = new ItemList();
      final long[] hhs = new long[is.length];
      for(int i = 0; i < vs.length; i++) {
        this.its.add(is[i]);
        if(is[i].e()) {  
          // Add long.max_value to denote empty sequence in item
          hhs[i] = Long.MAX_VALUE;
        } else {
          hhs[i] = is[i].hashCode();
        }
      }
      this.hash = java.util.Arrays.hashCode(hhs);
    }
    @Override
    public int hashCode() {
      return hash;
      
    }
// -- add checks vor group by invariants
//    /**
//     * Checks the current Node for equality with an existing
//     * GroupNode.
//     * @param p candidate node
//     * @return true if nodes are equal
//     */
//    public boolean essq(final GroupNode p) {
//      if(p == null) return false;
//
//      if(p.hashCode() != this.hashCode()) return false;
// //     if(p.vars.size() != vars.size()) return false;
//      for(int i = 0; i < vars.size(); i++) {
//        final boolean candedmpty = p.its.get(i).e();
//        final long groupitem = hs.get(i);
//        // n.b. empty sequences are considered equal
//        final long canditem = candedmpty ? Long.MAX_VALUE
//            : (long) p.its.get(i).hashCode();
//        if(groupitem != canditem) return false;
//        if(candedmpty && !its.get(i).e()) return false;
//      }
//      return true;
//    }
//

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return " " + vars.toString() + " with grouping var " + its.toString();
    }
  }
}
