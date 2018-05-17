package org.basex.http.webdav;

import static com.bradmcevoy.http.LockResult.*;
import static org.basex.http.webdav.WebDAVUtils.*;

import java.io.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.LockInfo.*;
import com.bradmcevoy.http.Request.*;
import com.bradmcevoy.http.exceptions.*;

/**
 * WebDAV resource representing an abstract folder within a collection database.
 *
 * @author BaseX Team 2005-18, BSD License
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
    return WebDAVService.authorize(meta.db);
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
  public void delete() throws BadRequestException, NotAuthorizedException {
    new WebDAVCode<Object>(this) {
      @Override
      public void run() throws IOException {
        remove();
      }
    }.eval();
  }

  @Override
  public void copyTo(final CollectionResource target, final String name) throws BadRequestException,
      NotAuthorizedException {

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
  public void moveTo(final CollectionResource target, final String name) throws BadRequestException,
      NotAuthorizedException {

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
   * Lock this resource and return a token.
   *
   * @param timeout - in seconds, or null
   * @param lockInfo lock info
   * @return result containing the token representing the lock if successful,
   * otherwise a failure reason code
   */
  @Override
  public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo)
      throws NotAuthorizedException, PreConditionFailedException, LockedException {

    return new WebDAVCode<LockResult>(this) {
      @Override
      public LockResult get() {
        try {
          final String tokenId = service.locking.lock(
            meta.db,
            meta.path,
            lockInfo.scope.name().toLowerCase(Locale.ENGLISH),
            lockInfo.type.name().toLowerCase(Locale.ENGLISH),
            lockInfo.depth.name().toLowerCase(Locale.ENGLISH),
            lockInfo.lockedByUser,
            timeout.getSeconds()
          );
          return success(new LockToken(tokenId, lockInfo, timeout));
        } catch(final IOException ex) {
          Util.stack(ex);
          return failed(FailureReason.ALREADY_LOCKED);
        }
      }
    }.evalNoEx();
  }

  /**
   * Renew the lock and return new lock info.
   *
   * @param token lock token
   * @return lock result
   */
  @Override
  public LockResult refreshLock(final String token) throws NotAuthorizedException,
      PreConditionFailedException {
    return new WebDAVCode<LockResult>(this) {
      @Override
      public LockResult get() throws IOException {
        service.locking.refreshLock(token);
        final LockToken lock = parse(service.locking.locks(token));
        return lock == null ? failed(FailureReason.ALREADY_LOCKED) : success(lock);
      }
    }.evalNoEx();
  }

  /**
   * If the resource is currently locked, and the token matches the current one,
   * unlock the resource.
   * @param token lock token
   */
  @Override
  public void unlock(final String token) throws NotAuthorizedException,
      PreConditionFailedException {
    new WebDAVCode<Object>(this) {
      @Override
      public void run() throws IOException {
        service.locking.unlock(token);
      }
    }.evalNoEx();
  }

  /**
   * Get the active lock for the current resource.
   * @return the current lock if the resource is locked, {@code null} otherwise
   */
  @Override
  public LockToken getCurrentLock() {
    return new WebDAVCode<LockToken>(this) {
      @Override
      public LockToken get() throws IOException {
        return parse(service.locking.locks(meta.db, meta.path));
      }
    }.evalNoEx();
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
   * @param n new name
   * @throws IOException I/O exception
   */
  void rename(final String n) throws IOException {
    service.rename(meta.db, meta.path, n);
  }

  /**
   * Copy folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected abstract void copyToRoot(String n) throws IOException;

  /**
   * Copy folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected abstract void copyTo(WebDAVFolder f, String n) throws IOException;

  /**
   * Move folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  void moveToRoot(final String n) throws IOException {
    // folder is moved to the root: create new database with it
    copyToRoot(n);
    remove();
  }

  /**
   * Move folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  private void moveTo(final WebDAVFolder f, final String n) throws IOException {
    if(f.meta.db.equals(meta.db)) {
      // folder is moved to a folder in the same database
      rename(f.meta.path + SEP + n);
    } else {
      // folder is moved to a folder in another database
      copyTo(f, n);
      remove();
    }
  }

  /**
   * Parse the lock info.
   * @param locks lock infos
   * @return lock token (can be {@code null})
   * @throws IOException I/O exception
   */
  private LockToken parse(final Value locks) throws IOException {
    if(locks.isEmpty()) return null;
    try {
      final LockToken lock = new LockToken(null, new LockInfo(), null);
      final Item info = locks.itemAt(0);
      for(Item item : new QueryProcessor("*", service.conn.context).context(info).value()) {
        final ANode node = (ANode) item;
        final String value = Token.string(node.string());
        switch(Token.string(node.qname().local())) {
          case "token":
            lock.tokenId = value;
            break;
          case "scope":
            lock.info.scope = LockScope.valueOf(value.toUpperCase(Locale.ENGLISH));
            break;
          case "type":
            lock.info.type = LockType.valueOf(value.toUpperCase(Locale.ENGLISH));
            break;
          case "depth":
            lock.info.depth = LockDepth.valueOf(value.toUpperCase(Locale.ENGLISH));
            break;
          case "owner":
            lock.info.lockedByUser = value;
            break;
          case "timeout":
            lock.timeout = LockTimeout.parseTimeout(value);
            break;
        }
      }
      return lock;
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
