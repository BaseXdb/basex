package org.basex.util;

import java.util.*;

/**
 * Blake3 algorithm, inspired by the RUST reference algorithm:
 * https://github.com/BLAKE3-team/BLAKE3.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Blake3 {
  /** Fixed permutation pattern. */
  private static final int[] PERMUTATION = { 2, 6, 3, 10, 7, 0, 4, 13, 1, 11, 12, 5, 9, 14, 15, 8 };
  /** Initial key. */
  private static final int[] INITIAL = { 0x6A09E667, 0xBB67AE85, 0x3C6EF372, 0xA54FF53A,
      0x510E527F, 0x9B05688C, 0x1F83D9AB, 0x5BE0CD19 };

  /** Words size. */
  private static final int WORDS_SIZE = 16;
  /** Hash size. */
  private static final int HASH_SIZE = 32;
  /** Block size. */
  private static final int BLOCK_SIZE = 64;
  /** Chunk size. */
  private static final int CHUNK_SIZE = 1024;

  /**
   * Generates a hash value.
   * @param input data to be added
   * @return hash
   */
  public byte[] digest(final byte[] input) {
    final int il = input.length;
    final int[][] stack = new int[54][];
    State state = new State(0);
    int sp = 0;
    for(int i = 0; i < il;) {
      if(state.length() == CHUNK_SIZE) {
        final long chunks = state.counter + 1;
        int[] value = state.node().value();
        for(long c = chunks; (c & 1) == 0;) {
          value = node(stack[--sp], value).value();
          c >>= 1;
        }
        stack[sp++] = value;
        state = new State(chunks);
      }
      final int i2 = i + Math.min(CHUNK_SIZE - state.length(), il - i);
      state.update(Arrays.copyOfRange(input, i, i2));
      i = i2;
    }

    Node node = state.node();
    for(int s = sp; --s >= 0;) node = node(stack[s], node.value());
    return node.finish();
  }

  /**
   * Creates a node.
   * @param left left array
   * @param right right array
   * @return node
   */
  private static Node node(final int[] left, final int[] right) {
    final int[] words = new int[WORDS_SIZE];
    int w = 0;
    for(final int l : left) words[w++] = l;
    for(final int r : right) words[w++] = r;
    return new Node(INITIAL, words, 0, BLOCK_SIZE, 4);
  }

  /**
   * Compresses integers.
   * @param value value
   * @param words words
   * @param counter counter
   * @param length length
   * @param flags flags
   * @return result
   */
  private static int[] compress(final int[] value, final int[] words, final long counter,
      final int length, final int flags) {
    final int lo = (int) (counter & 0xFFFFFFFFL);
    final int hi = (int) (counter >> 32 & 0xFFFFFFFFL);
    final int[] state = { value[0], value[1], value[2], value[3], value[4], value[5], value[6],
        value[7], INITIAL[0], INITIAL[1], INITIAL[2], INITIAL[3], lo, hi, length, flags };

    final int[] w = words, w2 = new int[words.length];
    round(state, w);
    permute(w, w2);
    round(state, w2);
    permute(w2, w);
    round(state, w);
    permute(w, w2);
    round(state, w2);
    permute(w2, w);
    round(state, w);
    permute(w, w2);
    round(state, w2);
    permute(w2, w);
    round(state, w);

    for(int i = 0; i < 8; i++) {
      state[i] ^= state[i + 8];
      state[i + 8] ^= value[i];
    }
    return state;
  }

  /**
   * Rounds values.
   * @param state state
   * @param words words
   */
  private static void round(final int[] state, final int[] words) {
    mix(state, 0, 4,  8, 12, words[ 0], words[ 1]);
    mix(state, 1, 5,  9, 13, words[ 2], words[ 3]);
    mix(state, 2, 6, 10, 14, words[ 4], words[ 5]);
    mix(state, 3, 7, 11, 15, words[ 6], words[ 7]);
    mix(state, 0, 5, 10, 15, words[ 8], words[ 9]);
    mix(state, 1, 6, 11, 12, words[10], words[11]);
    mix(state, 2, 7,  8, 13, words[12], words[13]);
    mix(state, 3, 4,  9, 14, words[14], words[15]);
  }

  /**
   * Mixes values.
   * @param state state
   * @param a first value
   * @param b second value
   * @param c third value
   * @param d fourth value
   * @param mx x
   * @param my y
   */
  private static void mix(final int[] state, final int a, final int b, final int c, final int d,
      final int mx, final int my) {
    state[a] = state[a] + state[b] + mx;
    state[d] = rotate(state[d] ^ state[a], 16);
    state[c] = state[c] + state[d];
    state[b] = rotate(state[b] ^ state[c], 12);
    state[a] = state[a] + state[b] + my;
    state[d] = rotate(state[d] ^ state[a], 8);
    state[c] = state[c] + state[d];
    state[b] = rotate(state[b] ^ state[c], 7);
  }

  /**
   * Rotates a value.
   * @param n value
   * @param length length
   * @return result
   */
  private static int rotate(final int n, final int length) {
    return n >>> length | n << 32 - length;
  }

  /**
   * Permutes values.
   * @param words first values
   * @param words2 second values
   */
  private static void permute(final int[] words, final int[] words2) {
    for(int i = 0; i < WORDS_SIZE; i++) words2[i] = words[PERMUTATION[i]];
  }

  /** Node. */
  private static final class Node {
    /** Value. */
    int[] value;
    /** Words. */
    int[] words;
    /** Counter. */
    long counter;
    /** Length. */
    int length;
    /** Flags. */
    int flags;

    /**
     * Constructor.
     * @param value value
     * @param words words
     * @param counter counter
     * @param length length
     * @param flags flags
     */
    private Node(final int[] value, final int[] words, final long counter, final int length,
        final int flags) {
      this.value = value;
      this.words = words;
      this.counter = counter;
      this.length = length;
      this.flags = flags;
    }

    /**
     * Returns the node value.
     * @return value
     */
    private int[] value() {
      return Arrays.copyOfRange(compress(value, words, counter, length, flags), 0, 8);
    }

    /**
     * Finishes the value.
     * @return result
     */
    private byte[] finish() {
      final byte[] hash = new byte[HASH_SIZE];
      for(int o = 0, i = 0;; o++) {
        for(final int c : compress(value, words, o, length, flags | 8)) {
          for(int j = 0; j < 4; j++, i++) hash[i] = (byte) (c >> (j << 3));
          if(i == HASH_SIZE) return hash;
        }
      }
    }
  }

  /** State. */
  private static final class State {
    /** Counter. */
    final long counter;
    /** Block. */
    byte[] block = new byte[BLOCK_SIZE];
    /** Value. */
    int[] value = INITIAL;
    /** Length. */
    byte length;
    /** Compare. */
    byte cmp;

    /**
     * Constructor.
     * @param counter counter
     */
    private State(final long counter) {
      this.counter = counter;
    }

    /**
     * Returns the length.
     * @return length
     */
    private int length() {
      return BLOCK_SIZE * cmp + length;
    }

    /**
     * Updates a value.
     * @param input value to be updated
     */
    private void update(final byte[] input) {
      final int il = input.length;
      for(int i = 0; i < il;) {
        if(length == BLOCK_SIZE) {
          value = Arrays.copyOfRange(compress(value, words(), counter, BLOCK_SIZE, cmp()), 0, 8);
          block = new byte[BLOCK_SIZE];
          length = 0;
          cmp++;
        }
        final int add = Math.min(BLOCK_SIZE - length, il - i);
        System.arraycopy(input, i, block, length, add);
        length += add;
        i += add;
      }
    }

    /**
     * Returns a node.
     * @return node
     */
    private Node node() {
      return new Node(value, words(), counter, length, cmp() | 2);
    }

    /**
     * Returns cmp.
     * @return cmp.
     */
    private int cmp() {
      return cmp == 0 ? 1 : 0;
    }

    /**
     * Returns words.
     * @return words
     */
    private int[] words() {
      final int tl = block.length >>> 2;
      final int[] tmp = new int[tl];
      for(int t = 0; t < tl; t++) {
        for(int j = 0, b = t << 2; j < 4; j++) tmp[t] |= (block[b + j] & 0xFF) << (j << 3);
      }
      return tmp;
    }
  }
}
