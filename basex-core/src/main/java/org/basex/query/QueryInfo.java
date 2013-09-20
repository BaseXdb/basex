package org.basex.query;

import static org.basex.core.Text.*;

import java.util.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class remembers descriptive query information sent back to the client.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class QueryInfo {
  /** Read locked databases. */
  public StringList readLocked;
  /** Write locked databases. */
  public StringList writeLocked;
  /** Parsing time. */
  public long pars;
  /** Compilation time. */
  public long cmpl;
  /** Evaluation time. */
  public long evlt;
  /** Serialization time. */
  public long srlz;
  /** Number of runs. */
  public int runs = 1;

  /**
   * Returns detailed query information.
   * @param qp query processor
   * @param out output stream
   * @param hits number of returned hits
   * @param detailed return detailed query info
   * @return query string
   */
  public String toString(final QueryProcessor qp, final PrintOutput out, final long hits,
      final boolean detailed) {

    final TokenBuilder tb = new TokenBuilder();
    final long total = pars + cmpl + evlt + srlz;
    if(detailed) {
      final int up = qp.updates();
      tb.add(qp.info()).add(NL);
      tb.add(PARSING_CC).add(Performance.getTime(pars, runs)).add(NL);
      tb.add(COMPILING_CC).add(Performance.getTime(cmpl, runs)).add(NL);
      tb.add(EVALUATING_CC).add(Performance.getTime(evlt, runs)).add(NL);
      tb.add(PRINTING_CC).add(Performance.getTime(srlz, runs)).add(NL);
      tb.add(TOTAL_TIME_CC).add(Performance.getTime(total, runs)).add(NL).add(NL);
      tb.add(HITS_X_CC + hits).add(' ').add(hits == 1 ? ITEM : ITEMS).add(NL);
      tb.add(UPDATED_CC + up).add(' ').add(up == 1 ? ITEM : ITEMS).add(NL);
      tb.add(PRINTED_CC).add(Performance.format(out.size())).add(NL);
      tb.add(READ_LOCKING_CC);
      if(readLocked == null) tb.add("global");
      else if(readLocked.isEmpty()) tb.add("none");
      else tb.add("local ").add(Arrays.toString(readLocked.toArray()));
      tb.add(NL).add(WRITE_LOCKING_CC);
      if(writeLocked == null) tb.add("global");
      else if(writeLocked.isEmpty()) tb.add("none");
      else tb.add("local ").add(Arrays.toString(writeLocked.toArray()));
      tb.add(NL);
    }
    final IO io = qp.sc.baseIO();
    final String name = io == null ? "" : " \"" + io.name() + "\"";
    tb.addExt(NL + QUERY_EXECUTED_X_X, name, Performance.getTime(total, runs));
    return tb.toString();
  }
}
