package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.server.Log.LogEntry;
import org.basex.util.*;

/**
 * Admin functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNAdmin extends BuiltinFunc {
  /** QName: user. */
  private static final String USER = "user";
  /** QName: user. */
  private static final String DATABASE = "database";
  /** QName: user. */
  private static final String SESSION = "session";
  /** QName: permission. */
  private static final String PERMISSION = "permission";
  /** QName: password. */
  private static final String PASSWORD = "password";
  /** QName: entry. */
  private static final String ENTRY = "entry";
  /** Size element name. */
  private static final String SIZE = "size";
  /** QName: time. */
  private static final String TIME = "time";
  /** QName: address. */
  private static final String ADDRESS = "address";
  /** QName: file. */
  private static final String FILE = "file";
  /** QName: type. */
  private static final String TYPE = "type";
  /** QName: ms. */
  private static final String MS = "ms";

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNAdmin(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkAdmin(qc);
    switch(func) {
      case _ADMIN_LOGS:     return logs(qc);
      case _ADMIN_USERS:    return users(qc);
      case _ADMIN_SESSIONS: return sessions(qc);
      default:              return super.iter(qc);
    }
  }

  /**
   * Lists all log files.
   * @param qc query context
   * @return users
   * @throws QueryException query exception
   */
  private Iter logs(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    if(exprs.length == 0) {
      // return list of all log files
      for(final IOFile f : qc.context.log.files()) {
        final String date = f.name().replace(IO.LOGSUFFIX, "");
        vb.add(new FElem(FILE).add(date).add(SIZE, Token.token(f.length())));
      }
    } else {
      // return content of single log file
      final String name = Token.string(toToken(exprs[0], qc)) + IO.LOGSUFFIX;
      final boolean merge = exprs.length > 1 && toBoolean(exprs[1], qc);
      final IOFile file = new IOFile(qc.context.log.dir(), name);
      if(!file.exists()) throw WHICHRES_X.get(info, file);
      final ArrayList<LogEntry> logs = logs(file);
      for(int s = 0; s < logs.size(); s++) {
        final LogEntry l1 = logs.get(s);
        final FElem elem = new FElem(ENTRY);
        if(l1.address != null) {
          if(merge && l1.ms.equals(BigDecimal.ZERO) && !Log.SERVER.equals(l1.address)) {
            for(int l = s + 1; l < logs.size(); l++) {
              final LogEntry l2 = logs.get(l);
              if(l2 != null && l1.address.equals(l2.address)) {
                if(l2.type.equals(Log.REQUEST)) continue;
                if(l1.type.equals(Log.REQUEST)) l1.type = "";
                l1.type = merge(l1.type, l2.type);
                l1.message = merge(l1.message, l2.message);
                l1.ms = l1.ms.add(l2.ms);
                logs.remove(l--);
                if(!l2.message.equals(Log.REQUEST)) break;
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
        if(cols.length > 2 && (cols[1].matches(".*:\\d+") || cols[1].equals(Log.SERVER))) {
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

  /**
   * Lists all registered users.
   * @param qc query context
   * @return users
   * @throws QueryException query exception
   */
  private Iter users(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final User u : exprs.length == 0 ? qc.context.users.users(null) :
      checkData(qc).meta.users.users(qc.context.users)) {
      vb.add(new FElem(USER).add(u.name).add(PERMISSION,
          u.perm.toString().toLowerCase(Locale.ENGLISH)).add(PASSWORD, u.password));
    }
    return vb;
  }

  /**
   * Lists all open sessions.
   * @param qc query context
   * @return users
   */
  private static Iter sessions(final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder();
    synchronized(qc.context.sessions) {
      for(final ClientListener sp : qc.context.sessions) {
        final String user = sp.context().user.name;
        final String addr = sp.address();
        final Data data = sp.context().data();
        final FElem elem = new FElem(SESSION).add(USER, user).add(ADDRESS, addr);
        if(data != null) elem.add(DATABASE, data.meta.name);
        vb.add(elem);
      }
    }
    return vb;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(oneOf(func, _ADMIN_USERS, _ADMIN_SESSIONS) && !visitor.lock(DBLocking.ADMIN)) &&
      !(func == _ADMIN_USERS && exprs.length > 0 && !dataLock(visitor, 1)) && super.accept(visitor);
  }
}
