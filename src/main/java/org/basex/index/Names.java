package org.basex.index;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.Arrays;

import org.basex.data.MetaData;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.hash.TokenSet;

/**
 * This class indexes and organizes the tags or attribute names,
 * used in an XML document.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class Names extends TokenSet implements Index {
  /** Statistic information. */
  private StatsKey[] stat;
  /** Reference to meta data. */
  private final MetaData meta;

  /**
   * Default constructor.
   * @param md meta data
   */
  public Names(final MetaData md) {
    stat = new StatsKey[CAP];
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
    stat = new StatsKey[keys.length];
    meta = md;
    for(int s = 1; s < size; ++s) stat[s] = new StatsKey(in, md);
  }

  /**
   * Initializes the statistics.
   */
  public void init() {
    for(int s = 1; s < size; ++s) stat[s] = new StatsKey(meta);
  }

  /**
   * Indexes a name and returns its unique id.
   * @param k name to be found
   * @param v value, evaluated in statistics
   * @param st statistics flag
   * @return name id
   */
  public int index(final byte[] k, final byte[] v, final boolean st) {
    final int s = Math.abs(add(k));
    if(st) {
      if(stat[s] == null) stat[s] = new StatsKey(meta);
      if(v != null) stat[s].add(v);
      stat[s].counter++;
    }
    return s;
  }

  /**
   * Evaluates the value for the specified key id.
   * @param i key id
   * @param v value, used for statistics
   */
  public void index(final int i, final byte[] v) {
    stat[i].add(v);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    super.write(out);
    for(int s = 1; s < size; ++s) {
      if(stat[s] == null) stat[s] = new StatsKey(meta);
      stat[s].finish(out);
    }
  }

  /**
   * Returns the statistics for the specified key id.
   * @param id id
   * @return statistics
   */
  public StatsKey stat(final int id) {
    return stat[id];
  }

  @Override
  public byte[] info() {
    final double[] tl = new double[size];
    int len = 0;
    tl[0] = 0;
    for(int i = 1; i < size; ++i) {
      if(len < keys[i].length) len = keys[i].length;
      if(stat[i] == null) continue;
      tl[i] = stat[i].counter;
    }
    len += 2;

    // print all entries in descending number of occurrences
    final int[] ids = Array.createOrder(tl, false);

    final TokenBuilder tb = new TokenBuilder();
    tb.add(INDEXSTRUC + HASHSTRUC + NL);
    tb.add(IDXENTRIES + (size - 1) + NL);
    for(int i = 0; i < size - 1; ++i) {
      final int s = ids[i];
      if(stat[s] == null) continue;
      final byte[] key = keys[s];
      tb.add("  ");
      tb.add(key);
      for(int j = 0; j < len - key.length; ++j) tb.add(' ');
      tb.add(stat[s] + NL);
    }
    return tb.finish();
  }

  @Override
  protected void rehash() {
    super.rehash();
    stat = Arrays.copyOf(stat, size << 1);
  }

  @Override
  public void close() { }

  // Unsupported methods ======================================================

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public int count(final IndexToken token) {
    throw Util.notexpected();
  }
}
