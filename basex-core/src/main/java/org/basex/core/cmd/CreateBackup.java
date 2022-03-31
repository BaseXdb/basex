package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'backup' command and creates a backup of a database.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CreateBackup extends ABackup {
  /** Total files in a zip operation. */
  private int total;
  /** Current file in a zip operation. */
  private int curr;

  /**
   * Default constructor.
   * @param name name of database
   */
  public CreateBackup(final String name) {
    this(name, null);
  }

  /**
   * Default constructor.
   * @param name name of database
   * @param comment (can be {@code null})
   */
  public CreateBackup(final String name, final String comment) {
    super(name, comment);
  }

  @Override
  protected boolean run() {
    final String pattern = args[0];
    if(!Databases.validPattern(pattern)) return error(NAME_INVALID_X, pattern);
    final String comment = args[1];

    // retrieve all databases
    final StringList dbs = context.listDBs(pattern);
    if(dbs.isEmpty()) return error(DB_NOT_FOUND_X, pattern);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      // don't open databases marked as updating
      if(MetaData.file(soptions.dbPath(db), DATAUPD).exists()) {
        // reject backups of databases that are currently being updated (or corrupt)
        info(DB_UPDATED_X, db);
        ok = false;
      } else {
        try {
          backup(db, comment, soptions, this);
          // backup was successful
          info(DB_BACKUP_X, db, jc().performance);
        } catch(final IOException ex) {
          Util.debug(ex);
          info(DB_NOT_BACKUP_X, db);
          ok = false;
        }
      }
    }
    return ok;
  }

  /**
   * Backups the specified database.
   * @param db name of the database
   * @param comment comment (can be {@code null})
   * @param sopts static options
   * @param cmd calling command instance
   * @throws IOException I/O Exception
   */
  public static void backup(final String db, final String comment, final StaticOptions sopts,
      final CreateBackup cmd) throws IOException {

    final IOFile dbpath = sopts.dbPath(db);
    final StringList files = dbpath.descendants();
    if(cmd != null) cmd.total = files.size();

    final String name = db + '-' + DateTime.format(new Date(), DateTime.DATETIME) + IO.ZIPSUFFIX;
    final IOFile backup = sopts.dbPath(name);
    try(BufferOutput bo = new BufferOutput(backup); ZipOutputStream out = new ZipOutputStream(bo)) {
      if(comment != null) {
        out.setComment(comment.length() > 100 ? comment.substring(0, 100) + DOTS : comment);
      }
      // use simple, fast compression
      out.setLevel(1);
      final byte[] data = new byte[IO.BLOCKSIZE];
      for(final String file : files) {
        // skip update file (generated when using XQuery)
        if(!file.equals(DATAUPD + IO.BASEXSUFFIX)) {
          final String path = Prop.WIN ? file.replace('\\', '/') : file;
          out.putNextEntry(new ZipEntry(dbpath.name() + '/' + path));
          try(FileInputStream in = new FileInputStream(new File(dbpath.file(), file))) {
            for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
          }
          out.closeEntry();
        }
        if(cmd != null) cmd.curr++;
      }
    }
  }

  @Override
  public void addLocks() {
    super.addLocks();
    addLocks(jc().locks.reads, 0);
  }

  @Override
  public String shortInfo() {
    return BACKUP;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  public double progressInfo() {
    return (double) curr / total;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.BACKUP).args();
  }
}
