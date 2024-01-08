package org.basex.query.func.db;

import static org.basex.query.func.db.DbAccess.*;
import static org.basex.query.QueryError.*;

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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DbBackups extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final String name = defined(0) ? toName(arg(0), true, DB_NAME_X, qc) : null;

    final Context ctx = qc.context;
    final StringList backups = name == null ? ctx.databases.backups() : ctx.databases.backups(name);
    return new BasicIter<FNode>(backups.size()) {
      final IOFile dbPath = ctx.soptions.dbPath();

      @Override
      public FNode get(final long i) {
        final String backup = backups.get((int) i), db = Databases.name(backup);
        final FBuilder elem = FElem.build(Q_BACKUP).add(backup);
        if(!db.isEmpty()) elem.add(Q_DATABASE, db);
        elem.add(Q_DATE, Dtm.get(DateTime.parse(Databases.date(backup)).getTime()).string(info));
        elem.add(Q_SIZE, new IOFile(dbPath, backup + IO.ZIPSUFFIX).length());
        final String comment = ShowBackups.comment(backup, ctx);
        if(!comment.isEmpty()) elem.add(Q_COMMENT, comment);
        return elem.finish();
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
