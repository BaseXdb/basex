package org.basex.query.up;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import static org.basex.query.up.primitives.PrimitiveType.*;
import org.basex.query.up.primitives.UpdatePrimitive;

/**
 * Container that holds all update primitives for a specific database node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
final class NodePrimitivesContainer implements NodePrimitives {
  /** Container for update primitives. */
  private List<UpdatePrimitive> primitives =
    new LinkedList<UpdatePrimitive>();
  /** The corresponding node is target of a delete primitive. */
  private boolean del;
  /** The corresponding node is target of a replace primitive. */
  private boolean rep;
  /** Text node adjacency possible as a result of updates. */
  private boolean textAdjacency;

  @Override
  public void add(final UpdatePrimitive p) throws QueryException {
    add(findPos(p), p);
  }

  /**
   * Adds the given primitive to this container at a specific position.
   * @param i position
   * @param p Update Primitive to add
   * @throws QueryException query exception
   */
  private void add(final int i, final UpdatePrimitive p) throws QueryException {
    final PrimitiveType pt = p.type();
    del |= pt == DELETE;
    rep |= pt == REPLACENODE;
    textAdjacency = del || rep || pt == INSERTBEFORE || pt == INSERTAFTER;
    if(i < primitives.size()) {
      final UpdatePrimitive a = primitives.get(i);
      if(a != null && a.type() == p.type()) a.merge(p);
      else primitives.add(i, p);
    } else {
      primitives.add(p);
    }
  }

  /**
   * Finds the position where the given primitive has to be inserted.
   * @param p update primitive
   * @return position
   */
  private int findPos(final UpdatePrimitive p) {
    int i = -1;
    final int to = p.type().ordinal();
    while(++i < primitives.size()) {
      final int po = primitives.get(i).type().ordinal();
      if(to <= po) return i;
    }
    return i;
  }

  @Override
  public Iterator<UpdatePrimitive> iterator() {
    return primitives.iterator();
  }

  @Override
  public UpdatePrimitive find(final PrimitiveType t) {
    for(final UpdatePrimitive p : primitives) if(p.type() == t) return p;
    return null;
  }

  @Override
  public void optimize() {
    // unnecessary primitives can only exist when the corresponding node is
    // deleted or replaced.
    if(rep || del) {
      // if a node is replaced, an eventual delete operation
      // is removed, as the actual node identity has been replaced.
      final PrimitiveType dominantOp = rep ? REPLACENODE : DELETE;
      final List<UpdatePrimitive> temp = new LinkedList<UpdatePrimitive>();
      // if a node is deleted or replaced, all other operations performing on
      // the corresponding node identity are without effect in the end.
      // insert before/after form the only exceptions, as they do not affect
      // the actual target node, but the sibling axis.
      for(final UpdatePrimitive p : primitives) {
        final PrimitiveType pt = p.type();
        if(pt == INSERTBEFORE || pt == INSERTAFTER || pt == dominantOp)
          temp.add(p);
      }
      primitives = temp;
    } 
  }

  @Override
  public boolean textAdjacency() {
    return textAdjacency;
  }
}
