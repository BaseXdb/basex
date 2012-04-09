package org.basex.query;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides access to resources used by an XQuery expression.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class QueryResources {
  /** Slash pattern. */
  private static final Pattern SLASHES = Pattern.compile("^/+|/+$");
  /** Database context. */
  private final QueryContext ctx;

  /** Opened databases. */
  private Data[] data = new Data[1];
  /** Number of databases. */
  private int datas;

  /** Collections: single nodes and sequences. */
  private Value[] coll = new Value[1];
  /** Names of collections. */
  private String[] collName = new String[1];
  /** Number of collections. */
  private int colls;

  /**
   * Constructor.
   * @param qc query context
   */
  QueryResources(final QueryContext qc) {
    ctx = qc;
  }

  /**
   * Compiles the resources.
   * @param nodes input node set
   * @throws QueryException query exception
   */
  void compile(final Nodes nodes) throws QueryException {
    final Data d = nodes.data;
    if(!ctx.context.perm(Perm.READ, d.meta)) PERMNO.thrw(null, Perm.READ);

    // assign initial context value
    // (if database only contains an empty root node, assign empty sequence)
    ctx.value = d.isEmpty() ? Empty.SEQ :
      DBNodeSeq.get(new IntList(nodes.list), d, nodes.root, nodes.root);

    // create default collection: use initial node set if it contains all
    // documents of the database. otherwise, create new node set
    addCollection(nodes.root ? ctx.value :
        DBNodeSeq.get(d.resources.docs(), d, true, true), d.meta.name);

    addData(d);
  }

  /**
   * Closes the opened data references.
   */
  void close() {
    for(int d = ctx.nodes != null ? 1 : 0; d < datas; ++d) {
      Close.close(data[d], ctx.context);
    }
    datas = 0;
  }

  /**
   * Opens a new database or returns a reference to an already opened database.
   * @param name name of database
   * @param ii input info
   * @return database instance
   * @throws QueryException query exception
   */
  public Data data(final String name, final InputInfo ii) throws QueryException {
    // check if a database with the same name has already been opened
    for(int d = 0; d < datas; ++d) {
      if(data[d].meta.name.equals(name)) return data[d];
    }

    try {
      // open and add new data reference
      final Data d = Open.open(name, ctx.context);
      addData(d);
      return d;
    } catch(final IOException ex) {
      throw NODB.thrw(ii, name);
    }
  }

  /**
   * Creates a new data reference for the specified input, or returns a
   * reference to an already opened file or database.
   * @param input file path or name of database
   * @param path optional path to the addressed sub-directory. Set to {@code null}
   *        if a single document is addressed
   * @param ii input info
   * @return data reference
   * @throws QueryException query exception
   */
  public Data data(final String input, final String path, final InputInfo ii)
      throws QueryException {

    // check if an opened database with the same name exists
    for(int d = 0; d < datas; ++d) {
      if(data[d].meta.name.equals(input)) return data[d];
    }

    // check if an opened database with the same file path exists
    final IO io = IO.get(input);
    for(int d = 0; d < datas; ++d) {
      if(IO.get(data[d].meta.original).eq(io)) return data[d];
    }

    IOException ex = null;
    Data d = null;
    try {
      // try to retrieve data reference
      d = Check.check(ctx.context, input, path);
    } catch(final IOException e) {
      ex = e;
      // try to retrieve data reference relative to base uri
      final IO base = ctx.sc.baseIO();
      if(base != null) {
        try {
          final String p = base.merge(input).path();
          if(!p.equals(input)) d = Check.check(ctx.context, p, path);
        } catch(final IOException exc) { /* ignore exception */ }
      }
    }

    if(d == null) {
      // handle first exception
      Util.debug(ex);
      if(ex instanceof FileNotFoundException) RESFNF.thrw(ii, input);
      IOERR.thrw(ii, ex);
    }

    // add reference to pool of opened databases
    addData(d);
    return d;
  }

  /**
   * Adds a collection instance or returns an existing one.
   * @param input name of the collection to be returned
   * @param ii input info
   * @return collection
   * @throws QueryException query exception
   */
  public Value collection(final String input, final InputInfo ii) throws QueryException {
    int c = 0;
    // no collection specified.. return default collection/current context set
    if(input == null) {
      // no default collection was defined
      if(colls == 0) NODEFCOLL.thrw(ii);
    } else {
      // invalid collection reference
      if(input.contains("<") || input.contains("\\")) INVCOLL.thrw(ii, input);

      // find specified collection
      while(c < colls && !collName[c].equals(input)) ++c;
      // unknown collection
      if(c == colls) {
        final IO base = ctx.sc.baseIO();
        if(base != null) {
          c = 0;
          final String in = base.merge(input).path();
          while(c < colls && !collName[c].equals(in)) ++c;
        }
      }

      // add new collection if not found
      if(c == colls) {
        String root = SLASHES.matcher(input).replaceFirst("");
        String path = "";
        final int s = root.indexOf('/');
        if(s > 0 && root.indexOf(':') == -1) {
          path = root.substring(s + 1);
          root = root.substring(0, s);
        }
        final Data d = data(root, path, ii);
        final String p = d instanceof MemData ? "" : path;
        addCollection(DBNodeSeq.get(d.resources.docs(p), d, true,
            path.isEmpty()), d.meta.name);
      }
    }
    return coll[c];
  }

  // API METHODS ==============================================================

  /**
   * Adds a document with the specified path.
   * @param name name of document, or {@code null}
   * @param path documents path
   * @throws QueryException query exception
   */
  public void addDoc(final String name, final String path) throws QueryException {
    final Data d = data(path, null, null);
    if(name != null) d.meta.name = name;
  }

  /**
   * Adds a collection with the specified paths.
   * @param name name of collection
   * @param paths documents paths
   * @throws QueryException query exception
   */
  public void addCollection(final String name, final String[] paths)
     throws QueryException {

    final int ns = paths.length;
    final DBNode[] nodes = new DBNode[ns];
    for(int n = 0; n < ns; n++) {
      nodes[n] = new DBNode(data(paths[n], null, null), 0, Data.DOC);
    }
    addCollection(Seq.get(nodes, ns), name);
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Adds a data reference to the global list.
   * @param d data reference to be added
   */
  private void addData(final Data d) {
    if(datas == data.length) {
      final Data[] tmp = new Data[Array.newSize(datas)];
      System.arraycopy(data, 0, tmp, 0, datas);
      data = tmp;
    }
    data[datas++] = d;
  }

  /**
   * Adds a collection to the global collection list.
   * @param nodes collection nodes
   * @param name collection name
   */
  private void addCollection(final Value nodes, final String name) {
    if(colls == coll.length) {
      coll = Arrays.copyOf(coll, colls << 1);
      collName = Array.copyOf(collName, colls << 1);
    }
    coll[colls] = nodes;
    collName[colls++] = name;
  }
}
