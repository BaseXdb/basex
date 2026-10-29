package org.basex.query;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.basex.build.*;
import org.basex.build.xml.SAXHandler.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.util.pkg.*;
import org.basex.query.util.regex.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryResources {
  /** Default options. */
  public static final DocOptions DOC_OPTIONS = new DocOptions().seal();
  /** Default options for creating new documents (no external access: fn-level default). */
  private static final MainOptions MAIN_OPTIONS = new MainOptions(DOC_OPTIONS, null).seal();

  /** Database context. */
  private final Context context;
  /** Root query context (used for resources added via the Java API). */
  private final QueryContext root;

  /** Module loader (can be {@code null}). */
  private ModuleLoader modules;
  /** Collections: single nodes and sequences. */
  private final ArrayList<Value> colls = new ArrayList<>(1);
  /** Names of collections. */
  private final ArrayList<String> collNames = new ArrayList<>(1);
  /** URIs of listed directories. */
  private final Map<String, Value> dirs = new HashMap<>();
  /** Indicates if the first database in the context is globally opened. */
  private boolean globalData;

  /** Opened databases (both temporary and persistent ones). */
  private final ArrayList<Data> datas = new ArrayList<>(1);
  /** Original input references of opened databases (created on demand). */
  private final Map<Data, IO> originals = new IdentityHashMap<>();
  /** External resources. */
  private final Map<Class<? extends QueryResource>, QueryResource> external = new HashMap<>();
  /** Function items. */
  private final Map<String, Value> functions = new HashMap<>();
  /** Input references. */
  private final ArrayList<InputStream> inputs = new ArrayList<>(1);

  /** Current directory for file operations (can be {@code null}). */
  public Path currentDir;

  /**
   * Constructor.
   * @param qc query context
   */
  QueryResources(final QueryContext qc) {
    context = qc.context;
    root = qc;
  }

  /**
   * Compiles the resources.
   * @param nodes input node set
   * @return context value
   */
  Value compile(final DBNodes nodes) {
    // add globally opened database
    final Data data = addData(nodes.data());
    synchronized(context.datas) { context.datas.pin(data); }
    globalData = true;

    // create context value; the node type is reused to avoid database lookups
    final boolean all = nodes.all();
    final Value value = DBNodeSeq.get(nodes.pres().clone(), data, nodes.type, all);

    // add default collection. use initial node set if it contains all documents of the database.
    // otherwise, create new node set
    final Value coll = all ? value : DBNodeSeq.get(data.resources.docs(), data, true, true);
    addCollection(coll, data.meta.name);

    return value;
  }

  /**
   * Finalizes data instances.
   */
  void close() {
    for(final Data data : datas) Close.close(data, context);
    datas.clear();
    originals.clear();
    // close dynamically loaded JAR files
    if(modules != null) modules.close();
    modules = null;
    // close external resources
    for(final QueryResource c : external.values()) c.close();
    external.clear();
    // close input resources
    for(final InputStream is : inputs) {
      try {
        is.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
    inputs.clear();
  }

  /**
   * Returns the globally opened database.
   * @return database or {@code null} if no database is globally opened
   */
  Data globalData() {
    return globalData ? datas.getFirst() : null;
  }

  /**
   * Returns or creates an external resource of the specified class.
   * @param <R> resource
   * @param resource external resource
   * @return resource
   */
  @SuppressWarnings("unchecked")
  public synchronized <R extends QueryResource> R index(final Class<? extends R> resource) {
    QueryResource value = external.get(resource);
    if(value == null) {
      try {
        value = resource.getDeclaredConstructor().newInstance();
        external.put(resource, value);
      } catch(final Throwable ex) {
        throw Util.notExpected(ex);
      }
    }
    return (R) value;
  }

  /**
   * Adds an input stream reference.
   * @param input input stream
   */
  public synchronized void add(final InputStream input) {
    inputs.add(input);
  }

  /**
   * Closes and removes an input stream reference.
   * @param input input stream
   * @throws IOException I/O exception
   */
  public synchronized void remove(final InputStream input) throws IOException {
    inputs.remove(input);
    input.close();
  }

  /**
   * Returns inspected functions.
   * @param path path to module
   * @return inspected functions
   */
  public synchronized Value functions(final String path) {
    return functions.get(path);
  }

  /**
   * Adds inspected functions.
   * @param path path to module
   * @param funcs functions
   */
  public synchronized void addFunctions(final String path, final Value funcs) {
    functions.put(path, funcs);
  }

  /**
   * Opens a new database or returns a reference to an already opened database.
   * @param name name of database
   * @param qc query context
   * @param updating updating access
   * @param info input info (can be {@code null})
   * @return database instance
   * @throws QueryException query exception
   */
  public synchronized Data database(final String name, final QueryContext qc,
      final boolean updating, final InputInfo info) throws QueryException {

    final boolean mainmem = context.options.get(MainOptions.MAINMEM);
    final Perm perm = updating ? Perm.WRITE : Perm.READ;
    qc.checkPerm(perm, name, name, info);

    // check if a database with the same name has already been opened
    for(final Data data : datas) {
      // default mode: skip main-memory database instances (which may result from fn:doc calls)
      if(data.inMemory() && !mainmem) continue;
      if(IO.equals(data.meta.name, name)) {
        checkTrusted(data, name, qc, info);
        return data;
      }
    }

    // open and register database
    try {
      return addData(Open.open(name, context, context.options, true, false));
    } catch(final IOException ex) {
      throw DB_GET2_X.get(info, ex);
    }
  }

  /**
   * Evaluates {@code fn:doc()}: opens an existing database document, or creates a new
   * database and node.
   * @param qi query input
   * @param docOpts options used by fn:doc or fn:collection
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param allowExternal allow access to external resources
   * @return document
   * @throws QueryException query exception
   */
  public synchronized DBNode doc(final QueryInput qi, final DocOptions docOpts,
      final QueryContext qc, final InputInfo info, final boolean allowExternal)
      throws QueryException {
    final MainOptions options = context.options;
    final boolean withdb = options.get(MainOptions.WITHDB);
    // favor default database
    if(withdb && options.get(MainOptions.DEFAULTDB)) {
      final Data data = globalData();
      if(data != null) {
        final int pre = doc(data, qi.original);
        if(pre != -1) {
          checkRead(data, qc, info);
          docOpts.checkDbAccess(info);
          return new DBNode(data, pre, Data.DOC);
        }
      }
    }

    // access open database or create new one
    Data data = open(qi, withdb, docOpts, null, qc, info);
    if(data == null) data = create(qi, docOpts, null, qc, info, true, allowExternal);
    // ensure that database contains a single document
    final IntList docs = docs(data, qi.dbPath, DocScope.ALL);
    if(docs.size() == 1) return new DBNode(data, docs.get(0), Data.DOC);
    throw (docs.isEmpty() ? BASEX_DBPATH1_X : BASEX_DBPATH2_X).get(info, qi.original);
  }

  /**
   * Evaluates {@code fn:collection()} and {@code fn:uri-collection()}: returns the resources of
   * an opened or new collection, or the URIs of the files of a directory.
   * @param qi query input (set to {@code null} if default collection is requested)
   * @param options collection options
   * @param uris return URIs of unopened directories
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return resources or URIs
   * @throws QueryException query exception
   */
  public synchronized Value collection(final QueryInput qi, final CollectionOptions options,
      final boolean uris, final QueryContext qc, final InputInfo info) throws QueryException {

    // return default collection
    final boolean withdb = context.options.get(MainOptions.WITHDB);
    if(qi == null) {
      if(!withdb || colls.isEmpty()) throw NODEFCOLL.get(info);
      return colls.getFirst();
    }

    // check if the resource matches the requested form
    final String form = options.get(CollectionOptions.FORM);
    final boolean db = withdb && (form == null || form.equals(CollectionOptions.DATABASE));
    if(form != null && !switch(form) {
      case CollectionOptions.DATABASE -> withdb && qi.dbName != null &&
        context.soptions.dbExists(qi.dbName);
      case CollectionOptions.DIRECTORY -> qi.io instanceof IOFile && qi.io.isDir();
      case CollectionOptions.ARCHIVE -> qi.io != null && qi.io.isArchive();
      default -> false;
    }) throw COLLFORM_X_X.get(info, form, qi.original);

    final DocOptions docOpts = options.xmlOptions();
    final Value value = openCollection(qi, db, docOpts, options, qc, info);
    if(value != null) return value;

    // list files of a directory without parsing them
    if(uris && qi.io instanceof final IOFile dir && dir.isDir()) {
      checkAccess(dir, qi.original, qc, info);
      final String key = dir.path() + ';' + key(docOpts, options);
      final Value cached = dirs.get(key);
      if(cached != null) return cached;
      final ArrayList<IOFile> files =
          parser(dir, options(docOpts, options, false), options, qc, info).files();
      if(files != null) {
        final TokenList list = new TokenList(files.size());
        for(final IOFile file : files) list.add(file.url());
        final Value urls = StrSeq.get(list, BasicType.ANY_URI);
        dirs.put(key, urls);
        return urls;
      }
    }

    final Data data = create(qi, docOpts, options, qc, info, false, false);
    return collection(data, "", DocScope.ALL);
  }

  /**
   * Returns the files of a directory collection that are not parsed as XML documents or archives.
   * @param qi query input
   * @param docOpts options used by fn:collection
   * @param options collection options
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return files
   * @throws QueryException query exception
   */
  public ArrayList<IOFile> others(final QueryInput qi, final DocOptions docOpts,
      final CollectionOptions options, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(!(qi.io instanceof final IOFile dir && dir.isDir()) ||
        CollectionOptions.DATABASE.equals(options.get(CollectionOptions.FORM))) {
      return new ArrayList<>(0);
    }
    checkAccess(dir, qi.original, qc, info);
    return parser(dir, options(docOpts, options, false), options, qc, info).others();
  }

  /**
   * Returns a collection from the default database, an opened collection or an opened database.
   * @param qi query input
   * @param withdb access databases
   * @param docOpts options used by fn:collection
   * @param collOpts collection options
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return collection, or {@code null} if it must be created
   * @throws QueryException query exception
   */
  private Value openCollection(final QueryInput qi, final boolean withdb, final DocOptions docOpts,
      final CollectionOptions collOpts, final QueryContext qc, final InputInfo info)
      throws QueryException {

    final DocScope scope = collOpts.get(CollectionOptions.RECURSIVE) ? DocScope.ALL :
      DocScope.CHILDREN;
    if(withdb) {
      // favor default database
      if(context.options.get(MainOptions.DEFAULTDB)) {
        final Data data = globalData();
        if(data != null) {
          checkRead(data, qc, info);
          return collection(data, qi.original, scope);
        }
      }

      // check currently opened collections (required for tests)
      final String path = qi.io != null ? qi.io.path() : null;
      final int cs = colls.size();
      for(int c = 0; c < cs; c++) {
        if(IO.equals(collNames.get(c), path)) return colls.get(c);
      }
    }

    // access open database
    final Data data = open(qi, withdb, docOpts, collOpts, qc, info);
    if(data == null) return null;
    // instances created by fn:doc and fn:collection were already filtered while being parsed
    return collection(data, qi.dbPath, data.meta.docOpts != null ? DocScope.ALL : scope);
  }

  /**
   * Returns the documents of a collection.
   * @param data data reference
   * @param path database path
   * @param scope scope of the returned documents
   * @return documents
   */
  private static Value collection(final Data data, final String path, final DocScope scope) {
    final IntList docs = docs(data, path, scope);
    return DBNodeSeq.get(docs, data, true, path.isEmpty() && scope == DocScope.ALL);
  }

  /**
   * Returns the document of a database path, or of its percent-decoded variant if there is none.
   * @param data data reference
   * @param path database path
   * @return PRE value of the document node, or {@code -1}
   */
  private static int doc(final Data data, final String path) {
    final int pre = data.resources.doc(path);
    return pre == -1 && decoded(path) instanceof final String dec ? data.resources.doc(dec) : pre;
  }

  /**
   * Returns the documents of a database path, or of its percent-decoded variant if there are none.
   * @param data data reference
   * @param path database path
   * @param scope scope of the returned documents
   * @return PRE values of document nodes
   */
  private static IntList docs(final Data data, final String path, final DocScope scope) {
    final IntList docs = data.resources.docs(path, scope);
    return docs.isEmpty() && decoded(path) instanceof final String dec ?
      data.resources.docs(dec, scope) : docs;
  }

  /**
   * Returns the percent-decoded variant of a database path.
   * @param path database path
   * @return decoded path, or {@code null} if it is identical
   */
  private static String decoded(final String path) {
    if(path.indexOf('%') == -1) return null;
    final String decoded = XMLToken.decodeUri(path);
    return decoded.equals(path) ? null : decoded;
  }

  /**
   * Returns the module loader. Called during parsing.
   * @return module loader
   */
  public ModuleLoader modules() {
    if(modules == null) modules = new ModuleLoader(context);
    return modules;
  }

  /**
   * Removes and closes the specified database. Called during updates.
   * @param name name of database to be removed
   */
  public void remove(final String name) {
    final boolean mainmem = context.options.get(MainOptions.MAINMEM);
    final int ds = datas.size();
    for(int d = globalData ? 1 : 0; d < ds; d++) {
      final Data data = datas.get(d);
      // default mode: skip main-memory database instances (which may result from fn:doc calls)
      if(data.meta.name.equals(name) && !(data.inMemory() && !mainmem)) {
        Close.close(data, context);
        datas.remove(d);
        originals.remove(data);
        break;
      }
    }
  }

  // TEST APIS ====================================================================================

  /** Textual resources. Required for test APIs (can be {@code null}). */
  private Map<String, String[]> texts;
  /** Cached stop word files. Required for test APIs (can be {@code null}). */
  private Map<String, IO> stop;
  /** Cached thesaurus files. Required for test APIs (can be {@code null}). */
  private Map<String, IO> thes;

  /**
   * Returns the document path of a textual resource and its encoding. Only required for test APIs.
   * @param path resource path
   * @param sc static context
   * @return path and encoding or {@code null}
   */
  public String[] text(final String path, final StaticContext sc) {
    if(texts == null) return null;
    final IO io = sc.resolve(path);
    final String[] resource = texts.get(io.path());
    return resource != null ? resource : texts.get(io.name());
  }

  /**
   * Returns stop words. Called during parsing, and only required for test APIs.
   * @param path resource path
   * @param sc static context
   * @return file reference
   */
  IO stopWords(final String path, final StaticContext sc) {
    final IO io = stop != null ? stop.get(path) : null;
    return io != null ? io : sc.resolve(path);
  }

  /**
   * Returns a thesaurus file. Called during parsing, and only required for Test APIs.
   * @param path resource path
   * @param sc static context
   * @return file reference
   */
  IO thesaurus(final String path, final StaticContext sc) {
    final IO io = thes != null ? thes.get(path) : null;
    return io != null ? io : sc.resolve(path);
  }

  /**
   * Adds a document with the specified path. Only called from the test APIs.
   * @param name document identifier (can be {@code null})
   * @param path document path
   * @param sc static context
   * @return data reference
   * @throws QueryException query exception
   */
  public Data addDoc(final String name, final String path, final StaticContext sc)
      throws QueryException {
    final QueryInput qi = new QueryInput(path, sc);
    final Data data = create(qi, DOC_OPTIONS, null, root, null, true, true);
    if(name != null) data.meta.original = name;
    return data;
  }

  /**
   * Adds opened databases. Only called from the test APIs.
   * @param dbs databases
   */
  public void addDatabases(final Collection<Data> dbs) {
    datas.addAll(dbs);
  }

  /**
   * Adds a resource with the specified path. Only called from the test APIs.
   * @param uri resource URI
   * @param strings resource strings (path, encoding)
   */
  public void addText(final String uri, final String... strings) {
    if(texts == null) texts = new HashMap<>();
    texts.put(uri, strings);
  }

  /**
   * Adds a collection with the specified paths. Only called from the test APIs.
   * @param name name of collection (can be empty string)
   * @param paths documents paths
   * @param sc static context
   * @throws QueryException query exception
   */
  public void addCollection(final String name, final String[] paths, final StaticContext sc)
      throws QueryException {

    final ItemList items = new ItemList(paths.length);
    for(final String path : paths) {
      final QueryInput qi = new QueryInput(path, sc);
      final Data data = create(qi, DOC_OPTIONS, null, root, null, false, true);
      items.add(new DBNode(data, 0, Data.DOC));
    }
    addCollection(items.value(NodeType.DOCUMENT), name);
  }

  /**
   * Adds a collection with the specified value. Only called from the test APIs.
   * @param name name of collection (can be empty string)
   * @param value collection value
   */
  public void addCollection(final String name, final Value value) {
    addCollection(value, name);
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

  /**
   * Assigns a sandpit directory.
   * @param path path to assign
   */
  public void sandpit(final Path path) {
    currentDir = path;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns an opened database instance, or opens an existing database.
   * @param qi query input
   * @param withdb access databases
   * @param docOpts options used by fn:doc or fn:collection
   * @param collOpts collection options (can be {@code null})
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return data reference, or {@code null} if a new instance must be created
   * @throws QueryException query exception
   */
  private Data open(final QueryInput qi, final boolean withdb, final DocOptions docOpts,
      final CollectionOptions collOpts, final QueryContext qc, final InputInfo info)
      throws QueryException {

    final String name = withdb ? qi.dbName : null, key = key(docOpts, collOpts);

    // check opened databases
    for(final Data data : datas) {
      final boolean mainmem = data.inMemory();
      if(withdb || mainmem) {
        // compare input path
        final String original = data.meta.original;
        if(!original.isEmpty() && qi.io != null && original(data).eq(qi.io) &&
            key.equals(data.meta.docOpts)) {
          checkTrusted(qi.io, qi.original, qc, info);
          // reset database path: indicates that database includes all files of the original path
          qi.dbPath = "";
          return data;
        }
        // compare database name; favor existing database instances
        if(IO.equals(data.meta.name, name) && (!mainmem || !context.soptions.dbExists(name))) {
          checkRead(data, qc, info);
          docOpts.checkDbAccess(info);
          return data;
        }
      }
    }

    // try to open existing database
    if(name != null) {
      try {
        final Data data = Open.open(name, context, context.options, false, false);
        if(data != null) {
          // check permissions only if the database exists: otherwise, the input is a file path
          try {
            qc.checkPerm(Perm.READ, name, name, info);
            docOpts.checkDbAccess(info);
          } catch(final QueryException ex) {
            Close.close(data, context);
            throw ex;
          }
          return addData(data);
        }
      } catch(final IOException ex) {
        throw IOERR_X.get(info, ex);
      }
    }
    return null;
  }

  /**
   * Returns the key for parsing options of a database instance.
   * @param docOpts options used by fn:doc or fn:collection
   * @param collOpts collection options (can be {@code null})
   * @return key
   */
  private static String key(final DocOptions docOpts, final CollectionOptions collOpts) {
    final String key = docOpts.toString();
    if(collOpts == null) return key;
    final boolean skip = collOpts.get(CollectionOptions.IGNORE_ERRORS);
    final boolean recursive = collOpts.get(CollectionOptions.RECURSIVE);
    final String match = collOpts.get(CollectionOptions.MATCH);
    return !skip && recursive && match == null ? key : key + ';' + skip + ';' + recursive +
      ';' + match + ';' + collOpts.get(CollectionOptions.MATCH_FLAGS);
  }

  /**
   * Checks if the code that accesses an instance created from an external resource is trusted.
   * @param data data reference
   * @param input original input (for error messages)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private void checkTrusted(final Data data, final String input, final QueryContext qc,
      final InputInfo info) throws QueryException {
    // instances created by fn:doc and fn:collection
    if(data.meta.docOpts != null) checkTrusted(original(data), input, qc, info);
  }

  /**
   * Checks if the current user and the accessing code may read the specified database instance.
   * @param data data reference
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private void checkRead(final Data data, final QueryContext qc, final InputInfo info)
      throws QueryException {
    final String name = data.meta.name;
    qc.checkPerm(Perm.READ, name, name, info);
    checkTrusted(data, name, qc, info);
  }

  /**
   * Returns the original input reference of a database instance.
   * @param data data reference
   * @return input reference
   */
  private IO original(final Data data) {
    return originals.computeIfAbsent(data, d -> IO.get(d.meta.original));
  }

  /**
   * Checks if the code that accesses an external resource is trusted.
   * @param io resource
   * @param input original input (for error messages)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void checkTrusted(final IO io, final String input, final QueryContext qc,
      final InputInfo info) throws QueryException {
    if(io.isExternal() && !qc.trusted(info)) {
      throw EXTERNALRESOURCE_X.get(info, input);
    }
  }

  /**
   * Checks if the current user and the accessing code may create an instance from a resource.
   * @param io resource
   * @param input original input (for error messages)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void checkAccess(final IO io, final String input, final QueryContext qc,
      final InputInfo info) throws QueryException {
    checkTrusted(io, input, qc, info);
    qc.checkPerm(io.isExternal() ? Perm.ADMIN : Perm.READ, null, input, info);
  }

  /**
   * Creates a new database instance.
   * @param input query input
   * @param docOpts options used by fn:doc or fn:collection
   * @param collOpts collection options (can be {@code null})
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param single expect single document
   * @param allowExternal allow access to external resources
   * @return data reference
   * @throws QueryException query exception
   */
  private Data create(final QueryInput input, final DocOptions docOpts,
      final CollectionOptions collOpts, final QueryContext qc, final InputInfo info,
      final boolean single, final boolean allowExternal) throws QueryException {

    final IO io = input.io;
    if(io == null) throw WHICHRES_X.get(info, input.original);
    checkAccess(io, input.original, qc, info);

    // check if input points to a single file
    if(!io.exists()) throw WHICHRES_X.get(info, io.path());
    if(single && io instanceof IOFile && io.isDir()) throw RESDIR_X.get(info, io.path());

    final MainOptions options = options(docOpts, collOpts, allowExternal);
    try {
      final DirParser parser = parser(io, options, collOpts, qc, info);
      final Data data = CreateDB.create(io.dbName(), parser, context, options, true);
      data.meta.docOpts = key(docOpts, collOpts);
      // reset database path: indicates that all documents were parsed
      input.dbPath = "";
      if(!docOpts.get(DocOptions.STABLE)) return data;
      return addData(data);
    } catch(final IOException ex) {
      final Throwable th = ex.getCause();
      if(th instanceof TrustedViolationException)
        throw EXTERNALRESOURCE_X.get(info, th.getMessage()).cause(ex);
      throw !(th instanceof ValidationException) ? IOERR_X.get(info, ex) :
        options.get(MainOptions.DTDVALIDATION) ? DTDVALIDATIONERR_X.get(info, ex) :
          XSDVALIDATIONERR_X.get(info, ex);
    }
  }

  /**
   * Returns the parsing options for a new database instance.
   * @param docOpts options used by fn:doc or fn:collection
   * @param collOpts collection options (can be {@code null})
   * @param allowExternal allow access to external resources
   * @return options
   */
  private MainOptions options(final DocOptions docOpts, final CollectionOptions collOpts,
      final boolean allowExternal) {
    final MainOptions mopts = context.options;
    final boolean skip = collOpts != null && collOpts.get(CollectionOptions.IGNORE_ERRORS);
    if(!allowExternal && !skip && docOpts == DOC_OPTIONS &&
        mopts.resolver().standard() && !mopts.get(MainOptions.TRUSTEXTERNAL)) return MAIN_OPTIONS;

    final MainOptions options = new MainOptions(docOpts, mopts);
    if(allowExternal) options.allowExternal(true);
    if(skip) options.set(MainOptions.SKIPCORRUPT, true);
    return options;
  }

  /**
   * Returns a parser for a new database instance.
   * @param io input
   * @param options parsing options
   * @param collOpts collection options (can be {@code null})
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return parser
   * @throws QueryException query exception
   */
  private static DirParser parser(final IO io, final MainOptions options,
      final CollectionOptions collOpts, final QueryContext qc, final InputInfo info)
      throws QueryException {
    final DirParser parser = new DirParser(io, options);
    // skip resources in subdirectories and resources whose URIs do not match
    final RegExpr regExpr = collOpts != null ? collOpts.regExpr(qc, info) : null;
    final boolean recursive = collOpts == null || collOpts.get(CollectionOptions.RECURSIVE);
    if(!recursive || regExpr != null) {
      parser.filter(recursive, regExpr != null ? uri -> regExpr.matcher(uri, qc).find() : null);
    }
    return parser;
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
   * @param name collection name (can be empty string)
   */
  private void addCollection(final Value coll, final String name) {
    colls.add(coll);
    collNames.add(name);
  }
}
