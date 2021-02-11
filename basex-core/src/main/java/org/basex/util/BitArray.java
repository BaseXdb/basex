package org.basex.util;

import static java.lang.Long.*;

import java.util.*;

/**
 * Bit array that grows when needed. The implementation is similar to {@link BitSet}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class BitArray {
  /** Number of bits needed to address a bit in a word; 2<sup>6</sup> = 64. */
  private static final int WORD_POWER = 6;
  /** Size of a word = 2<sup>{@link #WORD_POWER}</sup>. */
  static final int WORD_SIZE = 1 << WORD_POWER;
  /** A bit mask of 64 bits set to 1. */
  private static final long WORD_MASK = -1L;

  /** Bit storage. */
  private long[] words;
  /** Number of used bits. */
  private int size;

  /**
   * Constructs a new bit array with the specified backing array.
   * @param array array with bits
   * @param nr number of used bits
   */
  public BitArray(final long[] array, final int nr) {
    words = array;
    size = nr;
  }

  /**
   * Constructs a new bit array.
   */
  public BitArray() {
    this(new long[1], 0);
  }

  /**
   * Constructs a new bit array and an initial value.
   * @param capacity initial number of bits
   * @param set sets or clears all values
   */
  public BitArray(final int capacity, final boolean set) {
    this(new long[(Math.max(0, capacity - 1) >>> WORD_POWER) + 1], capacity);
    if(set) {
      final int p = Math.max(0, capacity - 1) >>> WORD_POWER;
      for(int i = 0; i < p; i++) words[i] = 0XFFFFFFFFFFFFFFFFL;
      for(int i = p << WORD_POWER; i < capacity; i++) set(i);
    }
  }

  /**
   * The word array used to store the bits. The array is shrunk to the last
   * word, where a bit is set.
   * @return array of longs
   */
  public long[] toArray() {
    // find the last index of a word which is different from 0
    int i = words.length;
    while(--i >= 0 && words[i] == 0);
    return Arrays.copyOf(words, ++i);
  }

  /**
   * Returns the number of bits set to {@code true}.
   * @return number of bits set to {@code true}
   */
  public int cardinality() {
    int sum = 0;
    final int inUse = size + WORD_SIZE - 1 >>> WORD_POWER;
    for(int i = 0; i < inUse; i++) sum += bitCount(words[i]);
    return sum;
  }

  /**
   * Gets the value of the i<sup>th</sup> bit.
   * @param i index of the bit
   * @return {@code true} if the i<sup>th</sup> bit is set
   */
  public boolean get(final int i) {
    if(i >= size) return false;
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wi = i >>> WORD_POWER;
    // check if the ith bit is 1
    return (words[wi] & 1L << i) != 0;
  }

  /**
   * Sets the i<sup>th</sup> bit to 1.
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
   * Sets the i<sup>th</sup> bit to 0.
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
   * Gets the index of the first clear bit.
   * @return index
   */
  public int nextFree() {
    final int wl = words.length;
    for(int w = 0; w < wl; w++) {
      final long word = ~words[w];
      if(word != 0) {
        return (w << WORD_POWER) + numberOfTrailingZeros(word);
      }
    }
    return wl << WORD_POWER;
  }

  /**
   * Gets the next bit set to 1, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @return index of the next set bit after the i<sup>th</sup> bit
   */
  public int nextSet(final int i) {
    if(i >= size) return -1;

    final int inUse = size + WORD_SIZE - 1 >>> WORD_POWER;
    int wi = i >>> WORD_POWER;
    long word = words[wi] & WORD_MASK << i;
    while(true) {
      if(word != 0) return (wi << WORD_POWER) + numberOfTrailingZeros(word);
      if(++wi == inUse) return -1;
      word = words[wi];
    }
  }

  /**
   * Expands the {@link #words} array to the desired size.
   * @param s new size
   */
  private void resize(final int s) {
    words = Arrays.copyOf(words, Math.max(words.length << 1, s));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("BitArray. Size: " + size + ", Entries: ");
    for(final long w : words) sb.append(toBinaryString(w)).append(' ');
    return sb.toString();
  }
}
