package org.basex.query.up;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class Updates {
  /** Current context modifier. */
  public final ContextModifier mod;
  /** All file paths that are targeted during a snapshot by an fn:put expression. */
  public final TokenSet putPaths = new TokenSet();
  /** Cached outputs. */
  private ValueBuilder output;

  /** Mapping between fragment IDs and the temporary data instances
   * to apply updates on the corresponding fragments. */
  private final IntObjMap<MemData> fragmentIDs = new IntObjMap<>();

  /**
   * Constructor.
   * @param transform transform flag
   */
  public Updates(final boolean transform) {
    mod = transform ? new TransformModifier() : new DatabaseModifier();
  }

  /**
   * Adds a data reference to list which keeps track of the nodes copied
   * within a transform expression.
   * @param data reference
   */
  public void addData(final Data data) {
    mod.addData(data);
  }

  /**
   * Adds an update primitive to the current context modifier.
   * @param up update primitive
   * @param qc query context
   * @throws QueryException query exception
   */
  public void add(final Update up, final QueryContext qc) throws QueryException {
    mod.add(up, qc);
  }

  /**
   * Adds output.
   * @param value value to be added
   * @param qc query context
   */
  public synchronized void addOutput(final Value value, final QueryContext qc) {
    if(output == null) output = new ValueBuilder(qc);
    output.add(value);
  }

  /**
   * Returns value to be output.
   * @param reset reset cache
   * @return value
   */
  public synchronized Value output(final boolean reset) {
    final ValueBuilder vb = output;
    if(reset) output = null;
    return vb != null ? vb.value() : Empty.VALUE;
  }

  /**
   * Determines the data reference and pre value for an update primitive
   * which has a fragment as a target node. If an ancestor of the given target
   * node has already been added to the pending update list, the corresponding
   * data reference and the pre value of the given target node within this
   * database table are calculated. Otherwise a new data instance is created.
   * This function is called during query evaluation.
   *
   * @param target target fragment
   * @param qc query context
   * @return database node created from input fragment
   */
  public DBNode determineDataRef(final ANode target, final QueryContext qc) {
    if(target instanceof DBNode) return (DBNode) target;

    // determine highest ancestor node
    ANode tmp = target;
    final BasicNodeIter iter = target.ancestorIter();
    for(ANode n; (n = iter.next()) != null;) tmp = n;
    final ANode root = tmp;

    // see if this ancestor has already been added to the pending update list
    // if data instance does not exist, create mapping between fragment id and data reference
    MemData data;
    synchronized(fragmentIDs) {
      data = fragmentIDs.computeIfAbsent(root.id, () -> (MemData) root.copy(qc).data());
    }

    // determine the pre value of the target node within its database
    final int pre = preSteps(root, target.id);
    return new DBNode(data, pre);
  }

  /**
   * Prepares update operations. Called after query evaluation.
   * @param qc query context
   * @return updated data references
   * @throws QueryException query exception
   */
  public HashSet<Data> prepare(final QueryContext qc) throws QueryException {
    final HashSet<Data> datas = new HashSet<>();
    mod.prepare(datas, qc);
    return datas;
  }

  /**
   * Executes all updates. Called after query evaluation.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void apply(final QueryContext qc) throws QueryException {
    mod.apply(qc);
  }

  /**
   * Returns the names of all databases that will be updated. Called after query evaluation.
   * @return databases
   */
  public StringList databases() {
    final StringList sl = new StringList(1);
    mod.databases(sl);
    return sl;
  }

  /**
   * Number of updates on the pending update list.
   * @return #updates
   */
  public int size() {
    return mod.size();
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
    for(final ANode nd : node.attributeIter()) {
      final int st = preSteps(nd, trgID);
      if(st == 0) return s;
      s += st;
    }
    for(final ANode nd : node.childIter()) {
      // n.id <= trgID: rewritten to catch ID overflow
      if(trgID - nd.id < 0) break;
      s += preSteps(nd, trgID);
    }
    return s;
  }
}
