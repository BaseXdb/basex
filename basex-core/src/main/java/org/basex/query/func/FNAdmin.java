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
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Admin functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNAdmin extends StandardFunc {
  /** QName: user. */
  private static final String USER = "user";
  /** QName: user. */
  private static final String DATABASE = "database";
  /** QName: user. */
  private static final String SESSION = "session";
  /** QName: permission. */
  private static final String PERMISSION = "permission";
  /** QName: entry. */
  private static final String ENTRY = "entry";
  /** Size element name. */
  private static final String SIZE = "size";
  /** QName: date. */
  private static final String DATE = "date";
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
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNAdmin(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
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
        vb.add(new FElem(FILE).add(DATE, date).add(SIZE, Token.token(f.length())));
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
              final FElem elem = new FElem(ENTRY);
              final String[] cols = l.split("\t");
              if(cols.length > 2 && (cols[1].matches(".*:\\d+") ||
                  cols[1].equals(Log.SERVER))) {
                // new format (more specific)
                elem.add(TIME, cols[0]).add(ADDRESS, cols[1]).add(USER, cols[2]);
                if(cols.length > 3) elem.add(TYPE, cols[3].toLowerCase(Locale.ENGLISH));
                if(cols.length > 4) elem.add(cols[4]);
                if(cols.length > 5) elem.add(MS, cols[5].replace(" ms", ""));
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
      vb.add(new FElem(USER).add(u.name).add(PERMISSION,
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
        final FElem elem = new FElem(SESSION).add(USER, user).add(ADDRESS, addr);
        if(data != null) elem.add(DATABASE, data.meta.name);
        vb.add(elem);
      }
    }
    return vb;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(oneOf(sig, _ADMIN_USERS, _ADMIN_SESSIONS) && !visitor.lock(DBLocking.ADMIN)) &&
      !(sig == _ADMIN_USERS && expr.length > 0 && !dataLock(visitor)) && super.accept(visitor);
  }
}
