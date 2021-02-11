package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdShow;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'show backups' command and shows available backups.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ShowBackups extends ABackup {
  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = BACKUPS_X;
    table.header.add(NAME);
    table.header.add(SIZE);

    for(final String name : context.databases.backups()) {
      final TokenList tl = new TokenList();
      tl.add(name);
      tl.add(soptions.dbPath(name + IO.ZIPSUFFIX).length());
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
  public void addLocks() {
    jc().locks.reads.add(Locking.BACKUP);
  }
}
