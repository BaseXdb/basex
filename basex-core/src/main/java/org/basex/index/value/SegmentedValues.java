package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * An updatable value index (text, attribute or token index) stored in segments on disk.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SegmentedValues extends SegmentedIndex {
  /** Number of references or nodes after which a segment is written (lowered by tests). */
  static int threshold = 100000;
  /** Maximum number of references whose live references are counted exactly. */
  private static final int EXACT = 256;

  /** File prefix. */
  private final String name;
  /** Largest node ID that may be referenced by the base structure. */
  private int baseid;
  /** Indicates if the index is unchanged since the base structure was written. */
  private boolean pristine;
  /** Keys of the buffer ({@code null} if they need to be sorted again). */
  private BufferSource snapshot;
  /** References of a key that are checked for liveness. */
  private final IntList refs = new IntList();

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param type index type
   * @throws IOException I/O exception
   */
  public SegmentedValues(final Data data, final IndexType type) throws IOException {
    super(data, type);
    name = DiskValues.fileSuffix(type);

    final MetaData meta = data.meta;
    final int[] numbers = numbers(meta.segments.get(type));
    final long[] state = state(meta.buffers.get(type));
    covered = (int) state[2];
    baseid = (int) state[3];
    orphans(name, "lrs", "lrt", numbers);
    pristine = numbers == null;
    buffer = new IndexBuffer(data, meta.dbFile(name + 'b'), pristine ? 0 : state[0],
        (int) state[1]);
    if(pristine) segments = new IndexSegment[] { new ValueBase(data, type, name) };
    else load(numbers);
    lastid = data.lastid;
  }

  @Override
  protected IndexSegment open(final int number) throws IOException {
    return number >= 0 ? new ValueSegment(data, type, number, prefix(number)) :
      new ValueBase(data, type, name);
  }

  /**
   * Returns the base structure.
   * @return base structure, or {@code null} if it has been merged into a segment
   */
  private ValueBase base() {
    return segments.length > 0 && segments[0].number < 0 ? (ValueBase) segments[0] : null;
  }

  @Override
  protected String prefix(final int number) {
    return number < 0 ? name : name + number;
  }

  @Override
  protected SegmentReader reader(final IndexSegment segment) {
    return new ValueReader((ValueSource) segment);
  }

  @Override
  protected SegmentWriter writer(final String prefix) throws IOException {
    return new ValueWriter(data, type, prefix, true);
  }

  @Override
  protected Comparator<byte[]> order() {
    return Token::compare;
  }

  @Override
  protected void drop(final String prefix) {
    data.meta.drop(prefix + "[lrsp]");
  }

  @Override
  protected boolean unsegmented() {
    return pristine;
  }

  @Override
  protected String state() {
    return super.state() + "," + baseid;
  }

  @Override
  protected int threshold() {
    return threshold;
  }

  // QUERIES ======================================================================================

  /**
   * Returns the sources of references: the segments and the buffer.
   * @return sources
   */
  private ValueSource[] sources() {
    final int sl = segments.length;
    final boolean buffered = buffer != null && !buffer.isEmpty();
    final ValueSource[] sources = Arrays.copyOf(segments, sl + (buffered ? 1 : 0),
      ValueSource[].class);
    if(buffered) {
      if(snapshot == null) snapshot = new BufferSource(buffer);
      sources[sl] = snapshot;
    }
    return sources;
  }

  @Override
  public synchronized IndexCosts costs(final IndexSearch search) {
    if(search instanceof StringRange) return IndexCosts.get(Math.max(1, data.nodes() / 10));
    if(search instanceof NumericRange) return IndexCosts.get(Math.max(1, data.nodes() / 3));

    // upper bound: superseded and deleted references are counted
    // exact lookups: the buffer is accessed without sorting its keys
    final byte[] key = search.token();
    final int sl = segments.length;
    final int[] pos = new int[sl];
    int count = buffer != null ? buffer.count(key) : 0;
    for(int s = 0; s < sl; s++) {
      final ValueSource source = (ValueSource) segments[s];
      pos[s] = source.find(key);
      if(pos[s] >= 0) count += source.count(pos[s]);
    }
    if(pristine || count == 0) return IndexCosts.exact(count);
    if(count > EXACT) return IndexCosts.get(count);

    // few references: count the live ones
    int live = buffer != null ? buffer.live(key, null, null, true) : 0;
    for(int s = 0; s < sl; s++) {
      if(pos[s] >= 0) live += collect((ValueSource) segments[s], pos[s], null);
    }
    return IndexCosts.exact(live);
  }

  @Override
  public synchronized IndexIterator iter(final IndexSearch search) {
    final IntList pres = new IntList();
    if(search instanceof final StringRange range) {
      final byte[] min = range.min(), max = range.max();
      for(final ValueSource source : sources()) {
        final int i = source.find(min), sz = source.size();
        for(int s = i < 0 ? -i - 1 : range.mni() ? i : i + 1; s < sz; s++) {
          final int d = compare(source.key(s), max);
          if(d > 0 || !range.mxi() && d == 0) break;
          collect(source, s, pres);
        }
      }
    } else if(search instanceof final NumericRange range) {
      final double min = range.min(), max = range.max();
      for(final ValueSource source : sources()) {
        final int sz = source.size();
        for(int s = 0; s < sz; s++) {
          final double v = toDouble(source.key(s));
          if(v >= min && v <= max) collect(source, s, pres);
        }
      }
    } else {
      final byte[] key = search.token();
      for(final IndexSegment segment : segments) {
        final ValueSource source = (ValueSource) segment;
        final int i = source.find(key);
        if(i >= 0) collect(source, i, pres);
      }
      if(buffer != null) buffer.live(key, pres, null, true);
    }
    return IndexIterator.get(pres.sort().finish());
  }

  /**
   * Collects the PRE values of the live references of the key at the specified position.
   * @param source source
   * @param i position of the key
   * @param pres PRE values (can be {@code null})
   * @return number of live references
   */
  private int collect(final ValueSource source, final int i, final IntList pres) {
    refs.reset();
    source.refs(i, refs, null);
    // references of the buffer are live; references of segments may be superseded
    final IndexSegment segment = source instanceof final IndexSegment s ? s : null;
    final int rs = refs.size();
    int count = 0;
    for(int r = 0; r < rs; r++) {
      final int id = refs.get(r), pre = segment != null ? pre(segment, id) : data.pre(id);
      if(pre == -1) continue;
      if(pres != null) pres.add(pre);
      count++;
    }
    return count;
  }

  @Override
  public synchronized EntryIterator entries(final IndexEntries entries) {
    final byte[] token = entries.token();
    final boolean prefix = entries.prefix, desc = entries.descending && !prefix;
    final ValueSource[] sources = sources();
    final int sl = sources.length;
    // current positions and keys in the sources
    final int[] pos = new int[sl];
    final byte[][] heads = new byte[sl][];
    for(int s = 0; s < sl; s++) {
      final ValueSource source = sources[s];
      if(token.length == 0 && !prefix) {
        pos[s] = desc ? source.size() - 1 : 0;
      } else {
        int i = source.find(token);
        if(i < 0) i = -i - 1;
        pos[s] = desc ? i - 1 : i;
      }
      if(pos[s] >= 0 && pos[s] < source.size()) heads[s] = source.key(pos[s]);
    }

    return new EntryIterator() {
      int count;

      @Override
      public byte[] next() {
        synchronized(SegmentedValues.this) {
          while(true) {
            // find the next key in the requested order
            byte[] key = null;
            for(final byte[] head : heads) {
              if(head != null && (key == null ||
                  (desc ? compare(head, key) > 0 : compare(head, key) < 0))) key = head;
            }
            if(key == null || prefix && !startsWith(key, token)) return null;

            // count the live references of all sources, proceed to the next keys
            count = 0;
            for(int s = 0; s < sl; s++) {
              if(heads[s] == null || !eq(heads[s], key)) continue;
              // unchanged index: all references are live
              final ValueSource source = sources[s];
              count += pristine ? source.count(pos[s]) : collect(source, pos[s], null);
              pos[s] += desc ? -1 : 1;
              heads[s] = pos[s] >= 0 && pos[s] < source.size() ? source.key(pos[s]) : null;
            }
            if(count > 0) return key;
          }
        }
      }

      @Override
      public int count() {
        return count;
      }
    };
  }

  @Override
  public synchronized byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE).add(SORTED_LIST).add(NL);
    tb.add(LI_NAMES).add(data.meta.names(type)).add(NL);
    long l = buffer.length();
    for(final IndexSegment segment : segments) l += segment.length();
    tb.add(LI_SIZE).add(Performance.formatHuman(l)).add(NL);
    if(!pristine) {
      segmentInfo(tb);
      final ValueBase base = base();
      if(base != null) tb.add(LI).add("Pinned: ").addInt(base.pins()).add(NL);
    }
    stats(tb, options, new IndexEntries(EMPTY, true, type));
    return tb.finish();
  }

  @Override
  public synchronized int size() {
    int size = buffer != null ? buffer.size() : 0;
    for(final IndexSegment segment : segments) size += segment.size();
    return size;
  }

  @Override
  public boolean drop() {
    return super.drop() && data.meta.drop(name + "(\\d*[lrsp]|b|tmp\\d+[lrt])");
  }

  // UPDATES ======================================================================================

  @Override
  public synchronized void delete(final int pre, final int size) {
    if(buffer == null) return;
    changed();
    // pin the keys of base entries whose anchors are deleted or changed; inserted nodes have no
    // descendants that are referenced by the base structure
    final ValueBase base = base();
    if(base == null || data.id(pre) > baseid) return;
    // nodes that are no longer included may still be anchors
    final int kind = IndexNames.kind(type, data.meta), last = pre + size;
    for(int p = pre; p < last; p++) {
      final int id = data.id(p);
      if(data.kind(p) != kind || id > baseid) continue;
      ValueIndex.keys(data, type, p, (key, pos) -> {
        final int i = base.find(key);
        if(i >= 0 && base.anchor(i) == id) base.pin(i, key);
      });
    }
  }

  @Override
  public synchronized void insert(final int pre, final int size) {
    if(buffer == null) return;
    changed();
    // top-level nodes: roots of inserted subtrees, or an existing node with a new value
    final int last = pre + size;
    for(int p = pre; p < last;) {
      final int kind = data.kind(p);
      touched.add(data.id(p));
      p += data.size(p, kind);
    }
  }

  @Override
  public synchronized void rename(final int pre, final int kind) {
    if(buffer == null) return;
    final IntList units = renamedUnits(pre, kind);
    if(units.isEmpty()) return;
    for(final int p : units.finish()) touched.add(data.id(p));
    changed();
  }

  @Override
  public synchronized void optimize() {
    if(buffer == null) return;
    try {
      finish();
      if(!pristine) rewrite();
    } catch(final IOException ex) {
      invalidate(ex);
    }
  }

  @Override
  public synchronized void flush(final boolean close) {
    if(buffer == null) return;
    try {
      finish();
      // small database: restore the layout that older versions can read
      if(close && !pristine && data.nodes() <= threshold) rewrite();
    } catch(final IOException ex) {
      invalidate(ex);
      return;
    }
    buffer.flush();
    updateMeta();
  }

  /**
   * Registers a change of the current transaction.
   */
  private void changed() {
    data.meta.optimized.remove(type);
    pristine = false;
  }

  @Override
  protected void finish() throws IOException {
    final ValueBase base = base();
    if(base != null) base.writePins();
    index();
    updateMeta();
  }

  @Override
  protected void unit(final int pre, final TokenList keys, final IntList poss) {
    ValueIndex.keys(data, type, pre, (key, pos) -> {
      keys.add(key);
      poss.add(pos);
    });
  }

  @Override
  protected void put(final int id, final int pre) throws IOException {
    super.put(id, pre);
    snapshot = null;
  }

  @Override
  protected void subtree(final int first, final int last) throws IOException {
    final int number = next++;
    new DiskValuesBuilder(data, type).build(first, last, writer(prefix(number)));
    add(number);
  }

  /**
   * Merges all segments and the buffer into a new base structure.
   * @throws IOException I/O exception
   */
  private void rewrite() throws IOException {
    writeBuffer();
    snapshot = null;

    // merge all segments into a temporary structure without keys
    final String tmp = prefix(next++);
    merge(segments, new ValueWriter(data, type, tmp, false));

    // replace the segments by the new base structure
    for(final IndexSegment segment : segments) {
      segment.close();
      drop(segment.prefix);
    }
    rename(tmp, name, "lr");
    segments = new IndexSegment[] { new ValueBase(data, type, name) };
    buffer.reset();
    baseid = data.lastid;
    covered = data.lastid;
    next = 0;
    pristine = true;
    unions();
    updateMeta();
  }
}
