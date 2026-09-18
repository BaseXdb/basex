package org.basex.index;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.*;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Updatable ID-PRE mapping.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public class IdPreMap {
  /** Invalid ID value. */
  private static final int INV = -1;
  /** Logged operation: insert. */
  private static final int INSERT = 0;
  /** Logged operation: delete. */
  private static final int DELETE = 1;
  /** Logged operation: mark base IDs as deleted. */
  private static final int MARK = 2;
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
  /** Deleted base IDs: sorted, disjoint ranges of first and last IDs (can be {@code null}). */
  private int[] deleted;
  /** Indexes of the inserted ID intervals, ordered by first ID (can be {@code null}). */
  private int[] order;

  /** Number of records in the table. */
  private int rows;
  /** Operations since the last write, three arguments each ({@code null}: write completely). */
  private IntList changes;
  /** File size of the last complete write. */
  private long written;

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
   * @throws IOException I/O exception
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
      // trailing block; missing in files written by older versions
      final int[] ranges = in.readNums();
      if(ranges.length != 0) deleted = ranges;
    }
    written = f.length();
    changes = new IntList();
  }

  /**
   * Replays the operations of a log.
   * @param log log file
   * @param length committed length of the log
   * @throws IOException I/O exception
   */
  public final void replay(final IOFile log, final long length) throws IOException {
    final byte[] bytes = log.exists() ? log.read() : Token.EMPTY;
    if(bytes.length < length) throw new IOException("ID/PRE log is incomplete: " + log);
    final IntList list = changes;
    changes = null;
    for(int p = 0; p < length;) {
      final int[] args = new int[4];
      for(int a = 0; a < 4; a++) {
        args[a] = Num.get(bytes, p);
        p += Num.length(bytes, p);
      }
      switch(args[0]) {
        case INSERT -> insert(args[1], args[2], args[3]);
        case DELETE -> delete(args[1], args[2], args[3]);
        case MARK   -> markDeleted(args[1], args[2]);
        default     -> throw new IOException("ID/PRE log is corrupt: " + log);
      }
    }
    changes = list;
  }

  /**
   * Persists the map by appending the operations since the last write to a log, or by
   * writing the complete map if enforced, if the log would exceed the map, or if appending fails.
   * @param file map file
   * @param log log file
   * @param length committed length of the log
   * @param complete enforce a complete write
   * @return new length of the log ({@code 0} if the map was written completely)
   * @throws IOException I/O exception
   */
  public final long write(final IOFile file, final IOFile log, final long length,
      final boolean complete) throws IOException {
    if(changes != null) {
      if(changes.isEmpty() && (length == 0 || !complete)) return length;
      if(!complete) {
        final ArrayOutput ao = new ArrayOutput();
        try(DataOutput out = new DataOutput(ao)) {
          final int cs = changes.size();
          for(int c = 0; c < cs; c++) out.writeNum(changes.get(c));
        }
        final byte[] bytes = ao.finish();
        final long ln = length + bytes.length;
        if(ln <= written) {
          try(RandomAccessFile raf = new RandomAccessFile(log.file(), "rw")) {
            raf.seek(length);
            raf.write(bytes);
            raf.setLength(ln);
            changes.reset();
            return ln;
          } catch(final IOException ex) {
            Util.debug(ex);
          }
        }
      }
    }
    write(file);
    written = file.length();
    changes = new IntList();
    return 0;
  }

  /**
   * Enforces a complete write of the map.
   */
  public final void enforceWrite() {
    changes = null;
  }

  /**
   * Records an operation.
   * @param op operation
   * @param a first argument
   * @param b second argument
   * @param c third argument
   */
  private void log(final int op, final int a, final int b, final int c) {
    if(changes != null) changes.add(op, a, b, c);
  }

  /**
   * Write the map to the specified file.
   * @param file file to write to
   * @throws IOException I/O exception
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
      out.writeNums(deleted == null ? new int[0] : deleted);
    }
  }

  /**
   * Finishes database creation.
   * @param base last ID
   */
  public final void finish(final int base) {
    baseid = base;
  }

  /**
   * Finds the PRE value of a given ID.
   * @param id ID
   * @return PRE, or -1 if the ID was deleted
   */
  public int pre(final int id) {
    // deleted base ID
    if(deleted != null && deleted(id)) return -1;
    // no updates or ID is not affected by updates
    if(rows == 0 || id < pres[0]) return id;

    if(id > baseid) {
      // ID was inserted by update: binary search in the intervals ordered by first ID
      final int[] ord = order();
      int low = 0, high = ord.length - 1;
      while(low <= high) {
        final int mid = low + high >>> 1, i = ord[mid];
        if(nids[i] < id) low = mid + 1;
        else if(fids[i] > id) high = mid - 1;
        else return pres[i] + id - fids[i];
      }
      return -1;
    }
    // ID is affected by updates
    final int i = sortedLastIndexOf(oids, id);
    return id + incs[i < 0 ? -i - 2 : i];
  }

  /**
   * Returns the indexes of the inserted ID intervals, ordered by first ID.
   * @return ordered indexes
   */
  private int[] order() {
    if(order == null) {
      final IntList indexes = new IntList(rows), keys = new IntList(rows);
      for(int i = 0; i < rows; i++) {
        if(fids[i] != INV) {
          indexes.add(i);
          keys.add(fids[i]);
        }
      }
      final int[] ord = keys.createOrder(true);
      final int ol = ord.length;
      for(int o = 0; o < ol; o++) ord[o] = indexes.get(ord[o]);
      order = ord;
    }
    return order;
  }

  /**
   * Inserts a new record.
   * @param pre record PRE
   * @param id record ID
   * @param c number of inserted records
   */
  public void insert(final int pre, final int id, final int c) {
    log(INSERT, pre, id, c);
    order = null;
    if(rows == 0 && pre == id && id == baseid + 1) {
      // no mapping, and we append at the end => nothing to do
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
          // check if inserting into an existing ID interval
          final int prev = pos - 1;
          final int prevcnt = nids[prev] - fids[prev] + 1;
          final int prevpre = pres[prev];

          if(pre < prevpre + prevcnt) {
            // split the ID interval
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
   * Returns the base ID.
   * @return base ID
   */
  public final int baseid() {
    return baseid;
  }

  /**
   * Indicates if every ID equals its PRE value.
   * @return result of check
   */
  public final boolean isIdentity() {
    return rows == 0 && deleted == null;
  }

  /**
   * Marks a range of base IDs as deleted.
   * @param first first ID
   * @param last last ID
   */
  public final void markDeleted(final int first, final int last) {
    log(MARK, first, last, 0);
    int f = first, l = Math.min(last, baseid);
    if(f > l) return;

    // merge with overlapping and adjacent ranges
    final IntList list = new IntList();
    final int dl = deleted == null ? 0 : deleted.length;
    int d = 0;
    for(; d < dl && deleted[d + 1] < f - 1; d += 2) list.add(deleted[d], deleted[d + 1]);
    for(; d < dl && deleted[d] <= l + 1; d += 2) {
      f = Math.min(f, deleted[d]);
      l = Math.max(l, deleted[d + 1]);
    }
    list.add(f, l);
    for(; d < dl; d += 2) list.add(deleted[d], deleted[d + 1]);
    deleted = list.finish();
  }

  /**
   * Checks if a base ID was deleted.
   * @param id ID
   * @return result of check
   */
  private boolean deleted(final int id) {
    int low = 0;
    int high = deleted.length / 2 - 1;
    while(low <= high) {
      final int mid = low + high >>> 1;
      if(deleted[mid * 2 + 1] < id) low = mid + 1;
      else if(deleted[mid * 2] > id) high = mid - 1;
      else return true;
    }
    return false;
  }

  /**
   * Deletes records.
   * @param pre PRE of the first record
   * @param id ID of the first deleted record
   * @param c number of deleted records (negative)
   */
  public void delete(final int pre, final int id, final int c) {
    log(DELETE, pre, id, c);
    order = null;
    if(rows == 0 && pre == id && id - c == baseid + 1) {
      // no mapping, and we delete at the end => nothing to do
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
    for(int i = startIndex; i < rows; i++) {
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
      // the deletion does not affect previous updates
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
    for(int i = from; i < rows; i++) {
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
   * Searches for a given PRE value.
   * @param pre PRE value
   * @return index of the record where the PRE is found, or the insertion point if not found
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
   * @param pre PRE value
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
    for(int i = 0; i < 5; i++) t.align.add(true);
    for(int i = 0; i < rows; i++) {
      final TokenList tl = new TokenList();
      tl.add(pres[i]).add(fids[i]).add(nids[i]).add(incs[i]).add(oids[i]);
      t.contents.add(tl);
    }
    return t + "\n- BaseID: " + baseid + "\n- Deleted: " + Arrays.toString(deleted) + '\n';
  }
}
