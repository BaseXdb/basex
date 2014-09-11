package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DbBackups extends StandardFunc {
  /** Backup element name. */
  private static final String BACKUP = "backup";
  /** Size element name. */
  private static final String SIZE = "size";

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final String name = exprs.length == 0 ? null : string(toToken(exprs[0], qc));

    final StringList backups = name == null ? qc.context.databases.backups() :
      qc.context.databases.backups(name);
    final IOFile dbpath = qc.context.globalopts.dbpath();
    return new Iter() {
      int up = -1;

      @Override
      public Item next() {
        if(++up >= backups.size()) return null;
        final String backup = backups.get(up);
        final long length = new IOFile(dbpath, backup + IO.ZIPSUFFIX).length();
        return new FElem(BACKUP).add(backup).add(SIZE, token(length));
      }
    };
  }
}
