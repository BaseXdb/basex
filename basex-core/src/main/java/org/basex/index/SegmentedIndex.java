package org.basex.index;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.index.value.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * An updatable index structure, consisting of immutable segments on disk and a buffer in main
 * memory.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class SegmentedIndex extends ValueIndex {
  /** Maximum number of segments before small segments are merged. */
  private static final int SEGMENTS = 8;

  /** Names to include. */
  protected final IndexNames names;
  /** Keys of the unit that is put into the buffer. */
  private final TokenList unitKeys = new TokenList();
  /** Positions of the unit that is put into the buffer. */
  private final IntList unitPoss = new IntList();
  /** Segments, oldest first. */
  protected IndexSegment[] segments = {};
  /** Buffer ({@code null} if the index is not updatable). */
  protected IndexBuffer buffer;
  /** IDs of the nodes touched by the current transaction. */
  protected IntSet touched = new IntSet();
  /** Last node ID before the current transaction. */
  protected int lastid;
  /** Largest node ID that may be referenced by a segment. */
  protected int covered;
  /** Number of the next segment. */
  protected int next;
  /** Closed flag. */
  private boolean closed;

  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   */
  protected SegmentedIndex(final Data data, final IndexType type) {
    super(data, type);
    names = new IndexNames(type, data);
  }

  /**
   * Opens a segment.
   * @param number segment number
   * @return segment
   * @throws IOException I/O exception
   */
  protected abstract IndexSegment open(int number) throws IOException;

  /**
   * Returns the file prefix of a segment.
   * @param number segment number
   * @return prefix
   */
  protected abstract String prefix(int number);

  /**
   * Returns a sequential reader for a segment.
   * @param segment segment
   * @return reader
   * @throws IOException I/O exception
   */
  protected abstract SegmentReader reader(IndexSegment segment) throws IOException;

  /**
   * Returns a writer for the index structure with the specified prefix.
   * @param prefix file prefix
   * @return writer
   * @throws IOException I/O exception
   */
  protected abstract SegmentWriter writer(String prefix) throws IOException;

  /**
   * Returns the order of the keys.
   * @return comparator
   */
  protected abstract Comparator<byte[]> order();

  /**
   * Deletes the files of an index structure.
   * @param prefix file prefix
   */
  protected abstract void drop(String prefix);

  /**
   * Indicates if the index has the layout of a non-updatable index, and no segment state.
   * @return result of check
   */
  protected abstract boolean unsegmented();

  /**
   * Returns the number of references or nodes after which a segment is written.
   * @return threshold
   */
  protected abstract int threshold();

  /**
   * Adds the keys of a unit and their positions.
   * @param pre PRE value
   * @param keys keys
   * @param poss positions
   * @throws IOException I/O exception
   */
  protected abstract void unit(int pre, TokenList keys, IntList poss) throws IOException;

  /**
   * Indexes a unit in the buffer.
   * @param id node ID
   * @param pre PRE value ({@code -1} if the unit is excluded)
   * @throws IOException I/O exception
   */
  protected void put(final int id, final int pre) throws IOException {
    unitKeys.reset();
    unitPoss.reset();
    if(pre != -1) unit(pre, unitKeys, unitPoss);
    buffer.put(id, unitKeys, unitPoss);
  }

  /**
   * Indexes the units of a large inserted subtree in a new segment.
   * @param first PRE value of the first node
   * @param last PRE value of the last node (exclusive)
   * @throws IOException I/O exception
   */
  protected abstract void subtree(int first, int last) throws IOException;

  /**
   * Processes the changes of the current transaction.
   * @throws IOException I/O exception
   */
  protected abstract void finish() throws IOException;

  /**
   * Indexes an existing node whose value or inclusion may have changed.
   * @param id node ID
   * @param pre PRE value
   * @throws IOException I/O exception
   */
  protected void existing(final int id, final int pre) throws IOException {
    put(id, names.unit(pre) ? pre : -1);
  }

  /**
   * Opens the listed segments.
   * @param numbers segment numbers
   * @throws IOException I/O exception
   */
  protected final void load(final int[] numbers) throws IOException {
    final int ns = numbers.length;
    segments = new IndexSegment[ns];
    for(int n = 0; n < ns; n++) {
      segments[n] = open(numbers[n]);
      next = Math.max(next, numbers[n] + 1);
    }
    unions();
  }

  /**
   * Indexes the touched nodes.
   * @throws IOException I/O exception
   */
  protected final void index() throws IOException {
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
      // sorts the PRE values; the order maps each sorted position to the original index
      final int[] order = pres.createOrder(true);
      final int kind = IndexNames.kind(type, data.meta), threshold = threshold();
      int end = -1;
      for(int o = 0; o < order.length; o++) {
        final int pre = pres.get(o), id = list.get(order[o]);
        if(pre < end) continue;
        if(id > lastid) {
          // root of an inserted subtree: index all units
          final int size = data.size(pre, data.kind(pre));
          end = pre + size;
          if(size > threshold) {
            subtree(pre, end);
          } else {
            for(int p = pre; p < end; p++) {
              if(names.unit(p)) put(data.id(p), p);
            }
          }
        } else if(data.kind(pre) == kind) {
          existing(id, pre);
        }
        if(buffer.appended() >= threshold) writeBuffer();
      }
      touched = new IntSet();
    }
    lastid = data.lastid;
  }

  @Override
  public synchronized void finishUpdate() {
    if(buffer == null) return;
    try {
      finish();
    } catch(final IOException ex) {
      invalidate(ex);
    }
  }

  /**
   * Drops the index after an error: an update never fails because of an index.
   * @param ex exception
   */
  protected final void invalidate(final IOException ex) {
    Util.stack(ex);
    close();
    drop();
    segments = new IndexSegment[0];
    buffer = null;
    touched = new IntSet();
    data.meta.index(type, false);
    data.meta.dirty = true;
  }

  /**
   * Stores the state of the index in the meta data.
   */
  protected void updateMeta() {
    final boolean unsegmented = unsegmented();
    final String segs = unsegmented ? null : numbers();
    final String buffered = unsegmented ? null : state();
    final MetaData meta = data.meta;
    if(!Objects.equals(segs, meta.segments.get(type)) ||
        !Objects.equals(buffered, meta.buffers.get(type))) {
      if(segs == null) meta.segments.remove(type);
      else meta.segments.put(type, segs);
      if(buffered == null) meta.buffers.remove(type);
      else meta.buffers.put(type, buffered);
      meta.dirty = true;
    }
  }

  @Override
  public boolean drop() {
    data.meta.segments.remove(type);
    data.meta.buffers.remove(type);
    return true;
  }

  /**
   * Parses segment numbers.
   * @param numbers segment numbers, separated by commas (can be {@code null})
   * @return numbers, or {@code null} if the index is not segmented
   */
  public static int[] numbers(final String numbers) {
    if(numbers == null) return null;
    final IntList list = new IntList();
    for(final String s : Strings.split(numbers, ',')) {
      if(!s.isEmpty()) list.add(Strings.toInt(s));
    }
    return list.finish();
  }

  /**
   * Parses the buffer state: committed log length, buffered references, covered node IDs, and
   * index-specific values.
   * @param state state (can be {@code null})
   * @return values (the last node ID is the default of all further values)
   */
  protected final long[] state(final String state) {
    final long[] values = { -1, 0, data.lastid, data.lastid };
    if(state != null) {
      final String[] strings = Strings.split(state, ',');
      for(int v = 0; v < Math.min(strings.length, values.length); v++) {
        values[v] = Strings.toLong(strings[v]);
      }
    }
    return values;
  }

  /**
   * Returns the segment numbers.
   * @return numbers, separated by commas
   */
  private String numbers() {
    final StringList list = new StringList();
    for(final IndexSegment segment : segments) list.add(Integer.toString(segment.number));
    return String.join(",", list.finish());
  }

  /**
   * Returns the buffer state.
   * @return state: committed log length, buffered references, covered node IDs
   */
  protected String state() {
    return buffer.length() + "," + buffer.appended() + "," + covered;
  }

  /**
   * Deletes the files of segments that are not listed, and temporary files: leftovers of an
   * interrupted segment write or merge.
   * @param base prefix of the index files
   * @param suffixes suffixes of the segment files
   * @param tmp suffixes of temporary files
   * @param numbers segment numbers (can be {@code null})
   */
  protected final void orphans(final String base, final String suffixes, final String tmp,
      final int[] numbers) {
    final Pattern pattern = Pattern.compile(Pattern.quote(base) + "(?:(\\d+)[" + suffixes +
        "]|tmp\\d+[" + tmp + "])" + Pattern.quote(IO.BASEXSUFFIX));
    for(final IOFile file : data.meta.dbFile(base).parent().children()) {
      final Matcher m = pattern.matcher(file.name());
      if(!m.matches()) continue;
      final String number = m.group(1);
      if(number == null || numbers == null ||
          Arrays.stream(numbers).noneMatch(n -> n == Strings.toInt(number))) file.delete();
    }
  }

  @Override
  public synchronized void close() {
    if(closed) return;
    closed = true;
    for(final IndexSegment segment : segments) segment.close();
    if(buffer != null) buffer.close();
  }

  /**
   * Writes the buffer as a new segment.
   * @throws IOException I/O exception
   */
  protected final void writeBuffer() throws IOException {
    if(buffer.isEmpty()) return;

    final int number = next++;
    final String prefix = prefix(number);
    try(SegmentWriter writer = writer(prefix)) {
      buffer.write(writer, order());
    }
    // the oldest segment supersedes nothing
    if(segments.length > 0) IndexSegment.superseded(data, prefix, buffer.superseded(covered));
    buffer.reset();
    add(number);
  }

  /**
   * Adds a new segment, and merges small segments if there are too many.
   * @param number segment number
   * @throws IOException I/O exception
   */
  protected final void add(final int number) throws IOException {
    segments = Array.add(segments, open(number));
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
   * @param number number of the new segment
   * @throws IOException I/O exception
   */
  protected final void merge(final int[] inputs, final int number) throws IOException {
    final int il = inputs.length, sl = segments.length, newest = inputs[il - 1];
    final String prefix = prefix(number);

    final IndexSegment[] merged = new IndexSegment[il];
    for(int i = 0; i < il; i++) merged[i] = segments[inputs[i]];
    merge(merged, writer(prefix));

    // supersede set: the sets of the inputs, without the IDs that are superseded by segments
    // between an input and the output (those segments hold the live references)
    if(il != newest + 1) {
      final IntSet set = new IntSet(), between = new IntSet();
      for(int s = newest, i = il - 1; s >= inputs[0]; s--) {
        final boolean input = i >= 0 && inputs[i] == s;
        if(input) i--;
        final IntSet superseded = segments[s].superseded;
        if(superseded == null) continue;
        for(final int id : superseded.keys()) {
          if(!input) between.add(id);
          else if(!between.contains(id)) set.add(id);
        }
      }
      IndexSegment.superseded(data, prefix, set.keys());
    }

    // replace the inputs by the output
    final IndexSegment[] updated = new IndexSegment[sl - il + 1];
    for(int s = 0, t = 0; s < sl; s++) {
      if(s == newest) {
        updated[t++] = open(number);
      } else if(Arrays.binarySearch(inputs, s) < 0) {
        updated[t++] = segments[s];
      }
    }
    for(final int i : inputs) {
      segments[i].close();
      drop(segments[i].prefix);
    }
    segments = updated;
    unions();
    updateMeta();
  }

  /**
   * Merges segments, skipping dead references.
   * @param inputs segments
   * @param writer writer of the output index structure (closed by this method)
   * @throws IOException I/O exception
   */
  protected final void merge(final IndexSegment[] inputs, final SegmentWriter writer)
      throws IOException {
    // a reference is live if its node exists and its unit is not superseded by newer segments
    final int il = inputs.length;
    final IntPredicate[] live = new IntPredicate[il];
    for(int i = 0; i < il; i++) {
      final IndexSegment segment = inputs[i];
      live[i] = id -> pre(segment, id) != -1;
    }
    try(writer) {
      merge(il, i -> reader(inputs[i]), writer, order(), live);
    }
  }

  /**
   * Opener of the readers of a merge.
   */
  public interface Opener {
    /**
     * Opens a reader.
     * @param i index of the input
     * @return reader
     * @throws IOException I/O exception
     */
    SegmentReader open(int i) throws IOException;
  }

  /**
   * Opens and merges sorted index structures, and closes the readers.
   * @param count number of inputs
   * @param opener opener of the readers
   * @param writer writer of the output
   * @param order order of the keys
   * @param live liveness tests for the references of each input (can be {@code null})
   * @throws IOException I/O exception
   */
  public static void merge(final int count, final Opener opener, final SegmentWriter writer,
      final Comparator<byte[]> order, final IntPredicate[] live) throws IOException {
    final SegmentReader[] readers = new SegmentReader[count];
    try {
      for(int i = 0; i < count; i++) readers[i] = opener.open(i);
      merge(readers, writer, order, live);
    } finally {
      for(final SegmentReader reader : readers) {
        if(reader != null) reader.close();
      }
    }
  }

  /**
   * Renames the files of an index structure; missing files are skipped.
   * @param from file prefix of the structure
   * @param to new file prefix
   * @param suffixes suffixes of the files
   * @throws IOException I/O exception
   */
  protected final void rename(final String from, final String to, final String suffixes)
      throws IOException {
    for(final char c : suffixes.toCharArray()) {
      final IOFile file = data.meta.dbFile(from + c);
      if(file.exists() && !file.rename(data.meta.dbFile(to + c))) {
        throw new IOException("Could not rename " + file + '.');
      }
    }
  }

  /**
   * Merges sorted index structures.
   * @param readers readers of the inputs
   * @param writer writer of the output
   * @param order order of the keys
   * @param live liveness tests for the references of each input (can be {@code null})
   * @throws IOException I/O exception
   */
  private static void merge(final SegmentReader[] readers, final SegmentWriter writer,
      final Comparator<byte[]> order, final IntPredicate[] live) throws IOException {
    final int rl = readers.length;
    final IntList list = new IntList(), ids = new IntList(), poss = new IntList();
    while(true) {
      // find next key to write on disk, and all readers that contain it
      list.reset();
      byte[] key = null;
      for(int r = 0; r < rl; r++) {
        final byte[] k = readers[r].key();
        if(k == null) continue;
        final int d = key == null ? -1 : order.compare(k, key);
        if(d < 0) {
          key = k;
          list.reset();
        }
        if(d <= 0) list.add(r);
      }
      if(key == null) break;

      // collect the references of the key
      ids.reset();
      poss.reset();
      final int ls = list.size();
      for(int l = 0; l < ls; l++) {
        final SegmentReader reader = readers[list.get(l)];
        final IntPredicate lv = live != null ? live[list.get(l)] : null;
        final int[] iv = reader.ids(), pv = reader.poss();
        final int il = iv.length;
        for(int i = 0; i < il; i++) {
          final int id = iv[i];
          if(lv == null || lv.test(id)) {
            ids.add(id);
            poss.add(pv[i]);
          }
        }
        reader.next();
      }
      if(ids.isEmpty()) continue;

      // references of a single input are sorted
      if(live != null && ls > 1) sort(ids, poss);
      writer.write(key, ids, poss);
    }
  }

  /**
   * Assigns each segment the IDs that are superseded by newer segments.
   */
  protected final void unions() {
    // the set is shared by all segments below a segment without supersede set
    IntSet acc = new IntSet();
    for(int s = segments.length - 1; s >= 0; s--) {
      final IndexSegment segment = segments[s];
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
   * Adds information on the segments and the buffer.
   * @param tb token builder
   */
  protected final void segmentInfo(final TokenBuilder tb) {
    // the number of superseded units is an upper bound
    int superseded = buffer.superseded(covered).length;
    for(final IndexSegment segment : segments) {
      if(segment.superseded != null) superseded += segment.superseded.size();
    }
    tb.add(LI).add("Segments: ").addInt(segments.length).add(NL);
    tb.add(LI).add("Superseded: ").addInt(superseded).add(NL);
    tb.add(LI).add("Buffered: ").addInt(buffer.appended()).add(NL);
  }

  /**
   * Adds statistics on the index entries.
   * @param tb token builder
   * @param options main options
   * @param entries requested entries
   */
  protected final void stats(final TokenBuilder tb, final MainOptions options,
      final IndexEntries entries) {
    final IndexStats stats = new IndexStats(options.get(MainOptions.MAXSTAT));
    final EntryIterator iter = entries(entries);
    for(byte[] key; (key = iter.next()) != null;) {
      final int count = iter.count();
      if(stats.adding(count)) stats.add(key, count);
    }
    stats.print(tb);
  }

  /**
   * Returns the PRE value of a live reference of a segment.
   * @param segment segment
   * @param id node ID
   * @return PRE value, or {@code -1} if the unit is superseded or the node has been deleted
   */
  protected final int pre(final IndexSegment segment, final int id) {
    return segment.newer.contains(id) || buffer.supersedes(id) ? -1 : data.pre(id);
  }

  /**
   * Packs references into long values that sort by ID and position.
   * @param ids IDs
   * @param poss positions
   * @return packed references
   */
  protected static long[] pack(final IntList ids, final IntList poss) {
    final int is = ids.size();
    final long[] values = new long[is];
    for(int i = 0; i < is; i++) values[i] = (long) ids.get(i) << 32 | poss.get(i);
    return values;
  }

  /**
   * Sorts references by ID and position.
   * @param ids IDs
   * @param poss positions
   */
  public static void sort(final IntList ids, final IntList poss) {
    final long[] values = pack(ids, poss);
    Arrays.sort(values);
    final int is = ids.size();
    for(int i = 0; i < is; i++) {
      final long v = values[i];
      ids.set(i, (int) (v >> 32));
      poss.set(i, (int) v);
    }
  }
}
