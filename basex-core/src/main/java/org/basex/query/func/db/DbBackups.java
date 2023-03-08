package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbBackups extends StandardFunc {
  /** Backup string. */
  private static final String BACKUP = "backup";
  /** Size string. */
  private static final String SIZE = "size";
  /** Date string. */
  private static final String DATE = "date";
  /** Database string. */
  private static final String DATABASE = "database";
  /** Comment string. */
  private static final String COMMENT = "comment";

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final String name = defined(0) ? toName(arg(0), true, DB_NAME_X, qc) : null;

    final Context ctx = qc.context;
    final StringList backups = name == null ? ctx.databases.backups() : ctx.databases.backups(name);
    return new BasicIter<FElem>(backups.size()) {
      final IOFile dbPath = ctx.soptions.dbPath();

      @Override
      public FElem get(final long i) {
        final String backup = backups.get((int) i);
        final IOFile zip = new IOFile(dbPath, backup + IO.ZIPSUFFIX);
        final String db = Databases.name(backup);

        final FElem elem = new FElem(BACKUP).add(backup);
        if(!db.isEmpty()) elem.add(DATABASE, db);
        elem.add(DATE, Dtm.get(DateTime.parse(Databases.date(backup)).getTime()).string(info));
        elem.add(SIZE, token(zip.length()));
        final String comment = ShowBackups.comment(backup, ctx);
        if(!comment.isEmpty()) elem.add(COMMENT, comment);
        return elem;
      }
    };
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (defined(0) ? dataLock(arg(0), true, visitor) : visitor.lock((String) null)) &&
        super.accept(visitor);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }
}
