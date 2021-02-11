package org.basex.index;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Updatable ID-PRE mapping.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public class IdPreMap {
  /** Invalid id-value. */
  private static final int INV = -1;
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
    fids = new int[1];
    nids = new int[1];
    incs = new int[1];
    oids = new int[1];
  }

  /**
   * Constructs a map by reading it from a file.
   * @param f file to read from
   * @throws IOException I/O error while reading from the file
   */
  public IdPreMap(final IOFile f) throws IOException {
    try(DataInput in = new DataInput(f)) {
      baseid = in.readNum();
      rows = in.readNum();
      pres = in.readNums();
      fids = in.readNums();
      nids = in.readNums();
      incs = in.readNums();
      oids = in.readNums();
    }
  }

  /**
   * Write the map to the specified file.
   * @param file file to write to
   * @throws IOException I/O error while writing to the file
   */
  public final void write(final IOFile file) throws IOException {
    try(DataOutput out = new DataOutput(file)) {
      out.writeNum(baseid);
      out.writeNum(rows);
      out.writeNums(pres);
      out.writeNums(fids);
      out.writeNums(nids);
      out.writeNums(incs);
      out.writeNums(oids);
    }
  }

  /**
   * Finishes database creation.
   * @param base last id
   */
  public final void finish(final int base) {
    baseid = base;
  }

  /**
   * Finds the PRE value of a given ID.
   * @param id ID
   * @return PRE or -1 if the ID is already deleted
   */
  public int pre(final int id) {
    // no updates or id is not affected by updates
    if(rows == 0 || id < pres[0]) return id;

    if(id > baseid) {
      // id was inserted by update
      for(int i = 0; i < rows; ++i) {
        if(fids[i] <= id && id <= nids[i]) return pres[i] + id - fids[i];
      }
    } else {
      // id is affected by updates
      final int i = sortedLastIndexOf(oids, id);
      return id + incs[i < 0 ? -i - 2 : i];
    }
    return -1;
  }

  /**
   * Inserts a new record.
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
          } else {
            oid = pre - incs[prev];
          }
          inc += incs[prev];
        }
      } else if(pos > 0) {
        oid = oids[pos];
        inc += incs[pos - 1];
      }

      increment(pos, c);
    }

    // add the new interval
    add(pos, pre, id, id + c - 1, inc, oid);
  }

  /**
   * Deletes records.
   * @param pre PRE of the first record
   * @param id ID of the first deleted record
   * @param c number of deleted records (negative)
   */
  public void delete(final int pre, final int id, final int c) {
    if(rows == 0 && pre == id && id - c == baseid + 1) {
      // no mapping and we delete at the end => nothing to do
      baseid += c;
      return;
    }

    if(rows == 0) {
      // no previous updates: add a new record
      add(0, pre, INV, INV, c, id);
      return;
    }

    final int end = pre - c - 1;
    final int startIndex = findPre(pre);

    // remove all updates which has affected records which now have to be deleted
    final int removeStart = startIndex < rows && pres[startIndex] < pre ?
         startIndex + 1 : startIndex;
    int removeEnd = -1;
    for(int i = startIndex; i < rows; ++i) {
      if(end < pres[i] + nids[i] - fids[i]) break;
      removeEnd = i;
    }

    final int inc;
    final int oid;
    int endIndex;
    if(removeEnd >= 0) {
      inc = incs[removeEnd];
      oid = oids[removeEnd];
      endIndex = removeStart;
      remove(removeStart, removeEnd);
    } else {
      inc = startIndex > 0 ? incs[startIndex - 1] : 0;
      oid = id;
      endIndex = startIndex;
    }

    if(rows <= startIndex) {
      // the delete does not affect previous updates
      add(startIndex, pre, INV, INV, inc + c, oid);
      return;
    }

    final int min = pres[startIndex];
    if(startIndex < endIndex) {
      if(endIndex < rows && pres[endIndex] <= end) {
        shrinkFromStart(endIndex, pre, c);
      } else {
        --endIndex;     // endIndex is not processed, so we let the increment do that
      }
      shrinkFromEnd(startIndex, pre, inc + c);
    } else if(min < pre) {
      add(++endIndex, pres[startIndex], fids[startIndex], nids[startIndex],
          incs[startIndex], oids[startIndex]);
      shrinkFromStart(endIndex, pre, c);
      shrinkFromEnd(startIndex, pre, inc + c);
    } else if(end < min) {
      add(endIndex, pre, INV, INV, inc + c, oid);
    } else {
      shrinkFromStart(startIndex, pre, c);
    }

    increment(endIndex + 1, c);
  }

  /**
   * Shrinks the given tuple from the start.
   * @param i index of the tuple
   * @param pre pre-value
   * @param c number of deleted records (negative number)
   */
  private void shrinkFromStart(final int i, final int pre, final int c) {
    incs[i] += c;
    fids[i] += pre - c - pres[i];
    pres[i] = pre;
  }

  /**
   * Shrinks the given tuple from the end.
   * @param i index of the tuple
   * @param pre pre-value
   * @param inc new inc-value
   */
  private void shrinkFromEnd(final int i, final int pre, final int inc) {
    nids[i] = fids[i] + pre - pres[i] - 1;
    incs[i] = inc;
  }

  /**
   * Increments the pre- and inc-values of all tuples starting from the given index.
   * @param from start index
   * @param with increment value
   */
  private void increment(final int from, final int with) {
    for(int i = from; i < rows; ++i) {
      pres[i] += with;
      incs[i] += with;
    }
  }

  /**
   * Returns the size of the map.
   * @return number of stored tuples
   */
  public int size() {
    return rows;
  }

  /**
   * Searches for a given pre value.
   * @param pre pre value
   * @return index of the record where the pre is found, or the insertion point if not found
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
    return low; // key not found.
  }

  /**
   * Binary search of a key in a list. If there are several hits the last one is returned.
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
   * Adds a record to the table and the ID index.
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
      final int s = Array.newCapacity(rows);
      pres = Arrays.copyOf(pres, s);
      fids = Arrays.copyOf(fids, s);
      nids = Arrays.copyOf(nids, s);
      incs = Arrays.copyOf(incs, s);
      oids = Arrays.copyOf(oids, s);
    }
    if(i < rows) {
      final int destPos = i + 1;
      final int length = rows - i;
      Array.copy(pres, i, length, pres, destPos);
      Array.copy(fids, i, length, fids, destPos);
      Array.copy(nids, i, length, nids, destPos);
      Array.copy(incs, i, length, incs, destPos);
      Array.copy(oids, i, length, oids, destPos);
    }
    pres[i] = pre;
    fids[i] = fid;
    nids[i] = nid;
    incs[i] = inc;
    oids[i] = oid;
    ++rows;
  }

  /**
   * Removes a records from the table and the ID index.
   * @param s start index of records in the table (inclusive)
   * @param e end index of records in the table (inclusive)
   */
  private void remove(final int s, final int e) {
    if(s <= e) {
      final int last = e + 1;
      final int length = rows - last;
      Array.copy(pres, last, length, pres, s);
      Array.copy(fids, last, length, fids, s);
      Array.copy(nids, last, length, nids, s);
      Array.copy(incs, last, length, incs, s);
      Array.copy(oids, last, length, oids, s);
      rows -= last - s;
    }
  }

  @Override
  public String toString() {
    final Table t = new Table();
    t.header.add("PRE").add("FID").add("NID").add("INC").add("OID");
    for(int i = 0; i < 5; ++i) t.align.add(true);
    for(int i = 0; i < rows; i++) {
      final TokenList tl = new TokenList();
      tl.add(pres[i]).add(fids[i]).add(nids[i]).add(incs[i]).add(oids[i]);
      t.contents.add(tl);
    }
    return t + "\n- BaseID: " + baseid + '\n';
  }
}
