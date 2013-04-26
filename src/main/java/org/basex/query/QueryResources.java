package org.basex.query;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides access to resources used by an XQuery expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QueryResources {
  /** Resources. */
  public final HashMap<String, String> resources = new HashMap<String, String>();

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
    if(!ctx.context.perm(Perm.READ, d.meta)) BASX_PERM.thrw(null, Perm.READ);

    // assign initial context value
    final boolean root = nodes.root;
    ctx.value = DBNodeSeq.get(new IntList(nodes.pres), d, root, root);

    // create default collection: use initial node set if it contains all
    // documents of the database. otherwise, create new node set
    addCollection(root ? ctx.value :
      DBNodeSeq.get(d.resources.docs(), d, true, true), d.meta.name);

    addData(d);
  }

  /**
   * Closes all opened data references that have not been added by the global context.
   */
  void close() {
    for(int d = ctx.nodes != null ? 1 : 0; d < datas; d++) {
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
      final String n = data[d].meta.name;
      if(Prop.CASE ? n.equals(name) : n.equalsIgnoreCase(name)) return data[d];
    }

    try {
      // open and add new data reference
      final Data d = Open.open(name, ctx.context);
      addData(d);
      return d;
    } catch(final IOException ex) {
      throw BXDB_OPEN.thrw(info, ex);
    }
  }

  /**
   * Evaluates {@code fn:doc()}: opens an existing database document, or creates a new
   * database and node.
   * @param qi query input
   * @param info input info
   * @return document
   * @throws QueryException query exception
   */
  public DBNode doc(final QueryInput qi, final InputInfo info) throws QueryException {
    // check currently opened databases
    for(int d = 0; d < datas; ++d) {
      final Data dt = data[d];
      // database has a single document, input paths are matching
      if(dt.resources.docs().size() == 1 && IO.get(dt.meta.original).eq(qi.input))
        return new DBNode(dt, 0, Data.DOC);

      // database instance has same name as input path
      final String n = dt.meta.name;
      if(Prop.CASE ? n.equals(n) : n.equalsIgnoreCase(qi.db)) return doc(dt, qi, info);
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
    if(in != null) {
      final String[] names = { in, input };
      for(int c = 0; c < colls; c++) {
        final String n = collName[c];
        if(Prop.CASE ? Token.eq(n, names) : Token.eqic(n, names)) return coll[c];
      }
    }

    // check currently opened databases
    final QueryInput qi = new QueryInput(input);
    Data dt = null;
    for(int i = 0; i < datas; ++i) {
      // return database instance with the same name or file path
      final Data d = data[i];
      final String n = d.meta.name;
      if(Prop.CASE ? n.equals(qi.db) : n.equalsIgnoreCase(qi.db) ||
          IO.get(d.meta.original).eq(qi.input)) {
        dt = d;
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
   * Adds a resource with the specified path. Only called from the test APIs.
   * @param uri resource uri
   * @param path resource path
   */
  public void addResource(final String uri, final String path) {
    resources.put(uri, path);
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
    addCollection(Seq.get(nodes, ns, NodeType.DOC), name);
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
      d = CreateDB.create(input.input, single, ctx.context);
    } catch(final IOException ex) {
      // try to create database with path relative to base uri
      final IO base = ctx.sc.baseIO();
      if(base != null) {
        try {
          final String in = base.merge(input.original).path();
          if(!in.equals(input.original))
            d = CreateDB.create(IO.get(in), single, ctx.context);
        } catch(final IOException exc) {
          // raise error
          IOERR.thrw(info, exc);
        }
      }
      // no base URI: raise error
      if(d == null) IOERR.thrw(info, ex);
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
  private static DBNode doc(final Data dt, final QueryInput qi, final InputInfo info)
      throws QueryException {

    // get all document nodes of the specified database
    final IntList docs = dt.resources.docs(qi.path);
    // ensure that a single document was filtered
    if(docs.size() != 1) BXDB_SINGLE.thrw(info, qi.original);
    return new DBNode(dt, docs.get(0), Data.DOC);
  }

  /**
   * Adds a data reference.
   * @param d data reference to be added
   */
  public void addData(final Data d) {
    if(datas == data.length) {
      final Data[] tmp = new Data[Array.newSize(datas)];
      System.arraycopy(data, 0, tmp, 0, datas);
      data = tmp;
    }
    data[datas++] = d;
  }

  /**
   * Removes and closes a database if it has not been added by the global context.
   * @param name name of database to be removed
   */
  public void removeData(final String name) {
    for(int d = ctx.nodes != null ? 1 : 0; d < datas; d++) {
      if(data[d].meta.name.equals(name)) {
        Close.close(data[d], ctx.context);
        Array.move(data, d + 1, -1, --datas - d);
        data[datas] = null;
        break;
      }
    }
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
