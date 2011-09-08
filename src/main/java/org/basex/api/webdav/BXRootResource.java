package org.basex.api.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;

/**
 * WebDAV resource representing the list of all databases.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXRootResource extends BXResource implements
    FolderResource {
  /**
   * Constructor.
   * @param s current session
   */
  public BXRootResource(final HTTPSession s) {
    super(null, null, s);
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Resource child(final String childName) {
    return new BXCode<BXResource>(this) {
      @Override
      public BXResource get() throws BaseXException {
        return listDBs(s).contains(childName) ?
            new BXDatabase(childName, session) : null;
      }
    }.evalNoEx();
  }

  @Override
  public List<BXResource> getChildren() {
    return new BXCode<List<BXResource>>(this) {
      @Override
      public List<BXResource> get() throws BaseXException {
        final List<BXResource> dbs = new ArrayList<BXResource>();
        for(final String d : listDBs(s)) dbs.add(new BXDatabase(d, session));
        return dbs;
      }
    }.evalNoEx();
  }

  @Override
  public BXDatabase createCollection(final String newName)
      throws BadRequestException {

    return new BXCode<BXDatabase>(this) {
      @Override
      public BXDatabase get() throws BaseXException {
        s.execute(new CreateDB(dbname(newName)));
        return new BXDatabase(newName, session);
      }
    }.eval();
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) throws BadRequestException {

    return new BXCode<BXDatabase>(this) {
      @Override
      public BXDatabase get() throws BaseXException {
        final String dbname = dbname(newName);
        s.create(dbname, inputStream);
        s.execute(new Close());
        return new BXDatabase(dbname, session);
      }
    }.eval();
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
