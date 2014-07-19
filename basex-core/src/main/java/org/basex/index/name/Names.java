package org.basex.index.name;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class indexes and organizes the element or attribute names used in an XML document.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class Names extends TokenSet implements Index {
  /** Statistical information. */
  private Stats[] stats;
  /** Meta data. */
  private final MetaData meta;

  /**
   * Default constructor.
   * @param md meta data
   */
  public Names(final MetaData md) {
    stats = new Stats[Array.CAPACITY];
    meta = md;
  }

  /**
   * Constructor, specifying an input file.
   * @param in input stream
   * @param md meta data
   * @throws IOException I/O exception
   */
  public Names(final DataInput in, final MetaData md) throws IOException {
    super(in);
    stats = new Stats[keys.length];
    meta = md;
    for(int s = 1; s < size; ++s) stats[s] = new Stats(in);
  }

  @Override
  public void init() {
    for(int s = 1; s < size; ++s) stats[s] = new Stats();
  }

  /**
   * Indexes a name and returns its unique id.
   * @param name name to be added
   * @param value value, added to statistics
   * @param stat statistics flag
   * @return name id
   */
  public int index(final byte[] name, final byte[] value, final boolean stat) {
    final int id = put(name);
    if(stat) {
      if(stats[id] == null) stats[id] = new Stats();
      final Stats s = stats[id];
      if(value != null) s.add(value, meta);
      s.count++;
    }
    return id;
  }

  /**
   * Adds a value to the statistics of the specified key.
   * Evaluates the value for the specified key id.
   * @param name name id
   * @param value value, added to statistics
   */
  public void index(final int name, final byte[] value) {
    stats[name].add(value, meta);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    super.write(out);
    for(int s = 1; s < size; ++s) {
      if(stats[s] == null) stats[s] = new Stats();
      stats[s].write(out);
    }
  }

  /**
   * Returns the statistics for the specified key id.
   * @param id id
   * @return statistics
   */
  public Stats stat(final int id) {
    return stats[id];
  }

  @Override
  public byte[] info() {
    final int[] tl = new int[size];
    tl[0] = 0;
    int len = 0;
    for(int i = 1; i < size; ++i) {
      if(len < keys[i].length) len = keys[i].length;
      if(stats[i] == null) continue;
      tl[i] = stats[i].count;
    }
    len += 2;

    // print all entries in descending number of occurrences
    final int[] ids = Array.createOrder(tl, false);

    final TokenBuilder tb = new TokenBuilder();
    tb.add(Text.LI_STRUCTURE + Text.HASH + Text.NL);
    tb.add(Text.LI_ENTRIES + (size - 1) + Text.NL);
    for(int i = 0; i < size - 1; ++i) {
      final int s = ids[i];
      if(stats[s] == null) continue;
      final byte[] key = keys[s];
      tb.add("  ");
      tb.add(key);
      for(int j = 0; j < len - key.length; ++j) tb.add(' ');
      tb.add(stats[s] + Text.NL);
    }
    return tb.finish();
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    return new EntryIterator() {
      int c;
      @Override public byte[] next() { return ++c < size ? keys[c] : null; }
      @Override public int count() { return stats[c].count; }
    };
  }

  @Override
  protected void rehash(final int s) {
    super.rehash(s);
    stats = Array.copy(stats, new Stats[s]);
  }

  @Override
  public void close() { }

  // Unsupported methods ======================================================

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notExpected();
  }

  @Override
  public int costs(final IndexToken token) {
    throw Util.notExpected();
  }
}
