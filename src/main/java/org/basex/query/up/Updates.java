package org.basex.query.up;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.AxisIter;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.Put;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.DataBuilder;
import org.basex.util.IntMap;
import org.basex.util.TokenSet;

/**
 * ***** Implementation of the W3C XQUERY UPDATE FACILITY 1.0 *****
 *
 *
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes the updates.
 *
 * Fragment updates are treated like database updates. An artificial data
 * instance is created for each fragment. Ultimately the updating process for
 * a fragment is the same as for a database node.
 *
 * The complete updating process is custom-tailored to the
 * sequential table encoding of BaseX. As a general rule, all updates are
 * collected and applied for each database from bottom to top, regarding the
 * PRE values of the corresponding target nodes. Updates on the highest pre
 * values are applied first.
 *
 *
 * ***** Updates work like the following: *****
 *
 * 1. Each call of an updating expression creates an {@link UpdatePrimitive}
 * 2. All update primitives for a snapshot/query are collected here
 * 3. Primitives are kept separately for each database that is addressed. This
 *    way we can operate on PRE values instead of node IDs, skip mapping
 *    overhead and further optimize the update process.
 *    {@link AggregatedDatabaseUpdates}
 * 4. Primitives are further kept separately for each database node - each
 *    individual target PRE value. There's a specific container for this:
 *    {@link AggregatedNodeUpdates}
 * 5. Transform expressions are executed in an 'isolated' updating environment,
 *    see {@link TransformModifier}. All the other updates are executed by
 *    a {@link DatabaseModifier}.
 * 6. Fn:put statements are treated exactly like an update primitive. They are
 *    represented by an update primitive.
 * 7. After the query has been parsed and all update primitives have been added
 *    to the list, constraints, which cannot be taken care of on the fly, are
 *    checked. If no problems occur, updates are TO BE carried out.
 *
 * 8. Updates are carried out separately for each database, the following
 *    way:
 *      1. Update primitives are sorted by the PRE value of their target node
 *         in a descending manner. Applying updates from the highest to the
 *         lowest PRE value ensures that updates, not-yet carried out, are not
 *         affected by PRE value shifts. PRE value shifts are a result of
 *         structural changes of the table - which occur each time a node is
 *         inserted or deleted, for more see {@link PrimitiveType}.
 *      2. For each specific target node, updates which are collected in a
 *         {@link AggregatedNodeUpdates} container are executed in a specific
 *         order, depending on their type {@link PrimitiveType}. This order
 *         relates more or less to the XQUF specification, at least it leads to
 *         the same result.
 *
 *      3. Neighboring text nodes have to be merged together (see XQuery Data
 *         Model). Adjacent text nodes can only be a result of structural
 *         updates. With our approach this takes some extra effort, but can be
 *         carried out on-the-fly, applying the two following steps:
 *         1. An {@link AggregatedNodeUpdates} container holds all update
 *            primitives for a specific database node N. If all updates with
 *            target N (all updates in the current container) are carried out,
 *            text node adjacency can only occur on the child axis, or the
 *            sibling axis of N.
 *
 *            Regarding adjacency on the child axis, this can be taken care off
 *            by first applying all updates on this axis and subsequently
 *            checking the affected PRE positions for adjacent text nodes.
 *
 *            The sibling axis of N can only be checked for adjacent text nodes
 *            after an eventual left sibling target container has executed its
 *            updates. Example: We have two siblings A(1) and B(2). A is a text
 *            node, B is an element. The numbers in brackets are their PRE
 *            values. If A is target of a delete primitive and B is target of
 *            an insert before primitive (text 'foo' is inserted) the following
 *            happens: insert before is executed on B, A is deleted, location
 *            around B is checked for adjacent text nodes. No merges occur. We
 *            repeat this for every target node and only apply text node merges
 *            if updates of the next container have been carried out.
 *
 *            If we would carry out text merges immediately, the following
 *            (which leads to inconsistency) happens: insert before
 *            is executed on B, 'foo' is adjacent to A and therefore the two
 *            nodes are merged together, A is finally deleted. As a result we
 *            loose the text node 'foo' during the process, as it is merged
 *            with A and subsequently deleted together with A (consider the pre
 *            value shifts!).
 *
 *
 * XQUF SPECIALTIES WITH BASEX
 *
 * Fn:put: the target node of an fn:put statement cannot be deleted, but it
 *  can be among the deleted nodes as a result of a 'replace element content'
 *  statement.
 *
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class Updates {
  /** Current context modifier. */
  public ContextModifier mod;
  /** Mapping between fragment IDs and the temorary data instances created
   * to apply updates on the corresponding fragments. */
  private final IntMap<Data> fragmentIDs = new IntMap<Data>();
  /** Set which contains all URIs which are targeted during a snapshot. */
  private final TokenSet putUris = new TokenSet();

  /**
   * Adds an update primitive to the current context modifier.
   * @param up update primitive
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive up, final QueryContext ctx)
  throws QueryException {
    if(mod == null) mod = new DatabaseModifier();

    // check for duplicate Put target URIs
    if(up instanceof Put) {
      final Put put = (Put) up;
      if(putUris.add(token(put.path(0))) < 0)
        UPURIDUP.thrw(put.input, put.path(0));
    }

    mod.add(up, ctx);
  }

  /**
   * Determines the data reference and pre value for an update primitive
   * which has a fragment as a target node. If an ancestor of the given target
   * node has already been added to the pending update list, the corresponding
   * data reference and the pre value of the given target node within this
   * database table are calculated. Otherwise a new data instance is created.
   *
   * @param target target fragment
   * @param ctx query context
   * @return database node created from input fragment
   */
  public DBNode determineDataRef(final ANode target, final QueryContext ctx) {
    if(target instanceof DBNode) return (DBNode) target;

    // determine highest ancestor node
    ANode anc = target;
    AxisIter it = target.anc();
    ANode p;
    while((p = it.next()) != null)
      anc = p;

    /* See if this ancestor has already been added to the pending update list.
     * In this case a database has already been created.
     */
    final int ancID = anc.id;
    Data data = fragmentIDs.get(ancID);
    // if data doesn't exist, create a new one
    if(data == null) {
      data =  new MemData(ctx.context.prop);
      new DataBuilder((MemData) data).context(ctx).build(anc);
      // create a mapping between the fragment id and the data reference
      fragmentIDs.add(ancID, data);
    }

    // determine the pre value of the target node within its database
    final int trgID = target.id;
    final int pre = preSteps(anc, trgID);

    return new DBNode(data, pre);
  }

  /**
   * Determines recursively the pre value for a given fragment node within the
   * corresponding data reference.
   * @param node current
   * @param trgID ID of fragment for which we calculate the pre value
   * @return pre value
   */
  private int preSteps(final ANode node, final int trgID) {
    if(node.id == trgID)
      return 0;

    int s = 1;
    ANode n;
    AxisIter it = node.attributes();
    while((n = it.next()) != null) {
      final int st = preSteps(n, trgID);
      if(st == 0) return s;
      s += st;
    }

    it = node.children();
    while((n = it.next()) != null && n.id <= trgID) {
      s += preSteps(n, trgID);
    }

    return s;
  }

  /**
   * Executes all updates.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void applyUpdates(final QueryContext ctx) throws QueryException {
    if(mod != null) mod.applyUpdates(ctx);
  }

  /**
   * Number of updates on the pending update list.
   * @return #updates
   */
  public int size() {
    if(mod == null) return 0;
    return mod.size();
  }
}