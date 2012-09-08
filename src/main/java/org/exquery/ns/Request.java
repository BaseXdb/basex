package org.exquery.ns;

import java.util.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * This module contains functions for handling servlet requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Request extends QueryModule {
  /**
   * Returns the method of a request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str method() throws QueryException {
    return Str.get(request().getMethod());
  }

  /**
   * Returns the Scheme component of a request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str scheme() throws QueryException {
    return Str.get(request().getScheme());
  }

  /**
   * Returns the Hostname fragment of a request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str hostname() throws QueryException {
    return Str.get(request().getServerName());
  }

  /**
   * Returns the Port fragment of a request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Int port() throws QueryException {
    return Int.get(request().getServerPort());
  }

  /**
   * Returns the path of the request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str path() throws QueryException {
    return Str.get(request().getRequestURI());
  }

  /**
   * Returns the query string of a request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str query() throws QueryException {
    final String query = request().getQueryString();
    return query == null ? null : Str.get(query);
  }

  /**
   * Returns the URI of a request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Uri uri() throws QueryException {
    return Uri.uri(request().getRequestURL().toString());
  }

  /**
   * Returns the query string of a request.
   * @return path
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str address() throws QueryException {
    return Str.get(request().getLocalAddr());
  }

  /**
   * Returns the remote host name of a request.
   * @return host name
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str remoteHostname() throws QueryException {
    return Str.get(request().getRemoteHost());
  }

  /**
   * Returns the remote host name of a request.
   * @return host name
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str remoteAddress() throws QueryException {
    return Str.get(request().getRemoteAddr());
  }

  /**
   * Returns the remote host name of a request.
   * @return host name
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Int remotePort() throws QueryException {
    return Int.get(request().getRemotePort());
  }

  /**
   * Returns the names of all query parameters.
   * @return parameter names
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Value parameterNames() throws QueryException {
    final TokenList tl = new TokenList();
    for(final String s : request().getParameterMap().keySet()) tl.add(s);
    return StrSeq.get(tl);
  }

  /**
   * Returns the value of a specific query parameter.
   * @param key key to be requested
   * @return parameter value
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Value parameter(final Str key) throws QueryException {
    return parameter(key, null);
  }

  /**
   * Returns the value of a specific query parameter.
   * @param key key to be requested
   * @param def default value
   * @return parameter value
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Value parameter(final Str key, final Value def) throws QueryException {
    final String[] val = request().getParameterValues(key.toJava());
    if(val == null) return def;
    final TokenList tl = new TokenList(val.length);
    for(final String v : val) tl.add(v);
    return StrSeq.get(tl);
  }

  /**
   * Returns the names of all header parameters.
   * @return parameter names
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Value headerNames() throws QueryException {
    final TokenList tl = new TokenList();
    final Enumeration<String> en = request().getHeaderNames();
    while(en.hasMoreElements()) tl.add(en.nextElement());
    return StrSeq.get(tl);
  }

  /**
   * Returns the value of a specific header parameter.
   * @param key key to be requested
   * @return parameter value
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str header(final Str key) throws QueryException {
    return header(key, null);
  }

  /**
   * Returns the value of a specific header parameter.
   * @param key key to be requested
   * @param def default value
   * @return parameter value
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str header(final Str key, final Str def) throws QueryException {
    final String val = request().getHeader(key.toJava());
    return val == null ? def : Str.get(val);
  }

  /**
   * Returns all cookie names.
   * @return parameter names
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Value cookieNames() throws QueryException {
    final TokenList tl = new TokenList();
    for(final Cookie c : request().getCookies()) tl.add(c.getName());
    return StrSeq.get(tl);
  }

  /**
   * Returns the value of a specific cookie.
   * @param key key to be requested
   * @return parameter value
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str cookie(final Str key) throws QueryException {
    return cookie(key, null);
  }

  /**
   * Returns the value of a specific cookie.
   * @param key key to be requested
   * @param def default value
   * @return parameter value
   * @throws QueryException query exception
   */
  @Deterministic @Requires(Permission.NONE)
  public Str cookie(final Str key, final Str def) throws QueryException {
    final String k = key.toJava();
    for(final Cookie c : request().getCookies()) {
      if(c.getName().equals(k)) return Str.get(c.getValue());
    }
    return def;
  }

  /**
   * Returns the servlet request instance.
   * @return request
   * @throws QueryException query exception
   */
  private HttpServletRequest request() throws QueryException {
    if(context.http == null) throw new QueryException("Servlet context required.");
    return ((HTTPContext) context.http).req;
  }
}
