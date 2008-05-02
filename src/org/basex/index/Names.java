package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.data.StatsKey;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.Set;
import org.basex.util.Token;

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
  /** Flag pointing to non-leaves. */
  private boolean[] noleaf;
  /** Hash values. */
  private StatsKey[] stat;
  /** Index type (tags/attribute names). */
  private final boolean tag;
  /** Statistics flag. */
  private boolean stats;

  /**
   * Empty Constructor.
   * @param t index type (tags/attribute names).
   */
  public Names(final boolean t) {
    counter = new int[CAP];
    noleaf = new boolean[CAP];
    stat = new StatsKey[CAP];
    stats = true;
    tag = t;
  }

  /**
   * Constructor, specifying an input file.
   * @param db name of the database
   * @param t index type (tags/attribute names).
   * @throws IOException IO Exception
   */
  public Names(final String db, final boolean t) throws IOException {
    final DataInput in = new DataInput(db, t ? DATATAG : DATAATN);
    keys = in.readBytesArray();
    next = in.readNums();
    bucket = in.readNums();
    counter = in.readNums();
    noleaf = in.readBooleans();
    size = in.readNum();
    stats = in.readBool();
    stat = new StatsKey[size];
    for(int s = 1; s < size; s++) stat[s] = new StatsKey(in);
    tag = t;
    in.close();
  }

  /**
   * Indexes a key, evaluates the value, and returns its unique id.
   * @param k key to be found
   * @param v value, used for statistics
   * @return token id or -1 if token was not found
   */
  public int index(final byte[] k, final byte[] v) {
    int i = add(k);
    if(i > 0) stat[i] = new StatsKey();
    else i = -i;
    counter[i]++;
    stat[i].add(v);
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
   * Finishes the index structure and optimizes its memory usage.
   * @param db name of the database
   * @throws IOException in case the file could not be written
   */
  public synchronized void finish(final String db) throws IOException {
    final DataOutput out = new DataOutput(db, tag ? DATATAG : DATAATN);
    out.writeBytesArray(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeNums(counter);
    out.writeBooleans(noleaf);
    out.writeNum(size);
    out.writeBool(stats);
    for(int s = 1; s < size; s++) stat[s].finish(out);
    out.close();
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
   * Returns information if the indexed keys offer statistics.
   * @return result of check
   */
  public boolean stats() {
    return stats;
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
   * Returns the statistics for the specified key.
   * @param key key
   * @return statistics
   */
  public StatsKey stat(final byte[] key) {
    return stat[id(key)];
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
  public String info() {
    final StringBuilder sb = new StringBuilder();
    sb.append("- Main-Memory Hash\n\n");
    final int[] ids = sort();
    int len = 0;
    for(int i = 1; i < size; i++) if(len < keys[i].length) len = keys[i].length;
    len += 3;

    // print all entries in descending number of occurrences
    for(int i = 1; i < size; i++) {
      final int s = ids[i];
      if(counter[s] == 0) continue;
      final byte[] key = keys[s];
      sb.append(i + ": " + Token.string(key));
      for(int j = 0; j < len - key.length; j++) sb.append(' ');
      sb.append(counter[s] + "x" + stat[s]);
      if(!noleaf[s]) sb.append(", leaf");
      sb.append("\n");
    }
    return sb.toString();
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
