package org.basex.index;

import java.io.IOException;
import org.basex.data.StatsKey;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.Set;
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
  /** Statistics flag. */
  public boolean stats;

  /**
   * Empty Constructor.
   */
  public Names() {
    counter = new int[CAP];
    noleaf = new boolean[CAP];
    stat = new StatsKey[CAP];
    stats = true;
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
    noleaf = in.readBooleans();
    size = in.readNum();
    stats = in.readBool();
    stat = new StatsKey[next.length];
    for(int s = 1; s < size; s++) stat[s] = new StatsKey(in);
  }

  /**
   * Indexes a name and returns its unique id.
   * @param k name to be found
   * @param v value, evaluated in statistics
   * @return name id
   */
  public int index(final byte[] k, final byte[] v) {
    final int i = Math.abs(add(k));
    if(stat[i] == null) stat[i] = new StatsKey();
    if(stats) {
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
    out.writeBooleans(noleaf);
    out.writeNum(size);
    out.writeBool(stats);
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
   * Removes the statistics.
   */
  public void noStats() {
    stats = false;
    for(int i = 1; i < size; i++) {
      stat[i] = new StatsKey();
      counter[i] = 0;
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
    final TokenBuilder sb = new TokenBuilder();
    final int[] ids = sort();
    int len = 0;
    for(int i = 1; i < size; i++) if(len < keys[i].length) len = keys[i].length;
    len += 3;

    // print all entries in descending number of occurrences
    for(int i = 1; i < size; i++) {
      final int s = ids[i];
      if(counter[s] == 0) continue;
      final byte[] key = keys[s];
      sb.add("- ");
      sb.add(key);
      for(int j = 0; j < len - key.length; j++) sb.add(' ');
      sb.add(counter[s] + "x" + stat[s]);
      if(!noleaf[s]) sb.add(", leaf");
      sb.add("\n");
    }
    return sb.finish();
  }

  /**
   * Sorts names by their number of occurrences.
   * @return sorted ids
   */
  private int[] sort() {
    final int[] ids = new int[size];
    for(int i = 0; i < size; i++) ids[i] = i;

    for(int i = 0; i < size; i++) {
      for(int j = 1; j < size; j++) {
        if(counter[ids[i]] > counter[ids[j]]) {
          final int t = ids[i];
          ids[i] = ids[j];
          ids[j] = t;
        }
      }
    }
    return ids;
  }

  @Override
  protected void rehash() {
    super.rehash();
    counter = Array.extend(counter);
    noleaf = Array.extend(noleaf);
    stat = Array.extend(stat);
  }
}
