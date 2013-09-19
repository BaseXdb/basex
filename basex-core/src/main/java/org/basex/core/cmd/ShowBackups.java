package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'show backups' command and shows available backups.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ShowBackups extends ABackup {
  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = BACKUPS_X;
    table.header.add(NAME);
    table.header.add(SIZE);

    final StringList list = context.databases.backups(null);
    final IOFile dbpath = context.mprop.dbpath();
    for(final String name : list) {
      final TokenList tl = new TokenList();
      tl.add(name);
      tl.add(new IOFile(dbpath, name).length());
      table.contents.add(tl);
    }
    out.println(table.sort().finish());
    return true;
  }

  @Override
  public boolean updating(final Context ctx) {
    return false;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.BACKUPS);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.BACKUP);
  }
}
