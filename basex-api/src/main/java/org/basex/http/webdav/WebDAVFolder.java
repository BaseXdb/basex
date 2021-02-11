package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.*;
import org.basex.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;

/**
 * WebDAV resource representing a folder within a collection database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
class WebDAVFolder extends WebDAVResource implements FolderResource, DeletableCollectionResource,
  LockingCollectionResource {

  /**
   * Constructor.
   * @param meta resource meta data
   * @param service service implementation
   */
  WebDAVFolder(final WebDAVMetaData meta, final WebDAVService service) {
    super(meta, service);
  }

  @Override
  public final Long getContentLength() {
    return null;
  }

  @Override
  public final String getContentType(final String accepts) {
    return null;
  }

  @Override
  public final Date getCreateDate() {
    return null;
  }

  @Override
  public final Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public final void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
  }

  @Override
  public final boolean isLockedOutRecursive(final Request request) {
    return WebDAVLocks.get().isLockedOut(meta);
  }

  @Override
  public WebDAVFolder createCollection(final String folder) throws BadRequestException {
    return new WebDAVCode<WebDAVFolder>(this) {
      @Override
      public WebDAVFolder get() throws IOException {
        return (WebDAVFolder) service.createFolder(meta.db, meta.path,  folder);
      }
    }.eval();
  }

  @Override
  public WebDAVResource child(final String childName) throws BadRequestException {
    return new WebDAVCode<WebDAVResource>(this) {
      @Override
      public WebDAVResource get() throws IOException {
        return service.resource(meta.db, meta.path + SEP + childName);
      }
    }.eval();
  }

  @Override
  public List<WebDAVResource> getChildren() throws BadRequestException {
    return new WebDAVCode<List<WebDAVResource>>(this) {
      @Override
      public List<WebDAVResource> get() throws IOException {
        return service.list(meta.db, meta.path);
      }
    }.eval();
  }

  @Override
  public WebDAVResource createNew(final String newName, final InputStream input,
      final Long length, final String contentType) throws BadRequestException {
    return new WebDAVCode<WebDAVResource>(this) {
      @Override
      public WebDAVResource get() throws IOException {
        return service.createFile(meta.db, meta.path, newName, input);
      }
    }.eval();
  }

  @Override
  public final LockToken createAndLock(final String name, final LockTimeout timeout,
      final LockInfo lockInfo) {
    try {
      final WebDAVResource r = createNew(name, new ArrayInput(Token.EMPTY), 0L, null);
      final LockResult lockResult = r.lock(timeout, lockInfo);
      if(lockResult.isSuccessful()) return lockResult.getLockToken();
    } catch(final Exception ex) {
      Util.debug("Cannot lock and create requested resource", ex);
    }
    return null;
  }

  @Override
  protected void copyToRoot(final String name) throws IOException {
    // folder is copied to the root: create new database with it
    final String dbname = dbName(name);
    service.createDb(dbname);
    service.copyAll(meta.db, meta.path, dbname, "");
  }

  @Override
  protected final void copyTo(final WebDAVFolder folder, final String name) throws IOException {
    // folder is copied to a folder in a database
    service.copyAll(meta.db, meta.path, folder.meta.db, folder.meta.path + SEP + name);
    service.deleteDummy(folder.meta.db, folder.meta.path);
  }
}
