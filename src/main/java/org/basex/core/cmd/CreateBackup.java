package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.IO;

/**
 * Evaluates the 'backup' command and creates a backup of a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CreateBackup extends Command {
  /** Date format. */
  private static final SimpleDateFormat DATE = new SimpleDateFormat(
      "yyyy-MM-dd-HH-mm-ss");
  /** Counter for outstanding files. */
  private int of;
  /** Counter of total files. */
  private int tf;

  /**
   * Default constructor.
   * @param arg optional argument
   */
  public CreateBackup(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    if(!validName(args[0], true)) return error(NAMEINVALID, args[0]);

    // retrieve all databases
    final String[] dbs = databases(args[0]);
    if(dbs.length == 0) return error(DBNOTFOUND, args[0]);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      if(!prop.dbpath(db).isDirectory()) continue;
      if(backup(db, prop)) {
        // backup was successful
        info(DBBACKUP, db, perf);
      } else {
        info(DBNOBACKUP, db);
        ok = false;
      }
    }
    return ok;
  }

  /**
   * Backups the specified database.
   * @param db database name
   * @param pr database properties
   * @return success flag
   */
  private boolean backup(final String db, final Prop pr) {
    ZipOutputStream zos = null;
    try {
      final File in = pr.dbpath(db);
      final File file = pr.dbpath(db + "-" + DATE.format(new Date()) +
          IO.ZIPSUFFIX);
      final byte[] data = new byte[IO.BLOCKSIZE];

      // OutputStream for zipping
      zos = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(file)));
      zos.putNextEntry(new ZipEntry(in.getName() + '/'));
      zos.closeEntry();

      // Process each file
      final File[] files = in.listFiles();
      tf = files.length;
      for(final File f : files) {
        of++;
        BufferedInputStream bis = null;
        try {
          bis = new BufferedInputStream(new FileInputStream(f), IO.BLOCKSIZE);
          zos.putNextEntry(new ZipEntry(in.getName() + '/' + f.getName()));
          int c;
          while((c = bis.read(data)) != -1) zos.write(data, 0, c);
          zos.closeEntry();
        } finally {
          if(bis != null) try { bis.close(); } catch(final IOException e) { }
        }
      }
      zos.close();
      return true;
    } catch(final IOException ex) {
      return false;
    } finally {
      if(zos != null) try { zos.close(); } catch(final IOException e) { }
    }
  }

  @Override
  protected String tit() {
    return BUTTONBACKUP;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  protected double prog() {
    return (double) of / tf;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.BACKUP).args();
  }
}
