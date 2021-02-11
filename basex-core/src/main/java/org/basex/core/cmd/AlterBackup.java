package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'restore' command and restores a backup of a database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class AlterBackup extends ABackup {
  /**
   * Default constructor.
   * @param db name of database
   * @param name new name
   */
  public AlterBackup(final String db, final String name) {
    super(db, name);
  }

  @Override
  protected boolean run() {
    final String db = args[0], name = args[1];
    if(!Databases.validName(db)) return error(NAME_INVALID_X, db);
    if(!Databases.validName(name)) return error(NAME_INVALID_X, name);

    // rename all backups
    final StringList backups = context.databases.backups(db);
    if(backups.isEmpty()) return error(BACKUP_NOT_FOUND_X, db);

    boolean ok = true;
    for(final String backup : backups) {
      try {
        alter(backup, name, soptions);
        info(BACKUP_RENAMED_X, backup);
      } catch(final IOException ex) {
        Util.debug(ex);
        info(BACKUP_NOT_RENAMED_X, backup);
        ok = false;
      }
    }
    return ok;
  }

  /**
   * Drops a backup with the specified name.
   * @param db name of database
   * @param name new name
   * @param sopts static options
   * @throws IOException I/O exception
   */
  public static void alter(final String db, final String name, final StaticOptions sopts)
      throws IOException {

    final IOFile src = sopts.dbPath(db + IO.ZIPSUFFIX);
    final IOFile trg = sopts.dbPath(name + '-' + Databases.date(db) + IO.ZIPSUFFIX);

    final byte[] data = new byte[IO.BLOCKSIZE];
    try(BufferInput bi = new BufferInput(src); ZipInputStream in = new ZipInputStream(bi);
        BufferOutput bo = new BufferOutput(trg); ZipOutputStream out = new ZipOutputStream(bo)) {
      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
        out.putNextEntry(new ZipEntry(name + '/' + ze.getName().replaceAll("^.*/",  "")));
        for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
      }
    }
    src.delete();
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.ALTER + " " + CmdDrop.BACKUP).args();
  }}
