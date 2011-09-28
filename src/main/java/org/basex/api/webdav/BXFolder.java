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
import org.basex.core.cmd.Close;
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
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXFolder extends BXAbstractResource implements FolderResource,
    DeletableCollectionResource {

  /**
   * Constructor.
   * @param dbname database name
   * @param folderPath path to folder
   * @param s current session
   */
  public BXFolder(final String dbname, final String folderPath,
      final HTTPSession s) {
    super(dbname, folderPath, s);
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
  public BXFolder createCollection(final String folder)
      throws BadRequestException {

    return new BXCode<BXFolder>(this) {
      @Override
      public BXFolder get() throws IOException {
        // [DP] WebDAV: possible optimization would be to rename the dummy, if
        // the current folder is empty (which not always the case)
        deleteDummy(s, db, path);
        final String newFolder = path + SEP + folder;
        createDummy(s, db, newFolder);
        return new BXFolder(db, newFolder, session);
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
            "for $r in " + DBLIST.args("$d", "$p") +
            "return (" +
                SUBAFTER.args("$r", "$p") + ',' +
                DBISRAW.args("$d", "$r") + ',' +
                DBCTYPE.args("$d", "$r") + ')');
        q.bind("d", db);
        q.bind("p", path);
        while(q.more()) {
          final String p = stripLeadingSlash(q.next());
          final boolean raw = Boolean.parseBoolean(q.next());
          final String ctype = q.next();
          final int ix = p.indexOf(SEP);
          // check if document or folder
          if(ix < 0) {
            if(!p.equals(DUMMY))
              ch.add(new BXDocument(db, path + SEP + p, session, raw, ctype));
          } else {
            final String folder = path + SEP + p.substring(0, ix);
            if(paths.add(folder)) ch.add(new BXFolder(db, folder, session));
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
      public BXDocument get() throws IOException {
        s.execute(new Open(db));
        final String dbp = path.isEmpty() ? newName : path + SEP + newName;
        // delete old resource if it already exists
        if(pathExists(s, db, dbp)) s.execute(new Delete(dbp));
        // otherwise, delete dummy file
        // check if document with this path already exists
        else deleteDummy(s, db, path);
        addFile(s, newName, input);
        return new BXDocument(db, dbp, session, isRaw(s, db, dbp), contentType);
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

    final BufferInput bi = new BufferInput(in, 1 << 22); // use 4MB cache
    try {
      // try to add every document as XML
      addXML(s, n, bi);
    } catch(final IOException ex) {
      // if the operations fails, and if all sent bytes are buffered,
      // store data in raw form
      if(!bi.markSupported()) throw ex;
      bi.reset();
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
    s.add(n, path, in);
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
    s.execute(new CreateDB(n));
    add(s, n, "");
    s.execute(new Close());
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
   * @param trgdb target database
   * @param trgdir target folder
   * @throws IOException I/O exception
   */
  private void add(final Session s, final String trgdb, final String trgdir)
      throws IOException {
    final Query q = s.query(
        "for $d in " + DBLIST.args("$db", "$path") +
        "let $t := tokenize(substring($d, string-length($path) + 1), '/') " +
        "let $p := string-join(($trgdir, $t[position() < last()]), '/') " +
        "let $n := $t[last()] return " +
        "if (" + DBISRAW.args("$db", "$d") + ") then " +
        DBSTORE.args("$trgdb", "$p||'/'||$n", DBRETRIEVE.args("$db", "$d")) +
        " else " +
        DBADD.args("$trgdb", DBOPEN.args("$db", "$d"), "$n", "$p"));
    q.bind("db", db);
    q.bind("path", path);
    q.bind("trgdb", trgdb);
    q.bind("trgdir", trgdir);
    q.execute();
  }

  @Override
  public boolean isLockedOutRecursive(final Request request) {
    return false;
  }
}

