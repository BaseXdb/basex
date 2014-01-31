package org.basex.http.webdav.impl;

import static org.basex.http.webdav.impl.Utils.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.query.func.Function.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.in.*;
import org.basex.query.func.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Service handling the various WebDAV operations.
 * @author BaseX Team 2005-13, BSD License
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
   * Authenticates the user with the given password.
   * @param user user name
   * @param pass password
   * @throws IOException if the login is invalid
   */
  public void authenticate(final String user, final String pass) throws IOException {
    http.credentials(user, pass);
    http.authenticate();
  }

  /**
   * Checks if the user is authorized to perform the given action.
   * @param user user name
   * @param action action
   * @param db database
   * @param path path
   * @return {@code true} if the user is authorized
   */
  @SuppressWarnings("unused")
  public boolean authorize(final String user, final String action, final String db,
      final String path) {
    return !WEBDAV_DB.equals(db);
  }

  /**
   * Returns an HTTP session.
   * @return session
   * @throws LoginException login exception
   */
  private LocalSession session() throws LoginException {
    return new LocalSession(http.authenticate(), http.user, http.pass);
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
    final Session session = session();
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
    final Query q = session().query(ext("db") + _DB_LIST.args() + "[. = $db]");
    q.bind("db", db);
    return !q.execute().isEmpty();
  }

  /**
   * Retrieves the last modified timestamp of a database.
   * @param db database
   * @return timestamp in milliseconds.
   * @throws IOException I/O exception
   */
  public long timestamp(final String db) throws IOException {
    final Query q = session().query(ext("path") + DATA.args(_DB_INFO.args("$path") +
        "/descendant::" + FNDb.toName(Text.TIMESTAMP) + "[1]"));
    q.bind("path", db);
    try {
      // retrieve and parse timestamp
      return DateTime.parse(q.execute(), DateTime.DATETIME).getTime();
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
  ResourceMetaData metaData(final String db, final String path) throws IOException {
    final Query q = session().query(
        ext("db") + ext("path") +
        "let $a := " + _DB_LIST_DETAILS.args("$db", "$path") +
        "return (" +
        "$a/@raw/data()," +
        "$a/@content-type/data()," +
        "$a/@modified-date/data()," +
        "$a/@size/data()," +
        "$a/text())");
    q.bind("db", db);
    q.bind("path", path);
    try {
      final boolean raw = Boolean.parseBoolean(q.next());
      final String ctype = q.next();
      final long mod = DateTime.parse(q.next());
      final Long size = raw ? Long.valueOf(q.next()) : null;
      final String pth = stripLeadingSlash(q.next());
      return new ResourceMetaData(db, pth, mod, raw, ctype, size);
    } finally {
      q.close();
    }
  }

  /**
   * Deletes a document or folder.
   * @param db database
   * @param path path
   * @throws IOException I/O exception
   */
  public void delete(final String db, final String path) throws IOException {
    final Session session = session();
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
    final Session session = session();
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

    final Query q = session().query(
        ext("db") + ext("path") + ext("tdb") + ext("tpath") +
        "declare option db:chop 'false'; " +
        "if(" + _DB_IS_RAW.args("$db", "$path") + ") " +
        " then " + _DB_STORE.args("$tdb", "$tpath", _DB_RETRIEVE.args("$db", "$path")) +
        " else " + _DB_ADD.args("$tdb", _DB_OPEN.args("$db", "$path"), "$tpath"));
    q.bind("db", db);
    q.bind("path", path);
    q.bind("tdb", tdb);
    q.bind("tpath", tpath);
    q.execute();
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

    final Query q = session().query(
        ext("db") + ext("path") + ext("tdb") + ext("tpath") +
        "declare option db:chop 'false'; " +
        "for $d in " + _DB_LIST.args("$db", "$path") +
        "let $t := $tpath ||'/'|| substring($d, string-length($path) + 1) return " +
        "if(" + _DB_IS_RAW.args("$db", "$d") + ") " +
        "then " + _DB_STORE.args("$tdb", "$t", _DB_RETRIEVE.args("$db", "$d")) +
        " else " + _DB_ADD.args("$tdb", _DB_OPEN.args("$db", "$d"), "$t"));
    q.bind("db", db);
    q.bind("path", path);
    q.bind("tdb", tdb);
    q.bind("tpath", tpath);
    q.execute();
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

    final Session session = session();
    session.setOutputStream(out);
    final Query q = session.query(
        ext("db") + ext("path") +
        "declare option output:" + (raw ?
        "method 'raw'; " + _DB_RETRIEVE.args("$db", "$path") :
        "use-character-maps 'webdav'; " + _DB_OPEN.args("$db", "$path")));
    q.bind("db", db);
    q.bind("path", path);
    q.execute();
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
    final List<T> ch = new ArrayList<T>();
    final HashSet<String> paths = new HashSet<String>();
    final Query q = session().query(
        ext("db") + ext("path") +
        _DB_LIST_DETAILS.args("$db", "$path") + " ! (" +
        "@raw/data()," +
        "@content-type/data()," +
        "@modified-date/data()," +
        "@size/data()," +
        SUBSTRING_AFTER.args("text()", "$path") + ')');
    q.bind("db", db);
    q.bind("path", path);
    while(q.more()) {
      final boolean raw = Boolean.parseBoolean(q.next());
      final String ctype = q.next();
      final long mod = DateTime.parse(q.next());
      final Long size = raw ? Long.valueOf(q.next()) : null;
      final String pth = stripLeadingSlash(q.next());
      final int ix = pth.indexOf(SEP);
      // check if document or folder
      if(ix < 0) {
        if(!pth.equals(DUMMY))
          ch.add(factory.file(this, new ResourceMetaData(db, path + SEP + pth, mod,
            raw, ctype, size)));
      } else {
        final String dir = path + SEP + pth.substring(0, ix);
        if(paths.add(dir))
          ch.add(factory.folder(this, new ResourceMetaData(db, dir, mod)));
      }
    }
    q.close();
    return ch;
  }

  /**
   * Lists all databases.
   * @return a list of database resources.
   * @throws IOException I/O exception
   */
  public List<T> listDbs() throws IOException {
    final List<T> dbs = new ArrayList<T>();
    final Query q = session().query(
        _DB_LIST_DETAILS.args() + "[. != '" + WEBDAV_DB + "'] ! " +
        "(text(), @modified-date/data())");
    try {
      while(q.more()) {
        final String name = q.next();
        final long mod = DateTime.parse(q.next());
        dbs.add(factory.database(this, new ResourceMetaData(name, mod)));
      }
    } catch(final Exception ex) {
      Util.errln(ex);
    } finally {
      q.close();
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
    final Session session = session();
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
   * Checks if any of the resources which start with the given path.
   * @param db name of database
   * @param path path
   * @return {@code true} if there are resources with the given prefix
   * @throws IOException I/O exception
   */
  private boolean pathExists(final String db, final String path) throws IOException {
    final Query q = session().query(
        ext("db") + ext("path") + EXISTS.args(_DB_LIST.args("$db", "$path")));
    q.bind("db", db);
    q.bind("path", path);
    return q.execute().equals(Text.TRUE);
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param db name of database
   * @param path resource path
   * @return {@code true} if there are resources with the name
   * @throws IOException I/O exception
   */
  private boolean exists(final String db, final String path) throws IOException {
    final Query q = session().query(
        ext("db") + ext("path") + _DB_EXISTS.args("$db", "$path"));
    q.bind("db", db);
    q.bind("path", path);
    return q.execute().equals(Text.TRUE);
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
    final Session session = session();
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
    final Session session = session();
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
    final BufferInput bi = new BufferInput(in, 1 << 22);
    try {
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
    } finally {
      bi.close();
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

    final Session session = session();
    session.execute(new Open(db));
    session.store(path + SEP + DUMMY, new ArrayInput(Token.EMPTY));
  }

  /**
   * Returns a string with an external variable declaration.
   * @param name name of the variable
   * @return string
   */
  private String ext(final String name) {
    return "declare variable $" + name + " external;";
  }
}
