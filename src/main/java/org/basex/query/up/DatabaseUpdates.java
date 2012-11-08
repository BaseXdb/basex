package org.basex.query.up;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class 'caches' all updates, fn:put operations and other database related
 * operations that are initiated within a snapshot. Regarding the XQUF specification it
 * fulfills the purpose of a 'pending update list'.
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
  /** Database operations which are applied after all updates have been executed. */
  private final List<BasicOperation> dbops = new LinkedList<BasicOperation>();
  /** Put operations which reflect all changes made during the snapshot, hence executed
   * after updates have been carried out. */
  private final Map<Integer, Put> puts = new HashMap<Integer, Put>();

  /**
   * Constructor.
   * @param d data reference
   */
  DatabaseUpdates(final Data d) {
    data = d;
  }

  /**
   * Adds an update primitive to the list.
   * @param o update primitive
   * @throws QueryException query exception
   */
  void add(final Operation o) throws QueryException {
    if(o instanceof UpdatePrimitive) {
      for(final UpdatePrimitive subp : ((UpdatePrimitive) o).substitute()) {
        final int pre = subp.targetPre;
        NodeUpdates pc = updatePrimitives.get(pre);
        if(pc == null) {
          pc = new NodeUpdates();
          updatePrimitives.add(pre, pc);
        }
        pc.add(subp);
      }

    } else if(o instanceof Put) {
      final Put p = (Put) o;
      final int id = p.nodeid;
      final Put old = puts.get(id);
      if(old == null)
        puts.put(id, p);
      else
        old.merge(p);

    } else {
      final BasicOperation oo = (BasicOperation) o;
      final BasicOperation d = find(oo);
      if(d == null) dbops.add(oo);
      else d.merge(oo);
    }
  }

  /**
   * Finds a {@link BasicOperation} of the same
   * {@link org.basex.query.up.primitives.BasicOperation.TYPE} in the operations list if
   * there is any.
   * @param oo DBOperation of a specific type
   * @return DBOperation of the same type, or null if there is none
   */
  private BasicOperation find(final BasicOperation oo) {
    for(final BasicOperation o : dbops)
      if(o.type == oo.type) return o;
    return null;
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
    if(!data.startUpdate()) BXDB_OPENED.thrw(null, data.meta.name);
  }

  /**
   * Locks the database for write operations.
   */
  void finishUpdate() {
    // may have been invalidated by db:drop
    if(data != null) data.finishUpdate();
  }

  /**
   * Returns the data instance.
   * @return data data instance
   */
  Data data() {
    return data;
  }

  /**
   * Applies all updates for this specific database.
   * @throws QueryException query exception
   */
  void apply() throws QueryException {
    // execute database updates
    createAtomicUpdates(preparePrimitives()).execute(true);

    // execute database operations
    final BasicOperation[] dbo = new BasicOperation[dbops.size()];
    dbops.toArray(dbo);
    Arrays.sort(dbo);
    for(final BasicOperation d : dbo) {
      d.prepare();
      d.apply();
    }

    // execute fn:put operations
    final Put[] o = puts.values().toArray(new Put[puts.values().size()]);
    for(final Put p : o) p.apply();

    if(data.meta.prop.is(Prop.WRITEBACK) && !data.meta.original.isEmpty()) {
      try {
        Export.export(data, data.meta.original, null);
      } catch(final IOException ex) {
        UPPUTERR.thrw(null, data.meta.original);
      }
    }
  }

  /**
   * Prepares the {@link UpdatePrimitive} for execution incl. ordering.
   * @return ordered list of update primitives
   */
  private List<UpdatePrimitive> preparePrimitives() {
    final List<UpdatePrimitive> upd = new ArrayList<UpdatePrimitive>();
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final NodeUpdates n = updatePrimitives.get(nodes.get(i));
      n.prepare();
      for(final UpdatePrimitive p : n.prim) {
        upd.add(p);
      }
    }
    Collections.sort(upd, new UpdatePrimitiveComparator());
    return upd;
  }

  /**
   * Creates a list of atomic updates that can be applied to the database.
   * @param l list of ordered {@link UpdatePrimitive}
   * @return list of atomic updates ready for execution
   */
  private AtomicUpdateList createAtomicUpdates(final List<UpdatePrimitive> l) {
    final AtomicUpdateList atomics = new AtomicUpdateList(data);
    // from the highest to the lowest score
    for(int i = l.size() - 1; i >= 0; i--) {
      final UpdatePrimitive u = l.get(i);
      u.addAtomics(atomics);
      l.set(i, null);
    }
    return atomics;
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
}
