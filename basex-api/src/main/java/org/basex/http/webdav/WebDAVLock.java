package org.basex.http.webdav;

import org.basex.util.*;

import com.bradmcevoy.http.*;

/**
 * Lock entry.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class WebDAVLock {
  /** Lock token. */
  final LockToken token;
  /** Lock path. */
  final String path;

  /**
   * Constructor.
   * @param token token
   * @param path path
   */
  WebDAVLock(final LockToken token, final String path) {
    this.token = token;
    this.path = path;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + path + ", " +
      token.info + ", " + token.timeout + ", " + token.tokenId + ']';
  }
}
