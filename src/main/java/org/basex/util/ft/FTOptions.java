package org.basex.util.ft;

/**
 * This enumeration assembles globally used full-text options.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public interface FTOptions {
  /** Sensitive flag. */
  int CS = 0;
  /** Lowercase flag. */
  int LC = 1;
  /** Uppercase flag. */
  int UC = 2;
  /** Diacritics flag. */
  int DC = 3;
  /** Stemming flag. */
  int ST = 4;
  /** Wildcards flag. */
  int WC = 5;
  /** Fuzzy flag. */
  int FZ = 6;
}
