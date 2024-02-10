package org.basex.query.func.admin;

import static org.basex.core.users.UserText.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.server.Log.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
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
    for(final IOFile file : qc.context.log.files()) {
      final String date = file.name().replace(IO.LOGSUFFIX, "");
      vb.add(FElem.build(Q_FILE).add(Q_SIZE, file.length()).add(date).finish());
    }
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

    final LinkedList<LogEntry> list = logs(file, qc);
    final HashMap<String, LinkedList<LogEntry>> map = new HashMap<>();
    if(merge) {
      // group entries by address
      for(final LogEntry entry : list) {
        map.computeIfAbsent(entry.address, address -> new LinkedList<>()).add(entry);
      }
    }
    final Pattern digits = Pattern.compile("\\d+");
    return new Iter() {
      @Override
      public Item next() {
        // scan and merge entries
        for(LogEntry entry; (entry = list.pollFirst()) != null;) {
          // REQUEST entries: find concluding entries (status code, OK, error)
          if(merge) {
            // skip entries that have already been consumed
            final LinkedList<LogEntry> entries = map.get(entry.address);
            if(entries.peekFirst() != entry) continue;
            entries.removeFirst();

            if(entry.type != null && entry.type.equals(LogType.REQUEST.name())) {
              final Iterator<LogEntry> iter = entries.iterator();
              while(iter.hasNext()) {
                final LogEntry next = iter.next();
                final String type = next.type;
                // REQUEST entry with identical address: no concluding entry exists
                if(type.equals(LogType.REQUEST.name())) break;
                if(digits.matcher(type).matches() ||
                    Strings.eq(type, LogType.OK.name(), LogType.ERROR.name())) {
                  entry.type = type;
                  entry.user = next.user;
                  entry.ms = entry.ms.add(next.ms);
                  final String msg1 = entry.message, msg2 = next.message;
                  if(!msg2.isEmpty()) entry.message = msg1.isEmpty() ? msg2 : msg1 + "; " + msg2;
                  iter.remove();
                  break;
                }
              }
            }
          }
          // add new element
          final FBuilder elem = FElem.build(Q_ENTRY).add(entry.message);
          if(entry.address != null) {
            elem.add(Q_TIME, entry.time).add(Q_ADDRESS, entry.address);
            elem.add(Q_USER, entry.user).add(Q_TYPE, entry.type);
            if(entry.ms != BigDecimal.ZERO) elem.add(Q_MS, entry.ms);
          }
          return elem.finish();
        }

        return null;
      }
    };
  }

  /**
   * Returns all log entries.
   * @param file log file
   * @param qc query context
   * @return list
   * @throws QueryException query exception
   */
  private LinkedList<LogEntry> logs(final LogFile file, final QueryContext qc)
      throws QueryException {

    try {
      final LinkedList<LogEntry> logs = new LinkedList<>();
      for(final String line : file.read()) {
        qc.checkStop();
        final LogEntry entry = new LogEntry();
        final String[] cols = line.split("\t");
        if(cols.length > 2) {
          entry.time = cols[0];
          entry.address = cols[1];
          entry.user = cols[2];
          entry.type = cols.length > 3 ? cols[3] : "";
          entry.message = cols.length > 4 ? cols[4] : "";
          entry.ms = BigDecimal.ZERO;
          if(cols.length > 5) {
            // skip errors caused by erroneous input
            final int i = cols[5].indexOf(" ms");
            if(i > -1) entry.ms = new BigDecimal(cols[5].substring(0, i));
          }
        } else {
          // legacy format
          entry.message = line;
        }
        logs.add(entry);
      }
      return logs;
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
