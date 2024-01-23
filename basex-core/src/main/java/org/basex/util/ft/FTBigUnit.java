package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text big units.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public enum FTBigUnit {
  /** Sentence unit. */  SENTENCE,
  /** Paragraph unit. */ PARAGRAPH;

  /**
   * Returns the unit.
   * @return unit
   */
  public FTUnit unit() {
    return this == SENTENCE ? FTUnit.SENTENCES : FTUnit.PARAGRAPHS;
  }

  /**
   * Returns a string representation.
   * @return string representation
   */
  @Override
  public String toString() {
    return EnumOption.string(name());
  }
}
