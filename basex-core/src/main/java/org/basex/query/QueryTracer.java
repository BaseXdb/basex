package org.basex.query;

import org.basex.util.*;

/**
 * Query tracer.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public interface QueryTracer {
  /** Prints trace output to the standard error. */
  QueryTracer ERRLN = new QueryTracer() {
    @Override
    public void print(final String string, final QueryContext qc) {
      Util.errln(string);
    }
  };
  /** Prints trace output to the evaluation info. */
  QueryTracer EVALINFO = new QueryTracer() {
    @Override
    public void print(final String string, final QueryContext qc) {
      qc.evalInfo(string);
    }
  };

  /**
   * Prints trace output.
   * @param string string to be output
   * @param qc query context
   */
  void print(String string, QueryContext qc);
}
