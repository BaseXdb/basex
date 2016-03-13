package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;
import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.List;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.func.db.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

/**
 * Service handling the various WebDAV operations.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Dimitar Popov
 */
final class WebDAVService {
  /** Name of the database with the WebDAV locks. */
  private static final String WEBDAV_DB = "~webdav";

  /** HTTP context. */
  final HTTPContext http;
  /** Locking service. */
  final WebDAVLockService locking;

  /** Session. */
  private LocalSession ls;

  /**
   * Constructor.
   * @param http http context
   */
  WebDAVService(final HTTPContext http) {
    this.http = http;
    locking = new WebDAVLockService(http);
  }

  /**
   * Closes an open session.
   */
  void close() {
    if(ls != null) ls.close();
  }

  /**
   * Authenticates the user with the given password.
   * @param user user name
   * @param pass password
   * @throws IOException I/O exception
   */
  void authenticate(final String user, final String pass) throws IOException {
    http.credentials(user, pass);
    session();
  }

  /**
   * Checks if the user is authorized to perform the given action.
   * @param db database (can be {@code null})
   * @return {@code true} if the user is authorized
   */
  static boolean authorize(final String db) {
    return !WEBDAV_DB.equals(db);
  }

  /**
   * Checks a folder for a dummy document and delete it.
   * @param db database
   * @param path path
   * @throws IOException I/O exception
   */
  void deleteDummy(final String db, final String path) throws IOException {
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
  boolean dbExists(final String db) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(_DB_EXISTS.args("$db")).bind("db", db);
    return execute(query).equals(Text.TRUE);
  }

