package org.basex.api.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;

/**
 * WebDAV resource representing an XML document.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXDocument extends BXResource implements FileResource {
  /**
   * Constructor.
   * @param dbname database name
   * @param docpath path to document
   */
  public BXDocument(final String dbname, final String docpath) {
    super(dbname, docpath);
  }

  /**
   * Constructor.
   * @param dbname name of database this document belongs to.
   * @param docpath document path to root
   * @param u user name
   * @param p password
   */
  public BXDocument(final String dbname, final String docpath, final String u,
      final String p) {
    this(dbname, docpath);
    user = u;
    pass = p;
  }

  @Override
  public void copyTo(final CollectionResource target, final String name) {
    try {
      ClientSession cs = login(user, pass);
      try {
        if(target instanceof BXAllDatabasesResource) {
          // Document is copied to the root directory => new database has to be
          // created
          final String dbName = name.endsWith("xml") ? name.substring(0,
              name.length() - 4) : name;
          // Get contents of this document
          ClientQuery q = cs.query("declare variable $doc as xs:string external; "
              + "collection($doc)");
          q.bind("$doc", db + DIRSEP + path);
          String doc = q.execute();
          // Create a new datatabase with this document
          cs.execute(new CreateDB(dbName, doc));
        } else if(target instanceof BXDatabase) {
          // Document is copied to a database
          if(((BXDatabase) target).db.equals(db)) {
            // Document is copied to the root if its own database
          }
        } else if(target instanceof BXFolder) {
          // Document is copied to a folder within a database
        }
      } finally {
        cs.close();
      }
    } catch(Exception ex) {
      // [RS] WebDAV: error handling
      ex.printStackTrace();
    }
  }

  @Override
  public void delete() {
    try {
      final ClientSession cs = login(user, pass);
      try {
        cs.execute(new Open(db));
        cs.execute(new Delete(path));
      } finally {
        cs.close();
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
    return MIMETYPEXML;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException {
    final ClientSession cs = login(user, pass);
    try {
      cs.setOutputStream(out);
      final ClientQuery q = cs.query("declare variable $path as xs:string external; "
          + "collection($path)");
      q.bind("$path", db + DIRSEP + path);
      q.execute();
    } catch(BaseXException e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    } finally {
      cs.close();
    }
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
