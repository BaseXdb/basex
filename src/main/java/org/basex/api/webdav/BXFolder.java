package org.basex.api.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
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
  public BXDocument createNew(final String newName, final InputStream input,
      final Long length, final String contentType) throws BadRequestException {

    return new BXCode<BXDocument>(this) {
      @Override
      public BXDocument get() throws BaseXException {
        s.execute(new Open(db));
        final String doc = path.isEmpty() ? newName : path + SEP + newName;
        // check if document with this path already exists
        if(count(s, db, doc) == 0) {
          s.add(newName, path, input);
          deleteDummy(s, db, path);
        } else {
          s.replace(doc, input);
        }
        return new BXDocument(db, doc, session);
      }
    }.eval();
  }

  @Override
  public BXFolder createCollection(final String folder)
      throws BadRequestException {

    return new BXCode<BXFolder>(this) {
      @Override
      public BXFolder get() throws BaseXException {
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
      public BXResource get() {
        return resource(s, db, path + SEP + childName, session);
      }
    }.evalNoEx();
  }

  @Override
  public List<BXResource> getChildren() {
    return new BXCode<List<BXResource>>(this) {
      @Override
      public List<BXResource> get() throws BaseXException {
        final List<BXResource> ch = new ArrayList<BXResource>();
        final HashSet<String> paths = new HashSet<String>();
        final Query q = s.query(
            "for $r in db:list($d, $p) return substring-after($r,$p)");
        q.bind("$d", db);
        q.bind("$p", path);
        while(q.more()) {
          final String p = stripLeadingSlash(q.next());
          final int ix = p.indexOf(SEP);
          // check if document or folder
          if(ix < 0) {
            if(!p.equals(DUMMY))
              ch.add(new BXDocument(db, path + SEP + p, session));
          } else {
            final String folder = path + SEP + p.substring(0, ix);
            if(!paths.contains(folder)) {
              paths.add(folder);
              ch.add(new BXFolder(db, folder, session));
            }
          }
        }
        q.close();
        return ch;
      }
    }.evalNoEx();
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws BaseXException {
    // folder is copied to the root: create new database with it
    s.execute(new CreateDB(n));
    add(s, n, "");
    s.execute(new Close());
  }

  @Override
  protected void copyTo(final Session s, final BXFolder f, final String n)
      throws BaseXException {
    // folder is copied to a folder in a database
    add(s, f.db, f.path + SEP + n);
    deleteDummy(s, f.db, f.path);
  }

  /**
   * Add all documents in the folder to another folder.
   * @param s current session
   * @param trgdb target database
   * @param trgdir target folder
   * @throws BaseXException database exception
   */
  private void add(final Session s, final String trgdb, final String trgdir)
      throws BaseXException {
    final Query q = s.query(
        "for $d in db:list($db, $path) " +
        "return db:add($trgdb, collection($db || '/' || $d), " +
        "$trgdir || '/' || substring-after($d, $path))");
    q.bind("$db", db);
    q.bind("$path", path);
    q.bind("$trgdb", trgdb);
    q.bind("$trgdir", trgdir);
    q.execute();
  }

  @Override
  public boolean isLockedOutRecursive(final Request request) {
    return false;
  }
}
