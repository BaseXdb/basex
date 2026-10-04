package org.basex.util;

import java.math.*;

/**
 * Exception for arrays that exceed the maximum array size.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySizeException extends ArrayIndexOutOfBoundsException {
  /** Requested size. */
  public final BigInteger size;
  /** Maximum size. */
  public final long max;

  /**
   * Constructor.
   * @param size requested size
   */
  public ArraySizeException(final long size) {
    this(BigInteger.valueOf(size), Array.MAX_SIZE);
  }

  /**
   * Constructor for two sizes whose sum exceeds the long range.
   * @param size1 first size
   * @param size2 second size
   */
  public ArraySizeException(final long size1, final long size2) {
    this(BigInteger.valueOf(size1).add(BigInteger.valueOf(size2)), Long.MAX_VALUE);
  }

  /**
   * Constructor.
   * @param size requested size
   * @param max maximum size
   */
  private ArraySizeException(final BigInteger size, final long max) {
    super("Maximum array size exceeded (" + size + " > " + max + ").");
    this.size = size;
    this.max = max;
  }
}
