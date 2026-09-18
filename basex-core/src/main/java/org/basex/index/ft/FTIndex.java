package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
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
 * ({@link MetaData#segments}). A segment may have a file <b>s</b> with the sorted IDs of the
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
public final class FTIndex extends SegmentedIndex {
  /** Minimum fixed size for each token entry. */
  static final int ENTRY = 9;
  /** Suffixes of the files of an index structure. */
  static final String FILES = "xyz";
  /** Log file of the buffer. */
  static final String LOG = DATAFTX + 'b';
  /** Number of references or nodes after which a segment is written (lowered by tests). */
  static int threshold = 100000;

  /** Lexer (created on demand). */
  private FTLexer lexer;

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @throws IOException I/O exception
   */
  public FTIndex(final Data data) throws IOException {
    super(data, IndexType.FULLTEXT);

    final MetaData meta = data.meta;
    final int[] numbers = numbers(meta.segments.get(type));
    final long[] state = state(meta.buffers.get(type));
    covered = (int) state[2];
    if(meta.updindex) orphans(DATAFTX, FTSegment.SUFFIXES, FILES, numbers);
    if(numbers == null) {
      // the unnumbered structure is updatable if its PRE values equal the node IDs
      if(meta.updindex && unnumbered(data)) buffer = new FTBuffer(data, -1, 0);
      segments = new IndexSegment[] { new FTSegment(data, -1, buffer) };
    } else {
      buffer = new FTBuffer(data, state[0], (int) state[1]);
      load(numbers);
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
   * Compares two tokens in index order: by length, then by bytes.
   * @param token first token
   * @param compare second token
   * @return result of comparison
   */
  static int compare(final byte[] token, final byte[] compare) {
    final int d = token.length - compare.length;
    return d != 0 ? d : Token.compare(token, compare);
  }

  @Override
  protected IndexSegment open(final int number) throws IOException {
    return new FTSegment(data, number, buffer);
  }

  @Override
  protected String prefix(final int number) {
    return segment(number);
  }

  @Override
  protected SegmentReader reader(final IndexSegment segment) throws IOException {
    return new FTList(data, segment.prefix);
  }

  @Override
  protected SegmentWriter writer(final String prefix) throws IOException {
    return new FTSegmentWriter(data, prefix);
  }

  @Override
  protected Comparator<byte[]> order() {
    return FTIndex::compare;
  }

  @Override
  protected void drop(final String prefix) {
    FTBuilder.drop(data, prefix);
  }

  /**
   * Returns the sources of references: the segments and the buffer.
   * @return sources
   */
  private FTSource[] sources() {
    final int sl = segments.length;
    final FTSource[] sources = Arrays.copyOf(segments, sl + (buffer != null ? 1 : 0),
      FTSource[].class);
    if(buffer != null) sources[sl] = (FTSource) buffer;
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
    if(buffer != null) segmentInfo(tb);
    stats(tb, options, new IndexEntries(EMPTY, IndexType.FULLTEXT));
    return tb.finish();
  }

  @Override
  public boolean drop() {
    data.meta.ftadopt = false;
    return super.drop() && data.meta.drop(DATAFTX + ".*");
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

    final int[] order = Array.createOrder(pack(pres, poss), true);
    final int size = pres.size();
    return new FTIndexIterator() {
      final FTMatches all = new FTMatches();
      int pos, pre, c;

      @Override
      public boolean more() {
        if(c == size) return false;
        all.reset(pos);
        int o = order[c];
        pre = pres.get(o);
        all.or(poss.get(o));
        while(++c < size) {
          o = order[c];
          if(pre != pres.get(o)) break;
          all.or(poss.get(o));
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
    if(buffer == null) return;
    for(final int p : renamedUnits(pre, kind).finish()) touched.add(data.id(p));
    if(!touched.isEmpty()) data.meta.optimized.remove(type);
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
          segments = new IndexSegment[] { new FTSegment(data, -1, buffer) };
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
  public synchronized void flush(final boolean close) {
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
      segments = new IndexSegment[0];
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
    final IndexSegment segment = segments[0];
    segment.close();
    rename(segment.prefix, segment(number), FTSegment.SUFFIXES);
    segments[0] = new FTSegment(data, number, buffer);
  }

  @Override
  protected void finish() throws IOException {
    // units were changed, or PRE values shifted: the index is no longer valid for older versions
    if(adoptable() && (!touched.isEmpty() || !data.idmap.isIdentity())) {
      // small database whose node IDs still equal their PRE values: keep the old layout
      if(unnumbered(data) && data.nodes() <= threshold) rebuild();
      else adopt();
    }
    index();
  }

  @Override
  protected void existing(final int id, final int pre) throws IOException {
    // a large mixed-content unit is indexed in a segment of its own
    if(data.meta.ftmixed && names.unit(pre) && data.size(pre, Data.ELEM) > threshold) {
      segment(pre, pre + 1, id);
    } else {
      super.existing(id, pre);
    }
  }

  @Override
  protected void unit(final int pre, final TokenList keys, final IntList poss)
      throws IOException {
    if(lexer == null) lexer = FTBuilder.lexer(data);
    FTBuilder.tokens(lexer, data.meta.maxlen, data.atom(pre), (token, pos) -> {
      keys.add(token);
      poss.add(pos);
    });
  }

  @Override
  protected void subtree(final int first, final int last) throws IOException {
    segment(first, last, -1);
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
    if(id != -1 && segments.length > 0) IndexSegment.superseded(data, prefix, new int[] { id });
    add(number);
  }

  @Override
  protected boolean unsegmented() {
    return buffer == null || adoptable();
  }

  @Override
  protected int threshold() {
    return threshold;
  }

  @Override
  protected void updateMeta() {
    super.updateMeta();
    final boolean adoptable = adoptable();
    if(adoptable != data.meta.ftadopt) {
      data.meta.ftadopt = adoptable;
      data.meta.dirty = true;
    }
  }
}
