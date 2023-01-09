package org.basex.query.func.java;

import java.lang.reflect.*;

/**
 * Candidate with function/constructor arguments.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class JavaCandidate {
  /** Arguments. */
  final Object[] arguments;

  /** Executable. */
  Executable executable;
  /** Exact argument matches. */
  boolean exact = true;

  /**
   * Constructor.
   * @param size number of arguments
   */
  JavaCandidate(final int size) {
    arguments = new Object[size];
  }
}
