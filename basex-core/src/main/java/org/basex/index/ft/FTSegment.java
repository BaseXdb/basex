package org.basex.index.ft;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.random.*;
import org.basex.query.expr.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * An immutable full-text index structure on disk (see {@link FTIndex} for the file format).
 * A segment of an updatable index holds node IDs; the unnumbered structure of a
 * non-updatable index holds PRE values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FTSegment implements FTSource {
  /** Suffixes of all segment files. */
  static final String SUFFIXES = FTIndex.FILES + 's';

  /** Data reference. */
  private final Data data;
  /** Segment number ({@code -1} for the unnumbered structure). */
  final int number;
  /** File prefix. */
  final String prefix;
  /** Buffer of the updatable index ({@code null} if the references are PRE values). */
  private final FTBuffer buffer;

  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  private final IntObjectMap<byte[]> ctext = new IntObjectMap<>();
  /** Index storing each token, its data size and pointer on the data. */
  private final DataAccess dataY;
  /** Storing ID and POS values for each token. */
  private final DataAccess dataZ;
  /** Cache for number of hits and data reference per token. */
  private final IndexCache cache = new IndexCache();
  /** Token positions. */
  private final int[] positions;

  /** IDs of the units whose older references this segment supersedes (can be {@code null}). */
  final IntSet superseded;
  /** IDs superseded by newer segments. */
  IntSet newer = new IntSet();

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param number segment number ({@code -1} for the unnumbered structure)
   * @param buffer buffer of the updatable index ({@code null} if the references are PRE values)
   * @throws IOException I/O exception
   */
  FTSegment(final Data data, final int number, final FTBuffer buffer) throws IOException {
    this.data = data;
    this.number = number;
    this.buffer = buffer;
    prefix = FTIndex.segment(number);
    dataY = new DataAccess(data.meta.dbFile(prefix + 'y'));
    dataZ = new DataAccess(data.meta.dbFile(prefix + 'z'));
    positions = positions(data, prefix, dataY.length());

    final IOFile file = data.meta.dbFile(prefix + 's');
    if(file.exists()) {
      superseded = new IntSet();
      try(DataInput in = new DataInput(file)) {
        for(final int id : in.readNums()) superseded.add(id);
      }
    } else {
      superseded = null;
    }
  }

  /**
   * Reads the token length index: the offset of the first token of each length in file y,
   * and the length of file y as last entry.
   * @param data data reference
   * @param prefix file prefix
   * @param length length of file y
   * @return offsets ({@code -1} for lengths without tokens)
   * @throws IOException I/O exception
   */
  static int[] positions(final Data data, final String prefix, final long length)
      throws IOException {
    final int[] positions = new int[data.meta.maxlen + 3];
    Arrays.fill(positions, -1);
    try(DataAccess dataX = new DataAccess(data.meta.dbFile(prefix + 'x'))) {
      for(int is = dataX.readNum(); --is >= 0;) {
        final int p = dataX.readNum();
        positions[p] = dataX.read4();
      }
    }
    positions[positions.length - 1] = (int) length;
    return positions;
  }

  /**
   * Writes the supersede set of a segment.
   * @param data data reference
   * @param prefix file prefix of the segment
   * @param ids IDs of the superseded units (nothing is written if empty)
   * @throws IOException I/O exception
   */
  static void superseded(final Data data, final String prefix, final int[] ids)
      throws IOException {
    if(ids.length == 0) return;
    Arrays.sort(ids);
    try(DataOutput out = new DataOutput(data.meta.dbFile(prefix + 's'))) {
      out.writeNums(ids);
    }
  }

  @Override
  public int count(final byte[] token) {
    return entry(token).size;
  }

  @Override
  public void exact(final byte[] token, final IntList pres, final IntList poss) {
    final IndexEntry entry = entry(token);
    if(entry.size > 0) collect(entry.offset, entry.size, pres, poss);
  }

  @Override
  public void wildcards(final FTWildcard wc, final boolean full, final IntList pres,
      final IntList poss) {
    final byte[] pref = wc.prefix();
    final int pl = positions.length, l = Math.min(pl - 1, wc.max(full));
    for(int p = pref.length; p <= l; p++) {
      int start = positions[p];
      if(start == -1) continue;
      final int end = end(p);
      start = find(pref, start, end, p);
      while(start < end) {
        final byte[] t = dataY.readBytes(start, p);
        if(!startsWith(t, pref)) break;
        if(wc.match(t)) collect(pointer(start, p), size(start, p), pres, poss);
        start += p + FTIndex.ENTRY;
      }
    }
  }

  @Override
  public void fuzzy(final FTFuzzy fuzzy, final IntList pres, final IntList poss) {
    final int pl = positions.length, e = Math.min(pl - 1, fuzzy.maxLength());
    for(int s = fuzzy.minLength(); s <= e; s++) {
      final int p = positions[s];
      if(p == -1) continue;
      final IntList offsets = fuzzy.offsets(dataY, p, end(s), s);
      final int os = offsets.size();
      for(int o = 0; o < os; o++) {
        final int off = offsets.get(o);
        collect(pointer(off, s), size(off, s), pres, poss);
      }
    }
  }

  /**
   * Collects live references.
   * @param off offset on the references
   * @param size number of references
   * @param pres PRE values
   * @param poss positions
   */
  private void collect(final long off, final int size, final IntList pres, final IntList poss) {
    // references are node IDs: skip superseded units, map the IDs to PRE values
    final boolean check = buffer != null && (!newer.isEmpty() || !buffer.isEmpty());
    dataZ.cursor(off);
    for(int c = 0; c < size; c++) {
      final int id = dataZ.readNum(), pos = dataZ.readNum();
      if(check && (newer.contains(id) || buffer.supersedes(id))) continue;
      final int pre = buffer != null ? data.pre(id) : id;
      if(pre == -1) continue;
      pres.add(pre);
      poss.add(pos);
    }
  }

  /**
   * Returns the offset of the first entry of the next length group.
   * @param length token length
   * @return offset
   */
  private int end(final int length) {
    final int pl = positions.length;
    int c = length + 1, end = -1;
    while(c < pl && end == -1) end = positions[c++];
    return end;
  }

  /**
   * Returns a cached index entry.
   * @param value token to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] value) {
    final IndexEntry entry = cache.get(value);
    if(entry != null) return entry;

    final long pt = token(value);
    return pt == -1 ? cache.add(value, 0, 0) :
      cache.add(value, size(pt, value.length), pointer(pt, value.length));
  }

  @Override
  public EntryIterator entries(final byte[] pref) {
    return new EntryIterator() {
      int p = pref.length - 1, start, end, nr;
      boolean inner;

      @Override
      public byte[] next() {
        if(inner && start < end) {
          // loop through all entries with the same character length
          final byte[] entry = dataY.readBytes(start, p);
          if(startsWith(entry, pref)) {
            final long poi = dataY.read5();
            nr = dataY.read4();
            if(pref.length != 0) cache.add(entry, nr, poi);
            start += p + FTIndex.ENTRY;
            return entry;
          }
        }
        // find next available entry group
        final int pl = positions.length;
        while(++p < pl - 1) {
          start = positions[p];
          if(start == -1) continue;
          end = end(p);
          nr = 0;
          inner = true;
          start = find(pref, start, end, p);
          // jump to inner loop
          final byte[] n = next();
          if(n != null) return n;
        }
        // all entries processed: return null
        return null;
      }

      @Override
      public int count() {
        return nr;
      }
    };
  }

  @Override
  public EntryIterator entries(final FTFuzzy fuzzy) {
    final int pl = positions.length, last = Math.min(pl - 2, fuzzy.maxLength());

    return new EntryIterator() {
      int s = fuzzy.minLength() - 1, i, nr;
      IntList offsets = new IntList(0);

      @Override
      public byte[] next() {
        while(true) {
          // loop through all similar entries with the same character length
          if(i < offsets.size()) {
            final int p = offsets.get(i++);
            nr = FTSegment.this.size(p, s);
            return dataY.readBytes(p, s);
          }
          // find next group of entries
          if(++s > last) return null;
          final int p = positions[s];
          if(p != -1) {
            offsets = fuzzy.offsets(dataY, p, end(s), s);
            i = 0;
          }
        }
      }

      @Override
      public int count() {
        return nr;
      }
    };
  }

  /**
   * Binary search.
   * @param token token to look for
   * @param start start position
   * @param end end position
   * @param ti entry length
   * @return position where the key was found, or would have been found
   */
  private int find(final byte[] token, final int start, final int end, final int ti) {
    final int tl = ti + FTIndex.ENTRY;
    int s = 0, e = (end - start) / tl;
    while(s <= e) {
      final int m = s + e >>> 1, pos = start + m * tl, d = compare(cache(pos, ti), token);
      if(d == 0) return start + m * tl;
      if(d < 0) s = m + 1;
      else e = m - 1;
    }
    return start + s * tl;
  }

  /**
   * Caches the text at the specified position and with the specified length.
   * @param pos position
   * @param ti text length
   * @return text
   */
  private byte[] cache(final int pos, final int ti) {
    // do not cache texts if the fulltext index contains unusually long tokens
    if(ti >= 128) return dataY.readBytes(pos, ti);

    // try to find cached text (requested length may vary in full-text requests)
    return ctext.computeIfAbsent((ti << 24) + pos, () -> dataY.readBytes(pos, ti));
  }

  @Override
  public long length() {
    return data.meta.dbFile(prefix + 'x').length() + dataY.length() + dataZ.length();
  }

  /**
   * Closes the index files.
   */
  void close() {
    dataY.close();
    dataZ.close();
  }

  @Override
  public int size() {
    final int pl = positions.length;
    int size = 0, t = pl - 1;
    while(true) {
      final int e = t;
      while(positions[--t] == -1) {
        if(t == 0) return size;
      }
      size += (positions[e] - positions[t]) / (t + FTIndex.ENTRY);
    }
  }

  /**
   * Determines the pointer on a token.
   * @param token token looking for
   * @return int pointer or {@code -1} if token was not found
   */
  private int token(final byte[] token) {
    final int tl = token.length;
    // left limit
    int s = positions[tl];
    if(s == -1) return -1;

    // find right limit
    final int x = end(tl);
    int e = x;

    // binary search
    final int o = tl + FTIndex.ENTRY;
    while(s < e) {
      final int m = s + (e - s >> 1) / o * o, d = compare(dataY.readBytes(m, tl), token);
      if(d == 0) return m;
      if(d < 0) s = m + o;
      else e = m - o;
    }
    // accept entry if pointer is inside relevant tokens
    return e != x && s == e && eq(dataY.readBytes(s, tl), token) ? s : -1;
  }

  /**
   * Gets the pointer on ftdata for a token.
   * @param pt pointer on token
   * @param lt length of the token
   * @return int pointer on ftdata
   */
  private long pointer(final long pt, final int lt) {
    return dataY.read5(pt + lt);
  }

  /**
   * Reads the size of ftdata from disk.
   * @param pt pointer on token
   * @param lt length of the token
   * @return size of the ftdata
   */
  private int size(final long pt, final int lt) {
    return dataY.read4(pt + lt + 5);
  }
}
