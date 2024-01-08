package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.core.locks.*;
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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class AlterBackup extends ABackup {
  /**
   * Default constructor.
   * @param name name of backup with optional date
   * @param newname new name
   */
  public AlterBackup(final String name, final String newname) {
    super(name, newname);
  }

  @Override
  protected boolean run() {
    final String name = args[0], newname = args[1];
    if(!Databases.validName(name)) return error(NAME_INVALID_X, name);
    if(!Databases.validName(newname)) return error(NAME_INVALID_X, newname);

    // rename all backups
    final StringList backups = context.databases.backups(name);
    if(backups.isEmpty()) return error(BACKUP_NOT_FOUND_X, name);

    boolean ok = true;
    for(final String backup : backups) {
      try {
        alter(backup, newname, soptions);
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
   * @param name name of backup
   * @param newname new name
   * @param sopts static options
   * @throws IOException I/O exception
   */
  public static void alter(final String name, final String newname, final StaticOptions sopts)
      throws IOException {

    final IOFile src = sopts.dbPath(name + IO.ZIPSUFFIX);
    final IOFile trg = sopts.dbPath(newname + '-' + Databases.date(name) + IO.ZIPSUFFIX);

    final byte[] data = new byte[IO.BLOCKSIZE];
    try(BufferInput bi = new BufferInput(src); ZipInputStream in = new ZipInputStream(bi);
        BufferOutput bo = new BufferOutput(trg); ZipOutputStream out = new ZipOutputStream(bo)) {
      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
        out.putNextEntry(new ZipEntry(newname + '/' + ze.getName().replaceAll("^.*/", "")));
        for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
      }
    }
    src.delete();
  }

  @Override
  public void addLocks() {
    final LockList list = jc().locks.writes;
    addLocks(list, 0);
    addLocks(list, 1);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.ALTER + " " + CmdDrop.BACKUP).args();
  }}
