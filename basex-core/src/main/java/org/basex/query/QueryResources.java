package org.basex.query;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides access to all kinds of resources (databases, documents, database connections,
 * sessions) used by an XQuery expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class QueryResources {
  /** Resources. */
  public final HashMap<String, String[]> resources = new HashMap<String, String[]>();

  /** Database context. */
  private final QueryContext qc;
  /** Opened databases. */
  private Data[] data = new Data[1];
  /** Number of databases. */
  private int datas;

  /** Module loader. */
  public final ModuleLoader modules;
  /** Opened connections to relational databases. */
  private JDBCConnections jdbc;
  /** Opened connections to relational databases. */
  private ClientSessions sessions;

  /** Pending output. */
  public final ValueBuilder output = new ValueBuilder();;
  /** Pending updates. */
  Updates updates;

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
    this.qc = qc;
    modules = new ModuleLoader(qc.context);
  }

  /**
   * Compiles the resources.
   * @param nodes input node set
   * @throws QueryException query exception
   */
  void compile(final Nodes nodes) throws QueryException {
    final Data d = nodes.data;
    if(!qc.context.perm(Perm.READ, d.meta)) throw BASX_PERM.get(null, Perm.READ);

    // assign initial context value
    final boolean root = nodes.root;
    qc.value = DBNodeSeq.get(new IntList(nodes.pres), d, root, root);

    // create default collection: use initial node set if it contains all
    // documents of the database. otherwise, create new node set
    addCollection(root ? qc.value : DBNodeSeq.get(d.resources.docs(), d, true, true), d.meta.name);

    addData(d);
    synchronized(qc.context.dbs) { qc.context.dbs.pin(d); }
  }

  /**
   * Closes all opened data references that have not been added by the global context.
   */
  void close() {
    for(int d = 0; d < datas; d++) Close.close(data[d], qc.context);
    datas = 0;

    // close JDBC connections
    if(jdbc != null) jdbc.close();
    // close client sessions
    if(sessions != null) sessions.close();
    // close dynamically loaded JAR files
    modules.close();
  }

  /**
   * Opens a new database or returns a reference to an already opened database.
   * @param name name of database
   * @param info input info
   * @return database instance
   * @throws QueryException query exception
   */
  public Data database(final String name, final InputInfo info) throws QueryException {
    // check if a database with the same name has already been opened
    for(int d = 0; d < datas; ++d) {
      final String n = data[d].meta.name;
      if(Prop.CASE ? n.equals(name) : n.equalsIgnoreCase(name)) return data[d];
    }
    try {
      // open and add new data reference
      final Data d = Open.open(name, qc.context);
      addData(d);
      return d;
    } catch(final IOException ex) {
      throw BXDB_OPEN.get(info, ex);
    }
  }

  /**
   * Evaluates {@code fn:doc()}: opens an existing database document, or creates a new
   * database and node.
   * @param qi query input
   * @param baseIO base URI
   * @param info input info
   * @return document
   * @throws QueryException query exception
   */
  public DBNode doc(final QueryInput qi, final IO baseIO, final InputInfo info)
      throws QueryException {

    // favor default database
    if(qc.context.options.get(MainOptions.DEFAULTDB) && qc.nodes != null) {
      final Data dt = data[0];
      final int pre = dt.resources.doc(qi.original);
      if(pre != -1) return new DBNode(dt, pre, Data.DOC);
    }

    // check currently opened databases
    for(int d = 0; d < datas; ++d) {
      final Data dt = data[d];
      // check if database has a single document with an identical input path
      if(dt.resources.docs().size() == 1 && IO.get(dt.meta.original).eq(qi.input))
        return new DBNode(dt, 0, Data.DOC);

      // check if database and input have identical name
      // database instance has same name as input path
      final String n = dt.meta.name;
      if(Prop.CASE ? n.equals(qi.db) : n.equalsIgnoreCase(qi.db)) return doc(dt, qi, info);
    }

    // open new database, or create new instance
    Data dt = open(qi);
    if(dt == null) dt = create(qi, true, baseIO, info);
    return doc(dt, qi, info);
  }

  /**
   * Returns the default collection.
   * @param info input info
   * @return collection
   * @throws QueryException query exception
   */
  public Value collection(final InputInfo info) throws QueryException {
    if(colls == 0) throw NODEFCOLL.get(info);
    return coll[0];
  }

  /**
   * Evaluates {@code fn:collection()}: opens an existing database collection, or creates
   * a new data reference.
   * @param qi query input
   * @param baseIO base URI
   * @param info input info
   * @return collection
   * @throws QueryException query exception
   */
  public Value collection(final QueryInput qi, final IO baseIO, final InputInfo info)
      throws QueryException {

    // favor default database
    if(qc.context.options.get(MainOptions.DEFAULTDB) && qc.nodes != null) {
      final Data dt = data[0];
      final IntList pres = dt.resources.docs(qi.original);
      return DBNodeSeq.get(pres, dt, true, qi.original.isEmpty());
    }

    // merge input with base directory
    final String in = baseIO != null ? baseIO.merge(qi.original).path() : null;

    // check currently opened collections
    if(in != null) {
      final String[] names = { in, qi.original };
      for(int c = 0; c < colls; c++) {
        final String n = collName[c];
        if(Prop.CASE ? Token.eq(n, names) : Token.eqic(n, names)) return coll[c];
      }
    }

    // check currently opened databases
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
    if(dt == null) dt = create(qi, false, baseIO, info);
    return DBNodeSeq.get(dt.resources.docs(qi.path), dt, true, qi.path.isEmpty());
  }

  // TEST APIS ====================================================================================

  /**
   * Adds a document with the specified path. Only called from the test APIs.
   * @param name document identifier (may be {@code null})
   * @param path documents path
   * @param baseIO base URI
   * @throws QueryException query exception
   */
  public void addDoc(final String name, final String path, final IO baseIO)
      throws QueryException {

    final QueryInput qi = new QueryInput(path);
    final Data d = create(qi, true, baseIO, null);
    if(name != null) d.meta.original = name;
  }

  /**
   * Adds a resource with the specified path. Only called from the test APIs.
   * @param uri resource uri
   * @param strings resource strings (path, encoding)
   */
  public void addResource(final String uri, final String... strings) {
    resources.put(uri, strings);
  }

  /**
   * Adds a collection with the specified paths. Only called from the test APIs.
   * @param name name of collection
   * @param paths documents paths
   * @param baseIO base URI
   * @throws QueryException query exception
   */
  public void addCollection(final String name, final String[] paths, final IO baseIO)
      throws QueryException {

    final int ns = paths.length;
    final DBNode[] nodes = new DBNode[ns];
    for(int n = 0; n < ns; n++) {
      final QueryInput qi = new QueryInput(paths[n]);
      nodes[n] = new DBNode(create(qi, true, baseIO, null), 0, Data.DOC);
    }
    addCollection(Seq.get(nodes, ns, NodeType.DOC), name);
  }

  /**
   * Returns JDBC connections.
   * @return jdbc connections
   */
  public JDBCConnections jdbc() {
    if(jdbc == null) jdbc = new JDBCConnections();
    return jdbc;
  }

  /**
   * Returns client sessions.
   * @return client session
   */
  public ClientSessions sessions() {
    if(sessions == null) sessions = new ClientSessions();
    return sessions;
  }

  /**
   * Returns a reference to the updates.
   * @return updates
   */
  public Updates updates() {
    if(updates == null) updates = new Updates();
    return updates;
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
        final Data d = Open.open(input.db, qc.context);
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
   * @param baseIO base URI
   * @param info input info
   * @return data reference
   * @throws QueryException query exception
   */
  private Data create(final QueryInput input, final boolean single, final IO baseIO,
      final InputInfo info) throws QueryException {

    // check if new databases can be created
    final boolean createDB = qc.context.options.get(MainOptions.FORCECREATE);
    if(!qc.context.user.has(Perm.READ))
      throw BXXQ_PERM.get(info, Util.info(Text.PERM_REQUIRED_X, Perm.READ));

    // check if input is an existing file
    final IO source = checkPath(input, baseIO, info);

    if(single && source.isDir()) WHICHRES.get(info, baseIO);
    try {
      final Data dt = createDB ? CreateDB.create(source, qc.context) :
        CreateDB.mainMem(source, qc.context);
      input.path = "";
      addData(dt);
      return dt;
    } catch(final IOException ex) {
      throw IOERR.get(info, ex);
    }
  }

  /**
   * Returns a valid reference if a file is found in the specified path or the static base uri.
   * Otherwise, returns an error.
   * @param input query input
   * @param baseIO base IO
   * @param info input info
   * @return input source, or exception
   * @throws QueryException query exception
   */
  public static IO checkPath(final QueryInput input, final IO baseIO, final InputInfo info)
      throws QueryException {

    IO in = input.input;
    if(in.exists()) return in;
    if(baseIO != null) {
      in = baseIO.merge(input.original);
      if(!in.path().equals(input.original) && in.exists()) return in;
    }
    throw WHICHRES.get(info, in);
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
    if(docs.size() == 1) return new DBNode(dt, docs.get(0), Data.DOC);
    throw (docs.isEmpty() ? BXDB_NODOC : BXDB_SINGLE).get(info, qi.original);
  }

  /**
   * Adds a data reference.
   * @param d data reference to be added
   */
  public void addData(final Data d) {
    if(datas == data.length) data = Array.copy(data, new Data[Array.newSize(datas)]);
    data[datas++] = d;
  }

  /**
   * Removes and closes a database if it has not been added by the global context.
   * @param name name of database to be removed
   */
  public void removeData(final String name) {
    for(int d = qc.nodes != null ? 1 : 0; d < datas; d++) {
      if(data[d].meta.name.equals(name)) {
        Close.close(data[d], qc.context);
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
      final int s = Array.newSize(colls);
      coll = Array.copy(coll, new Value[s]);
      collName = Array.copyOf(collName, s);
    }
    coll[colls] = nodes;
    collName[colls++] = name;
  }
}
