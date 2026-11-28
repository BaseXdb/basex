package org.basex.query.parse.syntax;

/**
 * Placeholder for dropped whitespace and skipped delimiters, which keeps child counts valid.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class Marker implements Node {
  /** Placeholder for dropped whitespace. */
  static final Marker DROPPED = new Marker();
  /** Placeholder for skipped delimiters. */
  static final Marker SKIPPED = new Marker();

  /** Private constructor. */
  private Marker() { }

  @Override
  public int sym() {
    return -1;
  }

  @Override
  public int begin() {
    return -1;
  }

  @Override
  public int end() {
    return -1;
  }
}
