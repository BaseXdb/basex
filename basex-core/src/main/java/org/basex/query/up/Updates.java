package org.basex.query.up;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.node.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * <p>Implementation of the W3C XQUERY UPDATE FACILITY 1.0.</p>
 *
 * <p>Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes the updates.</p>
 *
 * <p>Fragment updates are treated like database updates. An artificial data
 * instance is created for each fragment. Ultimately the updating process for
 * a fragment is the same as for a database node.</p>
 *
 * <p>The complete updating process is custom-tailored to the
 * sequential table encoding of BaseX. As a general rule, all updates are
 * collected and applied for each database from bottom to top, regarding the
 * PRE values of the corresponding target nodes. Updates on the highest PRE
 * values are applied first.</p>
 *
 * <p>Updates work like the following:</p>
 *
 * <ol>
 * <li> Each call of an updating expression creates an {@link NodeUpdate}.</li>
 * <li> All update primitives for a snapshot/query are collected here.</li>
 * <li> Primitives are kept separately for each database that is addressed. This
 *      way we can operate on PRE values instead of node IDs, skip mapping
 *      overhead and further optimize the process.
 *      {@link DataUpdates}</li>
 * <li> Primitives are further kept separately for each database node - each
 *      individual target PRE value. There's a specific container for this:
 *      {@link NodeUpdates}</li>
 * <li> Transform expressions are executed in an 'isolated' updating environment,
 *      see {@link TransformModifier}. All the other updates are executed by
 *      a {@link DatabaseModifier}.</li>
 * <li> After the query has been parsed and all update primitives have been added
 *      to the list, constraints, which cannot be taken care of on the fly, are
 *      checked. If no problems occur, updates are TO BE carried out.</li>
 * <li> Before applying the updates the {@link NodeUpdateComparator} helps to order
 *      {@link NodeUpdate} for execution. Each primitive then creates a sequence of
 *      {@link BasicUpdate} which are passed to the {@link Data} layer via an
 *      {@link AtomicUpdateCache}. This list takes care of optimization and also text node
 *      merging.</li>
 * </ol>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public final class Updates {
  /** Current context modifier. */
  public ContextModifier mod;
  /** All file paths that are targeted during a snapshot by an fn:put expression. */
  public final TokenSet putPaths = new TokenSet();

  /** Mapping between fragment IDs and the temporary data instances
   * to apply updates on the corresponding fragments. */
  private final IntObjMap<MemData> fragmentIDs = new IntObjMap<>();

  /**
   * Adds an update primitive to the current context modifier.
   * @param up update primitive
   * @param qc query context
   * @throws QueryException query exception
   */
  public void add(final Update up, final QueryContext qc) throws QueryException {
    if(mod == null) mod = new DatabaseModifier();
    mod.add(up, qc);
  }

  /**
   * Determines the data reference and pre value for an update primitive
   * which has a fragment as a target node. If an ancestor of the given target
   * node has already been added to the pending update list, the corresponding
   * data reference and the pre value of the given target node within this
   * database table are calculated. Otherwise a new data instance is created.
   *
   * @param target target fragment
   * @param qc query context
   * @return database node created from input fragment
   */
  public DBNode determineDataRef(final ANode target, final QueryContext qc) {
    if(target instanceof DBNode) return (DBNode) target;

    // determine highest ancestor node
    ANode anc = target;
    final BasicNodeIter it = target.ancestor();
    for(ANode p; (p = it.next()) != null;) anc = p;

    /* See if this ancestor has already been added to the pending update list.
     * In this case a database has already been created.
     */
    final int ancID = anc.id;
    MemData data = fragmentIDs.get(ancID);
    // if data doesn't exist, create a new one
    if(data == null) {
      data = (MemData) anc.dbCopy(qc.context.options).data();
      // create a mapping between the fragment id and the data reference
      fragmentIDs.put(ancID, data);
    }

    // determine the pre value of the target node within its database
    final int pre = preSteps(anc, target.id);
    return new DBNode(data, pre);
  }

  /**
   * Prepares update operations.
   * @param qc query context
   * @return updated data references
   * @throws QueryException query exception
   */
  public HashSet<Data> prepare(final QueryContext qc) throws QueryException {
    final HashSet<Data> datas = new HashSet<>();
    if(mod != null) mod.prepare(datas, qc);
    return datas;
  }

  /**
   * Executes all updates.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void apply(final QueryContext qc) throws QueryException {
    if(mod != null) mod.apply(qc);
  }

  /**
   * Returns the names of all databases that will be updated.
   * @return databases
   */
  public StringList databases() {
    final StringList sl = new StringList(1);
    if(mod != null) mod.databases(sl);
    return sl;
  }

  /**
   * Number of updates on the pending update list.
   * @return #updates
   */
  public int size() {
    return mod == null ? 0 : mod.size();
  }

  /**
   * Recursively determines the pre value for a given fragment node within the
   * corresponding data reference.
   * @param node current
   * @param trgID ID of fragment for which we calculate the pre value
   * @return pre value
   */
  private static int preSteps(final ANode node, final int trgID) {
    if(node.id == trgID) return 0;

    int s = 1;
    BasicNodeIter it = node.attributes();
    for(ANode n; (n = it.next()) != null;) {
      final int st = preSteps(n, trgID);
      if(st == 0) return s;
      s += st;
    }

    it = node.children();
    // n.id <= trgID: rewritten to catch ID overflow
    for(ANode n; (n = it.next()) != null && trgID - n.id >= 0;) {
      s += preSteps(n, trgID);
    }
    return s;
  }
}
