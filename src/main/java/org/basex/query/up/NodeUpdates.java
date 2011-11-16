package org.basex.query.up;

import static org.basex.query.up.primitives.PrimitiveType.*;

import java.util.Arrays;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.InsertBefore;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.StructuralUpdate;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.Util;
import org.basex.util.list.IntList;
import org.basex.util.list.ObjList;

/**
 * This container holds all update primitives for a specific database node.
 * It is identified by its target node's pre value and data reference. For each
 * database node, which is target of an update primitive, a container like this
 * accumulates all primitives that operate on this target.
 *
 * The pre value and data reference are not stored explicitly but are stored
 * individually with each update primitive in this container.
 *
 *
 * After updates in this container have been applied, there may be adjacent
 * text nodes. Resolving this issue is divided into two steps:
 *
 *   1. Resolving internal text node adjacency:
 *      Can be done immediately after updates have been applied. The sibling
 *      axis of this containers target node is not affected. In addition updates
 *      on the descendant or following axis have already been carried out, as
 *      updates are applied database-wise from the highest to the lowest pre
 *      value.
 *   2. Resolving external text node adjacency:
 *      Cannot be done on-the-fly as nodes on the preceding axis, which could
 *      also be target nodes, may be affected by merges.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
final class NodeUpdates {
  /** Container for update primitives. In most cases there will only be a single
   * update operation per target node, hence it's convenient to initialize this
   * container with a size of one. */
  UpdatePrimitive[] prim = new UpdatePrimitive[1];

  /** The corresponding node is target of a delete primitive. */
  private boolean del;
  /** The corresponding node is target of a replace primitive. */
  private boolean rep;
  /** Is external text node adjacency possible as a result of the updates,
   * that are held by this container. */
  private boolean adjEXT;
  /** Is internal text node adjacency possible. */
  private boolean adjINT;

  /**
   * Adds an update primitive to this container.
   * @param p primitive
   * @throws QueryException query exception
   */
  void add(final UpdatePrimitive p) throws QueryException {
    // container is initialized with one empty space
    if(prim[0] == null) {
      prim[0] = p;
      return;
    }

    /* If a primitive with the same type has already been added to
     * the list, merge both primitives together. */
    final PrimitiveType pt = p.type;
    for(final UpdatePrimitive up : prim) {
      if(up.type == pt) {
        up.merge(p);
        return;
      }
    }

    // otherwise, add the given primitive to the end of the list
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
    for(final UpdatePrimitive p : prim) if(p.type == t) return p;
    return null;
  }

  /**
   * Optimizes accumulated update operations for the specific target node
   * and sorts update primitives in this container regarding their type
   * {@link PrimitiveType}.
   *
   * Unnecessary operations are deleted. I.e. if the corresponding target is
   * deleted, all other operations on this node have no effect at all.
   */
  private void prepareExecution() {
    for(final UpdatePrimitive p : prim) {
      final PrimitiveType t = p.type;
      del |= t == DELETENODE;
      rep |= t == REPLACENODE;
    }

    /* Unnecessary primitives can only exist when the corresponding node is
     * deleted or replaced.
     */
    if(prim.length > 1 && (rep || del)) {
      /* If a node is replaced, an eventual delete operation
         is removed, as the actual node identity has been replaced. */
      final PrimitiveType dominantOp = rep ? REPLACENODE : DELETENODE;
      final ObjList<UpdatePrimitive> up = new ObjList<UpdatePrimitive>();

      /*
       * If a node is deleted or replaced, all other operations performing on
       * the corresponding node identity are without effect in the end.
       * Insert before/after form the only exception, as they do not affect
       * the actual target node, but the sibling axis.
       */
      for(final UpdatePrimitive p : prim) {
        final PrimitiveType t = p.type;
        if(t == INSERTBEFORE || t == INSERTAFTER || t == dominantOp) up.add(p);
      }
      prim = up.toArray(new UpdatePrimitive[up.size()]);
    }

    // determine if internal/external text node adjacency possible
    for(final UpdatePrimitive p : prim) {
      final PrimitiveType t = p.type;
      del |= t == DELETENODE;
      rep |= t == REPLACENODE;
      adjEXT |= del || rep || t == INSERTBEFORE || t == INSERTAFTER;
      adjINT |= t == INSERTINTO || t == INSERTINTOFIRST || t == INSERTINTOLAST;
    }

    /* Update primitives are executed in a strict order,
     * see {@link PrimitiveType}. */
    Arrays.sort(prim);
  }

  /**
   * Returns whether the node identity with the given pre value is destroyed
   * during the update process. A node identity can only be destroyed by a
   * delete, a replace or replace element content expression.
   * @param pre pre value of node to check
   * @return node identity with given pre value is destroyed
   */
  boolean updatesDestroyIdentity(final int pre) {
    /* if this containers target node is destroyed we don't have to
     * check for eventual replace element content expressions, which
     * only affect the child axis. */
    return (del || rep) && prim[0].pre == pre ||
    destroyedNodeIdentities().contains(pre);
  }

  /**
   * Calculates all pre values which identities are lost if all
   * update primitives of this container are made effective. To
   * determine node identities which are destroyed by a replace element
   * content expression, we have to determine all child nodes of
   * this target.
   * @return pre values of destroyed node identities
   */
  protected IntList destroyedNodeIdentities() {
    final IntList d = new IntList();
    for(final UpdatePrimitive p : prim) {
      final PrimitiveType t = p.type;
      switch(t) {
        case DELETENODE:
        case REPLACENODE:
          d.add(p.pre);
          break;
        case REPLACEELEMCONT:
          final Data data = p.data;
          final int pre = p.pre;
          final int kind = data.kind(pre);
          final int l = pre + data.size(pre, kind);
          int ipre = pre + data.attSize(pre, kind);
          while(ipre < l) {
            d.add(ipre);
            ipre += data.size(ipre, data.kind(ipre));
          }
          break;
        default:
      }
    }

    return d;
  }

  /**
   * Makes all primitives in this container effective and returns the total
   * number of pre value shifts as a result of structural updates.
   * @return number of pre value shifts
   * @throws QueryException exception
   */
  int makePrimitivesEffective() throws QueryException {
    prepareExecution();
    int sd = 0;

    for(final UpdatePrimitive p : prim) {
      p.apply();

      if(p instanceof StructuralUpdate)
        sd += ((StructuralUpdate) p).preShifts();
    }

    /* Internal text node adjacency is resolved and the number of pre value
     * shifts introduced by this container altered accordingly.
     */
    if(adjINT) return sd - resolveInternalTextNodeAdjacency();

    return sd;
  }

  /**
   * Resolves internal text node adjacency issues and returns the
   * number of occasions where two text nodes have been merged into
   * one to resolve these. This is necessary to take care of pre value
   * shifts and to resolve external text node adjacency issues later.
   * As we access this container at a later point again, we have to
   * keep track of the target's pre value.
   *
   * Internal text node adjacency means, that an update primitive
   * in this container may lead to text node adjacency on the target's
   * child axis. We can resolve this issues immediately as changes
   * won't affect the sibling axis of the target. If the sibling axis
   * is affected (i.e. the target is deleted) we have to carry out all
   * updates of the left sibling first (in case updates are held pending
   * for the left sibling).
   *
   * Merges can only be applied after all updates on the child axis
   * of this container's target node have been executed.
   *
   * @return number of text node merges
   */
  private int resolveInternalTextNodeAdjacency() {
    // number of occasions where text nodes have been merged
    int merged = 0;

    /* Second pass to merge potential adjacent text nodes.
     * Applied backwards to account for pre value shifts
     * which are kept track off by c.
     */
    final InsertBefore ib = (InsertBefore) find(INSERTBEFORE);
    int c = ib == null ? 0 : ib.preShifts();
    for(int i = prim.length - 1; i >= 0; i--) {
      if(prim[i] instanceof StructuralUpdate) {
        final StructuralUpdate p = (StructuralUpdate) prim[i];

        /* Skip primitives which have to be checked later (delete, replace,
         * insert before) as they affect the sibling axis and primitives
         * which cannot lead to adjacent text nodes.
         */
        if(p.type != INSERTAFTER && p.type != INSERTBEFORE &&
            p.type != REPLACENODE && p.type != DELETENODE) {
          if(p.adjacentTexts(c)) {
            merged++;
            c--;
          }
          c += p.preShifts();
        }
      }
    }

    return merged;
  }

  /**
   * Resolves external text node adjacency issues. As this is the last time
   * we touch this specific container we don't have to keep track of pre
   * value shifts any longer, so there's no return value.
   *
   * External text node adjacency can occur if an update primitive in this
   * container affects the sibling axis of this target node (Delete, Replace,
   * Insert After, Insert Before, see {@link PrimitiveType}.
   *
   * @param preShifts amount of pre value shifts that affect this container's
   * target pre value. These shifts are a product of the next container on
   * the preceding sibling axis. The 'left sibling container'.
   */
  void resolveExternalTextNodeAdjacency(final int preShifts) {
    if(!adjEXT) return;

    // take pre value shifts into account
    int c = preShifts;
    /* Check for adjacency backwards to account for pre value shifts introduced
     * by the individual updates in this container -> from the lowest to the
     * highest pre value
     */
    for(int i = prim.length - 1; i >= 0; i--) {
      if(prim[i] instanceof StructuralUpdate) {
        final StructuralUpdate p = (StructuralUpdate) prim[i];
        // only check primitives which can lead to external text node adjacency
        if(p.type == INSERTAFTER || p.type == INSERTBEFORE ||
            p.type == DELETENODE || p.type == REPLACENODE) {
          if(p.adjacentTexts(c))
            c--;
        }
        // consider amount of nodes deleted/inserted by the current primitive
        c += p.preShifts();
      }
    }
  }

  @Override
  public String toString() {
    return Util.name(this);
  }
}
