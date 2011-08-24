package org.basex.api.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Rename;
import org.basex.server.Query;
import org.basex.server.Session;

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
   * @param dbname name of database this document belongs to.
   * @param docpath document path to root
   * @param f resource factory
   * @param u user name
   * @param p password
   */
  public BXDocument(final String dbname, final String docpath,
      final BXResourceFactory f, final String u, final String p) {
    super(dbname, docpath, f);
    user = u;
    pass = p;
  }

  @Override
  public Long getContentLength() {
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
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void copyTo(final CollectionResource target, final String name) {
    try {
      final Session s = factory.login(user, pass);
      try {
        if(target instanceof BXAllDatabasesResource) {
          // document is copied to the root: create new database with it
          createDb(s, name);
        } else if(target instanceof BXDatabase) {
          // document is copied to a database
          add(s, ((BXDatabase) target).db, name);
        } else if(target instanceof BXFolder) {
          // document is copied to a folder within a database
          final BXFolder trg = (BXFolder) target;
          add(s, trg.db, name, trg.path);
          deleteDummy(s, trg.db, trg.path);
        }
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }
  }

  @Override
  public void delete() {
    try {
      final Session s = factory.login(user, pass);
      try {
        delete(s);
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }
  }

  @Override
  public String getContentType(final String accepts) {
    return MIMETYPEXML;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException {
    final Session s = factory.login(user, pass);
    try {
      s.setOutputStream(out);
      final Query q = s.query("declare variable $path as xs:string external; "
          + "collection($path)");
      q.bind("$path", db + SEP + path);
      q.execute();
    } catch(BaseXException e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    } finally {
      s.close();
    }
  }

  @Override
  public void moveTo(final CollectionResource target, final String name) {
    try {
      final Session s = factory.login(user, pass);
      try {
        if(target instanceof BXAllDatabasesResource) {
          // document is moved to the root: create new database with it
          createDb(s, name);
          delete(s);
        } else if(target instanceof BXDatabase) {
          final BXDatabase trg = (BXDatabase) target;
          if(trg.db.equals(db)) {
            // document is moved to the root of the database to which it belongs
            s.execute(new Open(db));
            s.execute(new Rename(path, name));
          } else {
            // document is moved to another database
            add(s, trg.db, name);
            delete(s);
          }
        } else if(target instanceof BXFolder) {
          final BXFolder trg = (BXFolder) target;
          if(trg.db.equals(db)) {
            // document is moved in another folder in the same database
            s.execute(new Open(db));
            s.execute(new Rename(path, trg.path + SEP + name));
          } else {
            // document is moved to a folder in another database
            add(s, trg.db, name, trg.path);
            deleteDummy(s, trg.db, trg.path);
            delete(s);
          }
        }
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [RS] WebDAV: error handling
      e.printStackTrace();
    }
  }

  /**
   * Creates a database with the given document.
   * @param s current session
   * @param d document name
   * @throws BaseXException database exception
   */
  private void createDb(final Session s, final String d)
      throws BaseXException {
    final String nm = d.endsWith(".xml") ? d.substring(0, d.length() - 4) : d;
    // get content of this document
    final Query q = s.query("declare variable $doc as xs:string external; "
        + "collection($doc)");
    q.bind("$doc", db + SEP + path);
    final String doc = q.execute();
    // create a new database with this document
    s.execute(new CreateDB(nm, doc));
  }

  /**
   * Copy a document to the root of a database.
   * @param s current session
   * @param dbName database name
   * @param name name of document in new database
   * @throws BaseXException database exception
   */
  private void add(final Session s, final String dbName, final String name)
      throws BaseXException {
    final Query q = s.query("declare variable $db as xs:string external; "
        + "declare variable $doc as xs:string external; "
        + "declare variable $name as xs:string external; "
        + "db:add($db, collection($doc), $name)");
    q.bind("$db", dbName);
    q.bind("$doc", db + SEP + path);
    q.bind("$name", name);
    q.execute();
  }

  /**
   * Copy a document to a folder in a database.
   * @param s current session
   * @param dbName database name
   * @param name name of document in new database
   * @param folderPath path to folder
   * @throws BaseXException database exception
   */
  private void add(final Session s, final String dbName,
      final String name, final String folderPath) throws BaseXException {
    final Query q = s.query("declare variable $db as xs:string external; "
        + "declare variable $doc as xs:string external; "
        + "declare variable $name as xs:string external; "
        + "declare variable $path as xs:string external; "
        + "db:add($db, collection($doc), $name, $path)");
    q.bind("$db", dbName);
    q.bind("$doc", db + SEP + path);
    q.bind("$name", name);
    q.bind("$path", folderPath);
    q.execute();
  }
}
