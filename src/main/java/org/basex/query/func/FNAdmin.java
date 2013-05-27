package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Admin functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNAdmin extends StandardFunc {
  /** QName: user. */
  private static final QNm Q_USER = new QNm("user");
  /** QName: user. */
  private static final QNm Q_DATABASE = new QNm("database");
  /** QName: user. */
  private static final QNm Q_SESSION = new QNm("session");
  /** QName: permission. */
  private static final QNm Q_PERMISSION = new QNm("permission");
  /** QName: entry. */
  private static final QNm Q_ENTRY = new QNm("entry");
  /** Size element name. */
  private static final QNm Q_SIZE = new QNm("size");
  /** QName: date. */
  private static final QNm Q_DATE = new QNm("date");
  /** QName: time. */
  private static final QNm Q_TIME = new QNm("time");
  /** QName: address. */
  private static final QNm Q_ADDRESS = new QNm("address");
  /** QName: file. */
  private static final QNm Q_FILE = new QNm("file");
  /** QName: type. */
  private static final QNm Q_TYPE = new QNm("type");
  /** QName: ms. */
  private static final QNm Q_MS = new QNm("ms");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNAdmin(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    switch(sig) {
      case _ADMIN_LOGS:     return logs(ctx);
      case _ADMIN_USERS:    return users(ctx);
      case _ADMIN_SESSIONS: return sessions(ctx);
      default:              return super.iter(ctx);
    }
  }

  /**
   * Lists all log files.
   * @param ctx query context
   * @return users
   * @throws QueryException query exception
   */
  private Iter logs(final QueryContext ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    if(expr.length == 0) {
      // return list of all log files
      for(final IOFile f : ctx.context.log.files()) {
        final String date = f.name().replace(IO.LOGSUFFIX, "");
        vb.add(new FElem(Q_FILE).add(Q_DATE, date).add(Q_SIZE, Token.token(f.length())));
      }
    } else {
      // return log file contents
      final String name = Token.string(checkStr(expr[0], ctx)) + IO.LOGSUFFIX;
      final IOFile file = new IOFile(ctx.context.log.dir(), name);
      if(file.exists()) {
        try {
          final NewlineInput nli = new NewlineInput(file);
          try {
            for(String l; (l = nli.readLine()) != null;) {
              final FElem elem = new FElem(Q_ENTRY);
              final String[] cols = l.split("\t");
              if(cols.length > 2 && (cols[1].matches(".*:\\d+") ||
                  cols[1].equals(Log.SERVER))) {
                // new format (more specific)
                elem.add(Q_TIME, cols[0]).add(Q_ADDRESS, cols[1]).add(Q_USER, cols[2]);
                if(cols.length > 3) elem.add(Q_TYPE, cols[3].toLowerCase(Locale.ENGLISH));
                if(cols.length > 4) elem.add(cols[4]);
                if(cols.length > 5) elem.add(Q_MS, cols[5].replace(" ms", ""));
              } else {
                elem.add(l);
              }
              vb.add(elem);
            }
          } finally {
            nli.close();
          }
        } catch(final IOException ex) {
          IOERR.thrw(info, ex);
        }
      }
    }
    return vb;
  }

  /**
   * Lists all registered users.
   * @param ctx query context
   * @return users
   * @throws QueryException query exception
   */
  private Iter users(final QueryContext ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final User u : expr.length == 0 ? ctx.context.users.users(null) :
      checkData(ctx).meta.users.users(ctx.context.users)) {
      vb.add(new FElem(Q_USER).add(u.name).add(Q_PERMISSION,
          u.perm.toString().toLowerCase(Locale.ENGLISH)));
    }
    return vb;
  }

  /**
   * Lists all open sessions.
   * @param ctx query context
   * @return users
   */
  private static Iter sessions(final QueryContext ctx) {
    final ValueBuilder vb = new ValueBuilder();
    synchronized(ctx.context.sessions) {
      for(final ClientListener sp : ctx.context.sessions) {
        final String user = sp.context().user.name;
        final String addr = sp.address();
        final Data data = sp.context().data();
        final FElem elem = new FElem(Q_SESSION).add(Q_USER, user).add(Q_ADDRESS, addr);
        if(data != null) elem.add(Q_DATABASE, data.meta.name);
        vb.add(elem);
      }
    }
    return vb;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(oneOf(sig, _ADMIN_USERS, _ADMIN_SESSIONS) && !visitor.lock(DBLocking.ADMIN))
      return false;
    if(sig == _ADMIN_USERS && expr.length > 0 && !dataLock(visitor)) return false;
    return super.accept(visitor);
  }
}
