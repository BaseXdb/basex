package org.basex.query.up;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;

/**
 * Container that holds all update primitives for a specific database node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
final class NodePrimitivesContainer implements NodePrimitives {
  /** Container for update primitives. */
  private final List<UpdatePrimitive> primitives =
    new LinkedList<UpdatePrimitive>();

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
}
