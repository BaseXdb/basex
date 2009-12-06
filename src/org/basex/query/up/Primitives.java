package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.util.HashMap;
import java.util.Map;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.QNm;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;
import org.basex.util.Atts;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
abstract class Primitives {
  /** Atomic update operations hashed by the pre value. */
  protected final Map<Integer, UpdatePrimitive[]> op =
    new HashMap<Integer, UpdatePrimitive[]>();
  /** Pre values of the target nodes which are updated, sorted ascending. */
  protected int[] nodes;
  /** Static put counter. */
  protected static int putCount;

  /**
   * Getter.
   * @return sorted pre values of target nodes
   */
  protected final int[] getNodes() {
    if(nodes == null) finish();
    return nodes;
  }

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   * @throws QueryException query exception
   */
  protected final void add(final UpdatePrimitive p) throws QueryException {
    int i;
    if(p.node instanceof DBNode) i = ((DBNode) p.node).pre;
    // Assign unique negative id to put operations (hmm..)
    else if(p.type() == PrimitiveType.PUT) i = --putCount;
    // Possible to use node id cause nodes in map belong to the same
    // database. Thus there won't be any collisions between dbnodes and
    // fragments.
    else i = p.node.id();

    UpdatePrimitive[] l = op.get(i);
    final int pos = p.type().ordinal();
    if(l == null) {
      l = new UpdatePrimitive[PrimitiveType.values().length];
      l[pos] = p;
      op.put(i, l);
    } else if(l[pos] == null) {
      l[pos] = p;
    } else {
      l[pos].merge(p);
    }
  }

  /**
   * Finishes something. Not finished yet.
   * [LK] ..to be merged and moved into this class?
   */
  protected abstract void finish();

  /**
   * Checks updates for violations.
   * @throws QueryException query exception
   */
  protected void check() throws QueryException {
    for(final int node : getNodes()) {
      for(final UpdatePrimitive p : op.get(node)) {
        if(p != null) p.prepare();
      }
    }
  };

  /**
   * Checks constraints and applies all updates to the databases.
   * @throws QueryException query exception
   */
  protected abstract void apply() throws QueryException;

  /**
   * Finds string duplicates in the given map.
   * @param m map reference
   * @throws QueryException query exception
   */
  protected static final void findDuplicates(final Map<QNm, Integer> m)
      throws QueryException {

    final Atts at = new Atts();
    for(final QNm name : m.keySet()) {
      if(m.get(name) > 1) Err.or(UPATTDUPL, name);
      final byte[] an = name.pref();
      final byte[] uri = name.uri.str();
      final int ai = at.get(an);
      if(ai == -1) at.add(an, uri);
      else if(!eq(uri, at.val[ai])) Err.or(UPNSCONFL2);
    }
  }

  /**
   * Increases or decreases the counters for the given string set.
   * @param m map reference
   * @param add increase if true
   * @param s string set
   */
  protected static final void changeAttributePool(final Map<QNm, Integer> m,
      final boolean add, final QNm... s) {

    if(s == null) return;
    for(final QNm st : s) {
      final Integer i = m.get(st);
      m.put(st, (i == null ? 0 : i) + (add ? 1 : -1));
    }
  }
}
