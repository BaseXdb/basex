package org.basex.query.up;

import static org.basex.query.up.primitives.PrimitiveType.*;
import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.up.primitives.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class holds all updates for a specific database. Before applied,
 * updates are sorted in a descending manner regarding the pre value of their
 * target nodes. As a result, update operations are applied from bottom to
 * top and we can stick to pre values as primitive identifier as pre value
 * shifts won't have any effect on updates that have not yet been applied.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
final class DatabaseUpdates {
  /** Data reference. */
  private final Data data;
  /** Pre values of target nodes. */
  private IntList nodes = new IntList(0);
  /** Mapping between pre values of the target nodes and all update primitives
   * which operate on this target. */
  private final IntMap<NodeUpdates> updatePrimitives = new IntMap<NodeUpdates>();

  /**
   * Constructor.
   * @param d data reference
   */
  DatabaseUpdates(final Data d) {
    data = d;
  }

  /**
   * Adds an update primitive to the list.
   * @param p update primitive
   * @throws QueryException query exception
   */
  void add(final UpdatePrimitive p) throws QueryException {
    final int pre = p.pre;
    NodeUpdates pc = updatePrimitives.get(pre);
    if(pc == null) {
      pc = new NodeUpdates();
      updatePrimitives.add(pre, pc);
    }
    pc.add(p);
  }

  /**
   * Checks updates for violations. If a violation is found the complete update
   * process is aborted.
   * @throws QueryException query exception
   */
  void check() throws QueryException {
    // get and sort keys (pre/id values)
    final int s = updatePrimitives.size();
    nodes = new IntList(s);
    for(int i = 1; i <= updatePrimitives.size(); i++)
      nodes.add(updatePrimitives.key(i));
    nodes.sort();

    for(int i = 0; i < s; ++i) {
      final NodeUpdates ups = updatePrimitives.get(nodes.get(i));
      for(final UpdatePrimitive p : ups.prim) {
        if(p instanceof NodeCopy) ((NodeCopy) p).prepare();
        /* check if the identity of all target nodes of fn:put operations is
           still available after the execution of updates. that includes parent
           nodes as well */
        if(p.type == PUT && ancestorDeleted(nodes.get(i))) UPFOTYPE.thrw(p.info, p);
      }
    }

    // check attribute duplicates
    int p = nodes.size() - 1;
    int par = -1;
    while(p >= 0) {
      // parent of a previous attribute has already been checked
      if(par == nodes.get(p) && --p < 0) break;
      int pre = nodes.get(p);

      // catching optimize statements which have PRE == -1 as a target
      if(pre == -1) return;

      final int k = data.kind(pre);
      if(k == Data.ATTR) {
        par = data.parent(pre, k);
        final IntList il = new IntList();
        while(p >= 0 && (pre = nodes.get(p)) > par) {
          il.add(pre);
          --p;
        }
        if(par != -1) il.add(par);
        checkNames(il.toArray());
      } else {
        if(k == Data.ELEM) checkNames(pre);
        --p;
      }
    }
  }

  /**
   * Locks the database for write operations.
   * @throws QueryException query exception
   */
  void startUpdate() throws QueryException {
    if(!data.startUpdate()) PINNED.thrw(null, data.meta.name);
  }

  /**
   * Locks the database for write operations.
   */
  void finishUpdate() {
    data.finishUpdate();
  }

  /**
   * Applies all updates for this specific database.
   * @throws QueryException query exception
   */
  void apply() throws QueryException {
    optimize();

    /*
     * For each target node, the update primitives in the corresponding
     * container are applied. Certain operations may lead to text node
     * adjacency. As the updates in a container, including eventual text node
     * merges, may not affect the preceding sibling axis (as there could be
     * other update primitives with a target on this axis), we have to make
     * sure that updates on the preceding sibling axis have been carried out.
     *
     * To achieve this we keep track of the most recently applied container
     * and resolve text adjacency issues after the next container on the
     * preceding axis has been executed.
     */
    NodeUpdates recent = null;
    // apply updates from the highest to the lowest pre value
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final NodeUpdates current = updatePrimitives.get(nodes.get(i));
      // first run, no recent container
      if(recent == null) {
        current.makePrimitivesEffective();
      } else {
        recent.resolveExternalTextNodeAdjacency(current.makePrimitivesEffective());
      }
      recent = current;
    }
    // resolve text adjacency issues of the last container
    recent.resolveExternalTextNodeAdjacency(0);

