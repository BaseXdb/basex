package org.basex.api.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.server.Session;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

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
   * @param s current session
   */
  public BXAllDatabasesResource(final HTTPSession s) {
    super(null, null, s);
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Resource child(final String childName) {
    try {
      final Session cs = session.login();
      try {
        return listDbs(cs).contains(childName) ?
            new BXDatabase(childName, session) : null;
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
      final Session s = session.login();
      try {
        for(final String d : listDbs(s))
          dbs.add(new BXDatabase(d, session));
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
  public CollectionResource createCollection(final String newName) throws
    NotAuthorizedException, ConflictException, BadRequestException {
    Session s = null;
    try {
      s = session.login();
      s.execute(new CreateDB(dbname(newName)));
      return new BXDatabase(newName, session);
    } catch(final Exception ex) {
      handle(ex);
      throw new BadRequestException(this, ex.getMessage());
    } finally {
      try { if(s != null) s.close(); } catch(final IOException e) { handle(e); }
    }
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) throws IOException,
      ConflictException, NotAuthorizedException, BadRequestException {
    Session s = null;
    try {
      s = session.login();
      final String dbname = dbname(newName);
      s.create(dbname, inputStream);
      s.execute(new Close());
      return new BXDatabase(dbname, session);
    } catch(final Exception ex) {
      handle(ex);
      throw new BadRequestException(this, ex.getMessage());
    } finally {
      try { if(s != null) s.close(); } catch(final IOException e) { handle(e); }
    }
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
