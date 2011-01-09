package org.basex.io;

/**
 * This class swallows all data it receives.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class NullOutput extends PrintOutput {
  /** Skip flag. */
  private final boolean stop;

  /**
   * Constructor.
   */
  public NullOutput() {
    this(true);
  }

  /**
   * Constructor.
   * @param s flag for stopping serialization as early as possible
   */
  public NullOutput(final boolean s) {
    stop = s;
  }

  @Override
  public void write(final int b) { }

  @Override
  public boolean finished() {
    return stop;
  }
}
