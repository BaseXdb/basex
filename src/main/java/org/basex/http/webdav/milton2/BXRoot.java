package org.basex.http.webdav.milton2;


import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;
import org.basex.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.basex.http.webdav.impl.Utils.dbname;

/**
 * WebDAV resource representing the list of all databases.
 *
 * @author BaseX Team 2005-13, BSD License
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
  public BXAbstractResource child(final String name) throws NotAuthorizedException,
    BadRequestException {
    try {
      return service.dbExists(name) ?
        new BXDatabase(new ResourceMetaData(name, service.timestamp(name)), service) :
        null;
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }

  @Override
  public List<BXAbstractResource> getChildren() throws NotAuthorizedException,
    BadRequestException {
    try {
      return service.listDbs();
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }

  @Override
  public BXDatabase createCollection(final String newName) throws NotAuthorizedException,
    ConflictException, BadRequestException {
    try {
      return (BXDatabase) service.createDb(dbname(newName));
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }

  @Override
  public BXAbstractResource createNew(final String newName, final InputStream input,
    final Long length, final String contentType) throws IOException, ConflictException,
    NotAuthorizedException, BadRequestException {
    try {
      return service.createFile(newName, input);
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }
}
