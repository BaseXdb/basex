package org.basex.query.util;

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
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.Atts;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;
import org.basex.util.TokenMap;

/**
 * HTTP Client.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  private static final byte[] APPL_EXT_XML =
    token("application/xml-external-parsed-entity");
  /** XML media type. */
  private static final byte[] TXT_XML = token("text/xml");
  /** XML media type. */
  private static final byte[] TXT_EXT_XML =
    token("text/xml-external-parsed-entity");
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
  /** Multipart. */
  @SuppressWarnings("unused")
  private Nod multipart;
  /** Body. */
  private Nod body;

  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param request XQuery request element
   * @param ii input info
   * @throws QueryException query exception
   */
  public HttpClient(final Nod request, final InputInfo ii)
      throws QueryException {
    info = ii;
    reqAttrs = new TokenMap();
    headers = new TokenMap();
    readRequestAttributes(request);
    readRequestChildren(request, ii);
  }

  /**
   * Constructor.
   * @param request request element
   * @param href HTTP uri
   * @param ii input info
   * @throws QueryException query exception
   */
  public HttpClient(final Nod request, final byte[] href, final InputInfo ii)
      throws QueryException {
    this(request, ii);
    reqAttrs.add(HREF, href);
  }

  /**
   * Scans the attributes of the http:request element.
   * @param request request element
   * @throws QueryException query exception
   */
  private void readRequestAttributes(final Nod request) throws QueryException {
    final NodeIter attrs = request.attr();

    Nod n = null;
    while((n = attrs.next()) != null) reqAttrs.add(n.nname(), n.atom());

    // If authorization is to be sent, check that both user name and password
    // are provided
    final byte[] sendAuth = reqAttrs.get(SENDAUTH);
    if((sendAuth != null) && Boolean.parseBoolean(string(sendAuth))) {
      final byte[] usrname = reqAttrs.get(USRNAME);
      final byte[] passwd = reqAttrs.get(PASSWD);

      if(usrname == null && passwd != null
          || usrname != null && passwd == null)
        throw new QueryException(info, CREDSERR);
    }
  }

  /**
   * Reads the children of the http:request element.
   * @param request request element
   * @param ii input info
   * @throws QueryException query exception
   */
  private void readRequestChildren(final Nod request, final InputInfo ii)
      throws QueryException {

    final NodeIter children = request.child();
    Nod n = null;

    while((n = children.next()) != null) {

      // Header children
      if(eq(n.nname(), HEADER)) {

        final NodeIter attrs = n.attr();
        Nod attr = null;
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
      } else if(eq(n.nname(), MULTIPART)) multipart = n;
      // Body child
      else if(eq(n.nname(), BODY)) body = n;
      else REQINV.thrw(ii);

    }
  }

  /**
   * Sends an HTTP request.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  public Iter sendHttpRequest(final QueryContext ctx) throws QueryException {
    try {
      final URL url = new URL(string(reqAttrs.get(HREF)));
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
    } catch(MalformedURLException ex) {
      throw new QueryException(info, URLINV, ex);
    } catch(ProtocolException ex) {
      throw new QueryException(info, PROTINV, ex);
    } catch(IOException ex) {
      throw new QueryException(info, HTTPERR, ex.getMessage());
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
    if(reqAttrs.get(TIMEOUT) != null)
      conn.setConnectTimeout(parseInt(string(reqAttrs.get(TIMEOUT))));
    if(reqAttrs.get(REDIR) != null)
      setFollowRedirects(Bln.parse(reqAttrs.get(REDIR), info));
  }

  /**
   * Sets HTTP request headers.
   * @param conn HTTP connection
   * @throws QueryException query exception
   */
  private void setHttpRequestHeaders(final HttpURLConnection conn)
    throws QueryException {

    final byte[][] headerNames = headers.keys();

    for(int i = 0; i < headerNames.length; i++)
      conn.addRequestProperty(string(headerNames[i]),
          string(headers.get(headerNames[i])));
    // HTTP Basic Authentication
    final byte[] sendAuth = reqAttrs.get(SENDAUTH);
    if(sendAuth != null && Bln.parse(sendAuth, info))
      conn.setRequestProperty(
          AUTH,
          encodeCredentials(
              string(reqAttrs.get(USRNAME)),
              string(reqAttrs.get(PASSWD))));
  }

  /**
   * Sets the content of the HTTP request.
   * @param conn HTTP connection
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void setHttpRequestContent(final HttpURLConnection conn)
      throws QueryException, IOException {

    final NodeIter attrs = body.attr();
    final StringBuilder sb = new StringBuilder();

    byte[] mediaType = null;
    String src = null;
    String method = null;

    Nod attr = null;
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
        if(eq(mediaType, APPL_XHTML)) sb.append("xhtml");
        else if(eq(mediaType, APPL_XML) || eq(mediaType, APPL_EXT_XML)
            || eq(mediaType, TXT_XML) || eq(mediaType, TXT_EXT_XML)
            || endsWith(mediaType, MIME_XML_SUFFIX)) sb.append("xml");
        else if(eq(mediaType, TXT_HTML)) sb.append("html");
        else if(startsWith(mediaType, MIME_TEXT_PREFIX))
          sb.append("text");
        else sb.append("xml");
      } else {
        sb.append(method);
      }

      // Serialize request content according to the
      // serialization parameters
      final OutputStream out = conn.getOutputStream();
      final SerializerProp serialProp = new SerializerProp(sb.toString());
      try {
        final XMLSerializer xml = new XMLSerializer(out, serialProp);

        final NodeIter children = body.child();
        Nod child = null;
        while((child = children.next()) != null) child.serialize(xml);
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
    final FElem responseElem = new FElem(new QNm(RESPONSE),
        setResponseChildren(conn), setResponseAttrs(conn), EMPTY, new Atts());

    final ItemIter iter = new ItemIter();
    iter.add(responseElem);

    final byte[] attrStatusOnly = reqAttrs.get(STATUSONLY);

    // Get response content if required
    if(attrStatusOnly == null || !Bln.parse(attrStatusOnly, info))
      iter.add(setResultContent(conn, ctx));
    return iter;
  }

  /**
   * Sets response element attributes.
   * @param conn HTTP connection
   * @return response attributes
   * @throws IOException I/O exception
   */
  private NodIter setResponseAttrs(final HttpURLConnection conn)
      throws IOException {

    final NodIter responseAttrs = new NodIter();

    final FAttr attrStatus = new FAttr(new QNm(STATUS, QueryTokens.HTTPURI),
        token(conn.getResponseCode()), null);

    final FAttr attrStatusMsg = new FAttr(new QNm(MSG, QueryTokens.HTTPURI),
        token(conn.getResponseMessage()), null);

    responseAttrs.add(attrStatus);
    responseAttrs.add(attrStatusMsg);

    return responseAttrs;
  }

  /**
   * Sets response element children.
   * @param conn HTTP connection
   * @return response children
   */
  private NodIter setResponseChildren(final HttpURLConnection conn) {

    final NodIter ch = new NodIter();

    // Set header children
    for(final String headerName : conn.getHeaderFields().keySet()) {
      if(headerName != null) {
        final NodIter headerAttrs = new NodIter();
        headerAttrs.add(new FAttr(new QNm(HDR_NAME), token(headerName), null));
        headerAttrs.add(new FAttr(new QNm(HDR_VALUE),
            token(conn.getHeaderField(headerName)), null));
        ch.add(new FElem(new QNm(HEADER), new NodIter(), headerAttrs, EMPTY,
            new Atts()));
      }
    }

    // Set body child
    final NodIter bodyAttrs = new NodIter();
    bodyAttrs.add(new FAttr(new QNm(MEDIATYPE), token(conn.getContentType()),
        null));
    ch.add(new FElem(new QNm(BODY), new NodIter(), bodyAttrs, EMPTY,
        new Atts()));

    return ch;
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

    final byte[] contentType = reqAttrs.get(OVERMEDIATYPE) == null ?
        token(conn.getContentType()) : reqAttrs.get(OVERMEDIATYPE);

    if(eq(contentType, TXT_XML) || eq(contentType, TXT_EXT_XML)
        || eq(contentType, APPL_XML) || eq(contentType, APPL_EXT_XML)
        || endsWith(contentType, MIME_XML_SUFFIX))
      // Parse XML
      return processXML(conn, ctx);
    else if(eq(contentType, TXT_HTML)) {
      // Parse HTML
      if(HTMLParser.isAvailable()) return processHTML(conn, ctx);
      HTMLERR.thrw(info); return null;
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

    final IOContent io = new IOContent(readHttpContent(conn));
    final Parser parser = new XMLParser(io, null, ctx.resource.context.prop);

    return new 
      DBNode(MemBuilder.build(parser, ctx.resource.context.prop, ""), 0);
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
    final Parser parser = new HTMLParser(io, null, ctx.resource.context.prop);

    return new 
      DBNode(MemBuilder.build(parser, ctx.resource.context.prop, ""), 0);
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
    final int len = conn.getContentLength();

    if(len != -1) {
      final byte[] content = new byte[len];
      try {
        input.read(content);
      } finally {
        input.close();
      }
      return content;
    }

    final ByteList bl = new ByteList();
    final BufferedInputStream bis = new BufferedInputStream(input);
    int i = 0;
    try {
      while((i = bis.read()) != -1) bl.add(i);
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
