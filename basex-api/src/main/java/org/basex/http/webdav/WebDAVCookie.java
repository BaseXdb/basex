package org.basex.http.webdav;

import com.bradmcevoy.http.*;

/**
 * Wrapper around {@link javax.servlet.http.Cookie}, which in addition implements {@link Cookie}.
 * This implementation is the same as the implementation of {@code ServletCookie} found in
 * {@code milton-servlet}. Since this is one of the few classes which is needed from that library,
 * the source was integrated into BaseX.
 *
 * @author Milton Development Team
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class WebDAVCookie implements Cookie {
  /** Wrapped instance. */
  final javax.servlet.http.Cookie cookie;

  /**
   * Constructor.
   * @param cookie servlet cookie
   */
  WebDAVCookie(final javax.servlet.http.Cookie cookie) {
    this.cookie = cookie;
  }

  @Override
  public int getVersion() { return cookie.getVersion(); }
  @Override
  public void setVersion(final int version) { cookie.setVersion(version); }
  @Override
  public String getName() { return cookie.getName(); }
  @Override
  public String getValue() { return cookie.getValue(); }
  @Override
  public void setValue(final String value) { cookie.setValue(value); }
  @Override
  public boolean getSecure() { return cookie.getSecure(); }
  @Override
  public void setSecure(final boolean secure) { cookie.setSecure(secure); }
  @Override
  public int getExpiry() { return cookie.getMaxAge(); }
  @Override
  public void setExpiry(final int expiry) { cookie.setMaxAge(expiry); }
  @Override
  public String getPath() { return cookie.getPath(); }
  @Override
  public void setPath(final String path) { cookie.setPath(path); }
  @Override
  public String getDomain() { return cookie.getDomain(); }
  @Override
  public void setDomain(final String domain) { cookie.setDomain(domain); }
}
