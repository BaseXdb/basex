package org.basex.util;

import static java.lang.Long.*;
import static java.lang.Math.*;

/**
 * Bit array that grows when needed. The implementation is similar to
 * {@link java.util.BitSet}.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
public class BitArray {

  /** Number of bits needed to address a bit in a word; 2<sup>6</sup> = 64. */
  private static final int WORD_POWER = 6;
  /** Size of a word = 2<sup>{@link #WORD_POWER}</sup>. */
  private static final int WORD_SIZE = 1 << WORD_POWER;
  /** A bit mask of 64 bits set to 1. */
  private static final long WORD_MASK = -1L;

  /** Bit storage. */
  private long[] words;
  /** Last used bit. */
  private long last;

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
   * Construct a new bit array with the specified number of bits.
   * @param n initial number of bits (> 0)
   */
  public BitArray(final long n) {
    init(n);
  }

  /**
   * Construct a new bit array with the specified backing array.
   * @param a array with bits
   * @param l last used bit
   */
  public BitArray(final long[] a, final long l) {
    setWords(a, l);
  }

  /** Initialize the bit array with an empty array. */
  public void init() {
    setWords(new long[1], 0);
  }

  /**
   * Initialize the bit array with a new size. All bits will be set to 0.
   * @param n initial number of bits (> 0)
   */
  public void init(final int n) {
    setWords(new long[(Math.max(0, n - 1) >>> WORD_POWER) + 1], n);
  }

  /**
   * Initialize the bit array with a new size. All bits will be set to 0.
   * @param n initial number of bits (> 0)
   */
  public void init(final long n) {
    setWords(new long[(int) (((n - 1L) >>> WORD_POWER) + 1L)], n);
  }

  /**
   * The word array used to store the bits.
   * @return array of longs
   */
  public long[] getWords() {
    return words;
  }

  /**
   * Initialize the bit array with the specified backing array.
   * @param a array with bits
   * @param l last used bit
   */
  public void setWords(final long[] a, final long l) {
    words = a;
    last = l;
  }

  /**
   * The last used bit.
   * @return index of last used bit
   */
  public long getLast() {
    return last;
  }

  /**
   * The word array used to store the bits. The array is shrunk to the last
   * word, where a bit is set.
   * @return array of longs
   */
  public long[] getTrimmedWords() {
    // find the last index of a word which is different from 0:
    int i;
    for(i = words.length - 1; i >= 0; i--) if(words[i] != 0) break;
    // final int i = last >>> WORD_POWER + 1;
    // final long[] result = new long[i];

    final long[] result = new long[++i];
    System.arraycopy(words, 0, result, 0, i);

    return result;
  }

  /**
   * Get the value of the i<sup>th</sup> bit.
   * @param i index of the bit
   * @return <code>true</code> if the ith bit is
   */
  public boolean get(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wordIndex = i >>> WORD_POWER;
    // check if the ith bit is 1
    return (words[wordIndex] & (1L << i)) != 0;
  }

  /**
   * Get the value of the i<sup>th</sup> bit.
   * @param i index of the bit
   * @return <code>true</code> if the ith bit is
   */
  public boolean get(final long i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wordIndex = (int) (i >>> WORD_POWER);
    // check if the ith bit is 1
    return (words[wordIndex] & (1L << i)) != 0;
  }

  /**
   * Set the i<sup>th</sup> bit to 1.
   * @param i index of the bit
   */
  public void set(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wordIndex = i >>> WORD_POWER;
    if(wordIndex >= words.length) expandTo(wordIndex + 1);
    words[wordIndex] |= 1L << i;
    if(i > last) last = i;
  }

  /**
   * Set the i<sup>th</sup> bit to 1.
   * @param i index of the bit
   */
  public void set(final long i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wordIndex = (int) (i >>> WORD_POWER);
    if(wordIndex >= words.length) expandTo(wordIndex + 1);
    words[wordIndex] |= 1L << i;
    if(i > last) last = i;
  }

  /**
   * Set a range of bits, expanding the size if necessary. <br/>
   * NOTE: <code>s < e</code> must be true!
   * @param s index of the first bit to set
   * @param e index of the last bit to set + 1 (to be in conformance with
   *          {@link java.util.BitSet})
   */
  public void set(final int s, final int e) {
    final int startWord = s >>> WORD_POWER;
    final int endWord = (e - 1) >>> WORD_POWER;

    if(endWord >= words.length) expandTo(endWord + 1);

    final long startMask = WORD_MASK << s;
    // 64 - (e % 64) = 64 - (e & 63) = -e, due to wrap
    final long endMask = WORD_MASK >>> -e;

    if(startWord == endWord) {
      words[startWord] |= startMask & endMask;
    } else {
      words[startWord] |= startMask;
      for(int i = startWord + 1; i < endWord; i++) words[i] = WORD_MASK;
      words[endWord] |= endMask;
    }
    if(e > last) last = e;
  }

  /**
   * Set a range of bits, expanding the size if necessary. <br/>
   * NOTE: <code>s < e</code> must be true!
   * @param s index of the first bit to set
   * @param e index of the last bit to set + 1 (to be in conformance with
   *          {@link java.util.BitSet})
   */
  public void set(final long s, final long e) {
    final int startWord = (int) (s >>> WORD_POWER);
    final int endWord = (int) ((e - 1L) >>> WORD_POWER);

    if(endWord >= words.length) expandTo(endWord + 1);

    final long startMask = WORD_MASK << s;
    // 64 - (e % 64) = 64 - (e & 63) = -e, due to wrap
    final long endMask = WORD_MASK >>> -e;

    if(startWord == endWord) {
      words[startWord] |= startMask & endMask;
    } else {
      words[startWord] |= startMask;
      for(int i = startWord + 1; i < endWord; i++) words[i] = WORD_MASK;
      words[endWord] |= endMask;
    }
    if(e > last) last = e;
  }

  /**
   * Set the i<sup>th</sup> bit to 0.
   * @param i index of the bit
   */
  public void clear(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wordIndex = i >>> WORD_POWER;
    if(wordIndex >= words.length) expandTo(wordIndex + 1);
    words[wordIndex] &= ~(1L << i);
    // it is not necessary to set the last used bit
  }

  /**
   * Set the i<sup>th</sup> bit to 0.
   * @param i index of the bit
   */
  public void clear(final long i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    final int wordIndex = (int) (i >>> WORD_POWER);
    if(wordIndex >= words.length) expandTo(wordIndex + 1);
    words[wordIndex] &= ~(1L << i);
    // it is not necessary to set the last used bit
  }

  /**
   * Clear a range of bits. <br/>
   * NOTE: <code>s < e</code> must be true!
   * @param s index of the first bit to set
   * @param e index of the last bit to set + 1 (to be in conformance with
   *          {@link java.util.BitSet})
   */
  public void clear(final int s, final int e) {
    final int startWord = s >>> WORD_POWER;
    final int endWord = min((e - 1) >>> WORD_POWER, words.length - 1);

    final long startMask = ~(WORD_MASK << s);
    // 64 - (e % 64) = 64 - (e & 63) = -e, due to wrap
    final long endMask = ~(WORD_MASK >>> -e);

    if(startWord == endWord) {
      words[startWord] &= startMask | endMask;
    } else {
      words[startWord] &= startMask;
      for(int i = startWord + 1; i < endWord; i++) words[i] = 0L;
      words[endWord] &= endMask;
    }
  }

  /**
   * Clear a range of bits. <br/>
   * NOTE: <code>s < e</code> must be true!
   * @param s index of the first bit to set
   * @param e index of the last bit to set + 1 (to be in conformance with
   *          {@link java.util.BitSet})
   */
  public void clear(final long s, final long e) {
    final int startWord = (int) (s >>> WORD_POWER);
    final int endWord = min((int) ((e - 1L) >>> WORD_POWER), words.length - 1);

    final long startMask = ~(WORD_MASK << s);
    // 64 - (e % 64) = 64 - (e & 63) = -e, due to wrap
    final long endMask = ~(WORD_MASK >>> -e);

    if(startWord == endWord) {
      words[startWord] &= startMask | endMask;
    } else {
      words[startWord] &= startMask;
      for(int i = startWord + 1; i < endWord; i++) words[i] = 0L;
      words[endWord] &= endMask;
    }
  }

  /**
   * Get the next bit set to 1, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @return index of the next set bit; -1 if there is no set bit after the
   *         i<sup>th</sup> bit
   */
  public int nextSetBit(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    int wordIndex = i >>> WORD_POWER;
    // skip the first i bits:
    long word = words[wordIndex] & (WORD_MASK << i);

    if(word != 0) {
      return (wordIndex << WORD_POWER) + numberOfTrailingZeros(word);
    }

    while(++wordIndex < words.length) {
      if((word = words[wordIndex]) != 0) {
        return wordIndex << WORD_POWER + numberOfTrailingZeros(word);
      }
    }

    return -1;
  }

  /**
   * Get the next bit set to 1, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @return index of the next set bit; -1 if there is no set bit after the
   *         i<sup>th</sup> bit
   */
  public long nextSetBit(final long i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    int wordIndex = (int) (i >>> WORD_POWER);
    // skip the first i bits:
    long word = words[wordIndex] & (WORD_MASK << i);

    if(word != 0) {
      return (((long) wordIndex) << WORD_POWER) + numberOfTrailingZeros(word);
    }

    while(++wordIndex < words.length) {
      if((word = words[wordIndex]) != 0) {
        return (((long) wordIndex) << WORD_POWER) + numberOfTrailingZeros(word);
      }
    }

    return -1L;
  }

  /**
   * Get the next bit set to 0, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @return index of the next clear bit; -1 if there is no set bit after the
   *         i<sup>th</sup> bit
   */
  public int nextClearBit(final int i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    int wordIndex = i >>> WORD_POWER;
    // invert the word and skip the first i bits:
    long word = ~words[wordIndex] & (WORD_MASK << i);

    if(word != 0) {
      return (wordIndex << WORD_POWER) + numberOfTrailingZeros(word);
    }

    while(++wordIndex < words.length) {
      if((word = ~words[wordIndex]) != 0) {
        return wordIndex << WORD_POWER + numberOfTrailingZeros(word);
      }
    }

    // wordIndex * 2^6:
    return -1;
  }

  /**
   * Get the next bit set to 0, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @return index of the next clear bit; -1 if there is no set bit after the
   *         i<sup>th</sup> bit
   */
  public long nextClearBit(final long i) {
    // calculate the index of the word in the array: i div 2^6 = i >> 6
    int wordIndex = (int) (i >>> WORD_POWER);
    // invert the word and skip the first i bits:
    long word = ~words[wordIndex] & (WORD_MASK << i);

    if(word != 0) {
      return (((long) wordIndex) << WORD_POWER) + numberOfTrailingZeros(word);
    }

    while(++wordIndex < words.length) {
      if((word = ~words[wordIndex]) != 0) {
        return (((long) wordIndex) << WORD_POWER) + numberOfTrailingZeros(word);
      }
    }

    // wordIndex * 2^6:
    return -1L;
  }

  /**
   * Get the next n clear bits, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @param n number of clear bits.
   * @return a list of clear bits
   */
  public int[] nextClearBits(final int i, final int n) {
    final int[] t = new int[n];
    t[0] = nextClearBit(i);
    for(int k = 1; k < n; k++)
      if((t[k] = nextClearBit(t[k - 1] + 1)) >= last) {
        final int[] r = new int[k];
        System.arraycopy(t, 0, r, 0, k);
        return r;
      }
    return t;
  }

  /**
   * Get the next n clear bits, starting from the i<sup>th</sup> bit.
   * @param i index from which to start the search (inclusive)
   * @param n number of clear bits.
   * @return a list of clear bits
   */
  public long[] nextClearBits(final long i, final int n) {
    final long[] t = new long[n];
    t[0] = nextClearBit(i);
    for(int k = 0; k < n; k++)
      if((t[k] = nextClearBit(t[k - 1] + 1L)) >= last) {
        final long[] r = new long[k];
        System.arraycopy(t, 0, r, 0, k);
        return r;
      }
    return t;
  }

  /**
   * Expand the {@link #words} array to the desired size.
   * @param s new size
   */
  private void expandTo(final int s) {
    final long[] newWords = new long[Math.max(words.length << 1, s)];
    System.arraycopy(words, 0, newWords, 0, words.length);
    words = newWords;
  }
}
