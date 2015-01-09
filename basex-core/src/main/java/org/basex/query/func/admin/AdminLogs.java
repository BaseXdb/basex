package org.basex.query.func.admin;

import static org.basex.core.users.UserText.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.server.Log.LogEntry;
import org.basex.server.Log.LogType;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class AdminLogs extends AdminFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final ValueBuilder vb = new ValueBuilder();
    if(exprs.length == 0) {
      // return list of all log files
      for(final IOFile f : qc.context.log.files()) {
        final String date = f.name().replace(IO.LOGSUFFIX, "");
        vb.add(new FElem(FILE).add(date).add(SIZE, Token.token(f.length())));
      }
    } else {
      // return content of single log file
      final String request = LogType.REQUEST.name();
      final String name = Token.string(toToken(exprs[0], qc)) + IO.LOGSUFFIX;
      final boolean merge = exprs.length > 1 && toBoolean(exprs[1], qc);
      final IOFile file = new IOFile(qc.context.log.dir(), name);
      if(!file.exists()) throw WHICHRES_X.get(info, file);
      final ArrayList<LogEntry> logs = logs(file);
      for(int s = 0; s < logs.size(); s++) {
        final LogEntry l1 = logs.get(s);
        final FElem elem = new FElem(ENTRY);
        if(l1.address != null) {
          if(merge && l1.ms.equals(BigDecimal.ZERO) &&
              !Strings.eq(l1.address, Log.SERVER, Log.STANDALONE)) {
            for(int l = s + 1; l < logs.size(); l++) {
              final LogEntry l2 = logs.get(l);
              if(l2 != null && l1.address.equals(l2.address)) {
                if(l2.type.equals(request)) continue;
                if(l1.type.equals(request)) l1.type = "";
                l1.type = merge(l1.type, l2.type);
                l1.message = merge(l1.message, l2.message);
                l1.ms = l1.ms.add(l2.ms);
                logs.remove(l--);
                if(!l2.message.equals(request)) break;
              }
            }
          }
          elem.add(TIME, l1.time).add(ADDRESS, l1.address).add(USER, l1.user);
          if(l1.type != null) elem.add(TYPE, l1.type);
          if(!l1.ms.equals(BigDecimal.ZERO)) elem.add(MS, l1.ms.toString());
          if(l1.message != null) elem.add(l1.message);
        } else {
          elem.add(l1.message);
        }
        vb.add(elem);
      }
    }
    return vb;
  }

  /**
   * Merges two strings.
   * @param s1 first string
   * @param s2 second string
   * @return merged string
   */
  private static String merge(final String s1, final String s2) {
    return s2.isEmpty() ? s1 : s1.isEmpty() ? s2 : s1 + "; " + s2;
  }

  /**
   * Returns all log entries.
   * @param file log file
   * @return list
   * @throws QueryException query exception
   */
  private ArrayList<LogEntry> logs(final IOFile file) throws QueryException {
    try(final NewlineInput nli = new NewlineInput(file)) {
      final ArrayList<LogEntry> logs = new ArrayList<>();
      for(String line; (line = nli.readLine()) != null;) {
        final LogEntry log = new LogEntry();
        final String[] cols = line.split("\t");
        if(cols.length > 2) {
          log.time = cols[0];
          log.address = cols[1];
          log.user = cols[2];
          log.type = cols.length > 3 ? cols[3] : "";
          log.message = cols.length > 4 ? cols[4] : "";
          log.ms = cols.length > 5 ? new BigDecimal(cols[5].replace(" ms", "")) : BigDecimal.ZERO;
        } else {
          // legacy format
          log.message = line;
        }
        logs.add(log);
      }
      return logs;
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
