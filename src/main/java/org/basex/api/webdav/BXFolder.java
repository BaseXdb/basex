package org.basex.api.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
import org.basex.server.Query;
import org.basex.server.Session;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;

/**
 * WebDAV resource representing a folder within a collection database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXFolder extends BXAbstractResource implements FolderResource {
  /**
   * Constructor.
   * @param dbname database name
   * @param folderPath path to folder
   * @param f resource factory
   * @param u user name
   * @param p password
   */
  public BXFolder(final String dbname, final String folderPath,
      final BXResourceFactory f, final String u, final String p) {
    super(dbname, folderPath, f, u, p);
  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
    // TODO Auto-generated method stub
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) {
    if(supported(contentType)) {
      try {
        final Session s = factory.login(user, pass);
        try {
          s.execute(new Open(db));
          final String doc = path.isEmpty() ? newName : path + SEP + newName;
          // check if document with this path already exists
          if(count(s, db, doc) == 0) {
            s.add(newName, path, inputStream);
            deleteDummy(s, db, path);
          } else {
            s.replace(doc, inputStream);
          }

          return new BXDocument(db, doc, factory, user, pass);
        } finally {
          s.close();
        }
      } catch(Exception ex) {
        handle(ex);
      }
    }
    return null;
  }

  @Override
  public CollectionResource createCollection(final String folder) {
    try {
      final Session s = factory.login(user, pass);
      try {
        // [DP] WebDAV: possible optimization would be to rename the dummy, if
        // the current folder is empty (which not always the case)
        deleteDummy(s, db, path);
        final String newFolder = path + SEP + folder;
        createDummy(s, db, newFolder);
        return new BXFolder(db, newFolder, factory, user, pass);
      } finally {
        s.close();
      }
    } catch(Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public Resource child(final String childName) {
    try {
      final Session s = factory.login(user, pass);
      try {
        return factory.resource(s, db, path + SEP + childName, user, pass);
      } finally {
        s.close();
      }
    } catch(Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public List<? extends Resource> getChildren() {
    final List<BXResource> ch = new ArrayList<BXResource>();
    final HashSet<String> paths = new HashSet<String>();
    try {
      final Session s = factory.login(user, pass);
      try {
        final Query q = s.query("declare variable $d as xs:string external; "
            + "declare variable $p as xs:string external; "
            + "for $r in db:list($d) return substring-after($r,$p)");
        q.bind("$d", db + SEP + path);
        q.bind("$p", path);
        while(q.more()) {
          final String p = stripLeadingSlash(q.next());
          final int ix = p.indexOf(SEP);
          // check if document or folder
          if(ix < 0) {
            if(!p.equals(DUMMY))
              ch.add(new BXDocument(db, path + SEP + p, factory, user, pass));
          } else {
            final String folder = path + SEP + p.substring(0, ix);
            if(!paths.contains(folder)) {
              paths.add(folder);
              ch.add(new BXFolder(db, folder, factory, user, pass));
            }
          }
        }
      } finally {
        s.close();
      }
    } catch(Exception ex) {
      handle(ex);
    }
    return ch;
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws BaseXException {
    // folder is copied to the root: create new database with it
    s.execute(new CreateDB(n));
    add(s, n, "");
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
    final Query q = s.query("declare variable $src as xs:string external; "
        + "declare variable $srcdb as xs:string external; "
        + "declare variable $trgdb as xs:string external; "
        + "declare variable $trgdir as xs:string external;"
        + "declare variable $prefix as xs:string external; "
        + "for $d in db:list($src) "
        + "return db:add($trgdb, collection($srcdb || $d), "
        + "$trgdir || substring-after($d, $prefix))");
    q.bind("$src", db + SEP + path);
    q.bind("$srcdb", db + SEP);
    q.bind("$trgdb", trgdb);
    q.bind("$trgdir", trgdir.isEmpty() ? "" : trgdir + SEP);
    q.bind("$prefix", path + SEP);
    q.execute();
  }
}
