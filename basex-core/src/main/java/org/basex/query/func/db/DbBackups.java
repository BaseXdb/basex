package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
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
    checkCreate(qc);
    final String name = exprs.length == 0 ? null : string(toToken(exprs[0], qc));

    final Context ctx = qc.context;
    final StringList backups = name == null ? ctx.databases.backups() : ctx.databases.backups(name);
    return new BasicIter<FElem>(backups.size()) {
      final IOFile dbPath = ctx.soptions.dbPath();

      @Override
      public FElem get(final long i) {
        final String backup = backups.get((int) i);
        final IOFile zip = new IOFile(dbPath, backup + IO.ZIPSUFFIX);
        final long length = zip.length();
        final String db = Databases.name(backup);
        final Dtm dtm = Dtm.get(DateTime.parse(Databases.date(backup)).getTime());
        final String comment = ShowBackups.comment(backup, ctx);
        return new FElem(BACKUP).add(backup).add(DATABASE, db).add(DATE, dtm.string(info)).
            add(SIZE, token(length)).add(COMMENT, comment);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }
}
