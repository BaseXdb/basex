package org.basex.util;

import java.nio.*;

import org.basex.core.jobs.*;

/**
 * Argon2id key derivation by Alex Biryukov, Daniel Dinu and Dmitry Khovratovich
 * (RFC 9106, version 0x13, without secret and associated data).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Argon2 {
  /** Algorithm version. */
  public static final int VERSION = 0x13;
  /** Default number of passes. */
  public static final int ITERATIONS = 2;
  /** Default memory size in KiB. */
  public static final int MEMORY = 19456;
  /** Default number of lanes. */
  public static final int PARALLELISM = 1;
  /** Maximum memory size in KiB. */
  private static final int MAX_MEMORY = 1 << 18;

  /** Algorithm type (Argon2id). */
  private static final int TYPE = 2;
  /** Number of 64-bit words in a block. */
  private static final int WORDS = 128;
  /** Zero block. */
  private static final long[] ZERO = new long[WORDS];
  /** Indexes of the eight rows and eight columns to which the permutation is applied. */
  private static final int[][] INDEXES = new int[16][16];

  static {
    for(int i = 0; i < 8; i++) {
      for(int k = 0; k < 16; k++) {
        INDEXES[i][k] = (i << 4) + k;
        INDEXES[i + 8][k] = (i << 1) + (k >>> 1 << 4) + (k & 1);
      }
    }
  }

  /** Job to be checked for interruptions (can be {@code null}). */
  private final Job job;
  /** XOR of the reference and previous block. */
  private final long[] r = new long[WORDS];
  /** Temporary block. */
  private final long[] z = new long[WORDS];
  /** Input block for address generation. */
  private final long[] input = new long[WORDS];
  /** Block with pseudo-random addresses. */
  private final long[] address = new long[WORDS];

  /**
   * Constructor.
   * @param job job to be checked for interruptions (can be {@code null})
   */
  public Argon2(final Job job) {
    this.job = job;
  }

  /**
   * Derives a key from a password.
   * @param password password
   * @param salt salt (at least 8 bytes)
   * @param iterations number of passes (at least 1)
   * @param memory memory size in KiB (from {@code 8 * parallelism} to 256 MiB)
   * @param parallelism number of lanes (at least 1)
   * @param length length of the derived key in bytes (at least 4)
   * @return derived key
   * @throws IllegalArgumentException invalid parameters
   */
  public byte[] derive(final byte[] password, final byte[] salt, final int iterations,
      final int memory, final int parallelism, final int length) {
    if(iterations < 1) throw new IllegalArgumentException("iterations " + iterations);
    if(parallelism < 1) throw new IllegalArgumentException("parallelism " + parallelism);
    if(memory / 8 < parallelism || memory > MAX_MEMORY)
      throw new IllegalArgumentException("memory " + memory);
    if(salt.length < 8) throw new IllegalArgumentException("salt-length " + salt.length);
    if(length < 4) throw new IllegalArgumentException("length " + length);

    final byte[] h0 = new Blake2b(64).update(parallelism).update(length).update(memory).
      update(iterations).update(VERSION).update(TYPE).update(password.length).update(password).
      update(salt.length).update(salt).update(0).update(0).digest();

    final int q = memory / (parallelism << 2) << 2, blocks = q * parallelism;
    final long[] mem = new long[blocks * WORDS];
    final byte[] block = new byte[WORDS << 3];
    final ByteBuffer seed = ByteBuffer.allocate(72).order(ByteOrder.LITTLE_ENDIAN).put(h0);
    final LongBuffer words = ByteBuffer.wrap(block).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
    for(int lane = 0; lane < parallelism; lane++) {
      for(int j = 0; j < 2; j++) {
        hash(seed.putInt(64, j).putInt(68, lane).array(), block);
        words.get(0, mem, (lane * q + j) * WORDS, WORDS);
      }
    }

    input[3] = blocks;
    input[4] = iterations;
    input[5] = TYPE;
    for(int pass = 0; pass < iterations; pass++) {
      for(int slice = 0; slice < 4; slice++) {
        for(int lane = 0; lane < parallelism; lane++) {
          segment(mem, pass, slice, lane, q, parallelism);
        }
      }
    }

    final long[] c = new long[WORDS];
    for(int lane = 0; lane < parallelism; lane++) {
      for(int w = 0; w < WORDS; w++) c[w] ^= mem[(lane * q + q - 1) * WORDS + w];
    }
    words.put(0, c);
    final byte[] key = new byte[length];
    hash(block, key);
    return key;
  }

  /**
   * Fills a segment of a lane.
   * @param mem memory
   * @param pass pass
   * @param slice slice
   * @param lane lane
   * @param q number of blocks in a lane
   * @param parallelism number of lanes
   */
  private void segment(final long[] mem, final int pass, final int slice, final int lane,
      final int q, final int parallelism) {
    // Argon2id: data-independent addressing in the first half of the first pass
    final boolean independent = pass == 0 && slice < 2;
    if(independent) {
      input[1] = lane;
      input[2] = slice;
      input[6] = 0;
    }
    final boolean first = pass == 0 && slice == 0;
    final int sl = q >>> 2;
    if(first) addresses();

    for(int i = first ? 2 : 0; i < sl; i++) {
      if(job != null && (i & 0xFF) == 0) job.checkStop();
      final int curr = lane * q + slice * sl + i, prev = curr % q == 0 ? curr + q - 1 : curr - 1;
      final long rand;
      if(independent) {
        if(i % WORDS == 0) addresses();
        rand = address[i % WORDS];
      } else {
        rand = mem[prev * WORDS];
      }
      final int ref = first ? lane : (int) ((rand >>> 32) % parallelism);
      final long size = (pass == 0 ? slice * sl : q - sl) +
        (ref == lane ? i - 1 : i == 0 ? -1 : 0);
      long pos = rand & 0xFFFFFFFFL;
      pos = pos * pos >>> 32;
      pos = size - 1 - (size * pos >>> 32);
      final long offset = pass != 0 && slice != 3 ? (slice + 1) * sl : 0;
      final int index = (int) ((offset + pos) % q);
      fill(mem, prev * WORDS, mem, (ref * q + index) * WORDS, mem, curr * WORDS, pass != 0);
    }
  }

  /**
   * Generates the next block of pseudo-random addresses.
   */
  private void addresses() {
    input[6]++;
    fill(ZERO, 0, input, 0, address, 0, false);
    fill(ZERO, 0, address, 0, address, 0, false);
  }

  /**
   * Applies the compression function to two blocks and writes the result to a third block.
   * @param prev array with the previous block
   * @param po offset of the previous block
   * @param ref array with the reference block
   * @param ro offset of the reference block
   * @param next array with the next block
   * @param no offset of the next block
   * @param xor XOR the result with the existing next block
   */
  private void fill(final long[] prev, final int po, final long[] ref, final int ro,
      final long[] next, final int no, final boolean xor) {
    for(int w = 0; w < WORDS; w++) {
      r[w] = ref[ro + w] ^ prev[po + w];
      z[w] = xor ? r[w] ^ next[no + w] : r[w];
    }
    for(final int[] idx : INDEXES) {
      mix(idx[0], idx[4], idx[8], idx[12]);
      mix(idx[1], idx[5], idx[9], idx[13]);
      mix(idx[2], idx[6], idx[10], idx[14]);
      mix(idx[3], idx[7], idx[11], idx[15]);
      mix(idx[0], idx[5], idx[10], idx[15]);
      mix(idx[1], idx[6], idx[11], idx[12]);
      mix(idx[2], idx[7], idx[8], idx[13]);
      mix(idx[3], idx[4], idx[9], idx[14]);
    }
    for(int w = 0; w < WORDS; w++) next[no + w] = z[w] ^ r[w];
  }

  /**
   * Mixes four words of the permuted block with the multiplication-hardened BLAKE2b function.
   * @param a first index
   * @param b second index
   * @param c third index
   * @param d fourth index
   */
  private void mix(final int a, final int b, final int c, final int d) {
    r[a] += r[b] + 2 * (r[a] & 0xFFFFFFFFL) * (r[b] & 0xFFFFFFFFL);
    r[d] = Long.rotateRight(r[d] ^ r[a], 32);
    r[c] += r[d] + 2 * (r[c] & 0xFFFFFFFFL) * (r[d] & 0xFFFFFFFFL);
    r[b] = Long.rotateRight(r[b] ^ r[c], 24);
    r[a] += r[b] + 2 * (r[a] & 0xFFFFFFFFL) * (r[b] & 0xFFFFFFFFL);
    r[d] = Long.rotateRight(r[d] ^ r[a], 16);
    r[c] += r[d] + 2 * (r[c] & 0xFFFFFFFFL) * (r[d] & 0xFFFFFFFFL);
    r[b] = Long.rotateRight(r[b] ^ r[c], 63);
  }

  /**
   * Computes the variable-length hash function H' and writes it to the output array.
   * @param data input data
   * @param output output
   */
  private static void hash(final byte[] data, final byte[] output) {
    final int t = output.length;
    byte[] v = new Blake2b(Math.min(64, t)).update(t).update(data).digest();
    int o = 0;
    for(; t - o > 64; o += 32) {
      System.arraycopy(v, 0, output, o, 32);
      v = new Blake2b(Math.min(64, t - o - 32)).update(v).digest();
    }
    System.arraycopy(v, 0, output, o, t - o);
  }
}
