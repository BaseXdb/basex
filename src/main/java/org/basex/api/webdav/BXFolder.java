package org.basex.api.webdav;

import static org.basex.query.func.Function.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.io.in.BufferInput;
import org.basex.server.Query;
import org.basex.server.Session;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableCollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.BadRequestException;

/**
 * WebDAV resource representing a folder within a collection database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXFolder extends BXAbstractResource implements FolderResource,
    DeletableCollectionResource {

  /**
   * Constructor.
   * @param d database name
   * @param p path to folder
   * @param m last modified date
   * @param s current session
   */
  public BXFolder(final String d, final String p, final long m,
      final HTTPSession s) {
    super(d, p, m, s);
  }

  @Override
  public Long getContentLength() {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return null;
  }

  @Override
  public Date getCreateDate() {
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
  }

  @Override
  public boolean isLockedOutRecursive(final Request request) {
    return false;
  }

  @Override
  public BXFolder createCollection(final String folder)
      throws BadRequestException {

    return new BXCode<BXFolder>(this) {
      @Override
      public BXFolder get() throws IOException {
        deleteDummy(s, db, path);
        final String newFolder = path + SEP + folder;
        createDummy(s, db, newFolder);
        return folder(s, db, newFolder, session);
      }
    }.eval();
  }

  @Override
  public BXResource child(final String childName) {
    return new BXCode<BXResource>(this) {
      @Override
      public BXResource get() throws IOException {
        return resource(s, db, path + SEP + childName, session);
      }
    }.evalNoEx();
  }

  @Override
  public List<BXResource> getChildren() {
    return new BXCode<List<BXResource>>(this) {
      @Override
      public List<BXResource> get() throws IOException {
        final List<BXResource> ch = new ArrayList<BXResource>();
        final HashSet<String> paths = new HashSet<String>();
        final Query q = s.query(
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
          final long mod = Long.parseLong(q.next());
          final Long size = raw ? Long.valueOf(q.next()) : null;
          final String p = stripLeadingSlash(q.next());
          final int ix = p.indexOf(SEP);
          // check if document or folder
          if(ix < 0) {
            if(!p.equals(DUMMY))
              ch.add(new BXFile(db, path + SEP + p, mod, raw, ctype, size,
                  session));
          } else {
            final String dir = path + SEP + p.substring(0, ix);
            if(paths.add(dir)) ch.add(new BXFolder(db, dir, mod, session));
          }
        }
        q.close();
        return ch;
      }
    }.evalNoEx();
  }

  @Override
  public BXResource createNew(final String newName, final InputStream input,
      final Long length, final String contentType) throws BadRequestException {

    return new BXCode<BXResource>(this) {
      @Override
      public BXResource get() throws IOException {
        s.execute(new Open(db));
        final String dbp = path.isEmpty() ? newName : path + SEP + newName;
        // delete old resource if it already exists
        if(pathExists(s, db, dbp)) s.execute(new Delete(dbp));
        // otherwise, delete dummy file
        else deleteDummy(s, db, path);
        addFile(s, newName, input);
        return file(s, db, dbp, session);
      }
    }.eval();
  }

  /**
   * Adds a file in the current folder.
   * @param s active session
   * @param n file name
   * @param in file content
   * @throws IOException I/O exception
   */
  protected void addFile(final Session s, final String n, final InputStream in)
      throws IOException {

    // use 4MB as buffer input
    final BufferInput bi = new BufferInput(in, 1 << 22);
    try {
      // guess the content type from the first character
      bi.encoding();
      final boolean xml = bi.readChar() == '<';
      try {
        bi.reset();
      } catch(final IOException e) {
      }

      if(xml) {
        try {
          // add input as XML document
          addXML(s, n, bi);
          return;
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
      addRaw(s, n, bi);
    } finally {
      bi.close();
    }
  }

  /**
   * Adds an XML document in the current folder.
   * @param s active session
   * @param n file name
   * @param in file content
   * @throws IOException I/O exception
   */
  protected void addXML(final Session s, final String n, final InputStream in)
      throws IOException {
    s.add(path + SEP + n, in);
  }

  /**
   * Adds a raw file in the current folder.
   * @param s active session
   * @param n file name
   * @param in file content
   * @throws IOException I/O exception
   */
  protected void addRaw(final Session s, final String n, final InputStream in)
      throws IOException {
    s.store(path + SEP + n, in);
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws IOException {
    // folder is copied to the root: create new database with it
    final String dbname = dbname(n);
    s.execute(new CreateDB(dbname));
    add(s, dbname, "");
  }

  @Override
  protected void copyTo(final Session s, final BXFolder f, final String n)
      throws IOException {
    // folder is copied to a folder in a database
    add(s, f.db, f.path + SEP + n);
    deleteDummy(s, f.db, f.path);
  }

  /**
   * Adds all documents in the folder to another folder.
   * @param s current session
   * @param tdb target database
   * @param tpath target folder
   * @throws IOException I/O exception
   */
  private void add(final Session s, final String tdb, final String tpath)
      throws IOException {

    final Query q = s.query(
        "for $d in " + _DB_LIST.args("$db", "$path") +
        "let $t := $tpath ||'/'|| substring($d, string-length($path) + 1) " +
        "return if (" + _DB_IS_RAW.args("$db", "$d") + ") then " +
        _DB_STORE.args("$tdb", "$t", _DB_RETRIEVE.args("$db", "$d")) +
        " else " + _DB_ADD.args("$tdb", _DB_OPEN.args("$db", "$d"), "$t"));
    q.bind("db", db);
    q.bind("path", path);
    q.bind("tdb", tdb);
    q.bind("tpath", tpath);
    q.execute();
  }
}
