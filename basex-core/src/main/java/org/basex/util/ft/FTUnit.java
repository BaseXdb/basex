package org.basex.util.ft;

import java.util.*;

/**
 * Full-text units.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum FTUnit {
  /** Word unit. */      WORDS,
  /** Sentence unit. */  SENTENCES,
  /** Paragraph unit. */ PARAGRAPHS;

  /**
   * Returns a string representation.
   * @return string representation
   */
  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
