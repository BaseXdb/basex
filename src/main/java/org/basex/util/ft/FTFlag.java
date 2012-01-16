package org.basex.util.ft;

/**
 * This enumeration assembles globally used full-text options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum FTFlag {
  /** Sensitive flag. */
  CS,
  /** Lowercase flag. */
  LC,
  /** Uppercase flag. */
  UC,
  /** Diacritics flag. */
  DC,
  /** Stemming flag. */
  ST,
  /** Wildcards flag. */
  WC,
  /** Fuzzy flag. */
  FZ;
}
