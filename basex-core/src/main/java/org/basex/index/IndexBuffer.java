package org.basex.index;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * The newest segment of an updatable index, kept in main memory and persisted in an append-only
 * log.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class IndexBuffer {
  /** Data reference. */
  private final Data data;
  /** Log file. */
  private final IOFile file;
  /** References per key length (the last entry for longer keys): key, [id, pos, generation]*. */
  private final TokenObjectMap<IntList>[] keys;
  /** Current generation of each unit. */
  private IntMap generations = new IntMap();
  /** Log output. */
  private DataOutput log;
  /** Number of references appended since the last reset. */
  private int appended;
  /** Length of the log in bytes, including records that have not been flushed yet. */
  private long length;
  /** Indicates if the log has been replayed into the main-memory structures. */
  private volatile boolean loaded;

  /**
   * Constructor.
   * @param data data reference
   * @param file log file
   * @param committed committed length of the log ({@code -1} if unknown)
   * @param refs number of references in the committed log
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unchecked")
  public IndexBuffer(final Data data, final IOFile file, final long committed, final int refs)
      throws IOException {
    this.data = data;
    this.file = file;
    keys = new TokenObjectMap[data.meta.maxlen + 2];
    final long fl = file.length();
    if(committed >= 0 && fl >= committed) {
      // records written after the last commit are discarded, the log is replayed on demand
      if(fl > committed) {
        try(RandomAccessFile raf = new RandomAccessFile(file.file(), "rw")) {
          raf.setLength(committed);
        }
      }
      length = committed;
      appended = refs;
      loaded = committed == 0;
    } else {
      replay();
    }
  }

  /**
   * Adds the references of a unit, superseding its older references.
   * @param id node ID
   * @param toks keys (empty if the unit is excluded)
   * @param poss positions
   * @throws IOException I/O exception
   */
  public final void put(final int id, final TokenList toks, final IntList poss)
      throws IOException {
    final int ts = toks.size();
    if(loaded) index(id, toks, poss);
    else appended += ts;
    if(log == null) log = new DataOutput(BufferOutput.get(
        new FileOutputStream(file.file(), true)));
    length += log.writeNum(id) + log.writeNum(ts);
    for(int t = 0; t < ts; t++) {
      length += log.writeToken(toks.get(t)) + log.writeNum(poss.get(t));
    }
  }

  /**
   * Adds the references of a unit under a new generation.
   * @param id node ID
   * @param toks keys
   * @param poss positions
   */
  private void index(final int id, final TokenList toks, final IntList poss) {
    final int gen = generations.get(id), ng = gen == Integer.MIN_VALUE ? 1 : gen + 1;
    generations.put(id, ng);
    final int ts = toks.size();
    for(int t = 0; t < ts; t++) {
      final byte[] key = toks.get(t);
      final int b = slot(key.length);
      TokenObjectMap<IntList> map = keys[b];
      if(map == null) {
        map = new TokenObjectMap<>();
        keys[b] = map;
      }
      map.computeIfAbsent(key, IntList::new).add(id, poss.get(t), ng);
    }
    appended += ts;
  }

  /**
   * Returns the slot of a key length.
   * @param len key length
   * @return slot
   */
  private int slot(final int len) {
    return Math.min(len, keys.length - 1);
  }

  /**
   * Replays the log if this has not been done yet.
   */
  private synchronized void load() {
    if(loaded) return;
    try {
      replay();
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * Replays the log, discarding an incomplete last record.
   * @throws IOException I/O exception
   */
  private void replay() throws IOException {
    if(log != null) log.flush();
    appended = 0;
    length = 0;
    final byte[] bytes = file.exists() ? file.read() : EMPTY;
    final int bl = bytes.length;
    final TokenList toks = new TokenList();
    final IntList poss = new IntList();
    // end of the last complete record
    int end = 0, p = 0;
    while(p < bl) {
      final int id;
      try {
        id = num(bytes, p);
        p += Num.length(bytes, p);
        final int ts = num(bytes, p);
        p += Num.length(bytes, p);
        toks.reset();
        poss.reset();
        for(int t = 0; t < ts; t++) {
          final int tl = num(bytes, p);
          p += Num.length(bytes, p);
          if(p + tl > bl) throw new IndexOutOfBoundsException();
          toks.add(Arrays.copyOfRange(bytes, p, p + tl));
          p += tl;
          poss.add(num(bytes, p));
          p += Num.length(bytes, p);
        }
      } catch(final IndexOutOfBoundsException ex) {
        // incomplete record: truncate the log
        Util.debug(ex);
        break;
      }
      index(id, toks, poss);
      end = p;
    }
    if(end < bl) file.write(Arrays.copyOf(bytes, end));
    length = end;
    loaded = true;
  }

  /**
   * Reads a compressed number, checking the array bounds.
   * @param bytes bytes
   * @param p position
   * @return number
   */
  private static int num(final byte[] bytes, final int p) {
    if(p + Num.length(bytes, p) > bytes.length) throw new IndexOutOfBoundsException();
    return Num.get(bytes, p);
  }

  /**
   * Indicates if a unit is superseded by the buffer.
   * @param id node ID
   * @return result of check
   */
  public final boolean supersedes(final int id) {
    if(!loaded) load();
    return generations.contains(id);
  }

  /**
   * Returns the IDs of the units superseded by the buffer, up to the specified ID.
   * @param max maximum ID
   * @return IDs
   */
  public final int[] superseded(final int max) {
    if(!loaded) load();
    final IntList list = new IntList();
    for(final int id : generations.keys()) {
      if(id <= max) list.add(id);
    }
    return list.finish();
  }

  /**
   * Returns the number of references appended since the last reset.
   * @return number of references
   */
  public final int appended() {
    return appended;
  }

  /**
   * Indicates if the buffer holds no units.
   * @return result of check
   */
  public final boolean isEmpty() {
    return length == 0;
  }

  /**
   * Returns the number of keys, including keys without live references.
   * @return number of keys
   */
  public final int size() {
    if(!loaded) load();
    int size = 0;
    for(final TokenObjectMap<IntList> map : keys) {
      if(map != null) size += map.size();
    }
    return size;
  }

  /**
   * Returns the number of references of a key, including superseded and deleted ones.
   * @param key key
   * @return number of references
   */
  public final int count(final byte[] key) {
    final IntList list = list(key);
    return list == null ? 0 : list.size() / 3;
  }

  /**
   * Returns the references of a key.
   * @param key key
   * @return references, or {@code null}
   */
  private IntList list(final byte[] key) {
    if(!loaded) load();
    final TokenObjectMap<IntList> map = keys[slot(key.length)];
    return map == null ? null : map.get(key);
  }

  /**
   * Returns the references of the keys with the specified length.
   * @param len key length (the last bucket contains all longer keys)
   * @return references, or {@code null}
   */
  protected final TokenObjectMap<IntList> bucket(final int len) {
    if(!loaded) load();
    return keys[len];
  }

  /**
   * Returns the number of buckets.
   * @return number of buckets
   */
  protected final int buckets() {
    return keys.length;
  }

  /**
   * Returns all keys.
   * @return keys
   */
  public final TokenList keys() {
    if(!loaded) load();
    final TokenList list = new TokenList();
    for(final TokenObjectMap<IntList> map : keys) {
      if(map == null) continue;
      for(final byte[] key : map) {
        list.add(key);
      }
    }
    return list;
  }

  /**
   * Counts the live references of a key, and appends them if lists are passed on.
   * @param key key
   * @param nodes IDs or PRE values (can be {@code null})
   * @param poss positions (can be {@code null})
   * @param pres append PRE values instead of IDs
   * @return number of live references
   */
  public final int live(final byte[] key, final IntList nodes, final IntList poss,
      final boolean pres) {
    final IntList list = list(key);
    return list == null ? 0 : live(list, nodes, poss, pres);
  }

  /**
   * Counts the live references of a list, and appends them if lists are passed on.
   * @param list references
   * @param nodes IDs or PRE values (can be {@code null})
   * @param poss positions (can be {@code null})
   * @param pres append PRE values instead of IDs
   * @return number of live references
   */
  protected final int live(final IntList list, final IntList nodes, final IntList poss,
      final boolean pres) {
    final int ls = list.size();
    int count = 0;
    for(int l = 0; l < ls; l += 3) {
      // live: current generation of the unit, and the node still exists
      final int id = list.get(l);
      if(list.get(l + 2) != generations.get(id)) continue;
      final int pre = data.pre(id);
      if(pre == -1) continue;
      if(nodes != null) {
        nodes.add(pres ? pre : id);
        if(poss != null) poss.add(list.get(l + 1));
      }
      count++;
    }
    return count;
  }

  /**
   * Writes the live references of the buffer as a segment.
   * @param writer segment writer
   * @param order order of the keys
   * @throws IOException I/O exception
   */
  public final void write(final SegmentWriter writer, final Comparator<byte[]> order)
      throws IOException {
    if(!loaded) replay();
    final byte[][] sorted = keys().finish();
    Arrays.sort(sorted, order);
    final IntList ids = new IntList(), poss = new IntList();
    for(final byte[] key : sorted) {
      ids.reset();
      poss.reset();
      live(key, ids, poss, false);
      if(ids.isEmpty()) continue;
      SegmentedIndex.sort(ids, poss);
      writer.write(key, ids, poss);
    }
  }

  /**
   * Discards all references and the log.
   */
  public final void reset() {
    Arrays.fill(keys, null);
    generations = new IntMap();
    appended = 0;
    length = 0;
    loaded = true;
    close();
    file.delete();
  }

  /**
   * Flushes the log.
   */
  public final void flush() {
    if(log != null) {
      try {
        log.flush();
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    }
  }

  /**
   * Closes the log.
   */
  public final void close() {
    if(log != null) {
      try {
        log.close();
      } catch(final IOException ex) {
        Util.stack(ex);
      }
      log = null;
    }
  }

  /**
   * Returns the length of the log.
   * @return length in bytes
   */
  public final long length() {
    return length;
  }
}
