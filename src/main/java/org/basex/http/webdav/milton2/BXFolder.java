package org.basex.http.webdav.milton2;

import io.milton.http.*;
import io.milton.http.exceptions.*;
import io.milton.resource.DeletableCollectionResource;
import io.milton.resource.FolderResource;
import io.milton.resource.LockingCollectionResource;
import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;
import org.basex.io.in.*;
import org.basex.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.basex.http.webdav.impl.Utils.SEP;
import static org.basex.http.webdav.impl.Utils.dbname;

/**
 * WebDAV resource representing a folder within a collection database.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public class BXFolder extends BXAbstractResource implements FolderResource,
    DeletableCollectionResource, LockingCollectionResource {

  /**
   * Constructor.
   * @param d resource meta data
   * @param s service implementation
   */
  public BXFolder(final ResourceMetaData d, final WebDAVService<BXAbstractResource> s) {
    super(d, s);
  }

  @Override
  public Long getContentLength() {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return null;
  }

  @Override
  public Date getCreateDate() {
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) throws IOException,
      NotAuthorizedException, BadRequestException, NotFoundException { }

  @Override
  public boolean isLockedOutRecursive(final Request request) {
    try {
      return service.conflictingLocks(meta.db, meta.path);
    } catch(IOException e) {
      Util.errln(e);
      return false;
    }
  }

  @Override
  public BXFolder createCollection(final String folder) throws NotAuthorizedException,
      ConflictException, BadRequestException {
    try {
      return (BXFolder) service.createFolder(meta.db, meta.path, folder);
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }

  @Override
  public BXAbstractResource child(final String childName) throws NotAuthorizedException,
      BadRequestException {
    try {
      return service.resource(meta.db, meta.path + SEP + childName);
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }

  @Override
  public List<BXAbstractResource> getChildren() throws NotAuthorizedException,
      BadRequestException {
    try {
      return service.list(meta.db, meta.path);
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
      return service.createFile(meta.db, meta.path, newName, input);
    } catch(IOException e) {
      Util.errln(e);
      throw new BadRequestException(this, e.getMessage());
    }
  }

  @Override
  public LockToken createAndLock(final String name, final LockTimeout timeout,
      final LockInfo lockInfo) throws NotAuthorizedException {
    try {
      final BXAbstractResource r = createNew(name, new ArrayInput(Token.EMPTY),
          Long.valueOf(0L), null);
      final LockResult lockResult = r.lock(timeout, lockInfo);
      if(lockResult.isSuccessful()) return lockResult.getLockToken();
    } catch(Exception e) {
      Util.debug("Cannot lock and create requested resource", e);
    }
    return null;
  }

  @Override
  protected void copyToRoot(final String n) throws IOException {
    // folder is copied to the root: create new database with it
    final String dbname = dbname(n);
    service.createDb(dbname);
    service.copyAll(meta.db, meta.path, dbname, "");
  }

  @Override
  protected void copyTo(final BXFolder f, final String n) throws IOException {
    // folder is copied to a folder in a database
    service.copyAll(meta.db, meta.path, f.meta.db, f.meta.path + SEP + n);
    service.deleteDummy(f.meta.db, f.meta.path);
  }
}
