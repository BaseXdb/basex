package org.basex.io.in;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * This class provides a resettable input stream.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class ResettableInput extends BufferedInputStream {
  /** Max number of resettable bytes. */
  public static final int MAX = 1000000;

  /**
   * Constructor.
   * @param input wrapped input stream.
   */
  public ResettableInput(final InputStream input) {
    super(input);
  }

  /**
   * Current position of the input stream.
   * @return position
   */
  public int pos() {
    return pos;
  }

  @Override
  public void close() {
    if(markpos < 0 || markpos == pos) close();
  }
}
