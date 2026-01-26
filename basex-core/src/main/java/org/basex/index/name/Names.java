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
 * @author BaseX Team, BSD License
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
    for(int i = 1; i < size; i++) stats[i] = new Stats(in);
  }

  /**
   * Initializes the index.
   */
  public void init() {
    for(int i = 1; i < size; i++) stats[i] = new Stats();
  }

  /**
   * Stores a name, updates the statistics, and returns the index of the name.
   * @param name name to be added
   * @return index
   */
  public int store(final byte[] name) {
    return store(name, null);
  }

  /**
   * Stores a name, updates the statistics, and returns the index of the name.
   * @param name name to be added
   * @param value value, added to statistics (can be {@code null})
   * @return index
   */
  public int store(final byte[] name, final byte[] value) {
    final int i = put(name);
    Stats s = stats[i];
    if(s == null) {
      s = new Stats();
      stats[i] = s;
    }
    if(value != null) s.add(value, meta);
    s.count++;
    return i;
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    super.write(out);
    for(int i = 1; i < size; i++) {
      Stats s = stats[i];
      if(s == null) {
        s = new Stats();
        stats[i] = s;
      }
      s.write(out);
    }
  }

  /**
   * Returns the statistics for the key with the specified index.
   * @param index index of name
   * @return statistics (can be {@code null})
   */
  public Stats stats(final int index) {
    return stats[index];
  }

  @Override
  public byte[] info(final MainOptions options) {
    final int[] tl = new int[size];
    tl[0] = 0;
    int len = 0;
    for(int i = 1; i < size; i++) {
      if(len < keys[i].length) len = keys[i].length;
      if(stats[i] == null) continue;
      tl[i] = stats[i].count;
    }
    len += 2;

    // print all entries in descending number of occurrences
    final int[] ordered = Array.createOrder(tl, false);

    final TokenBuilder tb = new TokenBuilder();
    tb.add(Text.LI_STRUCTURE).add(Text.HASH).add(Text.NL);
    tb.add(Text.LI_ENTRIES).addInt(size - 1).add(Text.NL);
    for(int i = 0; i < size - 1; i++) {
      final int o = ordered[i];
      if(stats[o] == null) continue;
      final byte[] key = keys[o];
      tb.add("  ").add(key);
      final int kl = len - key.length;
      for(int k = 0; k < kl; ++k) tb.add(' ');
      tb.add(stats[o] + Text.NL);
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
