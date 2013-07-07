package org.basex.http.webdav.impl;

import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.cmd.*;
import org.basex.http.HTTPContext;
import org.basex.io.in.ArrayInput;
import org.basex.io.in.BufferInput;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.DateTime;
import org.basex.util.Token;
import org.basex.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.basex.http.webdav.impl.WebDAVLockService.*;
import static org.basex.http.webdav.impl.Utils.*;
import static org.basex.io.MimeTypes.APP_XML;
import static org.basex.query.func.Function.*;

/**
 * Service handling the various WebDAV operations.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 * @param <T> the type of resource
 */
public final class WebDAVService<T> {
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
    http.session().close();
  }

  /**
   * Checks if the user is authorized to perform the given action.
   * @param user user name
   * @param action action
   * @param db database
   * @param p path
   * @return {@code true} if the user is authorized
   */
  @SuppressWarnings("unused")
  public boolean authorize(final String user, final String action, final String db,
      final String p) {
    return !WEBDAV_LOCKS_DB.equals(db);
  }

  /**
   * Checks a folder for a dummy document and delete it.
   * @param db database
   * @param p path
   * @throws IOException I/O exception
   */
  public void deleteDummy(final String db, final String p) throws IOException {
    final String dummy = p + SEP + DUMMY;
    if(!pathExists(db, dummy)) return;

    // path contains dummy document
    final Session session = http.session();
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
    final Query q = http.session().query(_DB_LIST.args() + "[. = $db]");
    q.bind("db", db);
    try {
      return q.more();
    } finally {
      q.close();
    }
  }

  /**
   * Retrieves the last modified timestamp of a database.
   * @param db database
   * @return timestamp in milliseconds.
   * @throws IOException I/O exception
   */
  public long timestamp(final String db) throws IOException {
    final String s = DATA.args(_DB_INFO.args("$p") + "/descendant::" + TIME + "[1]");
    final Query q = http.session().query(s);
    q.bind("p", db);
    try {
      // retrieve and parse timestamp
      return DateTime.parse(q.execute(), DateTime.DATETIME).getTime();
    } catch(final Exception ex) {
      Util.errln(ex);
      return 0L;
    }
  }

  /**
   * Retrieves meta data about the resource at the given path.
   * @param db database
   * @param p resource path
   * @return resource meta data
   * @throws IOException I/O exception
   */
  ResourceMetaData metaData(final String db, final String p) throws IOException {
    final Query q = http.session().query(
        "let $a := " + _DB_LIST_DETAILS.args("$d", "$p") +
        "return (" +
        "$a/@raw/data()," +
        "$a/@content-type/data()," +
        "$a/@modified-date/data()," +
        "$a/@size/data()," +
        "$a/text())");
    q.bind("d", db);
    q.bind("p", p);
    try {
      final boolean raw = Boolean.parseBoolean(q.next());
      final String ctype = q.next();
      final long mod = DateTime.parse(q.next());
      final Long size = raw ? Long.valueOf(q.next()) : null;
      final String path = stripLeadingSlash(q.next());
      final boolean folder = path.lastIndexOf(SEP) <= 0;
      return new ResourceMetaData(db, path, mod, raw, ctype, size, folder);
    } finally {
      q.close();
    }
  }

  /**
   * Deletes a document or folder.
   * @param db database
   * @param p path
   * @throws IOException I/O exception
   */
  public void delete(final String db, final String p) throws IOException {
    final Session session = http.session();
    session.execute(new Open(db));
    session.execute(new Delete(p));

    // create dummy, if parent is an empty folder
    final int ix = p.lastIndexOf(SEP);
    if(ix > 0) createDummy(db, p.substring(0, ix));
  }

  /**
   * Renames a document or folder.
   * @param db database
   * @param p path
   * @param n new name
   * @throws IOException I/O exception
   */
  public void rename(final String db, final String p, final String n) throws IOException {
    final Session session = http.session();
    session.execute(new Open(db));
    session.execute(new Rename(p, n));

    // create dummy, if old parent is an empty folder
    final int i1 = p.lastIndexOf(SEP);
    if(i1 > 0) createDummy(db, p.substring(0, i1));

    // delete dummy, if new parent is an empty folder
    final int i2 = n.lastIndexOf(SEP);
    if(i2 > 0) deleteDummy(db, n.substring(0, i2));
  }

  /**
   * Copies a document to the specified target.
   * @param sdb source database
   * @param spath source path
   * @param tdb target database
   * @param tpath target path
   * @throws IOException I/O exception
   */
  public void copyDoc(final String sdb, final String spath, final String tdb,
      final String tpath) throws IOException {
    final Query q = http.session().query(
        "declare option db:chop 'false'; " +
        "if(" + _DB_IS_RAW.args("$db", "$path") + ") " +
        " then " + _DB_STORE.args("$tdb", "$tpath", _DB_RETRIEVE.args("$db", "$path")) +
        " else " + _DB_ADD.args("$tdb", _DB_OPEN.args("$db", "$path"), "$tpath"));
    q.bind("db", sdb);
    q.bind("path", spath);
    q.bind("tdb", tdb);
    q.bind("tpath", tpath);
    q.execute();
  }

  /**
   * Copies all documents in a folder to another folder.
   * @param sdb source database
   * @param spath source path
   * @param tdb target database
   * @param tpath target folder
   * @throws IOException I/O exception
   */
  public void copyAll(final String sdb, final String spath, final String tdb,
      final String tpath) throws IOException {
    final Query q = http.session().query(
        "declare option db:chop 'false'; " +
        "for $d in " + _DB_LIST.args("$db", "$path") +
        "let $t := $tpath ||'/'|| substring($d, string-length($path) + 1) return " +
        "if(" + _DB_IS_RAW.args("$db", "$d") + ") " +
        "then " + _DB_STORE.args("$tdb", "$t", _DB_RETRIEVE.args("$db", "$d")) +
        " else " + _DB_ADD.args("$tdb", _DB_OPEN.args("$db", "$d"), "$t"));
    q.bind("db", sdb);
    q.bind("path", spath);
    q.bind("tdb", tdb);
    q.bind("tpath", tpath);
    q.execute();
  }

  /**
   * Writes a file to the specified output stream.
   * @param db database
   * @param p path
   * @param raw is the file a raw file
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void retrieve(final String db, final String p, final boolean raw,
      final OutputStream out) throws IOException {
    final Session session = http.session();
    session.setOutputStream(out);
    final Query q = session.query(raw ?
        "declare option output:method 'raw'; " + _DB_RETRIEVE.args("$db", "$path") :
        _DB_OPEN.args("$db", "$path"));
    q.bind("db", db);
    q.bind("path", p);
    q.execute();
  }

  /**
   * Creates an empty database with the given name.
   * @param db database name
   * @return object representing the newly created database
   * @throws IOException I/O exception
   */
  public T createDb(final String db) throws IOException {
    http.session().execute(new CreateDB(db));
    return factory.database(this, new ResourceMetaData(db, timestamp(db)));
  }

  /**
   * Drops the database with the given name.
   * @param db database name
   * @throws IOException I/O exception
   */
  public void dropDb(final String db) throws IOException {
    http.session().execute(new DropDB(db));
  }

  /**
   * Renames the database with the given name.
   * @param db database name
   * @param n new name
   * @throws IOException I/O exception
   */
  public void renameDb(final String db, final String n) throws IOException {
    http.session().execute(new AlterDB(db, n));
  }

  /**
   * Copies the database with the given name.
   * @param db database name
   * @param n new database name
   * @throws IOException I/O exception
   */
  public void copyDb(final String db, final String n) throws IOException {
    http.session().execute(new Copy(db, n));
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
    final Query q = http.session().query(
        "for $a in " + _DB_LIST_DETAILS.args("$d", "$p") +
        "return ($a/@raw/data()," +
        "$a/@content-type/data()," +
        "$a/@modified-date/data()," +
        "$a/@size/data()," +
        SUBSTRING_AFTER.args("$a/text()", "$p") + ')');
    q.bind("d", db);
    q.bind("p", path);
    while(q.more()) {
      final boolean raw = Boolean.parseBoolean(q.next());
      final String ctype = q.next();
      final long mod = DateTime.parse(q.next());
      final Long size = raw ? Long.valueOf(q.next()) : null;
      final String p = stripLeadingSlash(q.next());
      final int ix = p.indexOf(SEP);
      // check if document or folder
      if(ix < 0) {
        if(!p.equals(DUMMY))
          ch.add(factory.file(this, new ResourceMetaData(db, path + SEP + p, mod,
            raw, ctype, size)));
      } else {
        final String dir = path + SEP + p.substring(0, ix);
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
    final Query q = http.session().query(
        "for $d in " + _DB_LIST_DETAILS.args() +
        "where not($d/text() eq '" + WEBDAV_LOCKS_DB + "') " +
        "return ($d/text(), $d/@modified-date/data())");
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
   * @param p path
   * @param n new folder name
   * @return new folder resource
   * @throws IOException I/O exception
   */
  public T createFolder(final String db, final String p, final String n)
      throws IOException {

    deleteDummy(db, p);
    final String newFolder = p + SEP + n;
    createDummy(db, newFolder);
    return factory.folder(this, new ResourceMetaData(db, newFolder, timestamp(db)));
  }

  /**
   * Gets the resource at the given path.
   * @param db database
   * @param p path
   * @return resource
   * @throws IOException I/O exception
   */
  public T resource(final String db, final String p) throws IOException {
    return exists(db, p) ?
      factory.file(this, metaData(db, p)) :
      pathExists(db, p) ?
        factory.folder(this, new ResourceMetaData(db, p, timestamp(db))) :
        null;
  }

  /**
   * Adds the given file to the specified path.
   * @param db database
   * @param p path
   * @param n file name
   * @param in file content
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  public T createFile(final String db, final String p, final String n,
    final InputStream in) throws IOException {
    final Session session = http.session();
    session.execute(new Open(db));
    final String dbp = p.isEmpty() ? n : p + SEP + n;
    // delete old resource if it already exists
    if(pathExists(db, dbp)) session.execute(new Delete(dbp));
      // otherwise, delete dummy file
    else deleteDummy(db, p);
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
   * @param p path
   * @return {@code true} if there are resources with the given prefix
   * @throws IOException I/O exception
   */
  private boolean pathExists(final String db, final String p) throws IOException {
    final Query q = http.session().query(COUNT.args(_DB_LIST.args("$d", "$p")));
    q.bind("d", db);
    q.bind("p", p);
    return !"0".equals(q.execute());
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param db name of database
   * @param p resource path
   * @return {@code true} if there are resources with the name
   * @throws IOException I/O exception
   */
  private boolean exists(final String db, final String p) throws IOException {
    final Query q = http.session().query(_DB_EXISTS.args("$d", "$p"));
    q.bind("d", db);
    q.bind("p", p);
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
    http.session().create(db, in);
    return factory.database(this, new ResourceMetaData(db, timestamp(db)));
  }

  /**
   * Adds a document with the specified name to the given path.
   * @param db database
   * @param p path where the document will be added
   * @param in data stream
   * @return object representing the newly added XML
   * @throws IOException I/O exception
   */
  private T addXML(final String db, final String p, final InputStream in)
      throws IOException {

    final Session session = http.session();
    session.execute(new Set(Prop.CHOP, false));
    session.add(p, in);
    return factory.file(this, new ResourceMetaData(db, p, timestamp(db), false,
      APP_XML, null));
  }

  /**
   * Adds a binary file with the specified name to the given path.
   * @param db database
   * @param p path where the file will be stored
   * @param in data stream
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  private T store(final String db, final String p, final InputStream in)
      throws IOException {

    http.session().store(p, in);
    return factory.file(this, metaData(db, p));
  }

  /**
   * Adds a file in to the given path.
   * @param db database
   * @param p path
   * @param in file content
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  private T addFile(final String db, final String p, final InputStream in)
      throws IOException {

    // use 4MB as buffer input
    final BufferInput bi = new BufferInput(in, 1 << 22);
    try {
      // guess the content type from the first character
      if(peek(bi) == '<') {
        try {
          // add input as XML document
          return db == null ? createDb(dbname(p), bi) : addXML(db, p, bi);
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
        d = dbname(p);
        createDb(d);
      } else {
        d = db;
      }
      return store(d, p, bi);
    } finally {
      bi.close();
    }
  }

  /**
   * Checks if a folder is empty and create a dummy document.
   * @param db database
   * @param p path
   * @throws IOException I/O exception
   */
  private void createDummy(final String db, final String p) throws IOException {
    // check if path is a folder and is empty
    if(p.matches("[^/]") || pathExists(db, p)) return;

    final Session session = http.session();
    session.execute(new Open(db));
    session.store(p + SEP + DUMMY, new ArrayInput(Token.EMPTY));
  }
}