  /**
   * Retrieves the last modified timestamp of a database.
   * @param db database
   * @return timestamp in milliseconds.
   * @throws IOException I/O exception
   */
  long timestamp(final String db) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(DATA.args(_DB_INFO.args("$db") +
        "/descendant::" + DbFn.toName(Text.TIMESTAMP) + "[1]")).bind("db",  db);
    return DateTime.parse(execute(query)).getTime();
  }

  /**
   * Retrieves meta data about the resource at the given path.
   * @param db database
   * @param path resource path
   * @return resource meta data
   * @throws IOException I/O exception
   */
  private WebDAVMetaData metaData(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(
      "let $a := " + _DB_LIST_DETAILS.args("$db", "$path") + "[1] " +
      "return string-join(($a/@raw, $a/@content-type, $a/@modified-date, $a/@size, $a),out:tab())");
    query.bind("db", db);
    query.bind("path", path);

    final String[] result = results(query);
    final boolean raw = Boolean.parseBoolean(result[0]);
    final MediaType type = new MediaType(result[1]);
    final long mod = DateTime.parse(result[2]).getTime();
    final Long size = raw ? Long.valueOf(result[3]) : null;
    final String pth = stripLeadingSlash(result[4]);
    return new WebDAVMetaData(db, pth, mod, raw, type, size);
  }

  /**
   * Deletes a document or folder.
   * @param db database
   * @param path path
   * @throws IOException I/O exception
   */
  void delete(final String db, final String path) throws IOException {
    final LocalSession session = session();
    session.execute(new Open(db));
    session.execute(new Delete(path));

    // create dummy if parent is an empty folder
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
  void rename(final String db, final String path, final String npath) throws IOException {
    final LocalSession session = session();
    session.execute(new Open(db));
    session.execute(new Rename(path, npath));

    // create dummy if old parent is an empty folder
    final int i1 = path.lastIndexOf(SEP);
    if(i1 > 0) createDummy(db, path.substring(0, i1));

    // delete dummy if new parent is an empty folder
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
  void copyDoc(final String db, final String path, final String tdb, final String tpath)
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
  void copyAll(final String db, final String path, final String tdb, final String tpath)
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
  void retrieve(final String db, final String path, final boolean raw, final OutputStream out)
      throws IOException {

    session().setOutputStream(out);
    final String string = SerializerOptions.USE_CHARACTER_MAPS.arg("&#xA0;=&amp;#xA0;") +
        (raw ? _DB_RETRIEVE : _DB_OPEN).args("$db", "$path") + "[1]";
    final WebDAVQuery query = new WebDAVQuery(string);
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
  WebDAVResource createDb(final String db) throws IOException {
    session().execute(new CreateDB(db));
    return WebDAVFactory.database(this, new WebDAVMetaData(db, timestamp(db)));
  }

  /**
   * Drops the database with the given name.
   * @param db database name
   * @throws IOException I/O exception
   */
  void dropDb(final String db) throws IOException {
    session().execute(new DropDB(db));
  }

  /**
   * Renames the database with the given name.
   * @param old database name
   * @param db new name
   * @throws IOException I/O exception
   */
  void renameDb(final String old, final String db) throws IOException {
    session().execute(new AlterDB(old, dbname(db)));
  }

  /**
   * Copies the database with the given name.
   * @param old database name
   * @param db new database name
   * @throws IOException I/O exception
   */
  void copyDb(final String old, final String db) throws IOException {
    session().execute(new Copy(old, dbname(db)));
  }

  /**
   * Lists the direct children of a path.
   * @param db database
   * @param path path
   * @return children
   * @throws IOException I/O exception
   */
  List<WebDAVResource> list(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(STRING_JOIN.args(
      _DB_LIST_DETAILS.args("$db", "$path") + " ! (" +
      "@raw,@content-type,@modified-date,@size," + SUBSTRING_AFTER.args("text()", "$path") + ')',
      "out:tab()"));
    query.bind("db", db);
    query.bind("path", path);
    final String[] result = results(query);

    final HashSet<String> paths = new HashSet<>();
    final List<WebDAVResource> ch = new ArrayList<>();
    final int rs = result.length;
    for(int r = 0; r < rs; r += 5) {
      final boolean raw = Boolean.parseBoolean(result[r]);
      final MediaType ctype = new MediaType(result[r + 1]);
      final long mod = DateTime.parse(result[r + 2]).getTime();
      final Long size = raw ? Long.valueOf(result[r + 3]) : null;
      final String pth = stripLeadingSlash(result[r + 4]);
      final int ix = pth.indexOf(SEP);
      // check if document or folder
      if(ix < 0) {
        if(!pth.equals(DUMMY)) ch.add(WebDAVFactory.file(this,
          new WebDAVMetaData(db, path + SEP + pth, mod, raw, ctype, size)));
      } else {
        final String dir = path + SEP + pth.substring(0, ix);
        if(paths.add(dir)) ch.add(WebDAVFactory.folder(this, new WebDAVMetaData(db, dir, mod)));
      }
    }
    return ch;
  }

  /**
   * Lists all databases.
   * @return a list of database resources.
   * @throws IOException I/O exception
   */
  List<WebDAVResource> listDbs() throws IOException {
    final WebDAVQuery query = new WebDAVQuery(STRING_JOIN.args(
        _DB_LIST_DETAILS.args() + "[. != $db] ! (text(), @modified-date)", "out:tab()"));
    query.bind("db", WEBDAV_DB);

    final String[] result = results(query);
    final List<WebDAVResource> dbs = new ArrayList<>();
    final int rs = result.length;
    for(int r = 0; r < rs; r += 2) {
      final String name = result[r];
      final long mod = DateTime.parse(result[r + 1]).getTime();
      dbs.add(WebDAVFactory.database(this, new WebDAVMetaData(name, mod)));
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
  WebDAVResource createFolder(final String db, final String path, final String name)
      throws IOException {

    deleteDummy(db, path);
    final String newFolder = path + SEP + name;
    createDummy(db, newFolder);
    return WebDAVFactory.folder(this, new WebDAVMetaData(db, newFolder, timestamp(db)));
  }

  /**
   * Gets the resource at the given path.
   * @param db database
   * @param path path
   * @return resource
   * @throws IOException I/O exception
   */
  WebDAVResource resource(final String db, final String path) throws IOException {
    return exists(db, path) ?
      WebDAVFactory.file(this, metaData(db, path)) :
      pathExists(db, path) ?
        WebDAVFactory.folder(this, new WebDAVMetaData(db, path, timestamp(db))) :
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
  WebDAVResource createFile(final String db, final String path, final String name,
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
  WebDAVResource createFile(final String n, final InputStream in) throws IOException {
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
    return execute(query).equals(Text.TRUE);
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
    return execute(query).equals(Text.TRUE);
  }

  /**
   * Creates a database with the given name and add the given document.
   * @param db database name
   * @param in data stream
   * @return object representing the newly created database
   * @throws IOException I/O exception
   */
  private WebDAVResource createDb(final String db, final InputStream in) throws IOException {
    session().create(db, in);
    return WebDAVFactory.database(this, new WebDAVMetaData(db, timestamp(db)));
  }

  /**
   * Adds a document with the specified name to the given path.
   * @param db database
   * @param path path where the document will be added
   * @param in data stream
   * @return object representing the newly added XML
   * @throws IOException I/O exception
   */
  private WebDAVResource addXML(final String db, final String path, final InputStream in)
      throws IOException {

    final LocalSession session = session();
    session.execute(new Set(MainOptions.CHOP, false));
    session.execute(new Open(db));
    session.add(path, in);
    return WebDAVFactory.file(this, new WebDAVMetaData(db, path, timestamp(db), false,
      MediaType.APPLICATION_XML, null));
  }

  /**
   * Adds a binary file with the specified name to the given path.
   * @param db database
   * @param path path where the file will be stored
   * @param in data stream
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  private WebDAVResource store(final String db, final String path, final InputStream in)
      throws IOException {

    final LocalSession session = session();
    session.execute(new Open(db));
    session.store(path, in);
    return WebDAVFactory.file(this, metaData(db, path));
  }

  /**
   * Adds a file in to the given path.
   * @param db database
   * @param path path
   * @param in file content
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  private WebDAVResource addFile(final String db, final String path, final InputStream in)
      throws IOException {

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
   * @return result
   * @throws IOException error during query execution
   */
  private String execute(final WebDAVQuery query) throws IOException {
    final XQuery xquery = new XQuery(query.toString());
    for(final Entry<String, String> entry : query.entries()) {
      xquery.bind(entry.getKey(), entry.getValue());
    }
    return session().execute(xquery);
  }

  /**
   * Executes a query and returns all results as a list.
   * @param query query to be executed
   * @return result
   * @throws IOException error during query execution
   */
  private String[] results(final WebDAVQuery query) throws IOException {
    final StringList sl = new StringList();
    for(final String result : Strings.split(execute(query), '\t')) {
      if(!result.isEmpty()) sl.add(result);
    }
    return sl.finish();
  }

  /**
   * Constructor.
   * @return local session
   * @throws IOException I/O exception
   */
  private LocalSession session() throws IOException {
    if(ls == null) ls = new LocalSession(http.context(true));
    return ls;
  }
}
