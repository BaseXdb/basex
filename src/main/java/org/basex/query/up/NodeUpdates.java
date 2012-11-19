package org.basex.query.up;

import static org.basex.query.up.primitives.PrimitiveType.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.up.primitives.*;

/**
 * This container holds all update primitives for a specific database node.
 * It is identified by its target node's PRE value and data reference.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
final class NodeUpdates {
  /** Container for update primitives. */
  UpdatePrimitive[] prim;

  /**
   * Adds an update primitive to this container.
   * @param p primitive
   * @throws QueryException query exception
   */
  void add(final UpdatePrimitive p) throws QueryException {
    if(prim == null) {
      prim = new UpdatePrimitive[] { p };
      return;
    }

    // Primitives can be merged eventually ...
    final int typeIndex = index(p.type);
    if(typeIndex != -1) {
      final UpdatePrimitive up = prim[typeIndex];
      // but an insertInto could be part of a substitute of a replaceElementContent,
      // then we cannot merge them, as this would insert unwanted nodes
      if(up instanceof InsertInto) {
        final InsertInto oldprim = (InsertInto) up;
        final InsertInto newprim = (InsertInto) p;
        // if the new primitive substitutes, than replace the old one with the new one
        if(newprim.rec) prim[typeIndex] = newprim;
        // if neither substitutes, merge them.
        if(!oldprim.rec && !newprim.rec) up.merge(p);

      // all other primitives can be merged regardless
      } else
        up.merge(p);
      return;
    }

    addToPrimitives(p);
  }

  /**
   * Adds the given primitive to the end of the list.
   * @param p Update primitive
   */
  private void addToPrimitives(final UpdatePrimitive p) {
    final int l = prim.length;
    final UpdatePrimitive[] t = new UpdatePrimitive[l + 1];
    System.arraycopy(prim, 0, t, 0, l);
    t[l] = p;
    prim = t;
  }

  /**
   * Find the update primitive with the given type. In case there is no
   * primitive of the given type, null is returned.
   * @param t PrimitiveType
   * @return primitive of type t, null if not found
   */
  private UpdatePrimitive find(final PrimitiveType t) {
    for(final UpdatePrimitive p : prim) {
      if(p.type == t) return p;
    }
    return null;
  }

  /**
   * Find the update primitive with the given {@link PrimitiveType} and returns its index.
   * @param t PrimitiveType
   * @return index of primitive with given type or -1 if not found
   */
  private int index(final PrimitiveType t) {
    for(int i = 0; i < prim.length; i++) {
      if(prim[i].type == t) return i;
    }
    return -1;
  }

  /**
   * Prepares the updates. Makes sure that ...
   *
   * - replaceElementContent has the desired effect.
   * - a replace and a delete on a target T result in replacing the node.
   *
   */
  void prepare() {
    /* Check if target node T is deleted and remove superfluous primitives. */
    final DeleteNode del = (DeleteNode) find(DELETENODE);
    /* If T is deleted due to a substitution of a replaceElementContent
     * expression all other primitives operating on this node must be removed. According
     * to the XQUF, a target T of a replaceElementContent contains, after the end of the
     * snapshot, either a text node as a single child or no child node at all. */
    if(del != null && del.rec) {
      prim = new UpdatePrimitive[] { del };
      return;
    }

    // If T is replaced, all primitives other than InsertBefore and InsertAfter can be
    // removed.
    final ReplaceNode replace = (ReplaceNode) find(REPLACENODE);
    if(replace != null) {
      final ArrayList<UpdatePrimitive> primnew = new ArrayList<UpdatePrimitive>();
      for(final UpdatePrimitive p : prim) {
        if(p.type == REPLACENODE || p.type == INSERTBEFORE || p.type == INSERTAFTER)
          primnew.add(p);
      }
      prim = primnew.toArray(new UpdatePrimitive[primnew.size()]);
      return;
    }

    /* If the target T is affected by a replaceElementContent expression, all
     * primitives that insert any nodes into T have to be removed from the list.
     * An InsertInto which is part of the substitution of the replaceElementContent
     * expression forms the only exception. */
    final ReplaceValue rec = (ReplaceValue) find(REPLACEVALUE);
    if(rec != null && rec.rec) {
      final ArrayList<UpdatePrimitive> primnew = new ArrayList<UpdatePrimitive>();
      for(final UpdatePrimitive p : prim) {
        /* Add only InsertIntos that are part of the substitution and make sure no
         * other primitive is added, that adds nodes to the child axis of T. */
        if(p.type != INSERTINTOFIRST && p.type != INSERTINTO && p.type != INSERTINTOLAST
            || p.type == INSERTINTO && ((InsertInto) p).rec)
          primnew.add(p);
      }
      prim = primnew.toArray(new UpdatePrimitive[primnew.size()]);
      return;
    }
  }
}
