package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'show backups' command and shows available backups.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ShowBackups extends ABackup {
  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = BACKUPS_X;
    table.header.add(NAME);
    table.header.add(SIZE);
    table.header.add(COMMENT);

    for(final String backup : context.databases.backups()) {
      final TokenList tl = new TokenList();
      tl.add(backup);
      final IOFile zip = soptions.dbPath(backup + IO.ZIPSUFFIX);
      tl.add(zip.length());
      tl.add(comment(backup, context));
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
    jc().locks.reads.addGlobal();
  }

  /**
   * Returns the comment of a backup file.
   * @param name name of backup
   * @param ctx database context
   * @return comment, possibly empty
   */
  public static String comment(final String name, final Context ctx) {
    String comment = null;
    try(ZipFile zf = new ZipFile(ctx.soptions.dbPath(name + IO.ZIPSUFFIX).file())) {
      comment = zf.getComment();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    return comment != null ? comment : "";
  }
}
