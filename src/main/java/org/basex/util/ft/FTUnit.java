package org.basex.util.ft;

/**
 * Full-text units.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum FTUnit {
  /** Word unit. */
  WORD,
  /** Sentence unit. */
  SENTENCE,
  /** Paragraph unit. */
  PARAGRAPH;

  /**
   * Returns a string representation.
   * @return string representation
   */
  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
