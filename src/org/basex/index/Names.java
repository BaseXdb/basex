package org.basex.index;

import static org.basex.Text.*;

import java.io.IOException;
import org.basex.data.StatsKey;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Set;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class indexes and organizes the tags or attribute names,
 * used in an XML document.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class Names extends Set {
  /** Number of token occurrences. */
  private int[] counter;
  /** Flag referencing non-leaves. */
  private boolean[] noleaf;
  /** Statistic information. */
  private StatsKey[] stat;

  /**
   * Empty Constructor.
   */
  public Names() {
    counter = new int[CAP];
    noleaf = new boolean[CAP];
    stat = new StatsKey[CAP];
  }

  /**
   * Constructor, specifying an input file.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Names(final DataInput in) throws IOException {
    keys = in.readBytesArray();
    next = in.readNums();
    bucket = in.readNums();
    counter = in.readNums();
    noleaf = in.readBools();
    size = in.readNum();
    stat = new StatsKey[next.length];
    for(int s = 1; s < size; s++) stat[s] = new StatsKey(in);
  }

  /**
   * Initializes the statistics.
   */
  public void init() {
    for(int i = 1; i < size; i++) {
      stat[i] = new StatsKey();
      counter[i] = 0;
    }
  }

  /**
   * Indexes a name and returns its unique id.
   * @param k name to be found
   * @param v value, evaluated in statistics
   * @param st statistics flag
   * @return name id
   */
  public int index(final byte[] k, final byte[] v, final boolean st) {
    final int i = Math.abs(add(k));
    if(stat[i] == null) stat[i] = new StatsKey();
    if(st) {
      counter[i]++;
      stat[i].add(v);
    }
    return i;
  }

  /**
   * Evaluates the value for the specified key id.
   * @param i key id
   * @param v value, used for statistics
   */
  public void index(final int i, final byte[] v) {
    stat[i].add(v);
  }

  /**
   * Writes the names to the specified output stream.
   * @param out output stream
   * @throws IOException in case the file could not be written
   */
  public synchronized void finish(final DataOutput out) throws IOException {
    out.writeBytesArray(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeNums(counter);
    out.writeBools(noleaf);
    out.writeNum(size);
    for(int s = 1; s < size; s++) stat[s].finish(out);
  }

  /**
   * Returns the tag counter (number of indexed keys).
   * @param id token id
   * @return tag counter
   */
  public int counter(final int id) {
    return counter[id];
  }

  /**
   * Sets node flag for the specified id.
   * @param id token id
   * @param l node flag
   */
  public void noleaf(final int id, final boolean l) {
    noleaf[id] = l;
  }

  /**
   * Returns the statistics for the specified key id.
   * @param id id
   * @return statistics
   */
  public StatsKey stat(final int id) {
    return stat[id];
  }

  /**
   * Returns node flag for the specified id.
   * @param id token id
   * @return the indexed token
   */
  public boolean noLeaf(final int id) {
    return noleaf[id];
  }

  /**
   * Returns index information.
   * @return statistics string
   */
  public byte[] info() {
    final byte[][] tl = new byte[size][];
    int len = 0;
    for(int i = 1; i < size; i++) {
      if(len < keys[i].length) len = keys[i].length;
      tl[i] = Token.token(counter[i]);
    }
    len += 2;

    // print all entries in descending number of occurrences
    final IntList ids = IntList.createOrder(tl, true, false);
    
    final TokenBuilder tb = new TokenBuilder();
    tb.add(NAMINDEX + NL);
    tb.add(IDXENTRIES + (size - 1) + NL);
    for(int i = 0; i < size - 1; i++) {
      final int s = ids.list[i];
      if(counter[s] == 0) continue;
      final byte[] key = keys[s];
      tb.add("  ");
      tb.add(key);
      for(int j = 0; j < len - key.length; j++) tb.add(' ');
      tb.add(counter[s] + "x" + stat[s]);
      if(!noleaf[s]) tb.add(", leaf");
      tb.add(NL);
    }
    return tb.finish();
  }

  @Override
  protected void rehash() {
    super.rehash();
    counter = Array.extend(counter);
    noleaf = Array.extend(noleaf);
    stat = Array.extend(stat);
  }
}
