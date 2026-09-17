package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.index.value.*;
import org.basex.io.*;
import org.basex.query.expr.ft.*;
import org.basex.query.util.ft.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * <p>This class provides access to a fuzzy full-text index structure
 * stored on disk. Each token has an entry in sizes, saving its length and a
 * pointer on ftdata, where to find the token and its ftdata.
 * The three database index files start with the prefix
 * {@link DataText#DATAFTX} and have the following format:</p>
 *
 * <ul>
 * <li>File <b>x</b> contains an entry for each token length.
 * Structure: {@code [l, p] ...}.
 * {@code l} is the length of a token [byte].
 * {@code p} is the pointer of the first token with length {@code l} [int].
 * </li>
 * <li>File <b>y</b> contains the tokens and references.
 * Structure: {@code [t0, t1, ... tl, z, s]}
 * {@code t0, t1, ... tl-1} is the token [byte[l]]
 * {@code z} is the pointer on the data entries of the token [long]
 * {@code s} is the number of references, saved in data [int]
 * </li>
 * <li>File <b>z</b> contains the {@code ID/POS} references.
 *   The values are ordered, but not distinct:
 *   {@code pre1/pos1, pre2/pos2, pre3/pos3, ...} [{@link Num}]</li>
 * </ul>
 *
 * <p>If incremental index updates are enabled ({@link MainOptions#UPDINDEX}), the index is
 * a list of immutable segments, oldest first, each stored in files with the prefix
 * {@code ftx<n>} and holding node IDs instead of PRE values, followed by an in-memory buffer
 * ({@link FTBuffer}). The segment numbers are stored in the meta data
 * ({@link MetaData#ftsegments}). A segment may have a file <b>s</b> with the sorted IDs of the
 * units (text nodes, or elements if {@link MainOptions#FTMIXED} is enabled) whose references
 * it supersedes: a reference is live if its node exists and if no newer segment, and not the
 * buffer, supersedes its unit. Updates are collected as touched node IDs and processed
 * once per transaction ({@link #finishUpdate()}); the buffer is written as a segment once its
 * references exceed a threshold, and small segments are merged.</p>
 *
 * <p>As long as all node IDs equal their PRE values, the index is stored in the unnumbered
 * layout, which older versions can read; it is rebuilt by updates that change units of a
 * database with at most {@link #threshold} nodes, and adopted as first segment by the first
 * update that changes units of a larger database or shifts PRE values.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTIndex extends ValueIndex {
  /** Minimum fixed size for each token entry. */
  static final int ENTRY = 9;
  /** Suffixes of the files of an index structure. */
  static final String FILES = "xyz";
  /** Log file of the buffer. */
  static final String LOG = DATAFTX + 'b';
  /** Maximum number of segments before small segments are merged. */
  private static final int SEGMENTS = 8;
  /** Number of references or nodes after which a segment is written (lowered by tests). */
  static int threshold = 100000;

  /** Segments, oldest first. */
  private FTSegment[] segments;
  /** Buffer ({@code null} if the index is not updatable). */
  private FTBuffer buffer;
  /** Names to include. */
  private final IndexNames names;

  /** IDs of the nodes touched by the current transaction. */
  private IntSet touched = new IntSet();
  /** Last node ID before the current transaction. */
  private int lastid;
  /** Largest node ID that may be referenced by a segment. */
  private int covered;
  /** Number of the next segment. */
  private int next;
  /** Lexer (created on demand). */
  private FTLexer lexer;
  /** Closed flag. */
  private boolean closed;

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @throws IOException I/O exception
   */
  public FTIndex(final Data data) throws IOException {
    super(data, IndexType.FULLTEXT);
    names = new IndexNames(IndexType.FULLTEXT, data);

    final MetaData meta = data.meta;
    final int[] numbers = segments(meta.ftsegments);
    // buffer state: committed log length, buffered references, covered node IDs
    final long[] state = { -1, 0, data.lastid };
    if(meta.ftbuffer != null) {
      final String[] values = Strings.split(meta.ftbuffer, ',');
      for(int v = 0; v < Math.min(values.length, state.length); v++) {
        state[v] = Strings.toLong(values[v]);
      }
    }
    covered = (int) state[2];
    if(meta.updindex) orphans(numbers);
    if(numbers == null) {
      // the unnumbered structure is updatable if its PRE values equal the node IDs
      if(meta.updindex && unnumbered(data)) buffer = new FTBuffer(data, -1, 0);
      segments = new FTSegment[] { new FTSegment(data, -1, buffer) };
    } else {
      buffer = new FTBuffer(data, state[0], (int) state[1]);
      final int ns = numbers.length;
      segments = new FTSegment[ns];
      for(int n = 0; n < ns; n++) {
        segments[n] = new FTSegment(data, numbers[n], buffer);
        next = Math.max(next, numbers[n] + 1);
      }
      unions();
    }
    meta.ftadopt = adoptable();
    lastid = data.lastid;
  }

  /**
   * Returns the file prefix of a segment.
   * @param number segment number ({@code -1} for the unnumbered structure)
   * @return prefix
   */
  static String segment(final int number) {
    return number < 0 ? DATAFTX : DATAFTX + number;
  }

  /**
   * Indicates if the index of a database can be stored in the unnumbered layout.
   * @param data data reference
   * @return result of check
   */
  static boolean unnumbered(final Data data) {
    return !data.meta.ftmixed && data.idmap.isIdentity();
  }

  /**
   * Parses the segment numbers of the meta data.
   * @param segments segment numbers (can be {@code null})
   * @return numbers, or {@code null} if the index is not segmented
   */
  private static int[] segments(final String segments) {
    if(segments == null) return null;
    final IntList list = new IntList();
    for(final String s : Strings.split(segments, ',')) {
      if(!s.isEmpty()) list.add(Strings.toInt(s));
    }
    return list.finish();
  }

  /**
   * Compares two tokens in index order: by length, then by bytes.
   * @param token first token
   * @param compare second token
   * @return result of comparison
   */
  static int compare(final byte[] token, final byte[] compare) {
    final int d = token.length - compare.length;
    return d != 0 ? d : Token.compare(token, compare);
  }

  /**
   * Deletes the files of segments that are not listed, and temporary files: leftovers of an
   * interrupted segment write or merge.
   * @param numbers segment numbers (can be {@code null})
   */
  private void orphans(final int[] numbers) {
    final Pattern pattern = Pattern.compile(DATAFTX + "(?:(\\d+)[" + FTSegment.SUFFIXES +
        "]|tmp\\d+[" + FILES + "])" + Pattern.quote(IO.BASEXSUFFIX));
    for(final IOFile file : data.meta.dbFile(DATAFTX).parent().children()) {
      final Matcher m = pattern.matcher(file.name());
      if(!m.matches()) continue;
      final String number = m.group(1);
      if(number == null || numbers == null ||
          Arrays.stream(numbers).noneMatch(n -> n == Strings.toInt(number))) file.delete();
    }
  }

  /**
   * Returns the sources of references: the segments and the buffer.
   * @return sources
   */
  private FTSource[] sources() {
    final int sl = segments.length;
    final FTSource[] sources = Arrays.copyOf(segments, sl + (buffer != null ? 1 : 0),
      FTSource[].class);
    if(buffer != null) sources[sl] = buffer;
    return sources;
  }

  @Override
  public synchronized IndexCosts costs(final IndexSearch search) {
    final byte[] token = search.token();
    if(token.length > data.meta.maxlen) return null;

    // estimate costs for queries which stretch over multiple index entries
    final FTOpt opt = ((FTLexer) search).ftOpt();
    if(opt.is(FZ) || opt.is(WC)) return IndexCosts.get(Math.max(1, data.nodes() / 16));

    // upper bound: superseded and deleted references are counted
    int count = 0;
    for(final FTSource source : sources()) count += source.count(token);
    return IndexCosts.get(count);
  }

  @Override
  public synchronized IndexIterator iter(final IndexSearch search) {
    // current search token
    final FTLexer ftl = (FTLexer) search;
    final FTOpt opt = ftl.ftOpt();
    final byte[] token = ftl.token();
    final FTSource[] sources = sources();

    // wildcard search
    if(opt.is(WC)) {
      final FTWildcard wc = new FTWildcard(token);
      if(!wc.valid()) return FTIndexIterator.FTEMPTY;
      if(!wc.simple()) {
        final IntList pres = new IntList(), poss = new IntList();
        final boolean full = opt.is(DC);
        for(final FTSource source : sources) source.wildcards(wc, full, pres, poss);
        return iter(pres, poss, token);
      }
    }

    // fuzzy search
    if(opt.is(FZ)) {
      final IntList pres = new IntList(), poss = new IntList();
      final FTFuzzy fuzzy = new FTFuzzy(token, ftl.errors());
      for(final FTSource source : sources) source.fuzzy(fuzzy, pres, poss);
      return iter(pres, poss, token);
    }

    // exact search: the counts of the sources are cached, and an upper bound
    int size = 0;
    for(final FTSource source : sources) size += source.count(token);
    final IntList pres = new IntList(size), poss = new IntList(size);
    for(final FTSource source : sources) source.exact(token, pres, poss);
    return iter(pres, poss, token);
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final byte[] token = entries.token();
    final FTFuzzy fuzzy = entries.errors >= 0 ? new FTFuzzy(token, entries.errors) : null;
    final FTSource[] sources = sources();
    final int il = sources.length;
    final EntryIterator[] iters = new EntryIterator[il];
    for(int i = 0; i < il; i++) {
      iters[i] = fuzzy != null ? sources[i].entries(fuzzy) : sources[i].entries(token);
    }

    // merge the entries in index order, summing up the counts
    return new EntryIterator() {
      final byte[][] heads = new byte[il][];
      boolean init;
      int nr;

      @Override
      public byte[] next() {
        synchronized(FTIndex.this) {
          // single source: no merge required
          if(il == 1) {
            final byte[] head = iters[0].next();
            nr = iters[0].count();
            return head;
          }
          if(!init) {
            for(int i = 0; i < il; i++) heads[i] = iters[i].next();
            init = true;
          }
          byte[] min = null;
          for(final byte[] head : heads) {
            if(head != null && (min == null || compare(head, min) < 0)) min = head;
          }
          if(min == null) return null;
          nr = 0;
          for(int i = 0; i < il; i++) {
            if(heads[i] != null && eq(heads[i], min)) {
              nr += iters[i].count();
              heads[i] = iters[i].next();
            }
          }
          return min;
        }
      }

      @Override
      public int count() {
        return nr;
      }
    };
  }

  @Override
  public synchronized byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    long l = 0;
    for(final FTSource source : sources()) l += source.length();
    tb.add(LI_NAMES).add(data.meta.ftinclude).add(NL);
    tb.add(LI_SIZE).add(Performance.formatHuman(l)).add(NL);
    if(buffer != null) {
      tb.add(LI).add("Segments: ").addInt(segments.length).add(NL);
      tb.add(LI).add("Superseded: ").addInt(superseded()).add(NL);
      tb.add(LI).add("Buffered: ").addInt(buffer.appended()).add(NL);
    }

    final IndexStats stats = new IndexStats(options.get(MainOptions.MAXSTAT));
    final EntryIterator iter = entries(new IndexEntries(EMPTY, IndexType.FULLTEXT));
    for(byte[] token; (token = iter.next()) != null;) {
      final int oc = iter.count();
      if(stats.adding(oc)) stats.add(token, oc);
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public boolean drop() {
    data.meta.ftsegments = null;
    data.meta.ftbuffer = null;
    data.meta.ftadopt = false;
    return data.meta.drop(DATAFTX + ".*");
  }

  @Override
  public synchronized void close() {
    if(closed) return;
    closed = true;
    for(final FTSegment segment : segments) segment.close();
    if(buffer != null) buffer.close();
  }

  @Override
  public synchronized int size() {
    int size = 0;
    for(final FTSource source : sources()) size += source.size();
    return size;
  }

  /**
   * Returns an iterator for the collected references.
   * @param pres PRE values
   * @param poss positions
   * @param token index token
   * @return iterator
   */
  private static FTIndexIterator iter(final IntList pres, final IntList poss,
      final byte[] token) {
    if(pres.isEmpty()) return FTIndexIterator.FTEMPTY;

    final FTCache ftc = new FTCache(pres, poss);
    final int size = pres.size();
    return new FTIndexIterator() {
      final FTMatches all = new FTMatches();
      int pos, pre, c;

      @Override
      public boolean more() {
        if(c == size) return false;
        all.reset(pos);
        int o = ftc.order[c];
        pre = ftc.pre.get(o);
        all.or(ftc.pos.get(o));
        while(++c < size) {
          o = ftc.order[c];
          if(pre != ftc.pre.get(o)) break;
          all.or(ftc.pos.get(o));
        }
        return true;
      }

      @Override
      public FTMatches matches() {
        return all;
      }

      @Override
      public int pre() {
        return pre;
      }

      @Override
      public void pos(final int p) {
        pos = p;
      }

      @Override
      public int size() {
        return size;
      }

      @Override
      public String toString() {
        return Strings.concat(token, '(', size, "x)");
      }
    };
  }

  /**
   * Full-text cache.
   */
  private static final class FTCache {
    /** Order. */
    private final int[] order;
    /** Pre values. */
    private final IntList pre;
    /** Pos values. */
    private final IntList pos;

    /**
     * Constructor.
     * @param pr PRE values
     * @param ps positions
     */
    private FTCache(final IntList pr, final IntList ps) {
      order = Array.createOrder(FTBuilder.pack(pr, ps), true);
      pre = pr;
      pos = ps;
    }
  }

  @Override
  public void add(final ValueCache values) {
    throw Util.notExpected();
  }

  @Override
  public void delete(final ValueCache values) {
    throw Util.notExpected();
  }

  // UPDATES ======================================================================================

  @Override
  public synchronized void delete(final int pre, final int size) {
    if(buffer == null) return;
    // references of deleted units remain in the segments until they are merged
    if(size > 1 || data.kind(pre) != Data.ATTR) data.meta.optimized.remove(type);
    // the string values of the included ancestors change if text nodes are deleted
    if(data.meta.ftmixed && (size > 1 || data.kind(pre) == Data.TEXT)) touchAncestors(pre);
  }

  @Override
  public synchronized void insert(final int pre, final int size) {
    if(buffer == null) return;
    // top-level nodes: roots of inserted subtrees, or an existing node with a new value
    final int last = pre + size;
    for(int p = pre; p < last;) {
      final int kind = data.kind(p);
      if(kind == Data.ELEM || kind == Data.TEXT || kind == Data.DOC) touched.add(data.id(p));
      p += data.size(p, kind);
    }
    if(data.meta.ftmixed && (size > 1 || data.kind(pre) == Data.TEXT)) touchAncestors(pre);
    if(!touched.isEmpty()) data.meta.optimized.remove(type);
  }

  @Override
  public synchronized void rename(final int pre, final int kind) {
    if(buffer == null || kind != Data.ELEM) return;
    if(data.meta.ftmixed) {
      touched.add(data.id(pre));
    } else {
      // the inclusion of the child text nodes depends on the name of the element
      for(final int p : childTexts(pre).finish()) touched.add(data.id(p));
    }
    if(!touched.isEmpty()) data.meta.optimized.remove(type);
  }

  @Override
  public void renamed(final int pre, final int kind) { }

  @Override
  public synchronized void finishUpdate() {
    if(buffer == null) return;
    try {
      finish();
    } catch(final IOException ex) {
      invalidate(ex);
    }
  }

  @Override
  public synchronized void optimize() {
    if(buffer == null) return;
    try {
      finish();
      writeBuffer();
      // the unnumbered index is clean, nothing to do
      if(adoptable()) return;

      // a single segment without superseded or deleted references needs no merge
      final int sl = segments.length;
      final boolean clean = sl == 1 && segments[0].superseded == null &&
          data.lastid + 1 == data.nodes();
      // if all IDs equal their PRE values, the result is written in the unnumbered layout,
      // which older versions can read (see #adopt)
      if(unnumbered(data)) {
        if(clean) {
          renumber(-1);
        } else if(sl == 0) {
          // empty index
          new FTSegmentWriter(data, DATAFTX).close();
          segments = new FTSegment[] { new FTSegment(data, -1, buffer) };
        } else {
          merge(Array.number(sl).finish(), -1);
        }
        updateMeta();
      } else if(!clean && sl > 0) {
        merge(Array.number(sl).finish(), next++);
      }
    } catch(final IOException ex) {
      invalidate(ex);
    }
  }

  @Override
  public synchronized void flush() {
    if(buffer != null) {
      buffer.flush();
      updateMeta();
    }
  }

  /**
   * Indicates if the index consists of the unnumbered structure that will be adopted as first
   * segment by the next update.
   * @return result of check
   */
  private boolean adoptable() {
    return buffer != null && segments.length == 1 && segments[0].number < 0;
  }

  /**
   * Touches the included ancestors of a node.
   * @param pre PRE value
   */
  private void touchAncestors(final int pre) {
    for(int p = data.parent(pre, data.kind(pre)); p != -1; p = data.parent(p, Data.ELEM)) {
      if(data.kind(p) != Data.ELEM) break;
      if(names.containsElement(p)) touched.add(data.id(p));
    }
  }

  /**
   * Adopts the unnumbered structure as first segment.
   * @throws IOException I/O exception
   */
  private void adopt() throws IOException {
    if(segments[0].size() == 0) {
      // an empty index (e.g. of a database created without input) is discarded
      segments[0].close();
      FTBuilder.drop(data, DATAFTX);
      segments = new FTSegment[0];
    } else {
      renumber(next++);
      covered = lastid;
    }
    updateMeta();
  }

  /**
   * Rebuilds the unnumbered structure.
   * @throws IOException I/O exception
   */
  private void rebuild() throws IOException {
    segments[0].close();
    FTBuilder.drop(data, DATAFTX);
    new FTBuilder(data).build(0, data.nodes(), DATAFTX);
    segments[0] = new FTSegment(data, -1, buffer);
    touched = new IntSet();
  }

  /**
   * Renumbers the first segment by renaming its files.
   * @param number new segment number ({@code -1} for the unnumbered structure)
   * @throws IOException I/O exception
   */
  private void renumber(final int number) throws IOException {
    final FTSegment segment = segments[0];
    segment.close();
    final String prefix = segment(number);
    for(final char c : FTSegment.SUFFIXES.toCharArray()) {
      final IOFile file = data.meta.dbFile(segment.prefix + c);
      if(file.exists() && !file.rename(data.meta.dbFile(prefix + c))) {
        throw new IOException("Could not rename " + file + '.');
      }
    }
    segments[0] = new FTSegment(data, number, buffer);
  }

  /**
   * Indexes the touched nodes.
   * @throws IOException I/O exception
   */
  private void finish() throws IOException {
    // units were changed, or PRE values shifted: the index is no longer valid for older versions
    if(adoptable() && (!touched.isEmpty() || !data.idmap.isIdentity())) {
      // small database whose node IDs still equal their PRE values: keep the old layout
      if(unnumbered(data) && data.nodes() <= threshold) rebuild();
      else adopt();
    }
    if(!touched.isEmpty()) {
      // process the nodes in document order, skipping the nodes of indexed subtrees
      final int[] ids = touched.keys();
      final IntList pres = new IntList(ids.length), list = new IntList(ids.length);
      for(final int id : ids) {
        final int pre = data.pre(id);
        if(pre != -1) {
          pres.add(pre);
          list.add(id);
        }
      }
      final boolean mixed = data.meta.ftmixed;
      // sorts the PRE values; the order maps each sorted position to the original index
      final int[] order = pres.createOrder(true);
      int end = -1;
      for(int o = 0; o < order.length; o++) {
        final int pre = pres.get(o), id = list.get(order[o]);
        if(pre < end) continue;
        if(id > lastid) {
          // root of an inserted subtree: index all units
          final int size = data.size(pre, data.kind(pre));
          end = pre + size;
          if(size > threshold) {
            segment(pre, end, -1);
          } else {
            for(int p = pre; p < end; p++) {
              if(names.unit(p)) put(data.id(p), data.atom(p));
            }
          }
        } else if(data.kind(pre) == (mixed ? Data.ELEM : Data.TEXT)) {
          // existing node: index it if it is a unit, exclude it otherwise
          if(!names.unit(pre)) put(id, null);
          else if(mixed && data.size(pre, Data.ELEM) > threshold) segment(pre, pre + 1, id);
          else put(id, data.atom(pre));
        }
        if(buffer.appended() >= threshold) writeBuffer();
      }
      touched = new IntSet();
    }
    lastid = data.lastid;
  }

  /**
   * Indexes a unit in the buffer.
   * @param id node ID
   * @param value value to be indexed ({@code null} if the unit is excluded)
   * @throws IOException I/O exception
   */
  private void put(final int id, final byte[] value) throws IOException {
    final TokenList toks = new TokenList();
    final IntList poss = new IntList();
    if(value != null) {
      if(lexer == null) lexer = FTBuilder.lexer(data);
      FTBuilder.tokens(lexer, data.meta.maxlen, value, (token, pos) -> {
        toks.add(token);
        poss.add(pos);
      });
    }
    buffer.put(id, toks, poss);
  }

  /**
   * Indexes the units of a large subtree, or a single large unit, in a new segment.
   * @param first PRE value of the first node
   * @param last PRE value of the last node (exclusive)
   * @param id ID of the existing unit ({@code -1} for an inserted subtree)
   * @throws IOException I/O exception
   */
  private void segment(final int first, final int last, final int id) throws IOException {
    // buffered references of the unit are older: write them first
    if(id != -1 && buffer.supersedes(id)) writeBuffer();

    final int number = next++;
    final String prefix = segment(number);
    new FTBuilder(data).build(first, last, prefix);
    if(id != -1 && segments.length > 0) FTSegment.superseded(data, prefix, new int[] { id });
    add(number);
  }

  /**
   * Writes the buffer as a new segment.
   * @throws IOException I/O exception
   */
  private void writeBuffer() throws IOException {
    if(buffer.isEmpty()) return;

    final int number = next++;
    final String prefix = segment(number);
    buffer.write(prefix);
    // the oldest segment supersedes nothing
    if(segments.length > 0) FTSegment.superseded(data, prefix, buffer.superseded(covered));
    buffer.reset();
    add(number);
  }

  /**
   * Adds a new segment, and merges small segments if there are too many.
   * @param number segment number
   * @throws IOException I/O exception
   */
  private void add(final int number) throws IOException {
    final int sl = segments.length;
    segments = Arrays.copyOf(segments, sl + 1);
    segments[sl] = new FTSegment(data, number, buffer);
    covered = data.lastid;
    unions();
    updateMeta();
    policy();
  }

  /**
   * Merges small segments if there are too many.
   * @throws IOException I/O exception
   */
  private void policy() throws IOException {
    final int sl = segments.length;
    if(sl <= SEGMENTS) return;

    // merge the segments below the size cap (always at least two); large segments are kept
    final long[] lengths = new long[sl];
    long total = 0;
    for(int s = 0; s < sl; s++) {
      lengths[s] = segments[s].length();
      total += lengths[s];
    }
    final long cap = total / SEGMENTS;
    final IntList list = new IntList();
    for(int s = 0; s < sl; s++) {
      if(lengths[s] <= cap) list.add(s);
    }
    merge(list.finish(), next++);
  }

  /**
   * Merges segments into a new segment, which takes the position of the newest input.
   * @param inputs indexes of the segments to be merged, ascending
   * @param number number of the new segment ({@code -1} for the unnumbered structure)
   * @throws IOException I/O exception
   */
  private void merge(final int[] inputs, final int number) throws IOException {
    final int il = inputs.length, sl = segments.length, newest = inputs[il - 1];
    final String prefix = segment(number);

    // a reference is live if its node exists and its unit is not superseded by newer segments
    final String[] prefixes = new String[il];
    final IntPredicate[] live = new IntPredicate[il];
    for(int i = 0; i < il; i++) {
      final FTSegment segment = segments[inputs[i]];
      prefixes[i] = segment.prefix;
      final IntSet newer = segment.newer;
      live[i] = id -> !newer.contains(id) && !buffer.supersedes(id) && data.pre(id) != -1;
    }
    FTBuilder.merge(data, prefixes, prefix, live);

    // supersede set: the sets of the inputs, without the IDs that are superseded by segments
    // between an input and the output (those segments hold the live references)
    if(il != newest + 1) {
      final IntSet set = new IntSet(), between = new IntSet();
      for(int s = newest, i = il - 1; s >= 0; s--) {
        final IntSet superseded = segments[s].superseded;
        if(superseded == null) continue;
        final boolean input = i >= 0 && inputs[i] == s;
        for(final int id : superseded.keys()) {
          if(!input) between.add(id);
          else if(!between.contains(id)) set.add(id);
        }
        if(input) i--;
      }
      FTSegment.superseded(data, prefix, set.keys());
    }

    // replace the inputs by the output
    final FTSegment[] segs = new FTSegment[sl - il + 1];
    for(int s = 0, t = 0; s < sl; s++) {
      if(s == newest) {
        segs[t++] = new FTSegment(data, number, buffer);
      } else if(Arrays.binarySearch(inputs, s) < 0) {
        segs[t++] = segments[s];
      }
    }
    for(final int i : inputs) {
      segments[i].close();
      FTBuilder.drop(data, segments[i].prefix);
    }
    segments = segs;
    unions();
    updateMeta();
  }

  /**
   * Assigns each segment the IDs that are superseded by newer segments.
   */
  private void unions() {
    // the set is shared by all segments below a segment without supersede set
    IntSet acc = new IntSet();
    for(int s = segments.length - 1; s >= 0; s--) {
      final FTSegment segment = segments[s];
      segment.newer = acc;
      final IntSet superseded = segment.superseded;
      if(superseded != null && !superseded.isEmpty()) {
        final IntSet copy = new IntSet(acc.size() + superseded.size());
        for(final int id : acc.keys()) copy.add(id);
        for(final int id : superseded.keys()) copy.add(id);
        acc = copy;
      }
    }
  }

  /**
   * Returns the number of superseded units, an upper bound.
   * @return number of units
   */
  private int superseded() {
    int count = buffer.superseded(covered).length;
    for(final FTSegment segment : segments) {
      if(segment.superseded != null) count += segment.superseded.size();
    }
    return count;
  }

  /**
   * Stores the segment numbers in the meta data.
   */
  private void updateMeta() {
    final StringList list = new StringList();
    for(final FTSegment segment : segments) list.add(Integer.toString(segment.number));
    final boolean adoptable = adoptable();
    final String segs = adoptable ? null : String.join(",", list.finish());
    final String buffered = adoptable ? null :
      buffer.length() + "," + buffer.appended() + "," + covered;
    final MetaData meta = data.meta;
    if(adoptable != meta.ftadopt || !Objects.equals(segs, meta.ftsegments) ||
        !Objects.equals(buffered, meta.ftbuffer)) {
      meta.ftsegments = segs;
      meta.ftbuffer = buffered;
      meta.ftadopt = adoptable;
      meta.dirty = true;
    }
  }

  /**
   * Drops the index after an error: an update never fails because of the full-text index.
   * @param ex exception
   */
  private void invalidate(final IOException ex) {
    Util.stack(ex);
    close();
    drop();
    segments = new FTSegment[0];
    buffer = null;
    touched = new IntSet();
    data.meta.ftindex = false;
    data.meta.dirty = true;
  }
}
