package org.basex.http.webdav;

import static com.bradmcevoy.http.LockResult.*;
import static org.basex.http.webdav.WebDAVUtils.*;

import java.io.*;
import java.util.*;

import org.basex.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.*;
import com.bradmcevoy.http.exceptions.*;

/**
 * WebDAV resource representing an abstract folder within a collection database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
abstract class WebDAVResource implements CopyableResource, DeletableResource, MoveableResource,
    LockableResource {

  /** Resource meta data. */
  final WebDAVMetaData meta;
  /** WebDAV service implementation. */
  final WebDAVService service;

  /**
   * Constructor.
   * @param meta resource meta data
   * @param service service
   */
  WebDAVResource(final WebDAVMetaData meta, final WebDAVService service) {
    this.meta = meta;
    this.service = service;
  }

  @Override
  public Object authenticate(final String user, final String pass) {
    return user;
  }

  @Override
  public boolean authorise(final Request request, final Method method, final Auth auth) {
    return true;
  }

  @Override
  public String checkRedirect(final Request request) {
    return null;
  }

  @Override
  public String getRealm() {
    return Prop.NAME;
  }

  @Override
  public String getUniqueId() {
    return null;
  }

  @Override
  public String getName() {
    return name(meta.path);
  }

  @Override
  public Date getModifiedDate() {
    return meta.mdate;
  }

  @Override
  public void delete() throws BadRequestException {
    new WebDAVCode<Object>(this) {
      @Override
      public void run() throws IOException {
        remove();
      }
    }.eval();
  }

  @Override
  public void copyTo(final CollectionResource target, final String name)
      throws BadRequestException {

    new WebDAVCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof WebDAVRoot) {
          copyToRoot(name);
        } else if(target instanceof WebDAVFolder) {
          copyTo((WebDAVFolder) target, name);
        }
      }
    }.eval();
  }

  @Override
  public void moveTo(final CollectionResource target, final String name)
      throws BadRequestException {

    new WebDAVCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof WebDAVRoot) {
          moveToRoot(name);
        } else if(target instanceof WebDAVFolder) {
          moveTo((WebDAVFolder) target, name);
        }
      }
    }.eval();
  }

  /**
   * Locks this resource and returns a token.
   * @param timeout in seconds (can be {@code null})
   * @param lockInfo lock info
   * @return result containing the token if successful, otherwise a failure reason code
   */
  @Override
  public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo) {
    final WebDAVLock lock = WebDAVLocks.get().create(timeout, lockInfo, meta);
    return success(lock.token);
  }

  /**
   * Refreshes a lock.
   * @param id token id
   * @return result containing the token if successful, otherwise a failure reason code
   */
  @Override
  public LockResult refreshLock(final String id) {
    final WebDAVLock lock = WebDAVLocks.get().refreshLock(id);
    return lock == null ? failed(FailureReason.PRECONDITION_FAILED) : success(lock.token);
  }

  /**
   * Unlocks a resource.
   * @param id token id
   */
  @Override
  public void unlock(final String id) {
    WebDAVLocks.get().unlock(id);
  }

  /**
   * Gets the active lock for the current resource.
   * @return the current lock if the resource is locked, {@code null} otherwise
   */
  @Override
  public LockToken getCurrentLock() {
    return WebDAVLocks.get().lockOn(meta);
  }

  /**
   * Deletes a document or folder.
   * @throws IOException I/O exception
   */
  void remove() throws IOException {
    service.remove(meta.db, meta.path);
  }

  /**
   * Rename document or folder.
   * @param name new name
   * @throws IOException I/O exception
   */
  void rename(final String name) throws IOException {
    service.rename(meta.db, meta.path, name);
  }

  /**
   * Copy folder to the root, creating a new database.
   * @param name new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected abstract void copyToRoot(String name) throws IOException;

  /**
   * Copy folder to another folder.
   * @param folder target folder
   * @param name new name of the folder
   * @throws IOException I/O exception
   */
  protected abstract void copyTo(WebDAVFolder folder, String name) throws IOException;

  /**
   * Move folder to the root, creating a new database.
   * @param name new name of the folder (database)
   * @throws IOException I/O exception
   */
  void moveToRoot(final String name) throws IOException {
    // folder is moved to the root: create new database with it
    copyToRoot(name);
    remove();
  }

  /**
   * Move folder to another folder.
   * @param folder target folder
   * @param name new name of the folder
   * @throws IOException I/O exception
   */
  private void moveTo(final WebDAVFolder folder, final String name) throws IOException {
    if(folder.meta.db.equals(meta.db)) {
      // folder is moved to a folder in the same database
      rename(folder.meta.path + SEP + name);
    } else {
      // folder is moved to a folder in another database
      copyTo(folder, name);
      remove();
    }
  }
}
