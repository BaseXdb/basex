package org.basex.io;

/**
 * This class swallows all data it receives.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class NullOutput extends PrintOutput {
  /** Skip flag. */
  private final boolean skip;

  /**
   * Constructor.
   */
  public NullOutput() {
    this(true);
  }

  /**
   * Constructor.
   * @param s if set to true, streaming is canceled as soon as possible.
   */
  public NullOutput(final boolean s) {
    skip = s;
  }
  
  @Override
  public void write(final int b) { }

  @Override
  public void flush() { }

  @Override
  public void close() { }

  @Override
  public boolean finished() {
    return skip;
  }
}
