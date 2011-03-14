package org.basex.query.util;

import static org.basex.data.DataText.*;
import static java.lang.Integer.*;
import static java.net.HttpURLConnection.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.file.HTMLParser;
import org.basex.build.xml.XMLParser;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.IOContent;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.iter.AxisIter;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;
import org.basex.util.TokenMap;

/**
 * HTTP Client.
 * 
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class HttpClient {
  /** Request element attributes. */
  /** Request attribute: HTTP method. */
  private static final byte[] METHOD = token("method");
  /** Request attribute: HTTP URI. */
  private static final byte[] HREF = token("href");
  /** Request attribute: status-only. */
  private static final byte[] STATUSONLY = token("status-only");
  /** Request attribute: username. */
  private static final byte[] USRNAME = token("username");
  /** Request attribute: password. */
  private static final byte[] PASSWD = token("password");
  /** Request attribute: send-authorization. */
  private static final byte[] SENDAUTH = token("send-authorization");
  /** Request attribute: override-media-type. */
  private static final byte[] OVERMEDIATYPE = token("override-media-type");
  /** Request attribute: follow-redirect. */
  private static final byte[] REDIR = token("follow-redirect");
  /** Request attribute: timeout. */
  private static final byte[] TIMEOUT = token("timeout");

  /** Header element. */
  /** http:header element. */
  private static final byte[] HEADER = token("http:header");
  /** Header attribute: name. */
  private static final byte[] HDR_NAME = token("name");
  /** Header attribute: value. */
  private static final byte[] HDR_VALUE = token("value");

  /** http:multipart element. */
  private static final byte[] MULTIPART = token("http:multipart");

  /** Body element. */
  /** http:body element. */
  private static final byte[] BODY = token("http:body");
  /** Body attribute: media-type. */
  private static final byte[] MEDIATYPE = token("media-type");
  /** Body attribute: src. */
  private static final byte[] SRC = token("src");

  /** Response element. */
  /** http:response element. */
  private static final byte[] RESPONSE = token("http:response");
  /** Response attribute: status. */
  private static final byte[] STATUS = token("status");
  /** Response attribute: message. */
  private static final byte[] MSG = token("message");

  /** Media Types. */
  /** XML media type. */
  private static final byte[] APPL_XHTML = token("application/html+xml");
  /** XML media type. */
  private static final byte[] APPL_XML = token("application/xml");
  /** XML media type. */
  private static final byte[] APPL_EXT_XML = token("application/xml-external-parsed-entity");
  /** XML media type. */
  private static final byte[] TXT_XML = token("text/xml");
  /** XML media type. */
  private static final byte[] TXT_EXT_XML = token("text/xml-external-parsed-entity");
  /** XML media types' suffix. */
  private static final byte[] MIME_XML_SUFFIX = token("+xml");
  /** HTML media type. */
  private static final byte[] TXT_HTML = token("text/html");
  /** Text media types' prefix. */
  private static final byte[] MIME_TEXT_PREFIX = token("text/");

  /** HTTP header: Content-Type. */
  private static final String CONT_TYPE = "Content-Type";
  /** HTTP header: Authorization. */
  private static final String AUTH = "Authorization";
  /** HTTP basic authentication. */
  private static final String AUTH_BASIC = "Basic ";

  /** Attributes. */
  private final TokenMap reqAttrs;
  /** Headers. */
  private final TokenMap headers;
  /** Multipart; currently not used. */
  @SuppressWarnings("unused")
  private ANode multipart;
  /** Body. */
  private ANode body;

  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param request request element
   * @param href HTTP uri
   * @param ii input info
   * @throws QueryException query exception
   */
  public HttpClient(final ANode request, final byte[] href, final InputInfo ii)
      throws QueryException {

    info = ii;
    reqAttrs = new TokenMap();
    headers = new TokenMap();
    readRequestAttributes(request);
    readRequestChildren(request, ii);
    if(href != null) reqAttrs.add(HREF, href);
  }

  /**
   * Scans the attributes of the http:request element.
   * @param request request element
   * @throws QueryException query exception
   */
  private void readRequestAttributes(final ANode request) throws QueryException {

    final AxisIter ai = request.atts();
    ANode n = null;
    while((n = ai.next()) != null)
      reqAttrs.add(n.nname(), n.atom());

    // If authorization is to be sent, check that both user name and password
    // are provided
    final byte[] sendAuth = reqAttrs.get(SENDAUTH);
    if(sendAuth != null && Boolean.parseBoolean(string(sendAuth))) {
      final byte[] usrname = reqAttrs.get(USRNAME);
      final byte[] passwd = reqAttrs.get(PASSWD);
      if(usrname == null ^ passwd != null) CREDSERR.thrw(info);
    }
  }

  /**
   * Reads the children of the http:request element.
   * @param request request element
   * @param ii input info
   * @throws QueryException query exception
   */
  private void readRequestChildren(final ANode request, final InputInfo ii)
      throws QueryException {

    final AxisIter ai = request.children();
    ANode n = null;
    while((n = ai.next()) != null) {
      // Header children
      if(eq(n.nname(), HEADER)) {
        final AxisIter attrs = n.atts();
        ANode attr = null;
        byte[] name = null;
        byte[] value = null;

        while((attr = attrs.next()) != null) {
          if(eq(attr.nname(), HDR_NAME)) name = attr.atom();
          if(eq(attr.nname(), HDR_VALUE)) value = attr.atom();

          if(name != null && name.length != 0 && value != null
              && value.length != 0) {
            headers.add(name, value);
            break;
          }
        }
      } else if(eq(n.nname(), MULTIPART)) {
        // [RS] not supported yet
        multipart = n;
      } else if(eq(n.nname(), BODY)) {
        body = n;
      } else {
        // Body child
        throw REQINV.thrw(ii);
      }
    }
  }

  /**
   * Sends an HTTP request.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  public Iter sendHttpRequest(final QueryContext ctx) throws QueryException {
    final String adr = string(reqAttrs.get(HREF));
    try {
      final URL url = new URL(adr);
      final HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      try {
        setConnectionProps(conn);
        setHttpRequestHeaders(conn);
        if(body != null) setHttpRequestContent(conn);
        conn.getResponseCode();
        return getHttpResponse(conn, ctx);
      } finally {
        conn.disconnect();
      }
    } catch(final MalformedURLException ex) {
      throw URLINV.thrw(info, adr);
    } catch(final ProtocolException ex) {
      throw PROTINV.thrw(info);
    } catch(final IOException ex) {
      ex.printStackTrace();
      throw HTTPERR.thrw(info, ex);
    }
  }

  /**
   * Sets the connection properties.
   * @param conn HTTP connection
   * @throws ProtocolException protocol exception
   * @throws QueryException query exception
   */
  private void setConnectionProps(final HttpURLConnection conn)
      throws ProtocolException, QueryException {
    if(body != null) conn.setDoOutput(true);
    conn.setRequestMethod(string(reqAttrs.get(METHOD)).toUpperCase());
    if(reqAttrs.get(TIMEOUT) != null) conn.setConnectTimeout(parseInt(string(reqAttrs.get(TIMEOUT))));
    if(reqAttrs.get(REDIR) != null) setFollowRedirects(Bln.parse(
        reqAttrs.get(REDIR), info));
  }

  /**
   * Sets HTTP request headers.
   * @param conn HTTP connection
   * @throws QueryException query exception
   */
  private void setHttpRequestHeaders(final HttpURLConnection conn)
      throws QueryException {

    final byte[][] headerNames = headers.keys();

    for(final byte[] headerName : headerNames)
      conn.addRequestProperty(string(headerName),
          string(headers.get(headerName)));
    // HTTP Basic Authentication
    final byte[] sendAuth = reqAttrs.get(SENDAUTH);
    if(sendAuth != null && Bln.parse(sendAuth, info)) conn.setRequestProperty(
        AUTH,
        encodeCredentials(string(reqAttrs.get(USRNAME)),
            string(reqAttrs.get(PASSWD))));
  }

  /**
   * Sets the content of the HTTP request.
   * @param conn HTTP connection
   * @throws IOException I/O exception
   */
  private void setHttpRequestContent(final HttpURLConnection conn)
      throws IOException {

    final AxisIter attrs = body.atts();
    final StringBuilder sb = new StringBuilder();

    byte[] mediaType = null;
    String src = null;
    String method = null;

    ANode attr = null;
    while((attr = attrs.next()) != null) {
      if(eq(attr.nname(), MEDIATYPE)) mediaType = attr.atom();
      else if(eq(attr.nname(), SRC)) src = string(attr.atom());
      else if(eq(attr.nname(), METHOD)) method = string(attr.atom());
      else sb.append(string(attr.nname())).append('=').append(
          string(attr.atom()));
    }

    conn.setRequestProperty(CONT_TYPE, string(mediaType));

    if(src == null) {
      // Set serial parameter "method" according to MIME type
      sb.append("method=");
      if(method == null) {
        if(eq(mediaType, APPL_XHTML)) sb.append(M_XHTML);
        else if(eq(mediaType, APPL_XML) || eq(mediaType, APPL_EXT_XML)
            || eq(mediaType, TXT_XML) || eq(mediaType, TXT_EXT_XML)
            || endsWith(mediaType, MIME_XML_SUFFIX)) sb.append(M_XML);
        else if(eq(mediaType, TXT_HTML)) sb.append(M_HTML);
        else if(startsWith(mediaType, MIME_TEXT_PREFIX)) sb.append(M_TEXT);
        else sb.append(M_XML);
      } else {
        sb.append(method);
      }

      // Serialize request content according to the
      // serialization parameters
      final OutputStream out = conn.getOutputStream();
      final SerializerProp serialProp = new SerializerProp(sb.toString());
      try {
        final XMLSerializer xml = new XMLSerializer(out, serialProp);
        final AxisIter ai = body.children();
        ANode child = null;
        while((child = ai.next()) != null)
          child.serialize(xml);
      } finally {
        out.close();
      }
    } else {
      // [RS] If the src attribute is present, the serialization
      // parameters shall be ignored
    }
  }

  /**
   * Gets the HTTP response.
   * @param conn HTTP connection
   * @param ctx query context
   * @return return result
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private Iter getHttpResponse(final HttpURLConnection conn,
      final QueryContext ctx) throws IOException, QueryException {

    // Construct http:response element
    final FElem responseElem = new FElem(new QNm(RESPONSE), null);
    setResponseChildren(conn, responseElem);
    setResponseAttrs(conn, responseElem);

    final ItemCache iter = new ItemCache();
    iter.add(responseElem);

    final byte[] attrStatusOnly = reqAttrs.get(STATUSONLY);

    // Get response content if required
    if(attrStatusOnly == null || !Bln.parse(attrStatusOnly, info)) iter.add(setResultContent(
        conn, ctx));
    return iter;
  }

  /**
   * Sets response element attributes.
   * @param conn HTTP connection
   * @param par parent node
   * @throws IOException I/O exception
   */
  private void setResponseAttrs(final HttpURLConnection conn, final FElem par)
      throws IOException {

    final FAttr attrStatus = new FAttr(new QNm(STATUS, QueryTokens.HTTPURI),
        token(conn.getResponseCode()), par);

    final FAttr attrStatusMsg = new FAttr(new QNm(MSG, QueryTokens.HTTPURI),
        token(conn.getResponseMessage()), par);

    par.atts.add(attrStatus);
    par.atts.add(attrStatusMsg);
  }

  /**
   * Sets response element children.
   * @param conn HTTP connection
   * @param par parent node
   */
  private void setResponseChildren(final HttpURLConnection conn, final FElem par) {

    // Set header children
    for(final String headerName : conn.getHeaderFields().keySet()) {
      if(headerName != null) {
        final FElem elem = new FElem(new QNm(HEADER), par);
        elem.atts.add(new FAttr(new QNm(HDR_NAME), token(headerName), elem));
        elem.atts.add(new FAttr(new QNm(HDR_VALUE),
            token(conn.getHeaderField(headerName)), elem));
        par.children.add(elem);
      }
    }

    // Set body child
    final FElem elem = new FElem(new QNm(BODY), par);
    elem.atts.add(new FAttr(new QNm(MEDIATYPE), token(conn.getContentType()),
        elem));
    par.children.add(elem);
  }

  /**
   * Sets the result content.
   * @param conn HTTP connection
   * @param ctx query context
   * @return item with the result content
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private Item setResultContent(final HttpURLConnection conn,
      final QueryContext ctx) throws IOException, QueryException {

    final byte[] contentType = reqAttrs.get(OVERMEDIATYPE) == null ? token(conn.getContentType())
        : reqAttrs.get(OVERMEDIATYPE);

    if(eq(contentType, TXT_XML) || eq(contentType, TXT_EXT_XML)
        || eq(contentType, APPL_XML) || eq(contentType, APPL_EXT_XML)
        || endsWith(contentType, MIME_XML_SUFFIX))
    // Parse XML
    return processXML(conn, ctx);
    else if(eq(contentType, TXT_HTML)) {
      // Parse HTML
      if(!HTMLParser.available()) throw HTMLERR.thrw(info);
      return processHTML(conn, ctx);
    } else if(startsWith(contentType, MIME_TEXT_PREFIX))
    // Process text content
    return Str.get(readHttpContent(conn));
    else
    // TODO: parse as binary type
    return null;
  }

  /**
   * Processes XML content.
   * @param conn HTTP connection
   * @param ctx query context
   * @return item with parsed content
   * @throws IOException I/O exception
   */
  private Item processXML(final HttpURLConnection conn, final QueryContext ctx)
      throws IOException {
    System.out.println("Process XML...");
    final IOContent io = new IOContent(readHttpContent(conn));
    System.out.println("IOContent created...");
    final Parser parser = new XMLParser(io, null, ctx.context.prop);
    System.out.println("XML parser created....");
    return new DBNode(MemBuilder.build(parser, ctx.context.prop, ""), 0);
  }

  /**
   * Processes HTML.
   * @param conn HTTP connection
   * @param ctx query context
   * @return item with parsed content
   * @throws IOException I/O exception
   */
  private Item processHTML(final HttpURLConnection conn, final QueryContext ctx)
      throws IOException {

    final IOContent io = new IOContent(readHttpContent(conn));
    final Parser parser = new HTMLParser(io, null, ctx.context.prop);
    return new DBNode(MemBuilder.build(parser, ctx.context.prop, ""), 0);
  }

  /**
   * Reads HTTP response content.
   * @param conn HTTP connection
   * @return content
   * @throws IOException I/O exception
   */
  private byte[] readHttpContent(final HttpURLConnection conn)
      throws IOException {

    final InputStream input = conn.getInputStream();

    final ByteList bl = new ByteList();
    final BufferedInputStream bis = new BufferedInputStream(input);
    int i = 0;
    try {
      while((i = bis.read()) != -1)
        bl.add(i);
    } finally {
      bis.close();
    }
    return bl.toArray();
  }

  /**
   * Encodes credentials with Base64 encoding.
   * @param usrname user name
   * @param passwd password
   * @return encoded credentials
   */
  private String encodeCredentials(final String usrname, final String passwd) {
    final B64 b64 = new B64(token(usrname + ":" + passwd));
    return AUTH_BASIC + string(b64.atom());
  }
}
