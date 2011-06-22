package org.basex.query.up;

import static org.basex.query.up.primitives.PrimitiveType.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.core.cmd.Export;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.up.primitives.NodeCopy;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.IntList;
import org.basex.util.IntMap;

/**
 * This class holds all updates for a specific database. Before applied,
 * updates are sorted in a descending manner regarding the pre value of their
 * target nodes. As a result, update operations are applied from bottom to
 * top and we can stick to pre values as primitive identifier as pre value
 * shifts won't have any effect on updates that have not yet been applied.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class AggregatedDatabaseUpdates {
  /** Data reference. */
  private final Data data;
  /** Pre values of target nodes. */
  protected IntList nodes;
  /** Mapping between pre values of the target nodes and all update primitives
   * which operate on this target.
   */
  protected final IntMap<AggregatedNodeUpdates> updatePrimitives =
    new IntMap<AggregatedNodeUpdates>();

  /**
   * Constuctor.
   * @param d data reference
   */
  public AggregatedDatabaseUpdates(final Data d) {
    this.data = d;
  }

  /**
   * Adds an update primitive to the list.
   * @param p update primitive
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive p) throws QueryException {
    final int pre = p.pre;
    AggregatedNodeUpdates pc = updatePrimitives.get(pre);
    if(pc == null) {
      pc = new AggregatedNodeUpdates();
      updatePrimitives.add(pre, pc);
    }
    pc.add(p);
  }

  /**
   * Checks updates for violations. If a violation is found the complete update
   * process is aborted.
   * @throws QueryException query exception
   */
  protected final void check() throws QueryException {
    // get and sort keys (pre/id values)
    final int s = updatePrimitives.size();
    nodes = new IntList(s);
    for(int i = 1; i <= updatePrimitives.size(); i++)
      nodes.add(updatePrimitives.key(i));
    nodes.sort();

    for(int i = 0; i < s; ++i) {
      final AggregatedNodeUpdates ups = updatePrimitives.get(nodes.get(i));
      for(final UpdatePrimitive p : ups.prim) {
        if(p instanceof NodeCopy) ((NodeCopy) p).prepare();
        /* check if the identity of all target nodes of fn:put operations is
           still available after the execution of updates. that includes parent
           nodes as well */

        if(p.type == PUT && ancestorDeleted(nodes.get(i))) {
          UPFOTYPE.thrw(p.input, p);
        }
      }
    }

    // check attribute duplicates
    int p = nodes.size() - 1;
    int par = -1;
    while(p >= 0) {
      // parent of a previous attribute has already been checked
      if(par == nodes.get(p) && --p < 0) break;

      int pre = nodes.get(p);
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
   * Checks nodes for duplicate attributes and namespace conflicts.
   * @param pres pre values of nodes to check (in descending order)
   * @throws QueryException query exception
   */
  private void checkNames(final int... pres) throws QueryException {
    final NamePool pool = new NamePool();
    final IntList il = new IntList();

    for(final int pre : pres) {
      final AggregatedNodeUpdates ups = updatePrimitives.get(pre);
      if(ups != null)
        for(final UpdatePrimitive up : ups.prim) up.update(pool);

      // pre values consist exclusively of element and attribute nodes
      if(data.kind(pre) == Data.ATTR) {
        il.add(pre);
      } else {
        final int ps = pre + data.attSize(pre, Data.ELEM);
        for(int p = pre + 1; p < ps; ++p) {
          final byte[] nm = data.name(p, Data.ATTR);
          if(!il.contains(p)) {
            final QNm name = new QNm(nm);
            final byte[] uri = data.ns.uri(data.ns.uri(nm, p));
            if(uri != null) name.uri(uri);
            pool.add(name, NodeType.ATT);
          }
        }
      }
    }

    // find duplicate attributes
    final QNm dup = pool.duplicate();
    if(dup != null) UPATTDUPL.thrw(null, dup);

    // find namespace conflicts
    if(!pool.nsOK()) UPNSCONFL2.thrw(null);
  }

  /**
   * Identifies unnecessary update operations and removes them from the pending
   * update list.
   */
  private void treeAwareUpdates() {
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
      for(int i = 0; i < destroyed.length; i++) {
        final int pd = destroyed[i];
        final int followingAxisPre = pd + data.size(pd, data.kind(pd));
        // mark obsolete target nodes on the descendant axis.
        while(ni < l && nodes.get(ni) < followingAxisPre) {
          nodes.set(-1, ni++);
          c++;
        }
      }
    }

    // in case nothing changed on the pending update list, return
    if(c == 0) return;

    // Create a new list that contains necessary targets only
    final IntList newNodes = new IntList(nodes.size() - c);
    for(int i = 0; i < nodes.size(); i++) {
      final int pre = nodes.get(i);
      if(pre != -1) newNodes.add(pre);
    }
    nodes = newNodes;
  }

  /**
   * Applies all updates for this specific database.
   * @param ctx query context
   * @throws QueryException query exception
   */
  protected void apply(final QueryContext ctx) throws QueryException {
    treeAwareUpdates();

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
    AggregatedNodeUpdates recent = null;
    // apply updates from the highest to the lowest pre value
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final AggregatedNodeUpdates current = updatePrimitives.get(nodes.get(i));
      // first run, no recent container
      if(recent == null)
        current.makePrimitivesEffective();
      else
        recent.resolveExternalTextNodeAdjacency(
            current.makePrimitivesEffective());

      recent = current;
    }
    // resolve text adjacency issues of the last container
    recent.resolveExternalTextNodeAdjacency(0);

    data.flush();
    if(data.meta.prop.is(Prop.WRITEBACK) && !data.meta.path.isEmpty()) {
      try {
        Export.export(ctx.context.prop, data, data.meta.path);
      } catch(final IOException ex) {
        UPPUTERR.thrw(null, data.meta.path);
      }
    }
  }

  /**
   * Determines recursively whether an ancestor of a given node is deleted.
   * @param n pre value
   * @return true if ancestor deleted
   */
  protected boolean ancestorDeleted(final int n) {
    final AggregatedNodeUpdates up = updatePrimitives.get(n);
    if(up != null && up.updatesDestroyIdentity(n)) return true;

    final int p = data.parent(n, data.kind(n));
    return p != -1 && ancestorDeleted(p);
  }
}