package org.basex.api.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.basex.util.Token;

import com.bradmcevoy.http.AbstractRequest;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.RequestParseException;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.Response.ContentType;

/**
 * Wrapper around {@link HttpServletRequest}, which in addition implements
 * {@link Request}. <br/>
 * This implementation is the same as the implementation of
 * {@code ServletRequest} found in {@code milton-servlet}. Since this is one of
 * the few classes which is needed from that library, the source is integrated
 * into BaseX.
 *
 * @author Milton Development Team
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXServletRequest extends AbstractRequest {
  /** HTTP servlet request. */
  private final HttpServletRequest req;
  /** Request method. */
  private final Request.Method method;
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
    for(final ContentType key : CONTENT_TYPES.keySet())
      TYPE_CONTENTS.put(CONTENT_TYPES.get(key), key);
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
  public BXServletRequest(final HttpServletRequest r) {
    req = r;
    method = Request.Method.valueOf(r.getMethod());
    url = r.getRequestURL().toString(); // MiltonUtils.stripContext(r);
    REQUEST.set(r);
  }

  /**
   * Returns the current session associated with this request, or if the request
   * does not have a session, creates one.
   * @return the <code>HttpSession</code> associated with this request
   */
  public HttpSession getSession() {
    return req.getSession();
  }

  @Override
  public String getFromAddress() {
    return req.getRemoteHost();
  }

  @Override
  public String getRequestHeader(final Request.Header header) {
    return req.getHeader(header.code);
  }

  @Override
  public Request.Method getMethod() {
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
    final String enc = getRequestHeader(Request.Header.AUTHORIZATION);
    if(enc == null || enc.length() == 0) return null;
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
    @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
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
  public static void parseQueryString(final Map<String, String> map,
      final String qs) {
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
  protected Response.ContentType getRequestContentType() {
    final String s = req.getContentType();
    if(s == null) return null;
    if(s.contains(Response.MULTIPART)) return ContentType.MULTIPART;
    return TYPE_CONTENTS.get(s);
  }

  /**
   * Is the content type of the request a multi-part?
   * @return {@code true} if the request is multi-part
   */
  protected boolean isMultiPart() {
    return ContentType.MULTIPART.equals(getRequestContentType());
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
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
class FileItemWrapper implements com.bradmcevoy.http.FileItem {
  /** Wrapped file item. */
  final FileItem wrapped;
  /** File name. */
  final String name;

  /**
   * Strip path information provided by IE.
   * @param s string
   * @return stripped string
   */
  public static String fixIEFileName(final String s) {
    final int pos = s.lastIndexOf('\\');
    return pos < 0 ? s : s.substring(pos + 1);
  }

  /**
   * Constructor.
   * @param f file item
   */
  public FileItemWrapper(final FileItem f) {
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
