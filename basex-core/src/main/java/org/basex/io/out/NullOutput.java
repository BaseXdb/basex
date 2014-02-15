package org.basex.io.out;

/**
 * This output stream swallows all data it receives.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class NullOutput extends PrintOutput {
  @Override
  public void write(final int b) { }
}
