package org.basex.query.up.primitives;

import static org.basex.util.Token.*;

import org.basex.data.Data;
import org.basex.util.InputInfo;

/**
 * Base class for all update primitives that lead to structural changes /
 * pre value shifts on the database table.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class StructuralUpdate extends UpdatePrimitive {
  /** Number of pre value shifts on the table introduced by this primitive. */
  int shifts;

  /**
   * Constructor.
   * @param t Primitive type
   * @param p pre
   * @param d data
   * @param i input info
   */
  StructuralUpdate(final PrimitiveType t, final int p,
                   final Data d, final InputInfo i) {
    super(t, p, d, i);
  }

  /**
   * Returns the number of pre value shifts that are introduced by this
   * primitive, if applied. I.e. for an insert primitive, the value is equal
   * to the number of inserted nodes by this primitive.
   * @return number of pre value shifts
   */
  public final int preShifts() {
    return shifts;
  }

  /**
   * Checks whether adjacent text nodes have been introduced as a result
   * of this update primitive. Text node adjacency can only be taken
   * care of if no more updates are to be expected for the left sibling
   * of this target node. The total number of deleted/inserted nodes
   * on the preceding axis of this target node is given recalculate the
   * actual pre value of this target.
   *
   * @param c pre value shifts introduced by updates on the preceding sibling
   * axis of this target node
   * @return adjacent text nodes have been found and merged
   */
  public abstract boolean adjacentTexts(final int c);

  /**
   * Merges two adjacent text nodes in a database. The two node arguments must
   * be sorted in ascending order, otherwise the text of the two nodes is
   * concatenated in the wrong order.
   * @param d data reference
   * @param a node pre value
   * @param b node pre value
   * @return true if nodes have been merged
   */
  static boolean mergeTexts(final Data d, final int a, final int b) {
    // some pre value checks to prevent database errors
    final int s = d.meta.size;
    if(a >= s || b >= s || a < 0 || b < 0) return false;
    if(d.kind(a) != Data.TEXT || d.kind(b) != Data.TEXT) return false;
    if(d.parent(a, Data.TEXT) != d.parent(b, Data.TEXT)) return false;

    d.update(a, Data.TEXT, concat(d.text(a, true), d.text(b, true)));
    d.delete(b);
    return true;
  }
}
