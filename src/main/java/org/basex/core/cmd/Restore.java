package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.io.IO;
import org.basex.util.StringList;

/**
 * Evaluates the 'restore' command and restores a backup of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Restore extends Command {
  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Restore(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    String db = args[0];
    if(!checkName(db)) return error(NAMEINVALID, db);

    final int i = db.indexOf("-");
    String name = null;
    if(i == -1) {
      final StringList list = list(db + '-', context);
      if(list.size() == 0) return error(DBBACKNF, db);
      name = list.get(0);
    } else {
      if(!db.endsWith(IO.ZIPSUFFIX)) db += IO.ZIPSUFFIX;
      name = prop.get(Prop.DBPATH) + Prop.SEP + db;
      db = db.substring(0, i);
    }
    final File file = new File(name);
    if(!file.exists()) return error(DBBACKNF, db);

    // close database if it's currently opened
    final boolean close = context.data != null &&
      db.equals(context.data.meta.name);
    if(close) new Close().run(context);

    // check if database is pinned
    if(context.pinned(db)) return error(DBLOCKED, db);

    // try to restore database
    return restore(file, prop) && (!close || new Open(db).run(context)) ?
        info(DBRESTORE, file.getName(), perf) : error(DBNORESTORE, db);
  }

  /**
   * Restores the specified database.
   * @param file file
   * @param pr database properties
   * @return success flag
   */
  public static boolean restore(final File file, final Prop pr) {
    try {
      final InputStream is = new BufferedInputStream(new FileInputStream(file));
      final ZipInputStream zis = new ZipInputStream(is);
      final byte[] data = new byte[IO.BLOCKSIZE];
      ZipEntry e;
      while((e = zis.getNextEntry()) != null) {
        final String path = pr.get(Prop.DBPATH) + Prop.SEP + e.getName();
        if(e.isDirectory()) {
          new File(path).mkdir();
        } else {
          final BufferedOutputStream bos = new BufferedOutputStream(
              new FileOutputStream(path));
          int c;
          while((c = zis.read(data)) != -1) bos.write(data, 0, c);
          bos.close();
        }
      }
      zis.close();
      return true;
    } catch(final IOException io) {
      return false;
    }
  }

  /**
   * Returns all backups of the specified database.
   * @param db database
   * @param ctx database context
   * @return available backups
   */
  public static StringList list(final String db, final Context ctx) {
    // create database list
    final StringList list = new StringList();

    final IO dir = IO.get(ctx.prop.get(Prop.DBPATH));
    if(!dir.exists()) return list;

    final String pre = db + (db.contains("-") ? "" : "-");
    for(final IO f : dir.children()) {
      final String n = f.name();
      if(n.startsWith(pre) && n.endsWith(IO.ZIPSUFFIX)) list.add(f.path());
    }
    list.sort(false, false);
    return list;
  }
}
