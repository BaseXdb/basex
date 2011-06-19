package org.basex.api.webdav;

import static org.basex.api.webdav.BXResourceFactory.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.basex.core.cmd.Add;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;

/**
 * WebDAV resource representing a collection database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXCollectionDatabase extends BXDatabase implements FolderResource {

  /**
   * Constructor.
   * @param db database name
   */
  public BXCollectionDatabase(final String db) {
    dbname = db;
  }

  /**
   * Constructor.
   * @param db database name
   * @param u user name
   * @param p user password
   */
  public BXCollectionDatabase(final String db, final String u, final String p) {
    dbname = db;
    user = u;
    pass = p;
  }

  @Override
  public CollectionResource createCollection(final String newName) {
    try {
      ClientSession cs = login(user, pass);
      try {
        // Open database
        cs.execute(new Open(dbname));
        // Create a folder in the database which contains a dummy xml document -
        // EMPTY.xml. This is needed because BaseX is not aware of folders, only
        // of xml files
        cs.execute(new Add("<empty/>", "EMPTY.xml", newName));
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // [RS] WebDav Error Handling
      e.printStackTrace();
    }
    return new BXFolder(dbname, newName, user, pass);
  }

  @Override
  public Resource child(final String childName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends Resource> getChildren() {
    final List<BXResource> ch = new ArrayList<BXResource>();
    final List<String> paths = new ArrayList<String>();
    try {
      final ClientSession cs = login(user, pass);
      try {
        // Get paths of all documents within the database
        ClientQuery q = cs.query("collection('" + dbname + "')/doc-name()");
        while(q.more()) {
          final String next = q.next();
          // Find first occurrence of file separator
          final int firstSep = next.indexOf(DIRSEP);
          // No occurrence => this is a document
          if(firstSep <= 0) ch.add(new BXDocument(dbname, next, user, pass));
          else {
            // Folder
            final String folderName = next.substring(0, firstSep);
            if(!paths.contains(folderName)) paths.add(folderName);
          }
        }
        for(final String f : paths)
          ch.add(new BXFolder(dbname, f, user, pass));
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // [RS] WebDav Error Handling
      e.printStackTrace();
    }
    return ch;
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) {
    try {
      ClientSession cs = login(user, pass);
      // Open database
      try {
      cs.execute(new Open(dbname));
      // Add document to databse
      cs.add(newName, "", inputStream);
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // [RS] WebDav Error Handling
      e.printStackTrace();
    }
    return new BXDocument(dbname, newName, user, pass);
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public void delete() {
    try {
      ClientSession cs = login(user, pass);
      try {
        cs.execute(new DropDB(dbname));
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // [RS] WebDav Error Handling
      e.printStackTrace();
    }

  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
    // TODO Auto-generated method stub
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long getContentLength() {
    return null;
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
