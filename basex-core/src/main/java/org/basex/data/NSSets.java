package org.basex.data;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class stores the distinct sets of prefix/namespace URI pairs of a database.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class NSSets {
  /** Empty set. */
  private static final int[] EMPTY = {};

  /** Encoded sets, used for hashing and serialization (IDs start with 1). */
  private final TokenSet index;
  /** Decoded sets, addressed by their IDs. */
  private int[][] sets;

  /**
   * Empty constructor.
   */
  NSSets() {
    index = new TokenSet();
    sets = new int[][] { EMPTY };
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  NSSets(final DataInput in) throws IOException {
    index = new TokenSet(in);
    final int is = index.size();
    sets = new int[is + 1][];
    sets[0] = EMPTY;
    for(int i = 1; i <= is; i++) sets[i] = decode(index.key(i));
  }

  /**
   * Writes the sets to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    index.write(out);
  }

  /**
   * Returns the number of stored sets (the empty set is not counted).
   * @return number of sets
   */
  int size() {
    return index.size();
  }

  /**
   * Returns the prefix/URI pairs with the specified ID.
   * @param id set ID ({@code 0}: empty set)
   * @return prefix/URI pairs
   */
  int[] get(final int id) {
    return sets[id];
  }

  /**
   * Returns the ID of the specified prefix/URI pairs and stores them if they are new.
   * @param values prefix/URI pairs
   * @return set ID ({@code 0}: empty set)
   */
  int put(final int[] values) {
    if(values.length == 0) return 0;
    final int id = index.put(encode(values));
    if(id >= sets.length) sets = Arrays.copyOf(sets, Array.newCapacity(id));
    if(sets[id] == null) sets[id] = values;
    return id;
  }

  /**
   * Returns the ID of the namespace URI for the specified prefix.
   * @param id set ID
   * @param prefixId prefix reference
   * @return ID of the namespace URI, or {@code 0} if none is found
   */
  int uri(final int id, final int prefixId) {
    final int[] values = sets[id];
    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      if(values[v] == prefixId) return values[v + 1];
    }
    return 0;
  }

  /**
   * Returns the ID of a set that is extended by the specified prefix/URI pair.
   * @param id set ID
   * @param prefixId prefix reference
   * @param uriId namespace URI reference
   * @return new set ID
   */
  int add(final int id, final int prefixId, final int uriId) {
    final int[] values = sets[id], vals = Arrays.copyOf(values, values.length + 2);
    vals[values.length] = prefixId;
    vals[values.length + 1] = uriId;
    return put(vals);
  }

  /**
   * Returns the ID of a set from which the specified namespace URI has been removed.
   * @param id set ID
   * @param uriId namespace URI reference
   * @return new set ID, or the old one if the URI was not found
   */
  int delete(final int id, final int uriId) {
    final int[] values = sets[id];
    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      if(values[v + 1] != uriId) continue;
      final int[] vals = new int[vl - 2];
      Array.copy(values, v, vals);
      Array.copy(values, v + 2, vl - v - 2, vals, v);
      return put(vals);
    }
    return id;
  }

  /**
   * Encodes prefix/URI pairs as a token.
   * @param values prefix/URI pairs
   * @return token
   */
  private static byte[] encode(final int[] values) {
    final int vl = values.length;
    final byte[] token = new byte[vl << 2];
    for(int v = 0, t = 0; v < vl; v++) {
      final int value = values[v];
      token[t++] = (byte) (value >> 24);
      token[t++] = (byte) (value >> 16);
      token[t++] = (byte) (value >> 8);
      token[t++] = (byte) value;
    }
    return token;
  }

  /**
   * Decodes a token to prefix/URI pairs.
   * @param token token
   * @return prefix/URI pairs
   */
  private static int[] decode(final byte[] token) {
    final int vl = token.length >>> 2;
    final int[] values = new int[vl];
    for(int v = 0, t = 0; v < vl; v++) {
      values[v] = (token[t++] & 0xFF) << 24 | (token[t++] & 0xFF) << 16 |
                  (token[t++] & 0xFF) << 8 | token[t++] & 0xFF;
    }
    return values;
  }
}
