package org.basex.query.up;

import static org.basex.query.QueryText.*;

import java.util.HashMap;
import java.util.Map;

import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class Primitives {
  /** Atomic update operations hashed by the pre value. */
  public Map<Integer, UpdatePrimitive[]> op;
  /** Pre values of the target nodes which are updated, sorted ascending. */
  int[] nodes;
  /** Primitives for this database are finished. */
  boolean finished;

  /**
   * Constructor.
   */
  public Primitives() {
    op = new HashMap<Integer, UpdatePrimitive[]>();
  }

  /**
   * Getter.
   * @return sorted pre values of target nodes
   */
  public int[] getNodes() {
    if(!finished) finish();
    return nodes;
  }

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive p) throws QueryException {
    int i;
    if(p.node instanceof DBNode) i = ((DBNode) p.node).pre;
    // Possible to use node id cause nodes in map belong to the same
    // database. Thus there won't be any collisions between dbnodes and
    // fragments.
    else i = ((FNode) p.node).id();
    UpdatePrimitive[] l = op.get(i);
    final int pos = p.type().ordinal();
    if(l == null) {
      l = new UpdatePrimitive[PrimitiveType.values().length];
      l[pos] = p;
      op.put(i, l);
    } else if(l[pos] == null) l[pos] = p;
    else l[pos].merge(p);
  }
  
  /**
   * Finishes something. Not finished yet.
   */
  public abstract void finish();
  
  /**
   * Checks updates for violations.
   * @throws QueryException query exception 
   */
  public void check() throws QueryException { 
    for(final int node : getNodes()) {
      for(final UpdatePrimitive p : op.get(node)) {
        if(p == null) continue;
        p.prepare();
      }
    }
  };

  /**
   * Checks constraints and applies all updates to the databases.
   * @throws QueryException query exception
   */
  public abstract void apply() throws QueryException;
  
  /**
   * Finds string duplicates in the given map. 
   * @param m map reference
   * @throws QueryException query exception
   */
  public static void findDuplicates(final Map<String, Integer> m) 
  throws QueryException {
    for(final String s : m.keySet()) {
      if(m.get(s) > 1) 
        Err.or(UPATTDUPL, s);
    }
  }

  /**
   * Increases or decreases the counters for the given string set.
   * @param m map reference
   * @param add increase if true
   * @param s string set
   */
  public static void changeAttributePool(final Map<String, Integer> m, 
      final boolean add, final String... s) {
    if(s == null) return;
    Integer i;
    for(final String st : s) {
      i = m.get(st);
      if(i == null) {
        m.put(st, 1);
      } else
        m.put(st, add ? ++i : --i);
    }
  }
}