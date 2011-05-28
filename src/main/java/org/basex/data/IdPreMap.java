package org.basex.data;

import java.util.Arrays;
import org.basex.util.IntList;

/**
 * ID -> PRE mapping.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 * [DP] support deleted IDs
 */
public class IdPreMap {
  /** Base ID value. */
  private final int baseid;
  /** PRE values of the inserted/deleted IDs. */
  private final IntListExt pres;
  /** Inserted ID values. */
  private final IntListExt ids;
  /** Increments showing how the PRE values have been modified. */
  private final IntListExt incs;
  /** ID values for the PRE, before inserting/deleting a record. */
  private final IntListExt oids;

  /**
   * Constructor.
   * @param id last inserted ID
   */
  public IdPreMap(final int id) {
    baseid = id;

    pres = new IntListExt(5);
    ids = new IntListExt(5);
    incs = new IntListExt(5);
    oids = new IntListExt(5);
  }

  /**
   * Find the PRE value of a given ID.
   * @param id ID
   * @return PRE
   */
  public int pre(final int id) {
    if(id < pres.get(0) || pres.size() == 0) return id;
    if(id > baseid) return pres.get(ids.indexOf(id));
    final int i = oids.sortedLastIndexOf(id);
    return id + incs.get(i < 0 ? -i - 2 : i);
  }

  /**
   * Insert new record.
   * @param pre record PRE
   * @param id record ID
   * @param c number of inserted records
   */
  public void insert(final int pre, final int id, final int c) {
    int i = 0;
    int inc = c;
    int oid = pre;

    if(pres.size() > 0) {
      i = pres.sortedIndexOf(pre);
      if(i < 0) {
        i = -i - 1;
        if(i != 0) {
          oid = pre - incs.get(i - 1);
          inc += incs.get(i - 1);
        }
      } else if(i > 0) {
        oid = oids.get(i);
        inc += incs.get(i - 1);
      }
      pres.add(pre, i);
      incs.add(inc, i);
      ids.add(id, i);
      oids.add(oid, i);
      for(int k = pres.size() - 1; k > i; --k) {
        pres.inc(k, c);
        incs.inc(k, c);
      }
    } else {
      pres.set(pre, i);
      incs.set(inc, i);
      ids.set(id, i);
      oids.set(oid, i);
    }
  }

  /**
   * Delete a record.
   * @param pre record PRE
   * @param id deleted record ID
   * @param c number of deleted records
   */
  public void delete(final int pre, final int id, final int c) {
    int i = 0;
    int inc = c;
    int oid = pre;

    if(pres.size() > 0) {
      i = pres.sortedIndexOf(pre);
      if(i < 0) {
        i = -i - 1;
        // the next entry will be the same after the correction:
        if(i < pres.size() && pres.get(i) + c == pre) {
          // re-use the next record:
          pres.inc(i, c);
          incs.inc(i, c);
        } else {
          if(i != 0) {
            oid = pre - incs.get(i - 1);
            inc += incs.get(i - 1);
          }
          pres.add(pre, i);
          incs.add(inc, i);
          ids.add(id, i);
          oids.add(oid, i);
        }
      } else {
        // the next entry will be the same after the correction:
        if(i + 1 < pres.size() && pres.get(i + 1) + c == pre) {
          pres.remove(i);
          incs.remove(i);
          ids.remove(i);
          oids.remove(i);

          pres.inc(i, c);
        }
        incs.inc(i, c);
      }
      // apply the correction to all subsequent records:
      for(int k = pres.size() - 1; k > i; --k) {
        pres.inc(k, c);
        incs.inc(k, c);
      }
    } else {
      pres.set(pre, i);
      incs.set(inc, i);
      ids.set(id, i);
      oids.set(oid, i);
    }
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();

    b.append("pres, ids, incs, oids");
    for(int i = 0; i < pres.size(); i++) {
      b.append('\n');
      b.append(pres.get(i));
      b.append(", ");
      b.append(ids.get(i));
      b.append(", ");
      b.append(incs.get(i));
      b.append(", ");
      b.append(oids.get(i));
    }

    return b.toString();
  }
}

/**
 * This is a simple container for int values.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
class IntListExt extends IntList {

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  public IntListExt(final int c) {
    super(c);
  }

  /**
   * Insert an element at the specified index position.
   * @param e element to insert
   * @param i index where to insert the element
   */
  public void add(final int e, final int i) {
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    if(i < size) System.arraycopy(list, i, list, i + 1, size - i);
    ++size;
    list[i] = e;
  }

  /**
   * Remove an element from the specified index position.
   * @param i index from where to remove the element
   * @return the removed element
   */
  public int remove(final int i) {
    final int e = list[i];
    if(i < --size) System.arraycopy(list, i + 1, list, i, size - i);
    return e;
  }

  /**
   * Binary search for a key in a list. If there are several hits the last one
   * is returned.
   * @param e key to search for
   * @return index of the found hit or where the key ought to be inserted
   */
  public int sortedLastIndexOf(final int e) {
    int i = sortedIndexOf(e);
    if(i >= 0) {
      while(++i < size && list[i] == e);
      return i - 1;
    }
    return i;
  }

  /**
   * Search for a key in the list.
   * @param e key to search for
   * @return index of the found hit or -1 if the key is not found
   */
  public int indexOf(final int e) {
    for(int i = 0; i < size; ++i) if(list[i] == e) return i;
    return -1;
  }

  /**
   * Increment the value of the element at the specified index.
   * @param i index of the element
   * @param c increment value
   */
  public void inc(final int i, final int c) {
    list[i] += c;
  }
}
