package org.basex.http.webdav.impl;

import static org.basex.http.webdav.impl.Utils.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.query.func.Function.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.List;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.in.*;
import org.basex.query.func.db.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Service handling the various WebDAV operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 * @param <T> the type of resource
 */
public final class WebDAVService<T> {
  /** Name of the database with the WebDAV locks. */
  private static final String WEBDAV_DB = "~webdav";
  /** HTTP context. */
  private final HTTPContext http;
  /** Resource factory. */
  private final ResourceMetaDataFactory<T> factory;
  /** Locking service. */
  public final WebDAVLockService locking;
  /** Session. */
  private LocalSession ls;

  /**
   * Constructor.
   * @param f resource factory
   * @param h http context
   */
  public WebDAVService(final ResourceMetaDataFactory<T> f, final HTTPContext h) {
    factory = f;
    http = h;
    locking = new WebDAVLockService(http);
  }

  /**
   * Closes an open session.
   */
  public void close() {
    if(ls != null) ls.close();
  }

  /**
   * Authenticates the user with the given password.
   * @param user user name
   * @param pass password
   * @throws LoginException if the login is invalid
   */
  public void authenticate(final String user, final String pass) throws LoginException {
    http.credentials(user, pass);
    http.authenticate();
  }

  /**
   * Checks if the user is authorized to perform the given action.
   * @param db database
   * @return {@code true} if the user is authorized
   */
  public static boolean authorize(final String db) {
    return !WEBDAV_DB.equals(db);
  }

  /**
   * Checks a folder for a dummy document and delete it.
   * @param db database
   * @param path path
   * @throws IOException I/O exception
   */
  public void deleteDummy(final String db, final String path) throws IOException {
    final String dummy = path + SEP + DUMMY;
    if(!pathExists(db, dummy)) return;

    // path contains dummy document
    final LocalSession session = session();
    session.execute(new Open(db));
    session.execute(new Delete(dummy));
  }

