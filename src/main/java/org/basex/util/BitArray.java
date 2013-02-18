package org.basex.util;

import static java.lang.Long.*;

/**
 * Bit array that grows when needed. The implementation is similar to
 * {@link java.util.BitSet}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class BitArray {
  /** Number of bits needed to address a bit in a word; 2<sup>6</sup> = 64. */
  private static final int WORD_POWER = 6;
  /** Size of a word = 2<sup>{@link #WORD_POWER}</sup>. */
  private static final int WORD_SIZE = 1 << WORD_POWER;
  /** A bit mask of 64 bits set to 1. */
  private static final long WORD_MASK = -1L;

  /** Bit storage. */
  private long[] words;
  /** Number of used bits. */
  private int size;

  /** Construct a new bit array. */
  public BitArray() {
    this(WORD_SIZE);
  }

  /**
   * Construct a new bit array with the specified number of bits.
   * @param n initial number of bits (> 0)
   */
  public BitArray(final int n) {
    init(n);
  }

  /**
   * Construct a new bit array with the specified backing array.
   * @param a array with bits
   * @param l number of used bits
   */
  public BitArray(final long[] a, final int l) {
    init(a, l);
}

  /** Initialize the bit array with an empty array. */
  public void init() {
    init(new long[1], 0);
  }

  /**
   * Initialize the bit array with a new size. All bits will be set to 0.
   * @param n initial number of bits (> 0)
   */
  void init(final int n) {
    init(new long[(Math.max(0, n - 1) >>> WORD_POWER) + 1], n);
  }

  /**
   * Initialize the bit array with the specified backing array.
   * @param a array with bits
   * @param l number of used bits
   */
  public void init(final long[] a, final int l) {
    words = a;
    size = l;
  }

  /**
   * The word array used to store the bits. The array is shrunk to the last
   * word, where a bit is set.
   * @return array of longs
   */
  public long[] toArray() {
    // find the last index of a word which is different from 0:
    int i = words.length;
    while(--i >= 0 && words[i] == 0);

    final long[] result = new long[++i];
    System.arraycopy(words, 0, result, 0, i);
    return result;
  }

  /**
   * Returns the number of bits set to {@code true}.
   * @return  the number of bits set to {@code true}
   */
  public int cardinality() {
    int sum = 0, inUse = (size + WORD_SIZE - 1) >>> WORD_POWER;
    for (int i = 0; i < inUse; i++) sum += bitCount(words[i]);
    return sum;
  }

  /**
   * Get the value of the i<sup>th</sup> bit.
   * @param i index of the bit
   * @return <code>true</code> if the i<sup>th</sup> bit is set
   */
  public boolean get(final int i) {
    if(i >= size) return false;
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wi = i >>> WORD_POWER;
    // check if the ith bit is 1
    return (words[wi] & 1L << i) != 0;
  }

  /**
   * Set the i<sup>th</sup> bit to 1.
   * @param i index of the bit
   */
  public void set(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wi = i >>> WORD_POWER;
    if(wi >= words.length) resize(wi + 1);
    words[wi] |= 1L << i;
    if(i >= size) size = i + 1;
  }

  /**
   * Set the i<sup>th</sup> bit to 0.
   * @param i index of the bit
   */
  public void clear(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wi = i >>> WORD_POWER;
    if(wi >= words.length) resize(wi + 1);
    words[wi] &= ~(1L << i);
    // it is not necessary to set the last used bit
  }

  /**
   * Get the next bit set to 0, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @return index of the next clear bit after the i<sup>th</sup> bit
   */
  public int nextFree(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    int wi = i >>> WORD_POWER;
    // invert the word and skip the first i bits:
    long word = ~words[wi] & WORD_MASK << i;

    if(word != 0) {
      return (wi << WORD_POWER) + numberOfTrailingZeros(word);
    }

    while(++wi < words.length) {
      if((word = ~words[wi]) != 0) {
        return (wi << WORD_POWER) + numberOfTrailingZeros(word);
      }
    }

    // wi * 2^6:
    return wi << WORD_POWER;
  }

  /**
   * Get the next bit set to 1, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @return index of the next set bit after the i<sup>th</sup> bit
   */
  public int nextSet(final int i) {
    if(i >= size) return -1;

    final int inUse = (size + WORD_SIZE - 1) >>> WORD_POWER;
    int wi = i >>> WORD_POWER;
    long word = words[wi] & (WORD_MASK << i);
    while(true) {
      if (word != 0) return (wi << WORD_POWER) + numberOfTrailingZeros(word);
      if (++wi == inUse) return -1;
      word = words[wi];
    }
  }

  /**
   * Expand the {@link #words} array to the desired size.
   * @param s new size
   */
  private void resize(final int s) {
    final long[] tmp = new long[Math.max(words.length << 1, s)];
    System.arraycopy(words, 0, tmp, 0, words.length);
    words = tmp;
  }
}
