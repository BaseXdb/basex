package org.basex.query;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

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
 * @author Christian Gruen
 */
public final class QueryResources {
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
   * @param info input info
   * @return database instance
   * @throws QueryException query exception
   */
  public Data data(final String name, final InputInfo info) throws QueryException {
    // check if a database with the same name has already been opened
    for(int d = 0; d < datas; ++d) {
      if(data[d].meta.name.equalsIgnoreCase(name)) return data[d];
    }

    try {
      // open and add new data reference
      final Data d = Open.open(name, ctx.context);
      addData(d);
      return d;
    } catch(final IOException ex) {
      throw NODB.thrw(info, ex);
    }
  }

  /**
   * Evaluates {@code fn:doc()}: opens an existing database document, or creates a new
   * database and node.
   * @param input document path
   * @param info input info
   * @return document
   * @throws QueryException query exception
   */
  public DBNode doc(final String input, final InputInfo info) throws QueryException {
    final QueryInput qi = new QueryInput(input);

    // check currently opened databases
    for(int d = 0; d < datas; ++d) {
      final Data dt = data[d];
      // database has a single document, input paths are matching
      if(dt.single() && IO.get(dt.meta.original).eq(qi.io))
        return new DBNode(dt, 0, Data.DOC);

      // database instance has same name as input path
      if(dt.meta.name.equalsIgnoreCase(qi.db)) return doc(dt, qi, info);
    }

    // open new database, or create new instance
    Data dt = open(qi);
    if(dt == null) dt = create(qi, true, info);
    return doc(dt, qi, info);
  }

  /**
   * Returns the default collection.
   * @param info input info
   * @return collection
   * @throws QueryException query exception
   */
  public Value collection(final InputInfo info) throws QueryException {
    if(colls == 0) NODEFCOLL.thrw(info);
    return coll[0];
  }

  /**
   * Evaluates {@code fn:collection()}: opens an existing database collection, or creates
   * a new data reference.
   * @param input collection path
   * @param info input info
   * @return collection
   * @throws QueryException query exception
   */
  public Value collection(final String input, final InputInfo info)
      throws QueryException {

    // merge input with base directory
    final IO base = ctx.sc.baseIO();
    final String in = base != null ? base.merge(input).path() : null;

    // check currently opened collections
    for(int c = 0; c < colls; c++) {
      if(in != null && collName[c].equalsIgnoreCase(in) ||
          collName[c].equalsIgnoreCase(input)) return coll[c];
    }

    // check currently opened databases
    final QueryInput qi = new QueryInput(input);
    Data dt = null;
    for(int d = 0; d < datas; ++d) {
      // return database instance with the same name or file path
      if(qi.db != null && data[d].meta.name.equalsIgnoreCase(qi.db) ||
          IO.get(data[d].meta.original).eq(qi.io)) {
        dt = data[d];
        break;
      }
    }

    // open new database, or create new instance
    if(dt == null) dt = open(qi);
    if(dt == null) dt = create(qi, false, info);
    return DBNodeSeq.get(dt.resources.docs(qi.path), dt, true, qi.path.isEmpty());
  }

  /**
   * Adds a document with the specified path. Only called from the test APIs.
   * @param name document identifier (may be {@code null})
   * @param path documents path
   * @throws QueryException query exception
   */
  public void addDoc(final String name, final String path) throws QueryException {
    final QueryInput qi = new QueryInput(path);
    final Data d = create(qi, true, null);
    if(name != null) d.meta.original = name;
  }

  /**
   * Adds a collection with the specified paths. Only called from the test APIs.
   * @param name name of collection
   * @param paths documents paths
   * @throws QueryException query exception
   */
  public void addCollection(final String name, final String[] paths)
     throws QueryException {

    final int ns = paths.length;
    final DBNode[] nodes = new DBNode[ns];
    for(int n = 0; n < ns; n++) {
      final QueryInput qi = new QueryInput(paths[n]);
      nodes[n] = new DBNode(create(qi, true, null), 0, Data.DOC);
    }
    addCollection(Seq.get(nodes, ns), name);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Tries to open the addressed database, or returns {@code null}.
   * @param input query input
   * @return data reference
   */
  private Data open(final QueryInput input) {
    if(input.db != null) {
      try {
        // try to open database
        final Data d = Open.open(input.db, ctx.context);
        addData(d);
        return d;
      } catch(final IOException ex) {
        /* ignored */
      }
    }
    return null;
  }

  /**
   * Creates a new database instance.
   * @param input query input
   * @param single expect single document
   * @param info input info
   * @return data reference
   * @throws QueryException query exception
   */
  private Data create(final QueryInput input, final boolean single, final InputInfo info)
      throws QueryException {

    Data d = null;
    try {
      // try to create database with original path
      d = CreateDB.create(input.io, single, ctx.context);
    } catch(final IOException ex) {
      // try to create database with path relative to base uri
      final IO base = ctx.sc.baseIO();
      if(base != null) {
        try {
          final String in = base.merge(input.original).path();
          if(!in.equals(input.original))
            d = CreateDB.create(IO.get(in), single, ctx.context);
        } catch(final IOException exc) { /* ignore exception */ }
      }

      if(d == null) {
        // handle exception
        Util.debug(ex);
        IOERR.thrw(info, ex);
      }
    }

    input.path = "";
    addData(d);
    return d;
  }

  /**
   * Returns a single document node for the specified data reference.
   * @param dt data reference
   * @param qi query input
   * @param info input info
   * @return document node
   * @throws QueryException query exception
   */
  private DBNode doc(final Data dt, final QueryInput qi, final InputInfo info)
      throws QueryException {

    // database contains a single document and no database path is specified
    if(dt.single() && qi.path.isEmpty()) return new DBNode(dt, 0, Data.DOC);

    // check if the database contains exactly one relevant document
    final IntList docs = dt.resources.docs(qi.path);
    if(docs.isEmpty()) RESFNF.thrw(info, qi.original);
    if(docs.size() != 1) EXPSINGLE.thrw(info, qi.original);
    return new DBNode(dt, docs.get(0), Data.DOC);
  }

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
