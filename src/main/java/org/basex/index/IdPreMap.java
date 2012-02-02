package org.basex.index;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.Array;
import org.basex.util.list.IntList;

/**
 * ID -> PRE mapping.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class IdPreMap {
  /** Base ID value. */
  private int baseid;
  /** PRE values of the inserted/deleted IDs. */
  private int[] pres;
  /** Inserted first ID values. */
  private int[] fids;
  /** Inserted last ID values. */
  private int[] nids;
  /** Increments showing how the PRE values have been modified. */
  private int[] incs;
  /** ID values for the PRE, before inserting/deleting a record. */
  private int[] oids;

  /** Number of records in the table. */
  private int rows;

  /**
   * Constructor.
   * @param id last inserted ID
   */
  public IdPreMap(final int id) {
    baseid = id;
    pres = new int[1];
    fids = new int[pres.length];
    nids = new int[pres.length];
    incs = new int[pres.length];
    oids = new int[pres.length];
  }

  /**
   * Construct a map by reading it from a file.
   * @param f file to read from
   * @throws IOException I/O error while reading from the file
   */
  public IdPreMap(final File f) throws IOException {
    final DataInput in = new DataInput(f);
    try {
      baseid = in.readNum();
      rows = in.readNum();
      pres = in.readNums();
      fids = in.readNums();
      nids = in.readNums();
      incs = in.readNums();
      oids = in.readNums();
    } finally {
      in.close();
    }
  }

  /**
   * Write the map to the specified file.
   * @param f file to write to
   * @throws IOException I/O error while writing to the file
   */
  public void write(final File f) throws IOException {
    final DataOutput out = new DataOutput(f);
    try {
      out.writeNum(baseid);
      out.writeNum(rows);
      out.writeNums(pres);
      out.writeNums(fids);
      out.writeNums(nids);
      out.writeNums(incs);
      out.writeNums(oids);
    } finally {
      out.close();
    }
  }

  /**
   * Find the PRE value of a given ID.
   * @param id ID
   * @return PRE or -1 if the ID is already deleted
   */
  public int pre(final int id) {
    // no updates or id is not affected by updates
    if(rows == 0 || id < pres[0]) return id;
    // id was inserted by update
    if(id > baseid) {
      // optimize if performance is low
      for(int i = 0; i < rows; ++i) {
        if(fids[i] == id) return pres[i];
        if(fids[i] < id && id <= nids[i]) return pres[i] + id - fids[i];
      }
    }
    // id is affected by updates
    final int i = sortedLastIndexOf(oids, id);
    return id + incs[i < 0 ? -i - 2 : i];
  }

  /**
   * Find the PRE values of a given list of IDs.
   * @param ids IDs
   * @param off start position in ids (inclusive)
   * @param len number of ids
   * @return a sorted array of PRE values
   */
  public int[] pre(final int[] ids, final int off, final int len) {
    final IntList p = new IntList(ids.length);
    for(int i = off; i < len; ++i) p.add(pre(ids[i]));
    return p.sort().toArray();
  }

  /**
   * Insert new record.
   * @param pre record PRE
   * @param id record ID
   * @param c number of inserted records
   */
  public void insert(final int pre, final int id, final int c) {
    if(rows == 0 && pre == id && id == baseid + 1) {
      // no mapping and we append at the end => nothing to do
      baseid += c;
      return;
    }

    int pos = 0;
    int inc = c;
    int oid = pre;

    if(rows > 0) {
      pos = Arrays.binarySearch(pres, 0, rows, pre);
      if(pos < 0) {
        pos = -pos - 1;
        if(pos != 0) {
          // check if inserting into an existing id interval
          final int prev = pos - 1;
          final int prevcnt = nids[prev] - fids[prev] + 1;
          final int prevpre = pres[prev];

          if(pre < prevpre + prevcnt) {
            // split the id interval
            final int split = pre - prevpre;
            final int fid = fids[prev] + split;

            // add a new next interval
            add(pos, pre, fid, nids[prev], incs[prev], oids[prev]);

            // shrink the previous interval
            nids[prev] = fid - 1;
            incs[prev] -= prevcnt - split;

            oid = oids[prev];
            inc += incs[prev];
          } else {
            oid = pre - incs[prev];
            inc += incs[prev];
          }
        }
      } else if(pos > 0) {
        oid = oids[pos];
        inc += incs[pos - 1];
      }

      // apply correction to all subsequent intervals
      for(int k = pos; k < rows; ++k) {
        pres[k] += c;
        incs[k] += c;
      }
    }

    // add the new interval
    add(pos, pre, id, id + c - 1, inc, oid);
  }

  /**
   * Delete records.
   * @param pre PRE of the first record
   * @param id ID of the first deleted record
   * @param c number of deleted records
   */
  public void delete(final int pre, final int id, final int c) {
    if(rows == 0 && pre == id && id - c == baseid + 1) {
      // no mapping and we delete at the end => nothing to do
      baseid += c;
      return;
    }

    int inc = c;
    int oid = id;

    if(rows > 0) {
      final int pre1 = pre;
      final int pre2 = pre - c - 1;

      int i1 = findPre(pre1);
      int i2 = -c > 1 ? findPre(pre2) : i1;

      final boolean found1 = i1 >= 0;
      final boolean found2 = i2 >= 0;

      if(!found1) i1 = -i1 - 1;
      if(!found2) i2 = -i2 - 1;

      if(i1 >= rows) {
        // no other intervals are affected => append the new one
        add(i1, pre, -1, -1, incs[i1 - 1] + inc, oid);
        return;
      }

      final int min1 = pres[i1];
      final int max1 = pres[i1] + nids[i1] - fids[i1];

      final int min2;
      final int max2;
      if(i2 >= rows) {
        min2 = max2 = pre2 + 1;
      } else {
        min2 = pres[i2];
        max2 = pres[i2] + nids[i2] - fids[i2];
      }

      // apply correction to all subsequent intervals
      for(int k = found2 ? i2 + 1 : i2; k < rows; ++k) {
        pres[k] += c;
        incs[k] += c;
      }

      if(i1 == i2) {
        // pre1 <= pre2 <= max2
        if(pre1 <= min1) {
          if(pre2 == max2) {
            if(i2 + 1 < rows && pres[i2 + 1] == pre) {
              // remove interval if the next one (already changed) is the same
              remove(i1, i2);
            } else {
              incs[i2] += c;
              pres[i2] = pre;
              fids[i2] = -1;
              nids[i2] = -1;
              //remove(i1, i2 - 1);
            }
          } else if(min2 <= pre2 && pre2 < max2) {
            incs[i2] += c;
            pres[i2] = pre;
            fids[i2] += pre2 - min2 + 1;
            //remove(i1, i2 - 1);
          } else if(pre2 < min2 - 1) {
            // the interval is not adjacent to next one => should be added
            add(i1, pre, -1, -1, i1 > 0 ? incs[i1 - 1] + c : c, oid);
          }
        } else if(min1 < pre1) {
          if(min2 < pre2 && pre2 < max2) {
            final int fid = fids[i2] + pre2 - min2 + 1;
            add(i2 + 1, pre2 + c + 1, fid, nids[i2], incs[i1] + c, oids[i2]);
          }
          final int s = max1 - pre1 + 1;
          nids[i1] -= s;
          incs[i1] -= s;
        }
      } else if(i1 < i2) {
        // pre1 <= max1 < pre2 <= max2
        // min1 <= max1 < min2 <= max2
        if(pre1 <= min1) {
          if(pre2 == max2) {
            if(i2 + 1 < rows && pres[i2 + 1] == pre) {
              // remove interval if the next one (already changed) is the same
              remove(i1, i2);
            } else {
              incs[i2] += c;
              pres[i2] = pre;
              fids[i2] = -1;
              nids[i2] = -1;
              remove(i1, i2 - 1);
            }
          } else if(min2 <= pre2 && pre2 < max2) {
            incs[i2] += c;
            pres[i2] = pre;
            fids[i2] += pre2 - min2 + 1;
            remove(i1, i2 - 1);
          } else if(pre2 < min2) {
            if(i2 < rows && pres[i2] == pre) {
              // remove interval if the next one (already changed) is the same
              remove(i1, --i2);
            } else {
              incs[--i2] += c;
              pres[i2] = pre;
              fids[i2] = -1;
              nids[i2] = -1;
              remove(i1, i2 - 1);
            }
          }
        } else if(min1 < pre1) {
          if(pre2 == max2) {
            inc += incs[i2];
            oid = oids[i2];
            remove(i1 + 1, i2);
            nids[i1] -= max1 - pre1 + 1;
            incs[i1] = inc;
            oids[i1] = oid;
          } else if(min2 <= pre2 && pre2 < max2) {
            fids[i2] += pre2 - min2 + 1;
            incs[i2] += c;
            pres[i2] = pre;
            remove(i1 + 1, i2 - 1);
            final int s = max1 - pre1 + 1;
            nids[i1] -= s;
            incs[i1] -= s;
          } else if(pre2 < min2) {
            incs[i1] = incs[i2 - 1] + c;
            remove(i1 + 1, i2 - 1);
            final int s = max1 - pre1 + 1;
            nids[i1] -= s;
          }
        }
      }
    } else {
      add(0, pre, -1, -1, inc, oid);
    }
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();

    b.append("pres, fids, nids, incs, oids");
    for(int i = 0; i < rows; i++) {
      b.append('\n');
      b.append(pres[i]); b.append(", ");
      b.append(fids[i]); b.append(", ");
      b.append(nids[i]); b.append(", ");
      b.append(incs[i]); b.append(", ");
      b.append(oids[i]);
    }

    return b.toString();
  }

  /**
   * Size of the mapping table (only for debugging purposes!).
   * @return number of rows in the table
   */
  public int size() {
    return rows;
  }

  /**
   * Search for a given pre value.
   * @param pre pre value
   * @return index of the record where the pre was found, or the negative
   *         insertion point - 1
   */
  private int findPre(final int pre) {
    int low = 0;
    int high = rows - 1;
    while(low <= high) {
      final int mid = low + high >>> 1;
      final int midValMin = pres[mid];
      final int midValMax = midValMin + nids[mid] - fids[mid];
      if(midValMax < pre) low = mid + 1;
      else if(midValMin > pre) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Binary search for a key in a list. If there are several hits the last one
   * is returned.
   * @param a array to search into
   * @param e key to search for
   * @return index of the found hit or where the key ought to be inserted
   */
  private int sortedLastIndexOf(final int[] a, final int e) {
    int i = Arrays.binarySearch(a, 0, rows, e);
    if(i >= 0) {
      while(++i < rows && a[i] == e);
      return i - 1;
    }
    return i;
  }

  /**
   * Add a record to the table and the ID index.
   * @param i index in the table where the record should be inserted
   * @param pre pre value
   * @param fid first ID value
   * @param nid last ID value
   * @param inc increment value
   * @param oid original ID value
   */
  private void add(final int i, final int pre, final int fid, final int nid,
      final int inc, final int oid) {
    if(rows == pres.length) {
      final int s = Array.newSize(rows);
      pres = Arrays.copyOf(pres, s);
      fids = Arrays.copyOf(fids, s);
      nids = Arrays.copyOf(nids, s);
      incs = Arrays.copyOf(incs, s);
      oids = Arrays.copyOf(oids, s);
    }
    if(i < rows) {
      System.arraycopy(pres, i, pres, i + 1, rows - i);
      System.arraycopy(fids, i, fids, i + 1, rows - i);
      System.arraycopy(nids, i, nids, i + 1, rows - i);
      System.arraycopy(incs, i, incs, i + 1, rows - i);
      System.arraycopy(oids, i, oids, i + 1, rows - i);
    }
    pres[i] = pre;
    fids[i] = fid;
    nids[i] = nid;
    incs[i] = inc;
    oids[i] = oid;
    ++rows;
  }

  /**
   * Remove a records from the table and the ID index.
   * @param s start index of records in the table (inclusive)
   * @param e end index of records in the table (inclusive)
   */
  private void remove(final int s, final int e) {
    if(s <= e) {
      System.arraycopy(pres, e + 1, pres, s, rows - (e + 1));
      System.arraycopy(fids, e + 1, fids, s, rows - (e + 1));
      System.arraycopy(nids, e + 1, nids, s, rows - (e + 1));
      System.arraycopy(incs, e + 1, incs, s, rows - (e + 1));
      System.arraycopy(oids, e + 1, oids, s, rows - (e + 1));
      rows -= e - s + 1;
    }
  }
}
