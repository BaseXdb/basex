package org.basex.index.name;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.util.index.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class indexes and organizes the element or attribute names used in an XML document.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param meta meta data
   */
  public Names(final MetaData meta) {
    this.meta = meta;
    stats = new Stats[Array.INITIAL_CAPACITY];
  }

  /**
   * Constructor, specifying an input file.
   * @param in input stream
   * @param meta meta data
   * @throws IOException I/O exception
   */
  public Names(final DataInput in, final MetaData meta) throws IOException {
    super(in);
    this.meta = meta;
    stats = new Stats[keys.length];
    for(int id = 1; id < size; id++) stats[id] = new Stats(in);
  }

  /**
   * Initializes the index.
   */
  public void init() {
    for(int id = 1; id < size; id++) stats[id] = new Stats();
  }

  /**
   * Indexes a name, updates the statistics, and returns the name id.
   * @param name name to be added
   * @return name id
   */
  public int index(final byte[] name) {
    return index(name, null);
  }

  /**
   * Indexes a name, updates the statistics, and returns the name id.
   * @param name name to be added
   * @param value value, added to statistics (can be {@code null})
   * @return name id
   */
  public int index(final byte[] name, final byte[] value) {
    final int id = put(name);
    Stats s = stats[id];
    if(s == null) {
      s = new Stats();
      stats[id] = s;
    }
    if(value != null) s.add(value, meta);
    s.count++;
    return id;
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    super.write(out);
    for(int id = 1; id < size; id++) {
      Stats s = stats[id];
      if(s == null) {
        s = new Stats();
        stats[id] = s;
      }
      s.write(out);
    }
  }

  /**
   * Returns the statistics for the specified key id.
   * @param id name id
   * @return statistics (can be {@code null})
   */
  public Stats stats(final int id) {
    return stats[id];
  }

  @Override
  public byte[] info(final MainOptions options) {
    final int[] tl = new int[size];
    tl[0] = 0;
    int len = 0;
    for(int id = 1; id < size; id++) {
      if(len < keys[id].length) len = keys[id].length;
      if(stats[id] == null) continue;
      tl[id] = stats[id].count;
    }
    len += 2;

    // print all entries in descending number of occurrences
    final int[] ids = Array.createOrder(tl, false);

    final TokenBuilder tb = new TokenBuilder();
    tb.add(Text.LI_STRUCTURE).add(Text.HASH).add(Text.NL);
    tb.add(Text.LI_ENTRIES).addInt(size - 1).add(Text.NL);
    for(int i = 0; i < size - 1; i++) {
      final int id = ids[i];
      if(stats[id] == null) continue;
      final byte[] key = keys[id];
      tb.add("  ").add(key);
      final int kl = len - key.length;
      for(int k = 0; k < kl; ++k) tb.add(' ');
      tb.add(stats[id] + Text.NL);
    }
    return tb.finish();
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final int sz = size();

    return new EntryIterator() {
      int c;

      @Override
      public byte[] next() {
        return c < sz ? get(c) : null;
      }

      @Override
      public byte[] get(final int i) {
        c = i + 1;
        return keys[c];
      }

      @Override
      public int count() {
        return stats[c].count;
      }

      @Override
      public int size() {
        return sz;
      }
    };
  }

  @Override
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    stats = Array.copy(stats, new Stats[newSize]);
  }

  @Override
  public void close() { }

  // Unsupported methods ==========================================================================

  @Override
  public boolean drop() {
    throw Util.notExpected();
  }

  @Override
  public IndexIterator iter(final IndexSearch search) {
    throw Util.notExpected();
  }

  @Override
  public IndexCosts costs(final IndexSearch search) {
    throw Util.notExpected();
  }
}
