package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;

import java.io.*;
import java.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;

/**
 * WebDAV resource representing the list of all databases.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class WebDAVRoot extends WebDAVFolder {
  /**
   * Constructor.
   * @param service service
   */
  WebDAVRoot(final WebDAVService service) {
    super(new WebDAVMetaData(), service);
  }

  @Override
  public String getName() {
    return "";
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

  @Override
  public WebDAVResource child(final String name) throws BadRequestException {
    return new WebDAVCode<WebDAVResource>(this) {
      @Override
      public WebDAVResource get() throws IOException {
        return service.dbExists(name) ?
          new WebDAVDatabase(new WebDAVMetaData(name, service.timestamp(name)), service) : null;
      }
    }.eval();
  }

  @Override
  public List<WebDAVResource> getChildren() throws BadRequestException {
    return new WebDAVCode<List<WebDAVResource>>(this) {
      @Override
      public List<WebDAVResource> get() throws IOException {
        return service.listDbs();
      }
    }.eval();
  }

  @Override
  public WebDAVDatabase createCollection(final String name) throws BadRequestException {
    return new WebDAVCode<WebDAVDatabase>(this) {
      @Override
      public WebDAVDatabase get() throws IOException {
        return (WebDAVDatabase) service.createDb(dbName(name));
      }
    }.eval();
  }

  @Override
  public WebDAVResource createNew(final String name, final InputStream input, final Long length,
      final String contentType) throws BadRequestException {

    return new WebDAVCode<WebDAVResource>(this) {
      @Override
      public WebDAVResource get() throws IOException {
        return service.createFile(name, input);
      }
    }.eval();
  }
}