  /**
   * Checks if the specified database exists.
   * @param db database to be found
   * @return result of check
   * @throws IOException I/O exception
   */
  public boolean dbExists(final String db) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(_DB_LIST.args() + "[. = $db]").bind("db", db);
    return !execute(query).isEmpty();
  }

  /**
   * Retrieves the last modified timestamp of a database.
   * @param db database
   * @return timestamp in milliseconds.
   * @throws IOException I/O exception
   */
  public long timestamp(final String db) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(DATA.args(_DB_INFO.args("$path") +
        "/descendant::" + DbFn.toName(Text.TIMESTAMP) + "[1]")).bind("path",  db);

    try {
      // retrieve and parse timestamp
      return DateTime.parse(execute(query).get(0), DateTime.DATETIME).getTime();
    } catch(final ParseException ex) {
      Util.errln(ex);
      return 0L;
    }
  }

  /**
   * Retrieves meta data about the resource at the given path.
   * @param db database
   * @param path resource path
   * @return resource meta data
   * @throws IOException I/O exception
   */
  private ResourceMetaData metaData(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(
      "let $a := " + _DB_LIST_DETAILS.args("$db", "$path") +
      "return (" +
      "string($a/@raw)," +
      "string($a/@content-type)," +
      "string($a/@modified-date)," +
      "string($a/@size)," +
      "string($a))");
    query.bind("db", db);
    query.bind("path", path);

    final StringList result = execute(query);
    final boolean raw  = Boolean.parseBoolean(result.get(0));
    final String ctype = result.get(1);
    final long mod     = DateTime.parse(result.get(2));
    final Long size    = raw ? Long.valueOf(result.get(3)) : null;
    final String pth   = stripLeadingSlash(result.get(4));
    return new ResourceMetaData(db, pth, mod, raw, ctype, size);
  }

  /**
   * Deletes a document or folder.
   * @param db database
   * @param path path
   * @throws IOException I/O exception
   */
  public void delete(final String db, final String path) throws IOException {
    final LocalSession session = session();
    session.execute(new Open(db));
    session.execute(new Delete(path));

    // create dummy, if parent is an empty folder
    final int ix = path.lastIndexOf(SEP);
    if(ix > 0) createDummy(db, path.substring(0, ix));
  }

  /**
   * Renames a document or folder.
   * @param db database
   * @param path path
   * @param npath new path
   * @throws IOException I/O exception
   */
  public void rename(final String db, final String path, final String npath) throws IOException {
    final LocalSession session = session();
    session.execute(new Open(db));
    session.execute(new Rename(path, npath));

    // create dummy, if old parent is an empty folder
    final int i1 = path.lastIndexOf(SEP);
    if(i1 > 0) createDummy(db, path.substring(0, i1));

    // delete dummy, if new parent is an empty folder
    final int i2 = npath.lastIndexOf(SEP);
    if(i2 > 0) deleteDummy(db, npath.substring(0, i2));
  }

  /**
   * Copies a document to the specified target.
   * @param db source database
   * @param path source path
   * @param tdb target database
   * @param tpath target path
   * @throws IOException I/O exception
   */
  public void copyDoc(final String db, final String path, final String tdb, final String tpath)
      throws IOException {

    final WebDAVQuery query = new WebDAVQuery(
      "declare option db:chop 'false';" +
      "if(" + _DB_IS_RAW.args("$db", "$path") + ')' +
      " then " + _DB_STORE.args("$tdb", "$tpath", _DB_RETRIEVE.args("$db", "$path")) +
      " else " + _DB_ADD.args("$tdb", _DB_OPEN.args("$db", "$path"), "$tpath"));
    query.bind("db", db);
    query.bind("path", path);
    query.bind("tdb", tdb);
    query.bind("tpath", tpath);
    execute(query);
  }

  /**
   * Copies all documents in a folder to another folder.
   * @param db source database
   * @param path source path
   * @param tdb target database
   * @param tpath target folder
   * @throws IOException I/O exception
   */
  public void copyAll(final String db, final String path, final String tdb, final String tpath)
      throws IOException {

    final WebDAVQuery query = new WebDAVQuery(
      "declare option db:chop 'false'; " +
      "for $d in " + _DB_LIST.args("$db", "$path") +
      "let $t := $tpath ||'/'|| substring($d, string-length($path) + 1) return " +
      "if(" + _DB_IS_RAW.args("$db", "$d") + ") " +
      "then " + _DB_STORE.args("$tdb", "$t", _DB_RETRIEVE.args("$db", "$d")) +
      " else " + _DB_ADD.args("$tdb", _DB_OPEN.args("$db", "$d"), "$t"));
    query.bind("db", db);
    query.bind("path", path);
    query.bind("tdb", tdb);
    query.bind("tpath", tpath);
    execute(query);
  }

  /**
   * Writes a file to the specified output stream.
   * @param db database
   * @param path path
   * @param raw is the file a raw file
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void retrieve(final String db, final String path, final boolean raw,
      final OutputStream out) throws IOException {

    session().setOutputStream(out);
    final WebDAVQuery query = new WebDAVQuery(
      "declare option output:" + (raw ?
      "method 'raw'; " + _DB_RETRIEVE.args("$db", "$path") :
      "use-character-maps 'webdav'; " + _DB_OPEN.args("$db", "$path")));
    query.bind("db", db);
    query.bind("path", path);
    execute(query);
  }

  /**
   * Creates an empty database with the given name.
   * @param db database name
   * @return object representing the newly created database
   * @throws IOException I/O exception
   */
  public T createDb(final String db) throws IOException {
    session().execute(new CreateDB(db));
    return factory.database(this, new ResourceMetaData(db, timestamp(db)));
  }

  /**
   * Drops the database with the given name.
   * @param db database name
   * @throws IOException I/O exception
   */
  public void dropDb(final String db) throws IOException {
    session().execute(new DropDB(db));
  }

  /**
   * Renames the database with the given name.
   * @param db database name
   * @param n new name
   * @throws IOException I/O exception
   */
  public void renameDb(final String db, final String n) throws IOException {
    session().execute(new AlterDB(db, n));
  }

  /**
   * Copies the database with the given name.
   * @param db database name
   * @param n new database name
   * @throws IOException I/O exception
   */
  public void copyDb(final String db, final String n) throws IOException {
    session().execute(new Copy(db, n));
  }

  /**
   * Lists the direct children of a path.
   * @param db database
   * @param path path
   * @return children
   * @throws IOException I/O exception
   */
  public List<T> list(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(
      _DB_LIST_DETAILS.args("$db", "$path") + " ! (" +
      "string(@raw), string(@content-type), string(@modified-date), string(@size)," +
      SUBSTRING_AFTER.args("text()", "$path") + ')');
    query.bind("db", db);
    query.bind("path", path);
    final StringList result = execute(query);
    final int rs = result.size();

    final HashSet<String> paths = new HashSet<>();
    final List<T> ch = new ArrayList<>(rs / 5);
    for(int r = 0; r < rs; r += 5) {
      final boolean raw  = Boolean.parseBoolean(result.get(r));
      final String ctype = result.get(r + 1);
      final long mod     = DateTime.parse(result.get(r + 2));
      final Long size    = raw ? Long.valueOf(result.get(r + 3)) : null;
      final String pth   = stripLeadingSlash(result.get(r + 4));
      final int ix       = pth.indexOf(SEP);
      // check if document or folder
      if(ix < 0) {
        if(!pth.equals(DUMMY)) ch.add(factory.file(this,
            new ResourceMetaData(db, path + SEP + pth, mod, raw, ctype, size)));
      } else {
        final String dir = path + SEP + pth.substring(0, ix);
        if(paths.add(dir)) ch.add(factory.folder(this, new ResourceMetaData(db, dir, mod)));
      }
    }
    return ch;
  }

  /**
   * Lists all databases.
   * @return a list of database resources.
   * @throws IOException I/O exception
   */
  public List<T> listDbs() throws IOException {
    final WebDAVQuery query = new WebDAVQuery(
        _DB_LIST_DETAILS.args() + "[. != $db] ! (text(), @modified-date/data())");
    query.bind("db", WEBDAV_DB);

    final StringList result = execute(query);
    final int rs = result.size();
    final List<T> dbs = new ArrayList<>(rs >>> 1);
    for(int r = 0; r < rs; r += 2) {
      final String name = result.get(r);
      final long mod = DateTime.parse(result.get(r + 1));
      dbs.add(factory.database(this, new ResourceMetaData(name, mod)));
    }
    return dbs;
  }

  /**
   * Creates a folder at the given path.
   * @param db database
   * @param path path
   * @param name new folder name
   * @return new folder resource
   * @throws IOException I/O exception
   */
  public T createFolder(final String db, final String path, final String name) throws IOException {
    deleteDummy(db, path);
    final String newFolder = path + SEP + name;
    createDummy(db, newFolder);
    return factory.folder(this, new ResourceMetaData(db, newFolder, timestamp(db)));
  }

  /**
   * Gets the resource at the given path.
   * @param db database
   * @param path path
   * @return resource
   * @throws IOException I/O exception
   */
  public T resource(final String db, final String path) throws IOException {
    return exists(db, path) ?
      factory.file(this, metaData(db, path)) :
      pathExists(db, path) ?
        factory.folder(this, new ResourceMetaData(db, path, timestamp(db))) :
        null;
  }

  /**
   * Adds the given file to the specified path.
   * @param db database
   * @param path path
   * @param name file name
   * @param in file content
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  public T createFile(final String db, final String path, final String name,
    final InputStream in) throws IOException {

    final LocalSession session = session();
    session.execute(new Open(db));
    final String dbp = path.isEmpty() ? name : path + SEP + name;
    // delete old resource if it already exists
    if(pathExists(db, dbp)) {
      session.execute(new Open(db));
      session.execute(new Delete(dbp));
    } else {
      // otherwise, delete dummy file
      deleteDummy(db, path);
    }
    return addFile(db, dbp, in);
  }

  /**
   * Creates a new database from the given file.
   * @param n file name
   * @param in file content
   * @return object representing the newly created database
   * @throws IOException I/O exception
   */
  public T createFile(final String n, final InputStream in) throws IOException {
    return addFile(null, n, in);
  }

  /**
   * Checks if any of the resources starts with the given path.
   * @param db name of database
   * @param path path
   * @return {@code true} if there are resources with the given prefix
   * @throws IOException I/O exception
   */
  private boolean pathExists(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(EXISTS.args(_DB_LIST.args("$db", "$path")));
    query.bind("db", db);
    query.bind("path", path);
    return execute(query).get(0).equals(Text.TRUE);
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param db name of database
   * @param path resource path
   * @return {@code true} if there are resources with the name
   * @throws IOException I/O exception
   */
  private boolean exists(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(_DB_EXISTS.args("$db", "$path"));
    query.bind("db", db);
    query.bind("path", path);
    return execute(query).get(0).equals(Text.TRUE);
  }

  /**
   * Creates a database with the given name and add the given document.
   * @param db database name
   * @param in data stream
   * @return object representing the newly created database
   * @throws IOException I/O exception
   */
  private T createDb(final String db, final InputStream in) throws IOException {
    session().create(db, in);
    return factory.database(this, new ResourceMetaData(db, timestamp(db)));
  }

  /**
   * Adds a document with the specified name to the given path.
   * @param db database
   * @param path path where the document will be added
   * @param in data stream
   * @return object representing the newly added XML
   * @throws IOException I/O exception
   */
  private T addXML(final String db, final String path, final InputStream in) throws IOException {
    final LocalSession session = session();
    session.execute(new Set(MainOptions.CHOP, false));
    session.execute(new Open(db));
    session.add(path, in);
    return factory.file(this, new ResourceMetaData(db, path, timestamp(db), false, APP_XML, null));
  }

  /**
   * Adds a binary file with the specified name to the given path.
   * @param db database
   * @param path path where the file will be stored
   * @param in data stream
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  private T store(final String db, final String path, final InputStream in) throws IOException {
    final LocalSession session = session();
    session.execute(new Open(db));
    session.store(path, in);
    return factory.file(this, metaData(db, path));
  }

  /**
   * Adds a file in to the given path.
   * @param db database
   * @param path path
   * @param in file content
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  private T addFile(final String db, final String path, final InputStream in) throws IOException {
    // use 4MB as buffer input
    try(final BufferInput bi = new BufferInput(in, 1 << 22)) {
      // guess the content type from the first character
      if(peek(bi) == '<') {
        try {
          // add input as XML document
          return db == null ? createDb(dbname(path), bi) : addXML(db, path, bi);
        } catch(final IOException ex) {
          // reset stream if it did not work out
          try {
            bi.reset();
          } catch(final IOException e) {
            // throw original exception if input cannot be reset
            throw ex;
          }
        }
      }

      // add input as raw file
      final String d;
      if(db == null) {
        d = dbname(path);
        createDb(d);
      } else {
        d = db;
      }
      return store(d, path, bi);
    }
  }

  /**
   * Checks if a folder is empty and create a dummy document.
   * @param db database
   * @param path path
   * @throws IOException I/O exception
   */
  private void createDummy(final String db, final String path) throws IOException {
    // check if path is a folder and is empty
    if(path.matches("[^/]") || pathExists(db, path)) return;

    final LocalSession session = session();
    session.execute(new Open(db));
    session.store(path + SEP + DUMMY, new ArrayInput(Token.EMPTY));
  }

  /**
   * Executes a query.
   * @param query query to be executed
   * @return result items
   * @throws IOException error during query execution
   */
  private StringList execute(final WebDAVQuery query) throws IOException {
    final LocalQuery lq = session().query(query.toString());
    for(final Entry<String, Object> entry : query.entries()) {
      lq.bind(entry.getKey(), entry.getValue());
    }
    final StringList sl = new StringList();
    while(lq.more()) sl.add(lq.next());
    return sl;
  }

  /**
   * Constructor.
   * @return local session
   * @throws LoginException login exception
   */
  private LocalSession session() throws LoginException {
    if(ls == null) ls = new LocalSession(http.authenticate(), http.user, http.pass);
    return ls;
  }
}
