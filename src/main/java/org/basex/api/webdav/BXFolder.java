package org.basex.api.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Delete;
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
public class BXFolder extends BXResource implements FolderResource {
  /**
   * Constructor.
   * @param dbname database name
   * @param folderPath path to folder
   * @param f resource factory
   */
  public BXFolder(final String dbname, final String folderPath,
      final BXResourceFactory f) {
    super(dbname, folderPath, f);
  }

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
    this(dbname, folderPath, f);
    user = u;
    pass = p;
  }

  @Override
  public CollectionResource createCollection(final String folder) {
    try {
      final Session s = factory.login(user, pass);
      try {
        final String newFolder = path + SEP + folder;
        s.execute(new Open(db));
        s.execute(new Add("<empty/>", "EMPTY.xml", newFolder));
        return new BXFolder(db, newFolder, factory, user, pass);
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Resource child(final String childName) {
    try {
      final Session s = factory.login(user, pass);
      try {
        return factory.resource(s, db, path + SEP + childName);
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [RS] WebDav Error Handling
      e.printStackTrace();
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
        final Query q = s.query(
            "declare variable $d as xs:string external; " +
            "declare variable $p as xs:string external; " +
            "for $r in db:list($d) return substring-after($r,$p)");
        q.bind("$d", db + SEP + path);
        q.bind("$p", path);
        while(q.more()) {
          final String p = stripLeadingSlash(q.next());
          final int ix = p.indexOf(SEP);
          // check if document or folder
          if(ix < 0) {
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
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }
    return ch;
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
          // Check if document with this path already exists
          if(factory.resource(s, db, doc) == null)
            s.add(newName, path, inputStream);
          else
            s.replace(doc, inputStream);
          return new BXDocument(db, path + SEP + newName, factory, user, pass);
        } finally {
          s.close();
        }
      } catch(Exception e) {
        // [RS] WebDAV: error handling
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public void delete() {
    try {
      final Session s = factory.login(user, pass);
      try {
        s.execute(new Open(db));
        s.execute(new Delete(path));
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }
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
  public void moveTo(final CollectionResource rDest, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
