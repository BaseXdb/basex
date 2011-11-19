package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Table;
import org.basex.util.list.StringList;
import org.basex.util.list.TokenList;

/**
 * Evaluates the 'show backups' command and shows available backups.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ShowBackups extends Command {
  /**
   * Default constructor.
   */
  public ShowBackups() {
    super(User.CREATE);
  }

  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = BACKUPS;
    table.header.add(INFODBNAME);
    table.header.add(INFODBSIZE);

    for(final IO f : mprop.dbpath().children()) {
      if(!f.name().endsWith(IO.ZIPSUFFIX)) continue;
      final TokenList tl = new TokenList();
      tl.add(f.name());
      tl.add(f.length());
      table.contents.add(tl);
    }
    table.sort();
    out.println(table.finish());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.BACKUPS);
  }

  /**
   * Lists names of all databases which exist either as a database or only as
   * a backup.
   * @param ctx context
   * @return unique database names
   */
  public static StringList listdbs(final Context ctx) {
    final Set<String> dbs = new HashSet<String>();
    for(final IOFile f : ctx.mprop.dbpath().children()) {
      final String name = f.name();
      if(name.startsWith(".")) continue;
      if(name.endsWith(IO.ZIPSUFFIX)) dbs.add(dbname(f.name()));
      else dbs.add(f.name());
    }
    final StringList sl = new StringList(dbs.size());
    sl.add(dbs.toArray(new String[dbs.size()]));
    sl.sort(false, true);
    return sl;
  }

  /**
   * Extracts the name of a database from its backup file.
   * @param s name of backup file
   * @return name of database
   */
  private static String dbname(final String s) {
    // [LK] clean up / use pattern
    // -yyyy-MM-dd-HH-mm-ss.zip , 24chars
    return s.length() < 25 ? null : s.substring(0, s.length() - 24);
  }

  /**
   * Returns the names of all backup files that are available for the given
   * database name.
   * @param db database name
   * @param ctx context
   * @return names of available backup files
   */
  public static StringList findBackups(final String db, final Context ctx) {
    final StringList sl = new StringList();
    for(final IOFile f : ctx.mprop.dbpath().children()) {
      final String name = f.name();
      if(name.endsWith(IO.ZIPSUFFIX) && db.equals(dbname(name)))
        sl.add(name.substring(0, name.length() - IO.ZIPSUFFIX.length()));
    }
    sl.sort(false, true);
    return sl;
  }
}