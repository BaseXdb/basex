package org.basex.query.func.admin;

import static org.basex.core.users.UserText.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.math.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.server.Log.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class AdminLogs extends AdminFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    if(exprs.length == 0) {
      // return list of all log files
      for(final IOFile f : qc.context.log.files()) {
        final String date = f.name().replace(IO.LOGSUFFIX, "");
        vb.add(new FElem(FILE).add(date).add(SIZE, Token.token(f.length())));
      }
    } else {
      // return content of single log file
      final String name = Token.string(toToken(exprs[0], qc));
      final boolean merge = exprs.length > 1 && toBoolean(exprs[1], qc);

      final ElementNodes<LogEntry>.NodeIterator iter = logs(name, qc).iterator();
      while(iter.hasNext()) {
        final LogEntry l1 = iter.next();
        final FElem elem = new FElem(ENTRY);
        if(l1.address != null) {
          if(merge && l1.type.equals(LogType.REQUEST.name())) {
            // merge REQUEST entry: find next OK, ERROR or status code entry from same address
            final ElementNodes<LogEntry>.NodeIterator iter2 = iter.copy();
            while(iter2.hasNext()) {
              final LogEntry l2 = iter2.next();
              if(l1.address.equals(l2.address) && (l2.type.matches("^\\d+$")) ||
                  Strings.eq(l2.type, LogType.OK.name(), LogType.ERROR.name())) {
                l1.type = l2.type;
                l1.ms = l1.ms.add(l2.ms);
                if(!l2.message.isEmpty()) l1.message += "; " + l2.message;
                iter2.remove();
                break;
              }
            }
          }
          elem.add(TIME, l1.time).add(ADDRESS, l1.address).add(USER, l1.user);
          if(l1.type != null) elem.add(TYPE, l1.type);
          if(l1.ms.compareTo(BigDecimal.ZERO) != 0) elem.add(MS, l1.ms.toString());
          if(l1.message != null) elem.add(l1.message);
        } else {
          elem.add(l1.message);
        }
        vb.add(elem);
        qc.checkStop();
      }
    }
    return vb.value();
  }

  /**
   * Returns all log entries.
   * @param name name of log file
   * @param qc query context
   * @return list
   * @throws QueryException query exception
   */
  private ElementNodes<LogEntry> logs(final String name, final QueryContext qc)
      throws QueryException {

    final Log log = qc.context.log;
    final LogFile file = log.file(name);
    if(file == null) throw WHICHRES_X.get(info, name);

    try {
      final ElementNodes<LogEntry> logs = new ElementNodes<>();
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
          entry.ms = cols.length > 5 ? new BigDecimal(cols[5].replace(" ms", "")) : BigDecimal.ZERO;
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
