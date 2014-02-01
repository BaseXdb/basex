package org.basex.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.*;
import org.basex.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Response.ContentType;
import com.bradmcevoy.http.Cookie;

/**
 * Wrapper around {@link HttpServletRequest}, which in addition implements {@link Request}.<br/>
 * This implementation is the same as the implementation of {@code ServletRequest} found in
 * {@code milton-servlet}. Since this is one of the few classes which is needed from that library
 * the source is integrated into BaseX.
 *
 * @author Milton Development Team
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class BXServletRequest extends AbstractRequest {
  /** HTTP servlet request. */
  private final HttpServletRequest req;
  /** Request method. */
  private final Method method;
  /** Request URL. */
  private final String url;
  /** Authentication. */
  private Auth auth;
  /** Content types map. */
  private static final Map<ContentType, String> CONTENT_TYPES =
      new EnumMap<ContentType, String>(ContentType.class);
  /** Type contents map. */
  private static final Map<String, ContentType> TYPE_CONTENTS =
      new HashMap<String, ContentType>();

  static {
    CONTENT_TYPES.put(ContentType.HTTP, Response.HTTP);
    CONTENT_TYPES.put(ContentType.MULTIPART, Response.MULTIPART);
    CONTENT_TYPES.put(ContentType.XML, Response.XML);
    for(final Entry<ContentType, String> entry : CONTENT_TYPES.entrySet())
      TYPE_CONTENTS.put(entry.getValue(), entry.getKey());
  }

  /** Thread local variable to hold the current request. */
  private static final ThreadLocal<HttpServletRequest> REQUEST =
      new ThreadLocal<HttpServletRequest>();

  /**
   * Get the current request.
   * @return the current {@link HttpServletRequest}
   */
  public static HttpServletRequest getRequest() {
    return REQUEST.get();
  }

  /**
   * Constructor.
   * @param r HTTP servlet request
   */
  BXServletRequest(final HttpServletRequest r) {
    req = r;
    method = Method.valueOf(r.getMethod());
    url = r.getRequestURL().toString(); // MiltonUtils.stripContext(r);
    REQUEST.set(r);
  }

  @Override
  public String getFromAddress() {
    return req.getRemoteHost();
  }

  @Override
  public String getRequestHeader(final Header header) {
    return req.getHeader(header.code);
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
    return req.getRemoteAddr();
  }

  @Override
  public Auth getAuthorization() {
    if(auth != null) return auth;
    final String enc = getRequestHeader(Header.AUTHORIZATION);
    if(enc == null || enc.isEmpty()) return null;
    auth = new Auth(enc);
    return auth;
  }

  @Override
  public void setAuthorization(final Auth a) {
    auth = a;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return req.getInputStream();
  }

  @Override
  public Map<String, String> getHeaders() {
    final Map<String, String> map = new HashMap<String, String>();
    final Enumeration<String> en = req.getHeaderNames();
    while(en.hasMoreElements()) {
      final String name = en.nextElement();
      final String val = req.getHeader(name);
      map.put(name, val);
    }
    return map;
  }

  @Override
  public Cookie getCookie(final String name) {
    for(final javax.servlet.http.Cookie c : req.getCookies()) {
      if(c.getName().equals(name)) return new BXServletCookie(c);
    }
    return null;
  }

  @Override
  public List<Cookie> getCookies() {
    final List<Cookie> list = new ArrayList<Cookie>();
    for(final javax.servlet.http.Cookie c : req.getCookies())
      list.add(new BXServletCookie(c));
    return list;
  }

  @Override
  public void parseRequestParameters(final Map<String, String> params,
      final Map<String, com.bradmcevoy.http.FileItem> files)
      throws RequestParseException {
    try {
      if(isMultiPart()) {
        parseQueryString(params, req.getQueryString());
        @SuppressWarnings("unchecked")
        final List<FileItem> items = new ServletFileUpload().parseRequest(req);
        for(final FileItem item : items) {
          if(item.isFormField())
            params.put(item.getFieldName(), item.getString());
          else
            files.put(item.getFieldName(), new FileItemWrapper(item));
        }
      } else {
        final Enumeration<String> en = req.getParameterNames();
        while(en.hasMoreElements()) {
          final String nm = en.nextElement();
          final String val = req.getParameter(nm);
          params.put(nm, val);
        }
      }
    } catch(final FileUploadException ex) {
      throw new RequestParseException("FileUploadException", ex);
    } catch(final Throwable ex) {
      throw new RequestParseException(ex.getMessage(), ex);
    }
  }

  /**
   * Parse the query string.
   * @param map parsed key-values will be stored here
   * @param qs query string
   */
  private static void parseQueryString(final Map<String, String> map, final String qs) {
    if(qs == null) return;
    for(final String nv : qs.split("&")) {
      final String[] parts = nv.split("=");
      final String key = parts[0];
      String val = null;
      if(parts.length > 1) {
        val = parts[1];
        if(val != null) {
          try {
            val = URLDecoder.decode(val, Token.UTF8);
          } catch(final UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
          }
        }
      }
      map.put(key, val);
    }
  }

  /**
   * Request content type.
   * @return the content type of the current request
   */
  ContentType getRequestContentType() {
    final String s = req.getContentType();
    if(s == null) return null;
    if(s.contains(Response.MULTIPART)) return ContentType.MULTIPART;
    return TYPE_CONTENTS.get(s);
  }

  /**
   * Is the content type of the request a multi-part?
   * @return {@code true} if the request is multi-part
   */
  boolean isMultiPart() {
    return getRequestContentType() == ContentType.MULTIPART;
  }
}

/**
 * Wrapper around {@link FileItem}, which in addition implements
 * {@link com.bradmcevoy.http.FileItem}.<br/>
 * This implementation is the same as the implementation of
 * {@code FileItemWrapper} found in {@code milton-servlet}. Since this is one of
 * the few classes which is needed from that library, the source is integrated
 * into BaseX.
 * @author Milton Development Team
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
class FileItemWrapper implements com.bradmcevoy.http.FileItem {
  /** Wrapped file item. */
  private final FileItem wrapped;
  /** File name. */
  private final String name;

  /**
   * Strip path information provided by IE.
   * @param s string
   * @return stripped string
   */
  private static String fixIEFileName(final String s) {
    final int pos = s.lastIndexOf('\\');
    return pos < 0 ? s : s.substring(pos + 1);
  }

  /**
   * Constructor.
   * @param f file item
   */
  FileItemWrapper(final FileItem f) {
    wrapped = f;
    name = fixIEFileName(wrapped.getName());
  }

  @Override
  public String getContentType() {
    return wrapped.getContentType();
  }

  @Override
  public String getFieldName() {
    return wrapped.getFieldName();
  }

  @Override
  public InputStream getInputStream() {
    try {
      return wrapped.getInputStream();
    } catch(final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public OutputStream getOutputStream() {
    try {
      return wrapped.getOutputStream();
    } catch(final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getSize() {
    return wrapped.getSize();
  }
}
