package org.basex.index;

import static org.basex.data.DataText.*;

import java.io.IOException;
import java.util.Arrays;

import org.basex.core.Text;
import org.basex.data.MetaData;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.serial.Serializer;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.hash.TokenIntMap;
import org.basex.util.hash.TokenSet;

/**
 * This class indexes and organizes the tags or attribute names,
 * used in an XML document.
 *
 * @author BaseX Team 2005-12, BSD License
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
    stats = new Stats[CAP];
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

  /**
   * Initializes the statistics.
   */
  public void init() {
    for(int s = 1; s < size; ++s) stats[s] = new Stats();
  }

  /**
   * Indexes a name and returns its unique id.
   * @param n name to be added
   * @param v value, added to statistics
   * @param st statistics flag
   * @return name id
   */
  public int index(final byte[] n, final byte[] v, final boolean st) {
    final int i = Math.abs(add(n));
    if(st) {
      if(stats[i] == null) stats[i] = new Stats();
      final Stats stat = stats[i];
      if(v != null) stat.add(v, meta);
      stat.count++;
    }
    return i;
  }

  /**
   * Adds a value to the statistics of the specified key.
   * Evaluates the value for the specified key id.
   * @param n name id
   * @param v value, added to statistics
   */
  public void index(final int n, final byte[] v) {
    stats[n].add(v, meta);
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
    final double[] tl = new double[size];
    int len = 0;
    tl[0] = 0;
    for(int i = 1; i < size; ++i) {
      if(len < keys[i].length) len = keys[i].length;
      if(stats[i] == null) continue;
      tl[i] = stats[i].count;
    }
    len += 2;

    // print all entries in descending number of occurrences
    final int[] ids = Array.createOrder(tl, false);

    final TokenBuilder tb = new TokenBuilder();
    tb.add(Text.INDEXSTRUC + Text.HASHSTRUC + Text.NL);
    tb.add(Text.IDXENTRIES + (size - 1) + Text.NL);
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
  public TokenIntMap entries(final byte[] prefix) {
    final TokenIntMap tim = new TokenIntMap();
    for(int i = 1; i < size; ++i) {
      tim.add(keys[i], stats[i].count);
    }
    return tim;
  }

  /**
   * Serializes the path node.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  void plan(final Serializer ser) throws IOException {
    ser.openElement(INDEX);
    for(int i = 1; i < size; ++i) {
      ser.openElement(KEY, NAME, keys[i]);
      stats[i].plan(ser);
      ser.closeElement();
    }
    ser.closeElement();
  }

  @Override
  protected void rehash() {
    super.rehash();
    stats = Arrays.copyOf(stats, size << 1);
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
