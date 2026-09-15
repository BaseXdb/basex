package org.basex.index.ft;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.out.DataOutput;
import org.basex.query.expr.ft.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * The newest segment of an updatable full-text index, kept in main memory: the references of
 * the units that were indexed since the last segment was written. A unit that is put again
 * leaves its old references behind; they are recognized by their stale generation and dropped
 * when the buffer is written as a segment.
 *
 * The buffer is persisted in an append-only log (file {@code ftxb}): records of
 * {@code [id, count, (token, pos)*]}, the last record of a unit being the valid one. The log is
 * replayed when the references are accessed for the first time.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FTBuffer implements FTSource {
  /** Data reference. */
  private final Data data;
  /** Log file. */
  private final IOFile file;
  /** References per token length: token, [id, pos, generation]*. */
  private final TokenObjectMap<IntList>[] tokens;
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
   * @param committed committed length of the log ({@code -1} if unknown)
   * @param refs number of references in the committed log
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unchecked")
  FTBuffer(final Data data, final long committed, final int refs) throws IOException {
    this.data = data;
    file = data.meta.dbFile(FTIndex.LOG);
    tokens = new TokenObjectMap[data.meta.maxlen + 1];
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
   * @param toks tokens (empty if the unit is excluded)
   * @param poss positions
   * @throws IOException I/O exception
   */
  void put(final int id, final TokenList toks, final IntList poss) throws IOException {
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
   * @param toks tokens
   * @param poss positions
   */
  private void index(final int id, final TokenList toks, final IntList poss) {
    final int gen = generations.get(id), ng = gen == Integer.MIN_VALUE ? 1 : gen + 1;
    generations.put(id, ng);
    final int ts = toks.size();
    for(int t = 0; t < ts; t++) {
      final byte[] token = toks.get(t);
      final int tl = token.length;
      TokenObjectMap<IntList> map = tokens[tl];
      if(map == null) {
        map = new TokenObjectMap<>();
        tokens[tl] = map;
      }
      map.computeIfAbsent(token, IntList::new).add(id, poss.get(t), ng);
    }
    appended += ts;
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
  boolean supersedes(final int id) {
    if(!loaded) load();
    return generations.contains(id);
  }

  /**
   * Returns the IDs of the units superseded by the buffer, up to the specified ID.
   * @param max maximum ID
   * @return IDs
   */
  int[] superseded(final int max) {
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
  int appended() {
    return appended;
  }

  /**
   * Indicates if the buffer holds no units.
   * @return result of check
   */
  boolean isEmpty() {
    return length == 0;
  }

  @Override
  public int size() {
    if(!loaded) load();
    int size = 0;
    for(final TokenObjectMap<IntList> map : tokens) {
      if(map != null) size += map.size();
    }
    return size;
  }

  @Override
  public int count(final byte[] token) {
    final IntList list = list(token);
    return list == null ? 0 : list.size() / 3;
  }

  /**
   * Returns the references of a token.
   * @param token token
   * @return references, or {@code null}
   */
  private IntList list(final byte[] token) {
    if(!loaded) load();
    final int tl = token.length;
    final TokenObjectMap<IntList> map = tl < tokens.length ? tokens[tl] : null;
    return map == null ? null : map.get(token);
  }

  @Override
  public void exact(final byte[] token, final IntList pres, final IntList poss) {
    final IntList list = list(token);
    if(list != null) live(list, pres, poss, true);
  }

  @Override
  public void wildcards(final FTWildcard wc, final boolean full, final IntList pres,
      final IntList poss) {
    final byte[] prefix = wc.prefix();
    collect(prefix.length, wc.max(full), token -> startsWith(token, prefix) && wc.match(token),
      pres, poss);
  }

  @Override
  public void fuzzy(final FTFuzzy fuzzy, final IntList pres, final IntList poss) {
    collect(fuzzy.minLength(), fuzzy.maxLength(), fuzzy::similar, pres, poss);
  }

  /**
   * Collects the live references of all matching tokens.
   * @param first first token length
   * @param last last token length
   * @param matcher token matcher
   * @param pres PRE values
   * @param poss positions
   */
  private void collect(final int first, final int last, final Predicate<byte[]> matcher,
      final IntList pres, final IntList poss) {
    if(!loaded) load();
    final int l = Math.min(tokens.length - 1, last);
    for(int s = first; s <= l; s++) {
      final TokenObjectMap<IntList> map = tokens[s];
      if(map == null) continue;
      final int ms = map.size();
      for(int m = 1; m <= ms; m++) {
        if(matcher.test(map.key(m))) live(map.value(m), pres, poss, true);
      }
    }
  }

  /**
   * Counts the live references of a token, and appends them if lists are passed on.
   * @param list references
   * @param nodes IDs or PRE values (can be {@code null})
   * @param poss positions (can be {@code null})
   * @param pres append PRE values instead of IDs
   * @return number of live references
   */
  private int live(final IntList list, final IntList nodes, final IntList poss,
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
        poss.add(list.get(l + 1));
      }
      count++;
    }
    return count;
  }

  @Override
  public EntryIterator entries(final byte[] prefix) {
    return entries(prefix.length, Integer.MAX_VALUE, token -> startsWith(token, prefix));
  }

  @Override
  public EntryIterator entries(final FTFuzzy fuzzy) {
    return entries(fuzzy.minLength(), fuzzy.maxLength(), fuzzy::similar);
  }

  /**
   * Returns all matching tokens with live references, in ascending order.
   * @param first first token length
   * @param last last token length
   * @param matcher token matcher
   * @return entry iterator
   */
  private EntryIterator entries(final int first, final int last,
      final Predicate<byte[]> matcher) {
    if(!loaded) load();
    final int l = Math.min(tokens.length - 1, last);
    return new EntryIterator() {
      int s = first - 1, i, nr;
      TokenList list = new TokenList(0);

      @Override
      public byte[] next() {
        while(true) {
          if(i < list.size()) {
            final byte[] token = list.get(i++);
            nr = live(tokens[s].get(token), null, null, false);
            if(nr > 0) return token;
            continue;
          }
          if(++s > l) return null;
          final TokenObjectMap<IntList> map = tokens[s];
          if(map == null) continue;
          list = new TokenList();
          for(final byte[] token : map) {
            if(matcher.test(token)) list.add(token);
          }
          list.sort();
          i = 0;
        }
      }

      @Override
      public int count() {
        return nr;
      }
    };
  }

  /**
   * Writes the references of the buffer as a segment, skipping superseded and deleted ones.
   * @param prefix file prefix of the segment
   * @throws IOException I/O exception
   */
  void write(final String prefix) throws IOException {
    if(!loaded) replay();
    final IntList ids = new IntList(), poss = new IntList();
    try(FTSegmentWriter writer = new FTSegmentWriter(data, prefix)) {
      for(final TokenObjectMap<IntList> map : tokens) {
        if(map == null) continue;
        final TokenList list = new TokenList(map).sort();
        for(final byte[] token : list) {
          ids.reset();
          poss.reset();
          live(map.get(token), ids, poss, false);
          if(ids.isEmpty()) continue;
          FTBuilder.sort(ids, poss);
          writer.write(token, ids, poss);
        }
      }
    }
  }

  /**
   * Discards all references and the log.
   */
  void reset() {
    Arrays.fill(tokens, null);
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
  void flush() {
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
  void close() {
    if(log != null) {
      try {
        log.close();
      } catch(final IOException ex) {
        Util.stack(ex);
      }
      log = null;
    }
  }

  @Override
  public long length() {
    return length;
  }
}
