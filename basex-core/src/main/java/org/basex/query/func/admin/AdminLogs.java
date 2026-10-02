package org.basex.query.func.admin;

import static org.basex.core.users.UserText.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.log.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AdminLogs extends AdminFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final String date = toStringOrNull(arg(0), qc);
    return date == null ? list(qc).iter() : logs(date, qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String date = toStringOrNull(arg(0), qc);
    return date == null ? list(qc) : logs(date, qc).value(qc, this);
  }

  /**
   * Returns a list of all log files.
   * @param qc query context
   * @return list
   */
  private Value list(final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    qc.context.log.files().forEach((date, size) ->
      vb.add(FElem.build(Q_FILE).attr(Q_SIZE, size).text(date).finish()));
    return vb.value(this);
  }

  /**
   * Returns the log entries from the specified log file.
   * @param date date of the log file
   * @param qc query context
   * @return iterator with log entries
   * @throws QueryException query exception
   */
  private Iter logs(final String date, final QueryContext qc) throws QueryException {
    final boolean merge = toBooleanOrFalse(arg(1), qc);

    final LogFile file = qc.context.log.file(date);
    if(file == null) throw WHICHRES_X.get(info, date);

    final ArrayList<LogEntry> list = logs(file, merge, qc);
    return new BasicIter<>(list.size()) {
      @Override
      public Item get(final long i) {
        return element(list.get((int) i));
      }
    };
  }

  /**
   * Creates an element for a log entry.
   * @param entry log entry
   * @return element
   */
  private static FNode element(final LogEntry entry) {
    final FBuilder elem = FElem.build(Q_ENTRY).text(entry.info);
    if(entry.address != null) {
      elem.attr(Q_TIME, entry.time).attr(Q_ADDRESS, entry.address);
      elem.attr(Q_USER, entry.user).attr(Q_TYPE, entry.type);
      if(entry.runtime != null && !zero(entry.runtime)) elem.attr(Q_MS, entry.runtime);
    }
    return elem.finish();
  }

  /**
   * Returns all log entries.
   * @param file log file
   * @param merge merge REQUEST entries with their concluding entries
   * @param qc query context
   * @return list
   * @throws QueryException query exception
   */
  private ArrayList<LogEntry> logs(final LogFile file, final boolean merge,
      final QueryContext qc) throws QueryException {

    final StringList lines;
    try {
      lines = file.read();
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }

    final ArrayList<LogEntry> logs = new ArrayList<>(lines.size());
    // REQUEST entries without concluding entry, indexed by address
    final HashMap<String, LogEntry> requests = new HashMap<>();
    final String[] cols = new String[6];
    for(final String line : lines) {
      qc.checkStop();
      final LogEntry entry = entry(line, cols);
      if(merge && entry.address != null) {
        if(entry.type.equals(LogType.REQUEST.name())) {
          // a previous REQUEST entry with identical address has no concluding entry
          requests.put(entry.address, entry);
        } else if(concluding(entry.type)) {
          final LogEntry request = requests.remove(entry.address);
          if(request != null) {
            merge(request, entry);
            continue;
          }
        }
      }
      logs.add(entry);
    }
    return logs;
  }

  /**
   * Parses a log entry.
   * @param line line
   * @param cols array for the columns
   * @return log entry
   */
  private static LogEntry entry(final String line, final String[] cols) {
    final LogEntry entry = new LogEntry();
    final int cl = split(line, cols);
    if(cl > 2) {
      entry.time = cols[0];
      entry.address = cols[1];
      entry.user = cols[2];
      entry.type = cl > 3 ? cols[3] : "";
      entry.info = cl > 4 ? cols[4] : "";
      if(cl > 5) {
        // skip errors caused by erroneous input
        final int i = cols[5].indexOf(" ms");
        if(i > -1) entry.runtime = cols[5].substring(0, i);
      }
    } else {
      // legacy format
      entry.info = line;
    }
    return entry;
  }

  /**
   * Merges a concluding entry into a REQUEST entry.
   * @param request REQUEST entry
   * @param entry concluding entry
   */
  private static void merge(final LogEntry request, final LogEntry entry) {
    request.type = entry.type;
    request.user = entry.user;
    request.runtime = add(request.runtime, entry.runtime);
    final String msg1 = request.info, msg2 = entry.info;
    if(!msg2.isEmpty()) request.info = msg1.isEmpty() ? msg2 : msg1 + "; " + msg2;
  }

  /**
   * Splits a line into tab-separated columns.
   * @param line line
   * @param cols array for the columns (further columns are ignored)
   * @return number of columns
   */
  private static int split(final String line, final String[] cols) {
    final int cl = cols.length, ll = line.length();
    int c = 0, s = 0;
    while(c < cl && s < ll) {
      int e = line.indexOf('\t', s);
      if(e == -1) e = ll;
      cols[c++] = line.substring(s, e);
      s = e + 1;
    }
    // ignore trailing empty columns (consistent with String#split)
    while(c > 0 && cols[c - 1].isEmpty()) c--;
    return c;
  }

  /**
   * Checks if the specified type concludes a request (status code, OK, error).
   * @param type type
   * @return result of check
   */
  private static boolean concluding(final String type) {
    final int tl = type.length();
    if(tl == 0) return false;
    for(int t = 0; t < tl; t++) {
      final char ch = type.charAt(t);
      if(ch < '0' || ch > '9') return Strings.eq(type, LogType.OK.name(), LogType.ERROR.name());
    }
    return true;
  }

  /**
   * Adds two runtimes.
   * @param rt1 first runtime (can be {@code null})
   * @param rt2 second runtime (can be {@code null})
   * @return sum (can be {@code null})
   */
  private static String add(final String rt1, final String rt2) {
    return rt1 == null ? rt2 : rt2 == null ? rt1 :
      new BigDecimal(rt1).add(new BigDecimal(rt2)).toString();
  }

  /**
   * Checks if a runtime is zero.
   * @param runtime runtime
   * @return result of check
   */
  private static boolean zero(final String runtime) {
    final int rl = runtime.length();
    for(int r = 0; r < rl; r++) {
      final char ch = runtime.charAt(r);
      if(ch != '0' && ch != '.' && ch != '-' && ch != '+') return false;
    }
    return true;
  }
}
