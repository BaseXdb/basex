package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'list' command and shows all available databases.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class List extends Command {
  /**
   * Default constructor.
   */
  public List() {
    this(null);
  }

  /**
   * Default constructor.
   * @param name database name (can be {@code null})
   */
  public List(final String name) {
    this(name, null);
  }

  /**
   * Default constructor.
   * @param name database name (can be {@code null})
   * @param path database path (can be {@code null})
   */
  public List(final String name, final String path) {
    super(Perm.NONE, name == null ? "" : name, path == null ? "" : path);
  }

  @Override
  protected boolean run() throws IOException {
    return args[0].isEmpty() ? list() : listDB();
  }

  @Override
  public void databases(final LockResult lr) {
    if(args[0].isEmpty()) lr.readAll = true;
    else lr.read.add(args[0]);
  }

  /**
   * Lists all databases.
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean list() throws IOException {
    final Table table = new Table();
    table.description = DATABASES_X;

    final boolean create = context.user().has(Perm.CREATE);
    table.header.add(NAME);
    table.header.add(RESOURCES);
    table.header.add(SIZE);
    if(create) table.header.add(INPUT_PATH);

    for(final String name : context.filter(Perm.READ, context.databases.listDBs())) {
      String file = null;
      long dbsize = 0;
      int count = 0;

      try {
        final MetaData meta = new MetaData(name, options, soptions);
        meta.read();
        dbsize = meta.dbsize();
        file = meta.original;
        // add number of raw files
        count = meta.ndocs + new IOFile(soptions.dbpath(name), IO.RAW).descendants().size();
      } catch(final IOException ex) {
        file = ERROR;
      }

      // create entry
      final TokenList tl = new TokenList(create ? 4 : 3);
      tl.add(name);
      tl.add(count);
      tl.add(dbsize);
      if(create) tl.add(file);
      table.contents.add(tl);
    }
    out.println(table.sort().finish());
    return true;
  }

  /**
   * Lists resources of the specified database.
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean listDB() throws IOException {
    final String db = args[0], path = args[1];
    if(!Databases.validName(db)) return error(NAME_INVALID_X, db);

    final Table table = new Table();
    table.description = RESOURCES;
    table.header.add(INPUT_PATH);
    table.header.add(TYPE);
    table.header.add(DataText.CONTENT_TYPE);
    table.header.add(SIZE);

    try {
      // add xml documents
      final Data data = Open.open(db, context, options);
      final Resources res = data.resources;
      final IntList il = res.docs(path);
      final int ds = il.size();
      for(int i = 0; i < ds; i++) {
        final int pre = il.get(i);
        final TokenList tl = new TokenList(3);
        final byte[] file = data.text(pre, true);
        tl.add(file);
        tl.add(SerialMethod.XML.toString());
        tl.add(MediaType.APPLICATION_XML.toString());
        tl.add(data.size(pre, Data.DOC));
        table.contents.add(tl);
      }
      // add binary resources
      for(final byte[] file : res.binaries(path)) {
        final String f = string(file);
        final TokenList tl = new TokenList(3);
        tl.add(file);
        tl.add(SerialMethod.RAW.toString());
        tl.add(MediaType.get(f).toString());
        tl.add(data.meta.binary(f).length());
        table.contents.add(tl);
      }
      Close.close(data, context);
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }
    out.println(table.sort().finish());
    return true;
  }

  /**
   * Returns a list of all databases.
   * @param sopts static options
   * @return list of databases
   */
  public static StringList list(final StaticOptions sopts) {
    final StringList db = new StringList();
    for(final IOFile f : sopts.dbpath().children()) {
      final String name = f.name();
      if(f.isDir() && !name.startsWith(".")) db.add(name);
    }
    return db.sort(false);
  }
}
