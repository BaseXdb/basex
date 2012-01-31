package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.in.DataInput;
import org.basex.util.Table;
import org.basex.util.list.StringList;
import org.basex.util.list.TokenList;

/**
 * Evaluates the 'list' command and shows all available databases.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class List extends Command {
  /** Pattern to extract the database name from a backup file name. */
  private static final Pattern PA =
      Pattern.compile(IO.DATEPATTERN + IO.ZIPSUFFIX + '$');

  /**
   * Default constructor.
   */
  public List() {
    super(STANDARD);
  }

  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = DATABASES;

    final boolean create = context.user.perm(User.CREATE);
    table.header.add(T_NAME);
    table.header.add(RESOURCES);
    table.header.add(SIZE);
    if(create) table.header.add(INPUT_PATH);

    for(final String name : list(context)) {
      DataInput di = null;
      String file = null;
      long size = 0;
      int docs = 0;
      final MetaData meta = new MetaData(name, context);
      try {
        di = new DataInput(meta.dbfile(DATAINF));
        meta.read(di);
        size = meta.dbsize();
        docs = meta.ndocs;
        if(context.perm(User.READ, meta)) file = meta.original.toString();
      } catch(final IOException ex) {
        file = ERROR;
      } finally {
        if(di != null) try { di.close(); } catch(final IOException ex) { }
      }

      // count number of raw files
      final File dir = new File(mprop.dbpath(name), M_RAW);
      final int bin = new IOFile(dir).descendants().size();

      // create entry
      if(file != null) {
        final TokenList tl = new TokenList(4);
        tl.add(name);
        tl.add(docs + bin);
        tl.add(size);
        if(create) tl.add(file);
        table.contents.add(tl);
      }
    }
    table.sort();
    out.println(table.finish());
    return true;
  }

  /**
   * Returns a list of all databases.
   * @param ctx database context
   * @return list of databases
   */
  public static StringList list(final Context ctx) {
    return list(ctx, false);
  }

  /**
   * Returns a list of all databases and (optionally) backed up databases.
   * @param ctx database context
   * @param backups include backups in the list
   * @return list of databases
   */
  public static StringList list(final Context ctx, final boolean backups) {
    final StringList db = new StringList();
    for(final IOFile f : ctx.mprop.dbpath().children()) {
      String name = f.name();
      if(backups && name.endsWith(IO.ZIPSUFFIX)) {
        name = dbname(name);
        if(!db.contains(name)) db.add(name);
      } else if(f.isDir() && !name.startsWith(".")) {
        db.add(name);
      }
    }
    db.sort(false, true);
    return db;
  }

  /**
   * Extracts the name of a database from its backup file.
   * @param s name of backup file
   * @return name of database
   */
  static String dbname(final String s) {
    return PA.split(s)[0];
  }
}
