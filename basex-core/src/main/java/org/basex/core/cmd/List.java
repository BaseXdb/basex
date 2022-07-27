package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'list' command and shows all available databases.
 *
 * @author BaseX Team 2005-22, BSD License
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
    return args[0].isEmpty() ? list() : listResources();
  }

  @Override
  public void addLocks() {
    final Locks locks = jc().locks;
    if(args[0].isEmpty()) locks.reads.addGlobal();
    else locks.reads.add(args[0]);
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
    table.header.add(NAME).add(RESOURCES).add(SIZE);
    if(create) table.header.add(INPUT_PATH);

    for(final String name : context.listDBs()) {
      String file;
      long dbsize = 0;
      int count = 0;

      try {
        final MetaData meta = new MetaData(name, options, soptions);
        meta.read();
        dbsize = meta.dbSize();
        file = meta.original;
        count = meta.ndocs + meta.dir(ResourceType.BINARY).descendants().size() +
            meta.dir(ResourceType.VALUE).descendants().size();
      } catch(final IOException ex) {
        Util.debug(ex);
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
  private boolean listResources() throws IOException {
    final String db = args[0], root = args[1];
    if(!Databases.validName(db)) return error(NAME_INVALID_X, db);

    final Table table = new Table();
    table.description = RESOURCES_X;
    table.header.add(INPUT_PATH).add(TYPE).add(DataText.CONTENT_TYPE).add(SIZE);

    try {
      final Data data = Open.open(db, context, options);
      final Resources resources = data.resources;

      // list XML documents
      final IntList docs = resources.docs(root);
      final int ds = docs.size();
      for(int d = 0; d < ds; d++) {
        final int pre = docs.get(d);
        final String string = Token.string(data.text(pre, true));
        add(table, string, data.size(pre, Data.DOC), ResourceType.XML);
      }
      // list file resources
      for(final ResourceType type : Resources.BINARIES) {
        final IOFile bin = data.meta.dir(type);
        for(final String path : resources.paths(root, type)) {
          add(table, path, type.filePath(bin, path).length(), type);
        }
      }
      Close.close(data, context);
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }
    out.println(table.sort().finish());
    return true;
  }

  /**
   * Adds a table entry.
   * @param table table
   * @param path path to resource
   * @param size size of resource
   * @param type resource type
   */
  private static void add(final Table table, final String path, final long size,
      final ResourceType type) {
    table.contents.add(new TokenList(4).add(path).add(type.toString()).
        add(type.contentType(path).toString()).add(size));
  }
}
