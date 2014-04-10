package org.basex.data.atomic;

import java.util.*;

import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Implementation of the Atomic Update Cache (AUC).
 *
 * <p>A container/list for atomic updates. Updates must be added from the lowest to
 * the highest PRE value (regarding the location of the update). Updates are finally
 * applied by this container from the highest to the lowest PRE value (reverse document
 * order) to support efficient structural bulk updates etc.</p>
 *
 * <p>If a collection of updates is carried out via the AUC there are several
 * benefits:</p>
 *
 * <ol>
 *   <li> Efficient distance adjustments after structural changes.</li>
 *   <li> Tree-Aware Updates (TAU): identification of superfluous updates (like updating
 *        the descendants of a deleted node).</li>
 *   <li> Resolution of text node adjacency.</li>
 *   <li> Merging of atomic updates to reduce number of I/Os. </li>
 * </ol>
 *
 * <p>To avoid ambiguity it is not allowed to add:</p>
 * <ul>
 * <li> more than one destructive update like {@link Delete} or {@link Replace} operating
 *      on the same node.</li>
 * <li> more than one {@link Rename} or {@link UpdateValue} operating
 *      on the same node.</li>
 * <li> sequences like <delete X, insert N at X>: This sequence would be carried out back
 * to front: first the insert, then the delete. This would lead to the inserted node N
 * being deleted by the 'delete X' statement. The correct order for this sequqence would
 * be <insert N at X, delete X>. </li>
 * <li> and so forth ... see check() function for details. </li>
 * </ul>
 *
 * <p>Updates are added in a streaming fashion where the most recently added update is
 * remembered. This avoids additional traversals of the AUC during consistency checks and
 * further optimizations.</p>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class AtomicUpdateCache {
  /** List of structural updates (nodes are inserted to / deleted from the table. */
  private final List<StructuralUpdate> struct;
  /** Value / non-structural updates like rename. */
  private final List<BasicUpdate> val;
  /** Most recently added update buffer. Used to merge/discard updates and to detect
   * inconsistencies on-the-fly eliminating the need to traverse all updates. */
  private BasicUpdate recent;
  /** Most recently added structural atomic update - if there is any. Used to calculate accumulated
   * pre value shifts on-the-fly, as {@link BasicUpdate} don't carry this information. */
  private BasicUpdate recentStruct;
  /** Target data reference. */
  public final Data data;

  /**
   * Constructor.
   * @param d target data reference
   */
  public AtomicUpdateCache(final Data d) {
    struct = new ArrayList<StructuralUpdate>();
    val = new ArrayList<BasicUpdate>();
    data = d;
  }

  /**
   * Adds a delete atomic to the list.
   * @param pre PRE value of the target node/update location
   */
  public void addDelete(final int pre) {
    considerAtomic(Delete.getInstance(data, pre), false);
  }

  /**
   * Adds an insert atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param par new parent of the inserted nodes
   * @param clip insertion sequence data clip
   * @param attr insert attribute if true or a node of any other kind if false
   */
  public void addInsert(final int pre, final int par, final DataClip clip,
      final boolean attr) {
    considerAtomic(attr ? InsertAttr.getInstance(pre, par, clip) :
      Insert.getInstance(pre, par, clip), false);
  }

  /**
   * Adds a replace atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param clip insertion sequence data clip
   */
  public void addReplace(final int pre, final DataClip clip) {
    considerAtomic(Replace.getInstance(data, pre, clip), false);
  }

  /**
   * Adds a rename atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param n new name for the target node
   * @param u new uri for the target node
   */
  public void addRename(final int pre, final byte[] n, final byte[] u) {
    considerAtomic(Rename.getInstance(data, pre, n, u), false);
  }

  /**
   * Adds an updateValue atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param v new value for the target node
   */
  public void addUpdateValue(final int pre, final byte[] v) {
    considerAtomic(UpdateValue.getInstance(data, pre, v), false);
  }

  /**
   * Resets the list.
   */
  public void clear() {
    struct.clear();
    val.clear();
    recent = null;
    recentStruct = null;
  }

  /**
   * Adds an update to the corresponding list.
   * @param candidate atomic update
   * @param slack skip consistency checks etc. if true (used during text node merging)
   */
  private void considerAtomic(final BasicUpdate candidate, final boolean slack) {
    // fill the one-atomic-update buffer
    if(recent == null) {
      recent = candidate;
      if(recent instanceof StructuralUpdate)
        recentStruct = candidate;
      return;
    }

    if(candidate instanceof StructuralUpdate) {
      ((StructuralUpdate) candidate).accumulatedShifts +=
          recentStruct == null ? 0 : recentStruct.accumulatedShifts();
    }

    // prepare & optimize incoming update
    if(slack) {
      add(candidate, false);
    } else {
      check(recent, candidate);
      if(treeAwareUpdates(recent, candidate)) return;

      final BasicUpdate m = recent.merge(data, candidate);
      if(m != null) add(m, true);
      else add(candidate, false);

    }
  }

  /**
   * Adds the given update to the updates/buffer depending on the type and whether it's
   * been merged or not.
   *
   * @param u update
   * @param merged if true, the given update has been merged w/ the recent one
   */
  private void add(final BasicUpdate u, final boolean merged) {
    if(u == null) return;

    if(!merged) {
      if(recent instanceof StructuralUpdate)
        struct.add((StructuralUpdate) recent);
      else val.add(recent);
    }
    recent = u;
    if(u instanceof StructuralUpdate)
      recentStruct = u;
  }

  /**
   * Flushes the buffer that contains the most previously added atomic update.
   */
  private void flush() {
    if(recent != null) {
      add(recent, false);
      recent = null;
      recentStruct = null;
    }
  }

  /**
   * Returns the number of structural updates.
   * @return number of structural updates
   */
  public int updatesSize() {
    flush();
    return struct.size() + val.size();
  }

  /**
   * Checks the given sequence of two updates for violations.
   *
   * Updates must be ordered strictly from the lowest to the highest PRE value.
   * Deletes must follow inserts.
   *
   * A single node must not be affected by more than one {@link Rename},
   * {@link UpdateValue} operation.
   *
   * A single node must not be affected by more than one destructive operation. These
   * operations include {@link Replace}, {@link Delete}.
   *
   * @param a first update in sequence
   * @param b second update in sequence
   */
  private static void check(final BasicUpdate a, final BasicUpdate b) {
    // check order of location PRE, must be strictly ordered low-to-high
    if(b.location < a.location)
      throw Util.notExpected("Invalid order at location " + a.location);

    if(b.location == a.location) {
      // check invalid sequence of {@link Delete}, {@link Insert}
      // - the inserted node would directly be deleted without this restriction
      if(b instanceof Insert || b instanceof InsertAttr)
        if(a instanceof Delete)
          throw Util.notExpected("Invalid sequence of delete, insert at location "
          + a.location);
        else if(a instanceof Replace)
          throw Util.notExpected("Invalid sequence of replace, insert at location "
              + a.location);

      // check multiple {@link Delete}, {@link Replace}
      if(b.destructive() && a.destructive())
        throw Util.notExpected("Multiple deletes/replaces on node " + a.location);

      // check multiple {@link Rename}
      if(b instanceof Rename && a instanceof Rename)
        throw Util.notExpected("Multiple renames on node " + a.location);

      // check multiple {@link UpdateValue}
      if(b instanceof UpdateValue && a instanceof UpdateValue)
        throw Util.notExpected("Multiple updates on node " + a.location);

      /* Check invalid order of destructive/non-destructive updates to support TAU
       *  cases like: <rename X, delete X>: node X would be deleted and then X+1 renamed,
       *  as this shifts down to X.
       */
      if(b.destructive() && !(a instanceof StructuralUpdate))
        throw Util.notExpected("Invalid sequence of value update and destructive update at" +
            " location " + a.location);
    }
  }

  /**
   * Checks if the second update is superfluous. An update is considered to be superfluous
   * if it targets a position in the subtree of a to-be-removed node.
   * @param a first update in sequence
   * @param b second update in sequence
   * @return true if second update superfluous
   */
  private boolean treeAwareUpdates(final BasicUpdate a, final BasicUpdate b) {
    if(a.destructive()) {
      // we determine the lowest and highest PRE values of a superfluous update
      final int pre = a.location;
      final int fol = pre + data.size(pre, data.kind(pre));
      /* CASE 1: candidate operates on the subtree of T and appends a node to the end of
       * the subtree (target PRE may be equal)...
       * CASE 2: operates within subtree of T */
      if(b.location <= fol && (b instanceof Insert || b instanceof InsertAttr) &&
          b.parent >= pre && b.parent < fol ||
        b.location < fol) {
        return true;
      }
    }
    return false;
  }

  /**
   * Executes the updates. Resolving text node adjacency can be skipped if adjacent text
   * nodes are not to be expected.
   * @param mergeTexts adjacent text nodes are to be expected and must be merged
   */
  public void execute(final boolean mergeTexts) {
    data.cache = true;
    applyUpdates();
    adjustDistances();
    if(mergeTexts)
      resolveTextAdjacency();
    data.cache = false;
    clear();
  }

  /**
   * Carries out structural updates.
   */
  public void applyUpdates() {
    // check if previous update still in buffer
    flush();

    // value updates applied front-to-back, doens't matter as there are no row shifts
    for(final BasicUpdate u : val)
      u.apply(data);
    // structural updates are applied back-to-front
    for(int i = struct.size() - 1; i >= 0; i--)
      struct.get(i).apply(data);
  }

  /**
   * Adjusts distances to restore parent-child relationships that have been invalidated
   * by structural updates.
   *
   * Each structural update (insert/delete) leads to a shift of higher PRE values. This
   * invalidates parent-child relationships. Distances are only adjusted after all
   * structural updates have been carried out to make sure each node (that has to be
   * updated) is only touched once.
   */
  private void adjustDistances() {
    final IntSet alreadyUpdatedNodes = new IntSet();

    for(final StructuralUpdate update : struct) {
      int newPreOfAffectedNode = update.preOfAffectedNode + update.accumulatedShifts;

      /* Update distance for the affected node and all following siblings of nodes
       * on the ancestor-or-self axis. */
      while(newPreOfAffectedNode < data.meta.size) {
        if(alreadyUpdatedNodes.contains(newPreOfAffectedNode)) break;
        data.dist(newPreOfAffectedNode, data.kind(newPreOfAffectedNode),
            calculateNewDistance(newPreOfAffectedNode));
        alreadyUpdatedNodes.add(newPreOfAffectedNode);
        newPreOfAffectedNode +=
            data.size(newPreOfAffectedNode, data.kind(newPreOfAffectedNode));
      }
    }
  }

  /**
   * Calculates the new distance value for the given node after updates have been
   * applied.
   * @param pre the new PRE value of the node after structural updates have
   * been applied
   * @return new distance for the given PRE node
   */
  private int calculateNewDistance(final int pre) {
    final int kind = data.kind(pre);
    final int distanceBefore = data.dist(pre, kind);
    final int preBefore = calculatePreValue(pre, true);
    final int parentBefore = preBefore - distanceBefore;
    final int parentAfter = calculatePreValue(parentBefore, false);
    return pre - parentAfter;
  }

  /**
   * Calculates the PRE value of a given node before/after updates.
   *
   * Finds all updates that affect the given node N. The result is than calculated based
   * on N and the accumulated PRE value shifts introduced by these updates.
   *
   * If a node has been inserted at position X and this method is used to calculate the
   * PRE value of X before updates, X is the result. As the node at position X has not
   * existed before the insertion, its PRE value is unchanged. If in contrast the PRE
   * value is calculated after updates, the result is X+1, as the node with the original
   * position X has been shifted by the insertion at position X.
   *
   * Make sure accumulated shifts have been calculated before calling this method!
   *
   * @param pre PRE value
   * @param beforeUpdates calculate PRE value before shifts/updates have been applied
   * @return index of update, or -1
   */
  public int calculatePreValue(final int pre, final boolean beforeUpdates) {
    // find update that affects the given PRE value
    int i = find(pre, beforeUpdates);
    // given PRE not changed by updates
    if(i == -1) return pre;
    // refine the search to determine accumulated shifts for the given PRE
    i = refine(struct, i, beforeUpdates);
    final int acm = struct.get(i).accumulatedShifts;
    return beforeUpdates ? pre - acm : pre + acm;
  }

  /**
   * Used to find the update that holds the accumulated shift value that is needed to
   * recalculate the given PRE value. In a low-to-high ordered list this is the right-most
   * update with a target PRE value smaller or equal the given PRE value, v.v.
   *
   * Finds the position of the update that affects the given PRE value P.
   * If there are multiple updates whose affected PRE value equals P, the search
   * has to be further refined as this method returns only the first match.
   * @param pre given PRE value
   * @param beforeUpdates compare based on PRE values before/after updates
   * @return index of update
   */
  private int find(final int pre, final boolean beforeUpdates) {
    int left = 0;
    int right = struct.size() - 1;

    while(left <= right) {
      if(left == right) {
        if(c(struct, left, beforeUpdates) <= pre) return left;
        return -1;
      }
      if(right - left == 1) {
        if(c(struct, right, beforeUpdates) <= pre) return right;
        if(c(struct, left, beforeUpdates) <= pre) return left;
        return -1;
      }
      final int middle = left + right >>> 1;
      final int value = c(struct, middle, beforeUpdates);
      if(value == pre) return middle;
      else if(value > pre) right = middle - 1;
      else left = middle;
    }

    // empty array
    return -1;
  }

  /**
   * Finds the update with the lowest index in the given list that affects the same
   * PRE value as the update with the given index.
   * @param l list of updates
   * @param index of update
   * @param beforeUpdates find update for PRE values before updates have been applied
   * @return update with the highest index that invalidates the distance of the given
   * node
   */
  private static int refine(final List<StructuralUpdate> l, final int index,
      final boolean beforeUpdates) {
    int i = index;
    final int value = c(l, i++, beforeUpdates);
    while(i < l.size() && c(l, i, beforeUpdates) == value) i++;
    return i - 1;
  }

  /**
   * Recalculates the PRE value of the first node whose distance is affected by the
   * given update.
   * @param l list of updates
   * @param index index of the update
   * @param beforeUpdates calculate PRE value before or after updates
   * @return PRE value
   */
  private static int c(final List<StructuralUpdate> l, final int index,
      final boolean beforeUpdates) {
    final StructuralUpdate u = l.get(index);
    return u.preOfAffectedNode + (beforeUpdates ? u.accumulatedShifts : 0);
  }

  /**
   * Resolves unwanted text node adjacency which can result from structural changes in
   * the database. Adjacent text nodes are two text nodes A and B, where
   * PRE(B)=PRE(A)+1 and PARENT(A)=PARENT(B).
   */
  private void resolveTextAdjacency() {
    // Text node merges are also gathered on a separate list to leverage optimizations.
    final List<Delete> deletes = new LinkedList<Delete>();

    // keep track of the visited locations to avoid superfluous checks
    int smallestVisited = Integer.MAX_VALUE;
    // Text nodes have to be merged from the highest to the lowest pre value
    for(int i = struct.size() - 1; i >= 0; i--) {
      final StructuralUpdate u = struct.get(i);
      final DataClip insseq = u.getInsertionData();
      // calculate the new location of the update, here we have to check for adjacency
      final int newLocation = u.location + u.accumulatedShifts - u.shifts;
      final int beforeNewLocation = newLocation - 1;
      // check surroundings of this location for adjacent text nodes depending on the
      // kind of update, first the one with higher PRE values (due to shifts!)
      // ... for insert/replace ...
      if(insseq != null) {
        // calculate the current following node
        final int followingNode = newLocation + insseq.size();
        final int beforeFollowingNode = followingNode - 1;
        // check the nodes at the end of/after the insertion sequence
        if(beforeFollowingNode < smallestVisited) {
          final Delete del = mergeTextNodes(beforeFollowingNode);
          if(del != null) deletes.add(0, del);
          smallestVisited = beforeFollowingNode;
        }
      }
      // check nodes for delete and for insert before the updated location
      if(beforeNewLocation < smallestVisited) {
        final Delete del = mergeTextNodes(beforeNewLocation);
        if(del != null) deletes.add(0, del);
        smallestVisited = beforeNewLocation;
      }
    }

    final AtomicUpdateCache atomicDeletes = new AtomicUpdateCache(data);
    for(final Delete delete : deletes) atomicDeletes.considerAtomic(delete, true);
    deletes.clear();
    atomicDeletes.applyUpdates();
    atomicDeletes.adjustDistances();
    atomicDeletes.clear();
  }

  /**
   * Returns atomic text node merging operations if necessary for the given node PRE and
   * its right neighbor PRE+1.
   * @param a node PRE value
   * @return list of text merging operations
   */
  private Delete mergeTextNodes(final int a) {
    final int s = data.meta.size;
    final int b = a + 1;
    // don't leave table
    if(a >= s || b >= s || a < 0 || b < 0) return null;
    // only merge texts
    if(data.kind(a) != Data.TEXT || data.kind(b) != Data.TEXT) return null;
    // only merge neighboring texts
    if(data.parent(a, Data.TEXT) != data.parent(b, Data.TEXT)) return null;

    // apply text node updates on the fly and throw them away
    UpdateValue.getInstance(data, a, Token.concat(data.text(a, true),
        data.text(b, true))).
      apply(data);
    // deletes must be cached to add them front-to-back to atomic update list
    return Delete.getInstance(data, b);
  }
}