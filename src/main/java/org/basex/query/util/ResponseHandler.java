package org.basex.query.util;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.file.HTMLParser;
import org.basex.build.xml.XMLParser;
import org.basex.core.Prop;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.util.Atts;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * HTTP response.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class ResponseHandler {

  /** Response element. */
  /** http:response element. */
  private static final byte[] RESPONSE = token("http:response");
  /** http:multipart element. */
  private static final byte[] MULTIPART = token("http:multipart");
  /** part element. */
  private static final byte[] PART = token("part");
  /** boundary marker. */
  private static final byte[] BOUNDARY = token("boundary");

  /** Header element. */
  /** http:header element. */
  private static final byte[] HEADER = token("http:header");
  /** Header attribute: name. */
  private static final byte[] HDR_NAME = token("name");
  /** Header attribute: value. */
  private static final byte[] HDR_VALUE = token("value");

  /** Body element. */
  /** http:body element. */
  private static final byte[] BODY = token("http:body");
  /** Body attribute: media-type. */
  private static final byte[] MEDIATYPE = token("media-type");

  /** Response attribute: status. */
  private static final byte[] STATUS = token("status");
  /** Response attribute: message. */
  private static final byte[] MSG = token("message");

  /** Media Types. */
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

  /** Content type text/plain. */
  private static final byte[] TXT_PLAIN = token("text/plain");
  /** HTTP header Content-Type lower case. */
  private static final byte[] CONT_TYPE_LC = token("content-type");

  /**
   * Constructor.
   */
  private ResponseHandler() {

  }

  /**
   * Constructs http:response element.
   * @param conn HTTP connection
   * @param statusOnly indicates if content is required
   * @param mediaType content type provided by the user to interpret the
   *          response content
   * @param prop query context properties
   * @param ii input info
   * @return result
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  public static Iter getResponse(final HttpURLConnection conn,
      final byte[] statusOnly, final byte[] mediaType, final Prop prop,
      final InputInfo ii) throws IOException, QueryException {

    final NodeCache attrs = extractAttrs(conn);
    final NodeCache hdrs = extractHdrs(conn);
    final byte[] contentType = mediaType ==
      null ? extractContentType(conn.getContentType())
        : mediaType;
    final ItemCache payloads = new ItemCache();

    final ANode body;
    final boolean b = statusOnly != null && Bln.parse(statusOnly, ii);
    if(startsWith(contentType, token("multipart"))) {
      final byte[] boundary = extractBoundary(conn.getContentType(), ii);

      final NodeCache a = new NodeCache();
      a.add(new FAttr(new QNm(MEDIATYPE, EMPTY), contentType, null));
      a.add(new FAttr(new QNm(BOUNDARY, EMPTY), boundary, null));

      body = new FElem(new QNm(MULTIPART, HTTPURI), extractParts(
          conn.getInputStream(), b, payloads, concat(token("--"), boundary),
          prop, ii), a, EMPTY, new Atts().add(HTTP, HTTPURI), null);
    } else {
      body = createBody(contentType);
      if(!b) payloads.add(interpretPayload(
          extractPayload(conn.getInputStream()), contentType, prop, ii));
    }

    // Construct http:response element
    final FElem responseEl = new FElem(new QNm(RESPONSE, HTTPURI), hdrs, attrs,
        EMPTY, new Atts().add(HTTP, HTTPURI), null);

    responseEl.children.add(body);

    // Result
    final ItemCache result = new ItemCache();
    result.add(responseEl);
    result.add(payloads);
    return result;
  }

  /**
   * Extracts status code and status message in order to set them later as
   * attributes of http:response element.
   * @param conn http connection
   * @return node cache with attributes
   * @throws IOException IO exception
   */
  private static NodeCache extractAttrs(final HttpURLConnection conn)
      throws IOException {
    final NodeCache a = new NodeCache();
    a.add(new FAttr(new QNm(STATUS, EMPTY),
        token(conn.getResponseCode()), null));
    a.add(new FAttr(new QNm(MSG, EMPTY),
        token(conn.getResponseMessage()), null));

    return a;
  }

  /**
   * Extracts response headers in order to set them later as children of the
   * http:response element.
   * @param conn HTTP connection
   * @return node cache with http:header elements
   */
  private static NodeCache extractHdrs(final HttpURLConnection conn) {

    final NodeCache h = new NodeCache();
    for(final String headerName : conn.getHeaderFields().keySet()) {
      if(headerName != null) {
        final FElem hdr = new FElem(new QNm(HEADER, HTTPURI), null, null, null,
            new Atts().add(HTTP, HTTPURI), null);
        hdr.atts.add(new FAttr(new QNm(HDR_NAME, EMPTY),
            token(headerName), hdr));
        hdr.atts.add(new FAttr(new QNm(HDR_VALUE, EMPTY),
            token(conn.getHeaderField(headerName)), hdr));
        h.add(hdr);
      }
    }
    return h;
  }

  /**
   * Creates a body element.
   * @param mediaType content type
   * @return body
   */
  private static FElem createBody(final byte[] mediaType) {
    final FElem b = new FElem(new QNm(BODY, HTTPURI), null, null, null,
        new Atts().add(HTTP, HTTPURI), null);
    b.atts.add(new FAttr(new QNm(MEDIATYPE, EMPTY), mediaType, null));

    return b;
  }

  /**
   * Extracts payload from HTTP message and returns it as an item.
   * @param io connection input stream
   * @return payload as byte array
   * @throws IOException IO exception
   */
  private static byte[] extractPayload(final InputStream io)
  throws IOException {
    final BufferedInputStream bis = new BufferedInputStream(io);
    try {
      final ByteList bl = new ByteList();
      int i = 0;
      while((i = bis.read()) != -1)
        bl.add(i);
      return bl.toArray();
    } finally {
      bis.close();
    }
  }

  /**
   * Interprets payload according to content type and returns a corresponding
   * item: - text content type => string - XML or HTML content type => document
   * node - binary content type => base64Binary.
   * @param p payload
   * @param c content type
   * @param prop context properties
   * @param ii input info
   * @return interpreted payload
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  private static Item interpretPayload(final byte[] p, final byte[] c,
      final Prop prop, final InputInfo ii) throws IOException, QueryException {

    final Parser parser;
    if(eq(c, TXT_XML) || eq(c, TXT_EXT_XML) || eq(c, APPL_XML)
        || eq(c, APPL_EXT_XML) || endsWith(c, MIME_XML_SUFFIX)) {
      parser = new XMLParser(new IOContent(p), null, prop);
    } else if(eq(c, TXT_HTML)) {
      if(!HTMLParser.available()) throw HTMLERR.thrw(ii);
      parser = new HTMLParser(new IOContent(p), null, prop);
    } else if(startsWith(c, MIME_TEXT_PREFIX)) {
      return Str.get(p);
    } else return new B64(p);

    return new DBNode(MemBuilder.build(parser, prop, ""), 0);

  }

  /**
   * Extracts the parts from a multipart message.
   * @param io connection input stream
   * @param statusOnly indicates if content is required
   * @param payloads item cache for part payloads
   * @param sep separation boundary
   * @param prop context properties
   * @param ii input info
   * @return array list will all parts
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  private static NodeCache extractParts(final InputStream io,
      final boolean statusOnly, final ItemCache payloads, final byte[] sep,
      final Prop prop, final InputInfo ii) throws IOException, QueryException {

    try {
      // Read first line of multipart content
      byte[] next = readLine(io);
      // RFC 1341:Preamble shall be ignored -> read till 1st boundary
      while(next != null && !eq(sep, next))
        next = readLine(io);
      // TODO: think what shall happen in such a case
      if(next == null) {
        // return;
      }
      final byte[] end = concat(sep, token("--"));
      FElem nextPart = extractNextPart(io, statusOnly, payloads, sep, end,
          prop, ii);
      final NodeCache p = new NodeCache();
      while(nextPart != null) {
        p.add(nextPart);
        nextPart = extractNextPart(io, statusOnly,
            payloads, sep, end, prop, ii);
      }
      return p;
    } finally {
      io.close();
    }
  }

  /**
   * Extracts a part from a multipart message.
   * @param io connection input stream
   * @param statusOnly indicates if content is required
   * @param payloads item cache for part payloads
   * @param sep separation boundary
   * @param end closing boundary
   * @param prop context properties
   * @param ii input info
   * @return part
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  private static FElem extractNextPart(final InputStream io,
      final boolean statusOnly, final ItemCache payloads, final byte[] sep,
      final byte[] end, final Prop prop, final InputInfo ii)
      throws IOException, QueryException {
    // Content type of part payload - if not defined by header 'Content-Type',
    // it equal to 'text/plain' (RFC 1341)
    byte[] partContType = TXT_PLAIN;
    byte[] firstLine = readLine(io);
    // Last line is reached:
    if(firstLine == null || eq(firstLine, end)) return null;

    final NodeCache partCh = new NodeCache();

    if(firstLine.length == 0) {
      // Part has no headers
      final byte[] p = extractPartPayload(io, sep, end);
      if(!statusOnly) payloads.add(interpretPayload(p, partContType, prop, ii));
    } else {
      // extract headers:
      byte[] nextHdr = firstLine;
      while(nextHdr != null && nextHdr.length > 0) {
        // parse header:
        final int pos = indexOf(nextHdr, ':');
        if(pos > 0) {
          // parse name
          final byte[] name = substring(nextHdr, 0, pos);
          if(pos + 1 < nextHdr.length) {
            // parse value
            final byte[] value = trim(substring(nextHdr, pos + 1,
                nextHdr.length));
            // construct attributes
            final FElem hdr = new FElem(new QNm(HEADER, HTTPURI), null);
            hdr.atts.add(new FAttr(new QNm(HDR_NAME, EMPTY), name, null));
            hdr.atts.add(new FAttr(new QNm(HDR_VALUE, EMPTY), value, null));
            partCh.add(hdr);

            if(eq(lc(name), CONT_TYPE_LC)) partContType = value;
          }
        }
        nextHdr = readLine(io);
      }

      final byte[] p = extractPartPayload(io, sep, end);
      if(!statusOnly) {
        payloads.add(interpretPayload(p, partContType, prop, ii));
      }
    }

    partCh.add(createBody(partContType));
    return new FElem(new QNm(PART, EMPTY), partCh, null, EMPTY, new Atts().add(
        HTTP, HTTPURI), null);
  }

  /**
   * Reads a line of HTTP multipart content.
   * @param in connection input stream
   * @return line
   * @throws IOException IO exception
   */
  private static byte[] readLine(final InputStream in) throws IOException {
    TokenBuilder tb = new TokenBuilder();
    int b;
    while((b = in.read()) != -1) {
      // RFC 1341: a line ends with CRLF
      if(b == '\r') {
        while(true) {
          int b2 = in.read();
          if(b2 == '\n') {
            return tb.finish();
          } else if(b2 == -1) {
            return tb.add(b).finish();
          } else if(b2 == '\r') {
            tb.add(b);
          } else {
            tb.add(b).add(b2);
            break;
          }
        }
      } else {
        tb.add(b);
      }

    }
    return tb.size() == 0 ? null : tb.finish();
  }

  /**
   * Reads the payload of a part.
   * @param io connection input stream
   * @param sep separation boundary
   * @param end closing boundary
   * @return payload
   * @throws IOException IO exception
   */
  private static byte[] extractPartPayload(final InputStream io,
      final byte[] sep, final byte[] end) throws IOException {
    final ByteList bl = new ByteList();
    while(true) {
      final byte[] next = readLine(io);
      if(next == null) {
        // TODO: throw ex
        return bl.toArray();
      }
      if(eq(next, sep)) return bl.toArray();
      if(eq(next, end)) break;
      bl.add(next).add('\n');
    }

    // RFC 1341: Epilogue shall be ignored
    while(readLine(io) != null)
      ;
    return bl.toArray();
  }

  /**
   * Extracts the content from a "Content-type" header.
   * @param c value for "Content-type" header
   * @return result
   */
  private static byte[] extractContentType(final String c) {
    int end = c.indexOf(';');
    return end == -1 ? token(c) : token(c.substring(0, end));
  }

  /**
   * Extracts the encapsulation boundary from the content type.
   * @param c content type
   * @param info input info
   * @return boundary
   * @throws QueryException query exception
   */
  private static byte[] extractBoundary(final String c, final InputInfo info)
      throws QueryException {

    int index = c.lastIndexOf("boundary=");
    if(index == -1) {
      NOBOUND.thrw(info);
    }
    String b = c.substring(index + 9); // 9 for "boundary="
    if(b.charAt(0) == '"') {
      // If the boundary is enclosed in quotes, strip them
      index = b.lastIndexOf('"');
      b = b.substring(1, index);
    }
    return token(b);
  }
}
