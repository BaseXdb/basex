package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.io.IO;

/**
 * Evaluates the 'drop backup' command and deletes backups of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DropBackup extends Command {
  /**
   * Default constructor.
   * @param name name of database
   */
  public DropBackup(final String name) {
    super(User.CREATE, name);
  }

  @Override
  protected boolean run() {
    // name of database
    String db = args[0];
    if(!checkName(db)) return error(NAMEINVALID, db);
    // drop backups
    if(!db.contains("-")) db += "-";
    drop(db, context);
    return info(DBBACKDROP, db + "*" + IO.ZIPSUFFIX);
  }

  /**
   * Drops one or more backups of the specified database.
   * @param db database
   * @param ctx database context
   * @return number of dropped backups
   */
  public static int drop(final String db, final Context ctx) {
    final IO dir = IO.get(ctx.prop.get(Prop.DBPATH));
    int c = 0;
    for(final IO f : dir.children()) {
      final String n = f.name();
      if(n.startsWith(db) && n.endsWith(IO.ZIPSUFFIX)) {
        if(f.delete()) c++;
      }
    }
    return c;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.BACKUP).args();
  }
}
