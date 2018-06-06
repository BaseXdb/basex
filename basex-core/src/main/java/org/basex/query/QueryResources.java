package org.basex.query;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.util.list.*;
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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class QueryResources {
  /** Database context. */
  private final QueryContext qc;

  /** Module loader. */
  private ModuleLoader modules;
  /** Collections: single nodes and sequences. */
  private final ArrayList<Value> colls = new ArrayList<>(1);
  /** Names of collections. */
  private final ArrayList<String> collNames = new ArrayList<>(1);
  /** Indicates if the first database in the context is globally opened. */
  private boolean globalData;

  /** Textual resources. Required for test APIs. */
  private Map<String, String[]> texts;
  /** Cached stop word files. Required for test APIs. */
  private Map<String, IO> stop;
  /** Cached thesaurus files. Required for test APIs. */
  private Map<String, IO> thes;

  /** Opened databases (both temporary and persistent ones). */
  private final ArrayList<Data> datas = new ArrayList<>(1);
  /** External resources. */
  private Map<Class<? extends QueryResource>, QueryResource> external;

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
    // add globally opened database
    final Data data = addData(nodes.data());
    synchronized(qc.context.datas) { qc.context.datas.pin(data); }
    globalData = true;

    // create context value
    final boolean all = nodes.all();
    final Value value = DBNodeSeq.get(new IntList(nodes.pres()), data, all, all);

    // add default collection. use initial node set if it contains all documents of the database.
    // otherwise, create new node set
    final Value coll = all ? value : DBNodeSeq.get(data.resources.docs(), data, true, true);
    addCollection(coll, data.meta.name);

    return value;
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
    if(external != null) {
      for(final QueryResource c : external.values()) c.close();
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
   * Returns or creates an external resource of the specified class.
   * @param <R> resource
   * @param resource external resource
   * @return resource
   */
  @SuppressWarnings("unchecked")
  public synchronized <R extends QueryResource> R index(final Class<? extends R> resource) {
    if(external == null) external = new HashMap<>();
    QueryResource value = external.get(resource);
    if(value == null) {
      try {
        value = resource.newInstance();
        external.put(resource, value);
      } catch(final Throwable ex) {
        throw Util.notExpected(ex);
      }
    }
    return (R) value;
  }

  /**
   * Opens a new database or returns a reference to an already opened database.
   * @param name name of database
   * @param info input info
   * @return database instance
   * @throws QueryException query exception
   */
  public synchronized Data database(final String name, final InputInfo info) throws QueryException {
    final Context ctx = qc.context;
    final boolean mainmem = ctx.options.get(MainOptions.MAINMEM);

    // check if a database with the same name has already been opened
    for(final Data data : datas) {
      // default mode: skip main-memory database instances (which may result from fn:doc calls)
      if(data.inMemory() && !mainmem) continue;
      final String n = data.meta.name;
      if(Prop.CASE ? n.equals(name) : n.equalsIgnoreCase(name)) return data;
    }

    // open and register database
    if(!ctx.perm(Perm.READ, name)) throw BASEX_PERMISSION_X_X.get(info, Perm.READ, name);
    try {
      return addData(Open.open(name, ctx, ctx.options));
    } catch(final IOException ex) {
      throw DB_OPEN2_X.get(info, ex);
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
  public synchronized DBNode doc(final QueryInput qi, final InputInfo info) throws QueryException {
    // favor default database
    Data data = globalData();
    if(data != null && qc.context.options.get(MainOptions.DEFAULTDB)) {
      final int pre = data.resources.doc(qi.original);
      if(pre != -1) return new DBNode(data, pre, Data.DOC);
    }

    // access open database or create new one
    data = data(qi, info, true);
    // ensure that database contains a single document
    final IntList docs = data.resources.docs(qi.dbPath);
    if(docs.size() == 1) return new DBNode(data, docs.get(0), Data.DOC);
    throw (docs.isEmpty() ? BASEX_DBPATH1_X : BASEX_DBPATH2_X).get(info, qi.original);
  }

  /**
   * Evaluates {@code fn:collection()}: opens an existing collection,
   * or creates a new data reference.
   * @param qi query input (set to {@code null} if default collection is requested)
   * @param info input info
   * @return collection
   * @throws QueryException query exception
   */
  public synchronized Value collection(final QueryInput qi, final InputInfo info)
      throws QueryException {

    // return default collection
    if(qi == null) {
      if(colls.isEmpty()) throw NODEFCOLL.get(info);
      return colls.get(0);
    }

    // favor default database
    Data data = globalData();
    if(data != null && qc.context.options.get(MainOptions.DEFAULTDB)) {
      final IntList pres = data.resources.docs(qi.original);
      return DBNodeSeq.get(pres, data, true, qi.original.isEmpty());
    }

    // check currently opened collections (required for tests)
    final int cs = colls.size();
    for(int c = 0; c < cs; c++) {
      final String name = collNames.get(c), path = qi.io.path();
      if(Prop.CASE ? name.equals(path) : name.equalsIgnoreCase(path)) {
        return colls.get(c);
      }
    }

    // access open database or create new one
    data = data(qi, info, false);
    final IntList docs = data.resources.docs(qi.dbPath);
    return DBNodeSeq.get(docs, data, true, qi.dbPath.isEmpty());
  }

  /**
   * Returns the module loader. Called during parsing.
   * @return module loader
   */
  public ModuleLoader modules() {
    if(modules == null) modules = new ModuleLoader(qc.context);
    return modules;
  }

  /**
   * Removes and closes a database. Called during updates.
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
   * Returns the document path of a textual resource and its encoding. Only required for test APIs.
   * @param uri resource uri
   * @return path and encoding or {@code null}
   */
  public String[] text(final String uri) {
    return texts == null ? null : texts.get(uri);
  }

  /**
   * Returns stop words. Called during parsing, and only required for test APIs.
   * @param path resource path
   * @param sc static context
   * @return file reference
   */
  public IO stopWords(final String path, final StaticContext sc) {
    return stop != null ? stop.get(path) : sc.resolve(path, null);
  }

  /**
   * Returns a thesaurus file. Called during parsing, and only required for Test APIs.
   * @param path resource path
   * @param sc static context
   * @return file reference
   */
  public IO thesaurus(final String path, final StaticContext sc) {
    return thes != null ? thes.get(path) : sc.resolve(path, null);
  }

  // TEST APIS ====================================================================================

  /**
   * Adds a document with the specified path. Only called from the test APIs.
   * @param name document identifier (may be {@code null})
   * @param path document path
   * @param sc static context (can be {@code null})
   * @throws QueryException query exception
   */
  public void addDoc(final String name, final String path, final StaticContext sc)
      throws QueryException {
    final QueryInput qi = new QueryInput(path, sc);
    final Data data = create(qi, true, null);
    if(name != null) data.meta.original = name;
  }

  /**
   * Adds a resource with the specified path. Only called from the test APIs.
   * @param uri resource uri
   * @param strings resource strings (path, encoding)
   */
  public void addResource(final String uri, final String... strings) {
    if(texts == null) texts = new HashMap<>();
    texts.put(uri, strings);
  }

  /**
   * Adds a collection with the specified paths. Only called from the test APIs.
   * @param name name of collection (can be empty string)
   * @param paths documents paths
   * @param sc static context (can be {@code null})
   * @throws QueryException query exception
   */
  public void addCollection(final String name, final String[] paths, final StaticContext sc)
      throws QueryException {

    final ItemList items = new ItemList(paths.length);
    for(final String path : paths) {
      final QueryInput qi = new QueryInput(path, sc);
      items.add(new DBNode(create(qi, true, null), 0, Data.DOC));
    }
    addCollection(items.value(NodeType.DOC), name);
  }

  /**
   * Attaches full-text maps. Only called from the test APIs.
   * @param sw stop words
   * @param th thesaurus
   */
  public void ftmaps(final HashMap<String, IO> sw, final HashMap<String, IO> th) {
    stop = sw;
    thes = th;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns an already open database for the specified input or creates a new one.
   * @param qi query input
   * @param info input info
   * @param single single document
   * @return document
   * @throws QueryException query exception
   */
  private Data data(final QueryInput qi, final InputInfo info, final boolean single)
      throws QueryException {

    // check opened databases
    for(final Data data : datas) {
      // compare input path
      final String orig = data.meta.original;
      if(!orig.isEmpty() && IO.get(orig).eq(qi.io)) {
        // reset database path: indicates that database includes all files of the original path
        qi.dbPath = "";
        return data;
      }
      // compare database name
      final String name = data.meta.name, dbName = qi.dbName;
      if(Prop.CASE ? name.equals(dbName) : name.equalsIgnoreCase(dbName)) return data;
    }

    // try to open existing database
    final String name = qi.dbName;
    if(name != null) {
      try {
        final Context ctx = qc.context;
        return addData(Open.open(name, ctx, ctx.options));
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }

    // otherwise, create new instance
    final Data data = create(qi, single, info);
    // reset database path: indicates that all documents were parsed
    qi.dbPath = "";
    return data;

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

    // check if new databases can be created
    final Context context = qc.context;

    // do not check for existence of input if user has no read permissions
    if(!context.user().has(Perm.READ))
      throw XQUERY_PERMISSION1_X.get(info, Util.info(Text.PERM_REQUIRED_X, Perm.READ));

    // check if input points to a single file
    final IO io = input.io;
    if(!io.exists()) throw WHICHRES_X.get(info, io);
    if(single && io.isDir()) throw RESDIR_X.get(info, io);

    // overwrite parsing options with default values
    final boolean mem = !context.options.get(MainOptions.FORCECREATE);
    final MainOptions opts = new MainOptions(context.options, true);
    final Parser parser = new DirParser(io, opts);

    final Data data;
    try {
      data = CreateDB.create(io.dbName(), parser, context, opts, mem);
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
    return addData(data);
  }

  /**
   * Adds a data reference.
   * @param data data reference to be added
   * @return argument
   */
  public Data addData(final Data data) {
    datas.add(data);
    return data;
  }

  /**
   * Adds a collection to the global collection list.
   * @param coll documents of collection
   * @param name collection name (can be empty string)
   */
  private void addCollection(final Value coll, final String name) {
    colls.add(coll);
    collNames.add(name);
  }
}
