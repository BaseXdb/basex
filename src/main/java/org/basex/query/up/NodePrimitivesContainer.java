package org.basex.query.up;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import static org.basex.query.up.primitives.PrimitiveType.*;
import org.basex.query.up.primitives.Primitive;
import org.basex.util.Util;

/**
 * Container that holds all update primitives for a specific database node.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
final class NodePrimitivesContainer implements NodePrimitives {
  /** Container for update primitives. */
  private List<Primitive> prim = new ArrayList<Primitive>(1);
  /** The corresponding node is target of a delete primitive. */
  private boolean del;
  /** The corresponding node is target of a replace primitive. */
  private boolean rep;
  /** Text node adjacency possible as a result of updates. */
  private boolean adj;

  @Override
  public void add(final Primitive p) throws QueryException {
    add(findPos(p), p);
  }

  /**
   * Adds the given primitive to this container at a specific index.
   * @param i index
   * @param p Update Primitive to add
   * @throws QueryException query exception
   */
  private void add(final int i, final Primitive p) throws QueryException {
    final PrimitiveType pt = p.type();
    del |= pt == DELETENODE;
    rep |= pt == REPLACENODE;
    // text node adjacency cannot be instantly fixed if updates that affect
    // the sibling axis are still held pending
    adj = del || rep || pt == INSERTBEFORE || pt == INSERTAFTER;
    if(i < prim.size()) {
      final Primitive a = prim.get(i);
      if(a != null && a.type() == p.type()) a.merge(p);
      else prim.add(i, p);
    } else {
      prim.add(p);
    }
  }

  /**
   * Finds the position where the given primitive has to be inserted.
   * @param p update primitive
   * @return position
   */
  private int findPos(final Primitive p) {
    int i = -1;
    final int to = p.type().ordinal();
    while(++i < prim.size()) {
      if(to <= prim.get(i).type().ordinal()) return i;
    }
    return i;
  }

  @Override
  public Iterator<Primitive> iterator() {
    return prim.iterator();
  }

  @Override
  public Primitive find(final PrimitiveType t) {
    for(final Primitive p : prim) if(p.type() == t) return p;
    return null;
  }

  @Override
  public void optimize() {
    // unnecessary primitives can only exist when the corresponding node is
    // deleted or replaced.
    if(rep || del) {
      // if a node is replaced, an eventual delete operation
      // is removed, as the actual node identity has been replaced.
      final PrimitiveType dominantOp = rep ? REPLACENODE : DELETENODE;
      final List<Primitive> up = new ArrayList<Primitive>(prim.size());
      // if a node is deleted or replaced, all other operations performing on
      // the corresponding node identity are without effect in the end.
      // insert before/after form the only exceptions, as they do not affect
      // the actual target node, but the sibling axis.
      for(final Primitive p : prim) {
        final PrimitiveType t = p.type();
        if(t == INSERTBEFORE || t == INSERTAFTER || t == dominantOp) up.add(p);
      }
      prim = up;
    }
  }

  @Override
  public boolean textAdjacency() {
    return adj;
  }

  @Override
  public boolean updatesDestroyIdentity() {
    return del || rep;
  }

  @Override
  public String toString() {
    return Util.name(this) + prim;
  }
}
