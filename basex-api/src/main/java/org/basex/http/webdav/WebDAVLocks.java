package org.basex.http.webdav;

import java.util.*;
import com.bradmcevoy.http.*;

/**
 * WebDAV locks.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebDAVLocks {
  /** Single instance. */
  private static final WebDAVLocks INSTANCE = new WebDAVLocks();
  /** Registered locks. */
  private final HashMap<String, WebDAVLock> locks = new HashMap<>();
  /** Id counter. */
  private long lockId;

  /** Private constructor. */
  private WebDAVLocks() { }

  /**
   * Returns singleton instance.
   * @return locks
   */
  static WebDAVLocks get() {
    synchronized(INSTANCE) {
      // remove expired entries before returning the object
      INSTANCE.locks.entrySet().removeIf(entry -> entry.getValue().token.isExpired());
      return INSTANCE;
    }
  }

  /**
   * Releases the lock for the given token.
   * @param id token id
   */
  synchronized void unlock(final String id) {
    locks.remove(id);
  }

  /**
   * Refreshes a lock.
   * @param id token id
   * @return refreshed lock (can be {@code null})
   */
  synchronized WebDAVLock refreshLock(final String id) {
    final WebDAVLock lock = locks.get(id);
    if(lock != null) lock.token.setFrom(new Date());
    return lock;
  }

  /**
   * Returns a lock token for the given path.
   * @param meta meta data
   * @return removed lock token (can be {@code null})
   */
  synchronized LockToken lockOn(final WebDAVMetaData meta) {
    final String path = meta.db + '/' + meta.path;
    for(final WebDAVLock lock : locks.values()) {
      if(path.startsWith(lock.path)) return lock.token;
    }
    return null;
  }

  /**
   * Creates a new lock.
   * @param timeout timeout
   * @param lockInfo lock info
   * @param meta meta data
   * @return new lock
   */
  synchronized WebDAVLock create(final LockTimeout timeout, final LockInfo lockInfo,
      final WebDAVMetaData meta) {
    final LockToken token = new LockToken(Long.toString(lockId++), lockInfo, timeout);
    final WebDAVLock lock = new WebDAVLock(token, meta.db + '/' + meta.path);
    locks.put(token.tokenId, lock);
    return lock;
  }

  /**
   * Checks if this resource, or one of its descendants, is currently locked.
   * @param meta meta data
   * @return result of check
   */
  synchronized boolean isLockedOut(final WebDAVMetaData meta) {
    final String path = meta.db + '/' + meta.path;
    for(final WebDAVLock lock : locks.values()) {
      if(lock.path.startsWith(path)) return true;
    }
    return false;
  }
}
