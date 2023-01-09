package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;
import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.index.resource.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Service handling the various WebDAV operations.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Dimitar Popov
 */
final class WebDAVService {
  /** Static WebDAV character map. */
  private static final String WEBDAV;

  static {
    final StringBuilder sb = new StringBuilder();
    add(160, sb);
    for(int cp = 8192; cp <= 8207; cp++) add(cp, sb);
    for(int cp = 8232; cp <= 8239; cp++) add(cp, sb);
    for(int cp = 8287; cp <= 8303; cp++) add(cp, sb);
    WEBDAV = sb.toString();
  }

  /** HTTP connection. */
  final HTTPConnection conn;
  /** Session. */
  private LocalSession ls;

  /**
   * Constructor.
   * @param conn HTTP connection
   */
  WebDAVService(final HTTPConnection conn) {
    this.conn = conn;
  }

  /**
   * Closes an open session.
   */
  void close() {
    if(ls != null) ls.close();
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
    final WebDAVQuery query = new WebDAVQuery(_DB_EXISTS.args(" $db")).variable("db", db);
    return query.execute(session()).equals(Text.TRUE);
  }

  /**
   * Retrieves the last modified timestamp of a database.
   * @param db database
   * @return timestamp in milliseconds
   * @throws IOException I/O exception
   */
  String timestamp(final String db) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(DATA.args(_DB_INFO.args(" $db") +
        "/descendant::timestamp[1]")).variable("db", db);
    return query.execute(session());
  }

  /**
   * Retrieves metadata about the resource at the given path.
   * @param db database
   * @param path resource path
   * @return resource meta data
   * @throws IOException I/O exception
   */
  private WebDAVMetaData metaData(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(
      "let $a := " + _DB_LIST_DETAILS.args(" $db", " $path") + "[1] " +
      "return string-join(($a, $a/(@type, @content-type, @modified-date, @size)), '\t')");
    query.variable("db", db);
    query.variable("path", path);

    final String[] result = Strings.split(query.execute(session()), '\t');
    return new WebDAVMetaData(db, stripLeadingSlash(result[0]), result[1], result[2],
        result[3], null);
  }

  /**
   * Deletes a document or folder.
   * @param db database
   * @param path path
   * @throws IOException I/O exception
   */
  void remove(final String db, final String path) throws IOException {
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
      "let $type := " + _DB_TYPE.args(" $db", " $path") +
      "return if($type = '" + ResourceType.VALUE + "') then " +
        _DB_PUT_VALUE.args(" $tdb", _DB_GET_VALUE.args(" $db", " $path"), " $tpath") +
      "else if($type = '" + ResourceType.BINARY + "') then " +
        _DB_PUT_BINARY.args(" $tdb", _DB_GET_BINARY.args(" $db", " $path"), " $tpath") +
      " else " + _DB_PUT.args(" $tdb", _DB_GET.args(" $db", " $path"), " $tpath"));
    query.variable("db", db);
    query.variable("path", path);
    query.variable("tdb", tdb);
    query.variable("tpath", tpath);
    query.execute(session());
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
      "for $d in " + _DB_LIST.args(" $db", " $path") +
      "let $t := $tpath ||'/'|| substring($d, string-length($path) + 1) return " +
      "let $type := " + _DB_TYPE.args(" $db", " $d") +
      "return if($type = '" + ResourceType.VALUE + "') then " +
        _DB_PUT_VALUE.args(" $tdb", _DB_GET_VALUE.args(" $db", " $d"), " $t") +
      "else if($type = '" + ResourceType.BINARY + "') then " +
        _DB_PUT_BINARY.args(" $tdb", _DB_GET_BINARY.args(" $db", " $d"), " $t") +
      " else " + _DB_PUT.args(" $tdb", _DB_GET.args(" $db", " $d"), " $t"));
    query.variable("db", db);
    query.variable("path", path);
    query.variable("tdb", tdb);
    query.variable("tpath", tpath);
    query.execute(session());
  }

  /**
   * Writes a file to the specified output stream.
   * @param db database
   * @param path path
   * @param type resource type
   * @param out output stream
   * @throws IOException I/O exception
   */
  void get(final String db, final String path, final String type, final OutputStream out)
      throws IOException {

    session().setOutputStream(out);
    final Function func = type.equals("xml") ? _DB_GET :
      type.equals("binary") ? _DB_GET_BINARY : _DB_GET_VALUE;
    final WebDAVQuery query = new WebDAVQuery(func.args(" $db", " $path"),
      SerializerOptions.USE_CHARACTER_MAPS.arg(WEBDAV));
    query.variable("db", db);
    query.variable("path", path);
    query.execute(session());
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
    session().execute(new AlterDB(old, dbName(db)));
  }

  /**
   * Copies the database with the given name.
   * @param old database name
   * @param db new database name
   * @throws IOException I/O exception
   */
  void copyDb(final String old, final String db) throws IOException {
    session().execute(new Copy(old, dbName(db)));
  }

  /**
   * Lists the direct children of a path.
   * @param db database
   * @param path path
   * @return children
   * @throws IOException I/O exception
   */
  List<WebDAVResource> list(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery("string-join(" +
      _DB_DIR.args(" $db", " $path") + " ! (string(), name() = 'dir', " +
      "for $a in ('type', 'content-type', 'modified-date', 'size') " +
      "return string(@*[name() = $a]))" + ", '\t')");
    query.variable("db", db);
    query.variable("path", path);

    final String[] result = Strings.split(query.execute(session()), '\t');
    final List<WebDAVResource> ch = new ArrayList<>();
    final int rs = result.length - 5;
    for(int r = 0; r < rs; r += 6) {
      final String name = result[r];
      final boolean dir = Boolean.parseBoolean(result[r + 1]);
      // check if document or folder
      final String pth = path + SEP + name;
      if(dir) {
        ch.add(WebDAVFactory.folder(this, new WebDAVMetaData(db, pth, result[r + 4])));
      } else if(!name.equals(DUMMY)) {
        ch.add(WebDAVFactory.file(this, new WebDAVMetaData(db, pth, result[r + 2], result[r + 3],
            result[r + 4], result[r + 5])));
      }
    }
    return ch;
  }

  /**
   * Lists all databases.
   * @return a list of database resources
   * @throws IOException I/O exception
   */
  List<WebDAVResource> listDbs() throws IOException {
    final WebDAVQuery query = new WebDAVQuery(STRING_JOIN.args(
        _DB_LIST_DETAILS.args() + " ! (string(), @modified-date)", _STRING_TAB.args()));

    final String[] result = Strings.split(query.execute(session()), '\t');
    final List<WebDAVResource> dbs = new ArrayList<>();
    final int rs = result.length - 1;
    for(int r = 0; r < rs; r += 2) {
      final String name = result[r];
      final String ms = result[r + 1];
      dbs.add(WebDAVFactory.database(this, new WebDAVMetaData(name, ms)));
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
    final WebDAVQuery query = new WebDAVQuery(EXISTS.args(_DB_LIST.args(" $db", " $path")));
    query.variable("db", db);
    query.variable("path", path);
    return query.execute(session()).equals(Text.TRUE);
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param db name of database
   * @param path resource path
   * @return {@code true} if there are resources with the name
   * @throws IOException I/O exception
   */
  private boolean exists(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery(_DB_EXISTS.args(" $db", " $path"));
    query.variable("db", db);
    query.variable("path", path);
    return query.execute(session()).equals(Text.TRUE);
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
  private WebDAVResource put(final String db, final String path, final InputStream in)
      throws IOException {

    final LocalSession session = session();
    session.execute(new Open(db));
    session.put(path, in);
    return WebDAVFactory.file(this, new WebDAVMetaData(db, path, ResourceType.XML.toString(),
        MediaType.APPLICATION_XML.toString(), timestamp(db), null));
  }

  /**
   * Adds a binary file with the specified name to the given path.
   * @param db database
   * @param path path where the file will be stored
   * @param in data stream
   * @return object representing the newly added file
   * @throws IOException I/O exception
   */
  private WebDAVResource putBinary(final String db, final String path, final InputStream in)
      throws IOException {

    final LocalSession session = session();
    session.execute(new Open(db));
    session.putBinary(path, in);
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
    try(BufferInput bi = new BufferInput(in)) {
      // guess the content type from the first character
      if(peek(bi) == '<') {
        try {
          // add input as XML document
          return db == null ? createDb(dbName(path), bi) : put(db, path, bi);
        } catch(final IOException ex) {
          // reset stream if it did not work out
          Util.debug(ex);
          try {
            bi.reset();
          } catch(final IOException e) {
            Util.debug(e);
            // throw original exception if input cannot be reset
            throw ex;
          }
        }
      }

      // add input as binary (do this also if an error occurred, and if the stream could be reset)
      final String d;
      if(db == null) {
        d = dbName(path);
        createDb(d);
      } else {
        d = db;
      }
      return putBinary(d, path, bi);
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
    session.putBinary(path + SEP + DUMMY, new ArrayInput(Token.EMPTY));
  }

  /**
   * Constructor.
   * @return local session
   */
  private LocalSession session() {
    if(ls == null) ls = new LocalSession(conn.context);
    return ls;
  }

  /**
   * Adds a character mapping to the specified string builder.
   * @param ch character to be added
   * @param sb string builder
   */
  private static void add(final int ch, final StringBuilder sb) {
    if(sb.length() > 0) sb.append(',');
    sb.append((char) ch).append("=&amp;#").append(ch).append(';');
  }
}
