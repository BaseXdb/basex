package org.basex.api.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.server.Session;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;

/**
 * WebDAV resource representing the list of all databases.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXAllDatabasesResource extends BXResource implements
    FolderResource {
  /**
   * Constructor.
   * @param f resource factory
   * @param u user name
   * @param p password
   */
  public BXAllDatabasesResource(final BXResourceFactory f, final String u,
      final String p) {
    super(null, null, f);
    user = u;
    pass = p;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Resource child(final String childName) {
    try {
      final Session cs = factory.login(user, pass);
      try {
        return listDbs(cs).contains(childName) ?
            new BXDatabase(childName, factory, user, pass) : null;
      } finally {
        cs.close();
      }
    } catch(final Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public List<? extends Resource> getChildren() {
    try {
      final List<BXResource> dbs = new ArrayList<BXResource>();
      final Session s = factory.login(user, pass);
      try {
        for(final String d : listDbs(s))
          dbs.add(new BXDatabase(d, factory, user, pass));
        return dbs;
      } finally {
        s.close();
      }
    } catch(final Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public CollectionResource createCollection(final String newName) {
    try {
      final Session s = factory.login(user, pass);
      try {
        s.execute(new CreateDB(dbname(newName)));
        return new BXDatabase(newName, factory, user, pass);
      } finally {
        s.execute(new Close());
        s.close();
      }
    } catch(final Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) {
    try {
      final Session s = factory.login(user, pass);
      try {
        final String dbname = dbname(newName);
        s.create(dbname, inputStream);
        return new BXDatabase(dbname, factory, user, pass);
      } finally {
        s.execute(new Close());
        s.close();
      }
    } catch(final Exception ex) {
      handle(ex);
    }
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
    // this method must do nothing
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    // this method must do nothing
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    // this method must do nothing
    return null;
  }

  @Override
  public Long getContentLength() {
    // this method must do nothing
    return null;
  }

  @Override
  public Date getCreateDate() {
    // this method must do nothing
    return null;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // this method must do nothing
  }

  @Override
  public void delete() {
    // this method must do nothing
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
    // this method must do nothing
  }
}
