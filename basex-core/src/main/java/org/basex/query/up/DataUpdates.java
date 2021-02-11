package org.basex.query.up;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class 'caches' all updates, fn:put operations and other database related operations that
 * are initiated within a snapshot. Regarding the XQUF specification it fulfills the purpose of
 * a 'pending update list'.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class DataUpdates {
  /** Data reference. */
  private final Data data;
  /** Write databases back to disk. */
  private final boolean writeback;

  /** Mapping between pre values of the target nodes and all node updates
   * which operate on this target. */
  private IntObjMap<NodeUpdates> nodeUpdates = new IntObjMap<>();
  /** Database updates. */
  private final List<DBUpdate> dbUpdates = new LinkedList<>();
  /** Put operations which reflect all changes made during the snapshot, hence executed
   * after updates have been carried out. */
  private final IntObjMap<Put> puts = new IntObjMap<>();

  /** Pre values of target nodes. */
  private IntList nodes = new IntList(0);
  /** Atomic update cache. */
  private AtomicUpdateCache auc;
  /** Number of updates. */
  private int size;

  /**
   * Constructor.
   * @param data data reference
   * @param qc query context
   */
  DataUpdates(final Data data, final QueryContext qc) {
    this.data = data;
    writeback = qc.context.options.get(MainOptions.WRITEBACK);
  }

  /**
   * Adds an update primitive to the list.
   * @param up update primitive
   * @param tmp temporary mem data
   * @throws QueryException query exception
   */
  void add(final DataUpdate up, final MemData tmp) throws QueryException {
    if(up instanceof NodeUpdate) {
      for(final NodeUpdate nodeUp : ((NodeUpdate) up).substitute(tmp)) {
        nodeUpdates.computeIfAbsent(nodeUp.pre, NodeUpdates::new).add(nodeUp);
      }

    } else if(up instanceof Put) {
      final Put p = (Put) up;
      final int id = p.id;
      final Put old = puts.get(id);
      if(old == null) puts.put(id, p);
      else old.merge(p);

    } else {
      final DBUpdate dbUp = (DBUpdate) up;
      for(final DBUpdate o : dbUpdates) {
        if(o.type == dbUp.type) {
          o.merge(dbUp);
          return;
        }
      }
      dbUpdates.add(dbUp);
    }
  }

  /**
   * Checks updates for violations. If a violation is found, the complete update process is aborted.
   * @param memData temporary data instance
   * @param qc query context
   * @throws QueryException query exception
   */
  void prepare(final MemData memData, final QueryContext qc) throws QueryException {
    // Prepare/check database operations
    for(final DBUpdate update : dbUpdates) update.prepare();

    // Prepare/check XQUP primitives:
    final int sz = nodeUpdates.size();
    nodes = new IntList(sz);
    for(int i = 1; i <= sz; i++) nodes.add(nodeUpdates.key(i));
    nodes.sort();

    for(int i = 0; i < sz; ++i) {
      final NodeUpdates updates = nodeUpdates.get(nodes.get(i));
      for(final NodeUpdate update : updates.updates) update.prepare(memData, qc);
    }

    // check attribute duplicates
    int p = nodes.size() - 1;
    int par = -1;
    while(p >= 0) {
      // parent of a previous attribute has already been checked
      if(par == nodes.get(p) && --p < 0) break;
      int pre = nodes.get(p);

      // catching optimize statements which have PRE == -1 as a target
      if(pre == -1) break;

      final int k = data.kind(pre);
      if(k == Data.ATTR) {
        par = data.parent(pre, Data.ATTR);
        final IntList il = new IntList();
        while(p >= 0 && (pre = nodes.get(p)) > par) {
          il.add(pre);
          --p;
        }
        if(par != -1) il.add(par);
        checkNames(il.finish());
      } else {
        if(k == Data.ELEM) checkNames(pre);
        --p;
      }
    }

    // build atomic update cache
    auc = createAtomicUpdates(preparePrimitives());
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
   * @param qc query context
   * @throws QueryException query exception
   */
  void apply(final QueryContext qc) throws QueryException {
    // execute database updates
    auc.execute(true);
    auc = null;

    // execute database operations
    Collections.sort(dbUpdates);
    final int sz = dbUpdates.size();
    for(int i = 0; i < sz; i++) {
      dbUpdates.get(i).apply();
      dbUpdates.set(i, null);
    }

    // execute fn:put operations
    for(final Put put : puts.values()) put.apply();

    try {
      Optimize.finish(data);
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(null, ex);
    }

    /* optional: export file if...
     * - WRITEBACK option is turned on
     * - an original file path exists (and does not start with a tilde)
     * - data is a main-memory instance
     */
    final String original = data.meta.original;
    if(!(original.isEmpty() || Strings.startsWith(original, '~')) && data.inMemory()) {
      if(writeback) {
        try {
          Export.export(data, original, qc.context.options, null);
        } catch(final IOException ex) {
          throw UPDBERROR_X.get(null, ex);
        }
      } else {
        FnTrace.trace(Token.token(original + ": Updates are not written back."), null, qc);
      }
    }
  }

  /**
   * Prepares the {@link NodeUpdate} for execution incl. ordering,
   * and removes the update primitive references to save memory.
   * @return ordered list of update primitives
   */
  private List<NodeUpdate> preparePrimitives() {
    final List<NodeUpdate> upd = new ArrayList<>();
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int pre = nodes.get(i);
      final NodeUpdates n = nodeUpdates.get(pre);
      for(final NodeUpdate p : n.finish()) {
        upd.add(p);
        size += p.size();
      }
    }
    for(int i = dbUpdates.size() - 1; i >= 0; i--) {
      size += dbUpdates.get(i).size();
    }
    nodeUpdates = null;
    nodes = null;
    upd.sort(new NodeUpdateComparator());
    return upd;
  }

  /**
   * Creates a list of atomic updates that can be applied to the database.
   * @param l list of ordered {@link NodeUpdate}
   * @return list of atomic updates ready for execution
   */
  private AtomicUpdateCache createAtomicUpdates(final List<NodeUpdate> l) {
    final AtomicUpdateCache ac = new AtomicUpdateCache(data);
    //  from the lowest to the highest score, corresponds w/ from lowest to highest PRE
    final int sz = l.size();
    for(int i = 0; i < sz; i++) {
      final NodeUpdate u = l.get(i);
      u.addAtomics(ac);
      l.set(i, null);
    }
    return ac;
  }

  /**
   * Returns the number of performed updates.
   * @return number of updates
   */
  int size() {
    return size;
  }

  /**
   * Checks nodes for namespace conflicts and duplicate attributes.
   * @param pres pre values of nodes to check (in descending order)
   * @throws QueryException query exception
   */
  private void checkNames(final int... pres) throws QueryException {
    // check for namespace conflicts
    final NamePool names = new NamePool();
    for(final int pre : pres) {
      final NodeUpdates ups = nodeUpdates.get(pre);
      // add changes introduced by updates to check namespaces and duplicate attributes
      if(ups != null) {
        for(final NodeUpdate up : ups.updates) up.update(names);
      }
    }
    // check namespaces
    final byte[][] ns = names.nsOK();
    if(ns != null) throw UPNSCONFL2_X_X.get(null, ns[0], ns[1]);

    // pre values of attributes that have already been added to the name pool
    final IntSet set = new IntSet();
    for(final int pre : pres) {
      // pre values consist exclusively of element and attribute nodes
      if(data.kind(pre) == Data.ATTR) {
        addToPool(pre, names);
        set.add(pre);
      } else {
        final int ps = pre + data.attSize(pre, Data.ELEM);
        for(int p = pre + 1; p < ps; ++p) {
          if(!set.contains(p)) addToPool(p, names);
        }
      }
    }
    final QNm dup = names.duplicate();
    if(dup != null) throw UPATTDUPL_X.get(null, dup);
  }

  /**
   * Adds an attribute to the pool.
   * @param pre pre value
   * @param names name pool
   */
  private void addToPool(final int pre, final NamePool names) {
    final byte[] nm = data.name(pre, Data.ATTR);
    final QNm name = new QNm(nm);
    if(name.hasPrefix()) {
      final int uriId = data.nspaces.uriIdForPrefix(Token.prefix(nm), pre, data);
      if(uriId != 0) name.uri(data.nspaces.uri(uriId));
    }
    names.add(name, NodeType.ATTRIBUTE);
  }

}
