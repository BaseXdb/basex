package org.basex.util.ft;

/**
 * This enumeration assembles globally used full-text options.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
