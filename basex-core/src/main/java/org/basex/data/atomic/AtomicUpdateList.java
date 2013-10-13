package org.basex.data.atomic;

import java.util.*;

import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * <p>A container/list for atomic updates. Updates are then carried out in the same order
 * that they have been added. No reordering takes place. The user is responsible to add
 * them in the correct order.</p>
 *
 * <p>Updates must be strictly ordered from the highest to the lowest PRE value, otherwise
 * an exception is returned.</p>
 *
 * <p>If a collection of updates is carried out via this container there are several
 * benefits:</p>
 *
 * <ol>
 *   <li> Caching distance updates of the table due to structural changes and carrying
 *        them out in an efficient manner.</li>
 *   <li> Tree-Aware Updates: identification of superfluous updates (like updating the
 *        descendants of a deleted node).</li>
 *   <li> Merging of adjacent text nodes which are not allowed (see XDM).</li>
 * </ol>
 *
 * <p>Mind that two delete atomics on a list targeting the same PRE value location result
 * in two nodes A and B being deleted, due to PRE value shifts after the first delete,
 * where pre(B) = pre(A) + 1. If a list of atomic updates is prepared for execution it
 * should be ordered from the highest to the lowest PRE value.</p>
 *
 * <p>To avoid ambiguity it is not allowed to add:</p>
 * <ul>
 * <li> more than one destructive update like {@link Delete} or {@link Replace} operating
 *      on the same node.</li>
 * <li> more than one {@link Rename} or {@link UpdateValue} operating
 *      on the same node.</li>
 * </ul>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class AtomicUpdateList {
  /** List of structural updates (nodes are inserted to / deleted from the table. */
  private final List<BasicUpdate> updStructural;
  /** List of value updates. */
  private final List<BasicUpdate> updValue;
  /** Target data reference. */
  public final Data data;
  /** States if update constraints have been checked. */
  private boolean ok;
  /** States if update haven been optimized. */
  private boolean opt;
  /** States if this list has been merged, hence accumulations are invalid. */
  private boolean dirty;

  /**
   * Constructor.
   * @param d target data reference
   */
  public AtomicUpdateList(final Data d) {
    updStructural = new ArrayList<BasicUpdate>();
    updValue = new ArrayList<BasicUpdate>();
    data = d;
  }

  /**
   * Adds a delete atomic to the list.
   * @param pre PRE value of the target node/update location
   */
  public void addDelete(final int pre) {
    final int k = data.kind(pre);
    final int s = data.size(pre, k);
    add(new Delete(pre, -s, pre + s), true);
  }

  /**
   * Adds an insert atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param par new parent of the inserted nodes
   * @param clip insertion sequence data clip
   * @param attr insert attribute if true or a node of any other kind if false
   */
  public void addInsert(final int pre, final int par, final DataClip clip, final boolean attr) {
    add(attr ? new InsertAttr(pre, par, clip) : new Insert(pre, par, clip), true);
  }

  /**
   * Adds a replace atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param clip insertion sequence data clip
   */
  public void addReplace(final int pre, final DataClip clip) {
    final int oldsize = data.size(pre, data.kind(pre));
    final int newsize = clip.size();
    add(new Replace(pre, newsize - oldsize, pre + oldsize, clip), true);
  }

  /**
   * Adds a rename atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param k kind of the target node
   * @param n new name for the target node
   * @param u new uri for the target node
   */
  public void addRename(final int pre, final int k, final byte[] n, final byte[] u) {
    add(new Rename(pre, k, n, u), false);
  }

  /**
   * Adds an updateValue atomic to the list.
   * @param pre PRE value of the target node/update location
   * @param k kind of the target node
   * @param v new value for the target node
   */
  public void addUpdateValue(final int pre, final int k, final byte[] v) {
    add(new UpdateValue(pre, k, v), false);
  }

  /**
   * Returns the number of structural updates.
   * @return number of structural updates
   */
  public int structuralUpdatesSize() {
    return updStructural.size();
  }

  /**
   * Resets the list.
   */
  public void clear() {
    updStructural.clear();
    updValue.clear();
    dirty();
  }

  /**
   * Adds an update to the corresponding list.
   * @param u atomic update
   * @param s true if given update performs structural changes, false if update only
   * alters values
   */
  private void add(final BasicUpdate u, final boolean s) {
    if(s) updStructural.add(u);
    else updValue.add(u);
    dirty();
  }

  /**
   * Adds all updates in a given list B to the end of the updates in this list (A).
   *
   * As updates must be unambiguous and are ordered from the highest to the lowest PRE
   * value, make sure that all updates in B access PRE values smaller than any update
   * in A.
   *
   * Mainly used to resolve text node adjacency.
   *
   * @param toMerge given list that is appended to the end of this list
   */
  private void merge(final AtomicUpdateList toMerge) {
    updStructural.addAll(toMerge.updStructural);
    updValue.addAll(toMerge.updValue);
    dirty();
  }

  /**
   * Marks this list dirty.
   */
  private void dirty() {
    ok = false;
    opt = false;
    dirty = true;
  }

  /**
   * Checks the list of updates for violations. Updates must be ordered strictly from
   * the highest to the lowest PRE value.
   *
   * A single node must not be affected by more than one {@link Rename},
   * {@link UpdateValue} operation.
   *
   * A single node must not be affected by more than one destructive operation. These
   * operations include {@link Replace}, {@link Delete}.
   */
  public void check() {
    if(ok || updStructural.size() < 2 && updValue.size() < 2) return;

    int i = 0;
    while(i + 1 < updStructural.size()) {
      final BasicUpdate current = updStructural.get(i);
      final BasicUpdate next = updStructural.get(++i);

      // check order of location PRE
      if(current.location < next.location)
        Util.notexpected("Invalid order at location " + current.location);

      // check multiple {@link Delete}, {@link Replace}
      if(current.location == next.location &&
          current.destructive() && next.destructive())
        Util.notexpected("Multiple deletes/replaces on node " + current.location);
    }

    i = 0;
    while(i + 1 < updValue.size()) {
      final BasicUpdate current = updValue.get(i++);
      final BasicUpdate next = updValue.get(i);

      // check order of location PRE
      if(current.location < next.location)
        Util.notexpected("Invalid order at location " + current.location);

      if(current.location == next.location) {
        // check multiple {@link Rename}
        if(current instanceof Rename && next instanceof Rename)
          Util.notexpected("Multiple renames on node " + current.location);

        // check multiple {@link UpdateValue}
        if(current instanceof UpdateValue && next instanceof UpdateValue)
          Util.notexpected("Multiple updates on node " + current.location);
      }
    }
    ok = true;
  }

  /**
   * Removes superfluous update operations. If a node T is deleted or replaced, all
   * updates on the descendant axis of T can be left out as they won't affect the database
   * after all.
   *
   * Superfluous updates can have a minimum PRE value of pre(T)+1 and a maximum PRE value
   * of pre(T)+size(T).
   *
   * An update with location pre(T)+size(T) can only be removed if the update is an
   * atomic insert and the inserted node is then part of the subtree of T.
   */
  public void optimize() {
    if(opt) return;

    check();
    // traverse from lowest to highest PRE value
    int i = updStructural.size() - 1;
    while(i >= 0) {
      final BasicUpdate u = updStructural.get(i);
      // If this update can lead to superfluous updates ...
      if(u.destructive()) {
        // we determine the lowest and highest PRE values of a superfluous update
        final int pre = u.location;
        final int fol = pre + data.size(pre, data.kind(pre));
        i--;
        // and have a look at the next candidate
        while(i >= 0) {
          final BasicUpdate desc = updStructural.get(i);
          final int descpre = desc.location;
          // if the candidate operates on the subtree of T and inserts a node ...
          if(descpre <= fol && (desc instanceof Insert || desc instanceof InsertAttr) &&
              desc.parent() >= pre && desc.parent() < fol) {
            // it is removed.
            updStructural.remove(i--);

          // Other updates (not inserting a node) that operate on the subtree of T can
          // only have a PRE value that is smaller than the following PRE of T
          } else if(descpre < fol) {
            // these we delete.
            updStructural.remove(i--);

          // Else there's nothing to delete
          } else
            break;
        }
      } else
        i--;
    }
    opt = true;
  }

  /**
   * Executes the updates. Resolving text node adjacency can be skipped if adjacent text
   * nodes are not to be expected.
   * @param mergeTexts adjacent text nodes are to be expected and must be merged
   */
  public void execute(final boolean mergeTexts) {
    check();
    optimize();
    applyValueUpdates();
    data.cache = true;
    applyStructuralUpdates();
    updateDistances();
    if(mergeTexts) resolveTextAdjacency();
    data.cache = false;
  }

  /**
   * Carries out structural updates.
   */
  public void applyStructuralUpdates() {
    accumulatePreValueShifts();
    for(final BasicUpdate t : updStructural)
      t.apply(data);
  }

  /**
   * Carries out value updates.
   */
  public void applyValueUpdates() {
    for(final BasicUpdate t : updValue) t.apply(data);
  }

  /**
   * Calculates the accumulated PRE value shifts for all updates on the list.
   */
  private void accumulatePreValueShifts() {
    if(!dirty) return;
    int s = 0;
    for(int i = updStructural.size() - 1; i >= 0; i--) {
      final BasicUpdate t = updStructural.get(i);
      s += t.shifts;
      t.accumulatedShifts = s;
    }
    dirty = false;
  }

  /**
   * Updates distances to restore parent-child relationships that have been invalidated
   * by structural updates.
   *
   * Each structural update (insert/delete) leads to a shift of higher PRE values. This
   * invalidates parent-child relationships. Distances are only updated after all
   * structural updates have been carried out to make sure each node (that has to be
   * updated) is only touched once.
   */
  private void updateDistances() {
    accumulatePreValueShifts();
    final IntSet alreadyUpdatedNodes = new IntSet();

    for(final BasicUpdate update : updStructural) {
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
   * Calculates the new distance value for the given node.
   * @param preAfter the current PRE value of the node (after structural updates have
   * been applied)
   * @return new distance for the given node
   */
  private int calculateNewDistance(final int preAfter) {
    final int kind = data.kind(preAfter);
    final int distanceBefore = data.dist(preAfter, kind);
    final int preBefore = calculatePreValue(preAfter, true);
    final int parentBefore = preBefore - distanceBefore;
    final int parentAfter = calculatePreValue(parentBefore, false);
    return preAfter - parentAfter;
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
    int i = find(pre, beforeUpdates);
    // given PRE not changed by updates
    if(i == -1) return pre;

    i = refine(updStructural, i, beforeUpdates);
    final int acm = updStructural.get(i).accumulatedShifts;
    return beforeUpdates ? pre - acm : pre + acm;
  }

  /**
   * Finds the position of the update with the lowest index that affects the given PRE
   * value P. If there are multiple updates whose affected PRE value equals P, the search
   * has to be further refined as this method returns the first match.
   * @param pre given PRE value
   * @param beforeUpdates compare based on PRE values before/after updates
   * @return index of update
   */
  private int find(final int pre, final boolean beforeUpdates) {
    int left = 0;
    int right = updStructural.size() - 1;

    while(left <= right) {
      if(left == right) {
        if(c(updStructural, left, beforeUpdates) <= pre) return left;
        return -1;
      }
      if(right - left == 1) {
        if(c(updStructural, left, beforeUpdates) <= pre) return left;
        if(c(updStructural, right, beforeUpdates) <= pre) return right;
        return -1;
      }
      final int middle = left + right >>> 1;
      final int value = c(updStructural, middle, beforeUpdates);
      if(value == pre) return middle;
      else if(value > pre) left = middle + 1;
      else right = middle;
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
   * @return update with the lowest index that invalidates the distance of the same node
   * as the given one
   */
  private static int refine(final List<BasicUpdate> l, final int index,
      final boolean beforeUpdates) {
    int i = index;
    final int value = c(l, i--, beforeUpdates);
    while(i >= 0 && c(l, i, beforeUpdates) == value) i--;
    return i + 1;
  }

  /**
   * Recalculates the PRE value of the first node whose distance is affected by the
   * given update.
   * @param l list of updates
   * @param index index of the update
   * @param beforeUpdates calculate PRE value before or after updates
   * @return PRE value
   */
  private static int c(final List<BasicUpdate> l, final int index, final boolean beforeUpdates) {
    final BasicUpdate u = l.get(index);
    return u.preOfAffectedNode + (beforeUpdates ? u.accumulatedShifts : 0);
  }

  /**
   * Resolves unwanted text node adjacency which can result from structural changes in
   * the database. Adjacent text nodes are two text nodes A and B, where
   * PRE(B)=PRE(A)+1 and PARENT(A)=PARENT(B).
   */
  private void resolveTextAdjacency() {
    // Text node merges are also gathered on a separate list to leverage optimizations.
    final AtomicUpdateList allMerges = new AtomicUpdateList(data);

    // keep track of the visited locations to avoid superfluous checks
    final IntSet s = new IntSet();
    // Text nodes have to be merged from the highest to the lowest pre value
    for(final BasicUpdate u : updStructural) {
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
        if(!s.contains(beforeFollowingNode)) {
          final AtomicUpdateList merges = necessaryMerges(beforeFollowingNode);
          merges.mergeNodes();
          allMerges.merge(merges);
          s.add(beforeFollowingNode);
        }
      }
      // check nodes for delete and for insert before the updated location
      if(!s.contains(beforeNewLocation)) {
        final AtomicUpdateList merges = necessaryMerges(beforeNewLocation);
        merges.mergeNodes();
        allMerges.merge(merges);
        s.add(beforeNewLocation);
      }
    }

    allMerges.updateDistances();
    allMerges.clear();
  }

  /**
   * Applies text node merges.
   */
  private void mergeNodes() {
    check();
    applyValueUpdates();
    applyStructuralUpdates();
  }

  /**
   * Returns atomic text node merging operations if necessary for the given node PRE and
   * its right neighbor PRE+1.
   * @param a node PRE value
   * @return list of text merging operations
   */
  private AtomicUpdateList necessaryMerges(final int a) {
    final AtomicUpdateList mergeTwoNodes = new AtomicUpdateList(data);
    final int s = data.meta.size;
    final int b = a + 1;
    // don't leave table
    if(a >= s || b >= s || a < 0 || b < 0) return mergeTwoNodes;
    // only merge texts
    if(data.kind(a) != Data.TEXT || data.kind(b) != Data.TEXT) return mergeTwoNodes;
    // only merge neighboring texts
    if(data.parent(a, Data.TEXT) != data.parent(b, Data.TEXT)) return mergeTwoNodes;

    mergeTwoNodes.addDelete(b);
    mergeTwoNodes.addUpdateValue(a, Data.TEXT,
        Token.concat(data.text(a, true), data.text(b, true)));

    return mergeTwoNodes;
  }
}