    if(data.meta.prop.is(Prop.WRITEBACK) && !data.meta.original.isEmpty()) {
      try {
        Export.export(data, data.meta.original, null);
      } catch(final IOException ex) {
        UPPUTERR.thrw(null, data.meta.original);
      }
    }
  }

  /**
   * Returns the number of performed updates.
   * @return number of updates
   */
  int size() {
    int s = 0;
    for(int i = nodes.size() - 1; i >= 0; i--) {
      for(final UpdatePrimitive up : updatePrimitives.get(nodes.get(i)).prim) {
        s += up.size();
      }
    }
    return s;
  }

  /**
   * Determines recursively whether an ancestor of a given node is deleted.
   * @param n pre value
   * @return true if ancestor deleted
   */
  private boolean ancestorDeleted(final int n) {
    final NodeUpdates up = updatePrimitives.get(n);
    if(up != null && up.updatesDestroyIdentity(n)) return true;

    final int p = data.parent(n, data.kind(n));
    return p != -1 && ancestorDeleted(p);
  }

  /**
   * Checks nodes for namespace conflicts and duplicate attributes.
   * @param pres pre values of nodes to check (in descending order)
   * @throws QueryException query exception
   */
  private void checkNames(final int... pres) throws QueryException {
    // check for namespace conflicts
    final NamePool pool = new NamePool();
    for(final int pre : pres) {
      final NodeUpdates ups = updatePrimitives.get(pre);
      if(ups != null) for(final UpdatePrimitive up : ups.prim) up.update(pool);
    }
    if(!pool.nsOK()) UPNSCONFL2.thrw(null);

    // check for duplicate attributes
    final IntList il = new IntList();
    for(final int pre : pres) {
      // pre values consist exclusively of element and attribute nodes
      if(data.kind(pre) == Data.ATTR) {
        il.add(pre);
      } else {
        final int ps = pre + data.attSize(pre, Data.ELEM);
        for(int p = pre + 1; p < ps; ++p) {
          final byte[] nm = data.name(p, Data.ATTR);
          if(!il.contains(p)) {
            final QNm name = new QNm(nm);
            final byte[] uri = data.nspaces.uri(data.nspaces.uri(nm, p));
            if(uri != null) name.uri(uri);
            pool.add(name, NodeType.ATT);
          }
        }
      }
    }
    final QNm dup = pool.duplicate();
    if(dup != null) UPATTDUPL.thrw(null, dup);
  }

  /**
   * Identifies unnecessary update operations and removes them from the pending
   * update list.
   */
  private void optimize() {
    /* Tree Aware Updates: Unnecessary updates on the descendant axis of a
     * deleted or replaced node or of a node which is target of a replace
     * element content expression are identified and removed from the pending
     * update list. */
    final int l = nodes.size();
    int ni = 0;
    int c = 0;
    while(ni < l - 1) {
      final int pre = nodes.get(ni++);
      // If a node is deleted or replaced or affected by a replace element
      // content expression ...
      final int[] destroyed = updatePrimitives.get(pre).
          destroyedNodeIdentities().toArray();
      for(final int pd : destroyed) {
        final int followingAxisPre = pd + data.size(pd, data.kind(pd));
        // mark obsolete target nodes on the descendant axis.
        while(ni < l && nodes.get(ni) < followingAxisPre) {
          nodes.set(ni++, -1);
          c++;
        }
      }
    }
    // return if nothing changed on the pending update list
    if(c == 0) return;

    // Create a new list that contains necessary targets only
    final IntList newNodes = new IntList(nodes.size() - c);
    for(int i = 0; i < nodes.size(); i++) {
      final int pre = nodes.get(i);
      if(pre != -1) newNodes.add(pre);
    }
    nodes = newNodes;
  }
}
