package org.basex.query;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
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
  /** Textual resources. */
  public final HashMap<String, String[]> texts = new HashMap<>();

  /** Database context. */
  private final QueryContext qc;

  /** Collections: single nodes and sequences. */
  private final ArrayList<Value> colls = new ArrayList<>(1);
  /** Names of collections. */
  private final ArrayList<String> collNames = new ArrayList<>(1);
  /** Opened databases. */
  private final ArrayList<Data> datas = new ArrayList<>(1);
  /** Indicates if the first database in the context is globally opened. */
  private boolean globalData;

  /** Module loader. */
  private ModuleLoader modules;
  /** External resources. */
  private final HashMap<Class<? extends QueryResource>, QueryResource> external = new HashMap<>();

  /** Pending output. */
  public final ValueBuilder output = new ValueBuilder();
  /** Pending updates. */
  Updates updates;

  /**
   * Constructor.
   * @param qc query context
   */
  QueryResources(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Compiles the resources.
   * @param nodes input node set
   * @return context value
   */
  Value compile(final DBNodes nodes) {
    // assign initial context value
    final Data data = nodes.data;
    final boolean all = nodes.all;
    final Value value = DBNodeSeq.get(new IntList(nodes.pres), data, all, all);

    // create default collection: use initial node set if it contains all
    // documents of the database. otherwise, create new node set
    final Value coll = all ? value : DBNodeSeq.get(data.resources.docs(), data, true, true);
    addCollection(coll, data.meta.name);
    addData(data);
    synchronized(qc.context.dbs) { qc.context.dbs.pin(data); }

    globalData = true;
    return value;
  }

  /**
   * Adds an external resource.
   * @param ext external resource
   */
  public void add(final QueryResource ext) {
    external.put(ext.getClass(), ext);
  }

  /**
   * Returns an external resource of the specified class.
   * @param <R> resource
   * @param resource external resource
   * @return resource
   */
  @SuppressWarnings("unchecked")
  public <R extends QueryResource> R get(final Class<? extends R> resource) {
    return (R) external.get(resource);
  }

  /**
   * Closes all opened data references that have not been added by the global context.
   */
  void close() {
    for(final Data data : datas) Close.close(data, qc.context);
    datas.clear();

    // close dynamically loaded JAR files
    if(modules != null) modules.close();
    // close external resources
    for(final QueryResource c : external.values()) c.close();
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
    for(final Data data : datas) {
      if(data.inMemory()) continue;
      final String n = data.meta.name;
      if(Prop.CASE ? n.equals(name) : n.equalsIgnoreCase(name)) return data;
    }
    try {
      // open and add new data reference
      return addData(Open.open(name, qc.context));
    } catch(final IOException ex) {
      throw BXDB_OPEN_X.get(info, ex);
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
    final Data gd = globalData();
    if(qc.context.options.get(MainOptions.DEFAULTDB) && gd != null) {
      final int pre = gd.resources.doc(qi.original);
      if(pre != -1) return new DBNode(gd, pre, Data.DOC);
    }

    // check currently opened databases
    for(final Data data : datas) {
      // check if database has a single document with an identical input path
      if(data.resources.docs().size() == 1 && IO.get(data.meta.original).eq(qi.input))
        return new DBNode(data, 0, Data.DOC);

      // check if database and input have identical name
      // database instance has same name as input path
      final String n = data.meta.name;
      if(Prop.CASE ? n.equals(qi.db) : n.equalsIgnoreCase(qi.db)) return doc(data, qi, info);
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
    if(colls.isEmpty()) throw NODEFCOLL.get(info);
    return colls.get(0);
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
    final Data gd = globalData();
    if(qc.context.options.get(MainOptions.DEFAULTDB) && gd != null) {
      final IntList pres = gd.resources.docs(qi.original);
      return DBNodeSeq.get(pres, gd, true, qi.original.isEmpty());
    }

    // merge input with base directory
    final String in = baseIO != null ? baseIO.merge(qi.original).path() : null;

    // check currently opened collections
    if(in != null) {
      final String[] names = { in, qi.original };
      final int cs = colls.size();
      for(int c = 0; c < cs; c++) {
        final String name = collNames.get(c);
        if(Prop.CASE ? Token.eq(name, names) : Token.eqic(name, names)) return colls.get(c);
      }
    }

    // check currently opened databases
    Data dt = null;
    for(final Data data : datas) {
      // return database instance with the same name or file path
      final String n = data.meta.name;
      if(Prop.CASE ? n.equals(qi.db) : n.equalsIgnoreCase(qi.db) ||
          IO.get(data.meta.original).eq(qi.input)) {
        dt = data;
        break;
      }
    }

    // open new database, or create new instance
    if(dt == null) dt = open(qi);
    if(dt == null) dt = create(qi, false, baseIO, info);
    return DBNodeSeq.get(dt.resources.docs(qi.path), dt, true, qi.path.isEmpty());
  }

  /**
   * Returns a reference to the updates.
   * @return updates
   */
  public Updates updates() {
    if(updates == null) updates = new Updates();
    return updates;
  }

  /**
   * Returns the module loader.
   * @return module loader
   */
  public ModuleLoader modules() {
    if(modules == null) modules = new ModuleLoader(qc.context);
    return modules;
  }

  /**
   * Removes and closes a database if it has not been added by the global context.
   * @param name name of database to be removed
   */
  public void remove(final String name) {
    final int ds = datas.size();
    for(int d = globalData ? 1 : 0; d < ds; d++) {
      final Data data = datas.get(d);
      if(data.meta.name.equals(name)) {
        Close.close(data, qc.context);
        datas.remove(d);
        break;
      }
    }
  }

  /**
   * Returns the globally opened database.
   * @return database or {@code null} if no database is globally opened
   */
  Data globalData() {
    return globalData ? datas.get(0) : null;
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
    throw WHICHRES_X.get(info, in);
  }

  // TEST APIS ====================================================================================

  /**
   * Adds a document with the specified path. Only called from the test APIs.
   * @param name document identifier (may be {@code null})
   * @param path documents path
   * @param baseIO base URI
   * @throws QueryException query exception
   */
  public void addDoc(final String name, final String path, final IO baseIO) throws QueryException {
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
    texts.put(uri, strings);
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

  // PRIVATE METHODS ==============================================================================

  /**
   * Tries to open the addressed database, or returns {@code null}.
   * @param input query input
   * @return data reference
   */
  private Data open(final QueryInput input) {
    if(input.db != null) {
      try {
        // try to open database
        return addData(Open.open(input.db, qc.context));
      } catch(final IOException ex) { Util.debug(ex); }
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
    final Context context = qc.context;

    // do not check input if no read permissions are given
    if(!qc.context.user.has(Perm.READ))
      throw BXXQ_PERM_X.get(info, Util.info(Text.PERM_REQUIRED_X, Perm.READ));

    // check if input is an existing file
    final IO source = checkPath(input, baseIO, info);
    if(single && source.isDir()) WHICHRES_X.get(info, baseIO);

    // overwrite parsing options with default values
    final MainOptions mo = new MainOptions(context.options);
    try {
      final boolean fc = mo.get(MainOptions.FORCECREATE);
      return addData(CreateDB.create(source.dbname(), new DirParser(source, mo), context, !fc));
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    } finally {
      input.path = "";
    }
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
    throw (docs.isEmpty() ? BXDB_NODOC_X : BXDB_SINGLE_X).get(info, qi.original);
  }

  /**
   * Adds a data reference.
   * @param data data reference to be added
   * @return argument
   */
  private Data addData(final Data data) {
    datas.add(data);
    return data;
  }

  /**
   * Adds a collection to the global collection list.
   * @param coll documents of collection
   * @param name collection name
   */
  private void addCollection(final Value coll, final String name) {
    colls.add(coll);
    collNames.add(name);
  }
}
