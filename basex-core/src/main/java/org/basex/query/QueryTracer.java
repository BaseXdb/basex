package org.basex.query;

import org.basex.util.*;

/**
 * Query tracer.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public interface QueryTracer {
  /** Prints trace output to the standard error. */
  QueryTracer ERRLN = Util::errln;
  /** Prints trace output to the evaluation info. */
  QueryTracer EVALINFO = (string, qc) -> qc.evalInfo(string);

  /**
   * Prints trace output.
   * @param string string to be output
   * @param qc query context
   */
  void print(String string, QueryContext qc);
}
