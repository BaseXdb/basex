package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.http.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Cookie;

import jakarta.servlet.http.*;

/**
 * Wrapper around {@link HttpServletRequest}, which in addition implements {@link Request}.
 * This implementation is the same as the implementation of {@code ServletRequest} found in
 * {@code milton-servlet}. Since this is one of the few classes which is needed from that library
 * the source is integrated into BaseX.
 *
 * @author Milton Development Team
 * @author BaseX Team 2005-24, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class WebDAVRequest extends AbstractRequest {
  /** Destination string. */
  private static final String DESTINATION = "Destination";

  /** HTTP servlet request. */
  private final HttpServletRequest request;
  /** Request method. */
  private final Method method;
  /** Request URL. */
  private final String url;
  /** Authentication. */
  private Auth auth;

  /**
   * Constructor.
   * @param conn HTTP connection
   */
  WebDAVRequest(final HTTPConnection conn) {
    request = conn.request;
    method = Method.valueOf(request.getMethod());
    url = decode(request.getRequestURL().toString());
    auth = new Auth(conn.clientName(), null);
  }

  @Override
  public String getFromAddress() {
    return request.getRemoteHost();
  }

  @Override
  public String getRequestHeader(final Header header) {
    final String value = request.getHeader(header.code);
    return header.code.equals(DESTINATION) ? decode(value) : value;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public String getAbsoluteUrl() {
    return url;
  }

  @Override
  public String getRemoteAddr() {
    return request.getRemoteAddr();
  }

  @Override
  public Auth getAuthorization() {
    return auth;
  }

  @Override
  public void setAuthorization(final Auth a) {
    auth = a;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return request.getInputStream();
  }

  @Override
  public Map<String, String> getHeaders() {
    final Map<String, String> map = new HashMap<>();
    final Enumeration<String> en = request.getHeaderNames();
    while(en.hasMoreElements()) {
      final String name = en.nextElement();
      final String val = request.getHeader(name);
      map.put(name, val);
    }
    return map;
  }

  @Override
  public Cookie getCookie(final String name) {
    for(final jakarta.servlet.http.Cookie c : request.getCookies()) {
      if(c.getName().equals(name)) return new WebDAVCookie(c);
    }
    return null;
  }

  @Override
  public List<Cookie> getCookies() {
    final List<Cookie> list = new ArrayList<>();
    for(final jakarta.servlet.http.Cookie c : request.getCookies()) {
      list.add(new WebDAVCookie(c));
    }
    return list;
  }

  @Override
  public void parseRequestParameters(final Map<String, String> params,
      final Map<String, com.bradmcevoy.http.FileItem> files) throws RequestParseException {
    for(Entry<String, String[]> e : request.getParameterMap().entrySet()) {
      if(e.getValue().length > 0) params.put(e.getKey(), e.getValue()[0]);
    }
  }
}
