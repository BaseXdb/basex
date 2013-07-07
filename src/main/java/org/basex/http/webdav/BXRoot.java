package org.basex.http.webdav;

import static org.basex.http.webdav.impl.Utils.*;

import java.io.*;
import java.util.List;

import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;

/**
 * WebDAV resource representing the list of all databases.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXRoot extends BXFolder {
  /**
   * Constructor.
   * @param s service
   */
  public BXRoot(final WebDAVService<BXAbstractResource> s) {
    super(new ResourceMetaData(), s);
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
  public BXAbstractResource child(final String name) {
    return new BXCode<BXAbstractResource>(this) {
      @Override
      public BXAbstractResource get() throws IOException {
        return service.dbExists(name) ?
          new BXDatabase(new ResourceMetaData(name, service.timestamp(name)), service) :
          null;
      }
    }.evalNoEx();
  }

  @Override
  public List<BXAbstractResource> getChildren() {
    return new BXCode<List<BXAbstractResource>>(this) {
      @Override
      public List<BXAbstractResource> get() throws IOException {
        return service.listDbs();
      }
    }.evalNoEx();
  }

  @Override
  public BXDatabase createCollection(final String newName) throws BadRequestException {
    return new BXCode<BXDatabase>(this) {
      @Override
      public BXDatabase get() throws IOException {
        return (BXDatabase) service.createDb(dbname(newName));
      }
    }.eval();
  }

  @Override
  public BXAbstractResource createNew(final String newName, final InputStream input,
      final Long length, final String contentType) throws BadRequestException {
    return new BXCode<BXAbstractResource>(this) {
      @Override
      public BXAbstractResource get() throws IOException {
        return service.createFile(newName, input);
      }
    }.eval();
  }
}
