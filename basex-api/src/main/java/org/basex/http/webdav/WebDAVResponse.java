package org.basex.http.webdav;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.basex.http.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Cookie;

/**
 * Wrapper around {@link HttpServletResponse}, which in addition implements {@link Response}.
 * This implementation is the same as the implementation of {@code ServletResponse} found in
 * {@code milton-servlet}. Since this is one of the few classes which is needed from that library,
 * the source was integrated into BaseX.
 *
 * @author Milton Development Team
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class WebDAVResponse extends AbstractResponse {
  /** HTTP servlet response. */
  private final HttpServletResponse response;
  /** Response headers. */
  private final Map<String, String> headers = new HashMap<>();
  /** Response status. */
  private Status status;

  /**
   * Constructor.
   * @param conn HTTP connection
   */
  WebDAVResponse(final HTTPConnection conn) {
    response = conn.response;
  }

  @Override
  protected void setAnyDateHeader(final Header name, final Date date) {
    response.setDateHeader(name.code, date.getTime());
  }

  @Override
  public String getNonStandardHeader(final String code) {
    return headers.get(code);
  }

  @Override
  public void setNonStandardHeader(final String name, final String value) {
    response.addHeader(name, value);
    headers.put(name, value);
  }

  @Override
  public void setStatus(final Status s) {
    response.setStatus(s.code);
    status = s;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public OutputStream getOutputStream() {
    try {
      return response.getOutputStream();
    } catch(final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void close() {
    try {
      response.flushBuffer();
      response.getOutputStream().flush();
    } catch(final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void sendRedirect(final String url) {
    try {
      response.sendRedirect(response.encodeRedirectURL(url));
    } catch(final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public void setAuthenticateHeader(final List<String> challenges) {
    for(final String ch : challenges) {
      response.addHeader(Header.WWW_AUTHENTICATE.code, ch);
    }
  }

  @Override
  public Cookie setCookie(final Cookie cookie) {
    if(cookie instanceof WebDAVCookie) {
      response.addCookie(((WebDAVCookie) cookie).cookie);
      return cookie;
    }

    final javax.servlet.http.Cookie c = new javax.servlet.http.Cookie(
        cookie.getName(), cookie.getValue());
    c.setDomain(cookie.getDomain());
    c.setMaxAge(cookie.getExpiry());
    c.setPath(cookie.getPath());
    c.setSecure(cookie.getSecure());
    c.setVersion(cookie.getVersion());

    response.addCookie(c);
    return new WebDAVCookie(c);
  }

  @Override
  public Cookie setCookie(final String name, final String value) {
    final javax.servlet.http.Cookie c = new javax.servlet.http.Cookie(name, value);
    response.addCookie(c);
    return new WebDAVCookie(c);
  }
}
