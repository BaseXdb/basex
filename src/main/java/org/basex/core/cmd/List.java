package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'list' command and shows all available databases.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class List extends Command {
  /** Pattern to extract the database name from a backup file name. */
  private static final Pattern PA = Pattern.compile(IO.DATEPATTERN + IO.ZIPSUFFIX + '$');

  /**
   * Default constructor.
   */
  public List() {
    this(null);
  }

  /**
   * Default constructor.
   * @param name database name
   */
  public List(final String name) {
    this(name, null);
  }

  /**
   * Default constructor.
   * @param name database name
   * @param path database path
   */
  public List(final String name, final String path) {
    super(Perm.NONE, name, path);
  }

  @Override
  protected boolean run() throws IOException {
    return args[0] == null || args[0].isEmpty() ? list() : listDB();
  }

  /**
   * Lists all databases.
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean list() throws IOException {
    final Table table = new Table();
    table.description = DATABASES;

    final boolean create = context.user.has(Perm.CREATE);
    table.header.add(T_NAME);
    table.header.add(RESOURCES);
    table.header.add(SIZE);
    if(create) table.header.add(INPUT_PATH);

    for(final String name : context.databases().listDBs()) {
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
        if(context.perm(Perm.READ, meta)) file = meta.original;
      } catch(final IOException ex) {
        file = ERROR;
      } finally {
        if(di != null) try { di.close(); } catch(final IOException ex) { }
      }

      // count number of raw files
      final IOFile dir = new IOFile(mprop.dbpath(name), M_RAW);
      final int bin = dir.descendants().size();

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
   * Lists resources of the specified database.
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean listDB() throws IOException {
    final String db = args[0];
    final String path = args[1] != null ? args[1] : "";
    if(!MetaData.validName(db, false)) return error(NAME_INVALID_X, db);

    final Table table = new Table();
    table.description = RESOURCES;
    table.header.add(INPUT_PATH);
    table.header.add(TYPE);
    table.header.add(MimeTypes.CONTENT_TYPE);
    table.header.add(SIZE);

    try {
      // add xml documents
      final Data data = Open.open(db, context);
      final Resources res = data.resources;
      final IntList il = res.docs(path);
      for(int i = 0, ds = il.size(); i < ds; i++) {
        final int pre = il.get(i);
        final TokenList tl = new TokenList(3);
        final byte[] file = data.text(pre, true);
        tl.add(file);
        tl.add(DataText.M_XML);
        tl.add(MimeTypes.APP_XML);
        tl.add(data.size(pre, Data.DOC));
        table.contents.add(tl);
      }
      // add binary resources
      for(final byte[] file : res.binaries(path)) {
        final String f = string(file);
        final TokenList tl = new TokenList(3);
        tl.add(file);
        tl.add(DataText.M_RAW);
        tl.add(MimeTypes.get(f));
        tl.add(data.meta.binary(f).length());
        table.contents.add(tl);
      }
      Close.close(data, context);
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return msg.isEmpty() ? error(DB_NOT_OPENED_X, db) : error(msg);
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
    return db.sort(false, true);
  }

  /**
   * Extracts the name of a database from its backup file.
   * @param s name of backup file
   * @return name of database
   */
  private static String dbname(final String s) {
    return PA.split(s)[0];
  }
}
