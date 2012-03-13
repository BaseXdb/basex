package org.basex.query.util.http;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.query.util.http.HTTPText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * {@code <http:response/>} element.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class HTTPResponse {
  /** Input information. */
  private final InputInfo input;
  /** Database properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param ii input info
   * @param pr database properties
   */
  public HTTPResponse(final InputInfo ii, final Prop pr) {
    input = ii;
    prop = pr;
  }

  /**
   * Constructs http:response element and reads HTTP response content.
   * @param conn HTTP connection
   * @param status indicates if content is required
   * @param mediaTypeOvr content type provided by the user to interpret the
   *          response content
   * @return result sequence of <http:response/> and content items
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  public ValueIter getResponse(final HttpURLConnection conn, final byte[] status,
      final byte[] mediaTypeOvr) throws IOException, QueryException {

    final NodeCache attrs = extractAttrs(conn);
    final NodeCache hdrs = extractHdrs(conn);
    final String cType = mediaTypeOvr == null ?
        extractContentType(conn.getContentType()) : string(mediaTypeOvr);
    final ItemCache payloads = new ItemCache();
    final FNode body;
    final boolean s = status != null && Bln.parse(status, input);

    // multipart response
    if(cType.startsWith(MULTIPART)) {
      final byte[] boundary = extractBoundary(conn.getContentType());
      final NodeCache a = new NodeCache();
      a.add(new FAttr(Q_MEDIA_TYPE, token(cType)));
      a.add(new FAttr(Q_BOUNDARY, boundary));
      body = new FElem(HTTP_MULTIPART, extractParts(conn.getInputStream(), s,
          payloads, concat(token("--"), boundary)), a, new Atts(HTTP, HTTPURI));
      // single part response
    } else {
      body = createBody(cType);
      if(!s) payloads.add(interpretPayload(extractPayload(conn.getInputStream(),
          cType, extractCharset(conn.getContentType())), cType));
    }

    // construct <http:response/>
    final FElem responseEl = new FElem(HTTP_RESPONSE,
        hdrs, attrs, new Atts(HTTP, HTTPURI));
    responseEl.add(body);

    // result
    final ItemCache result = new ItemCache();
    result.add(responseEl);
    result.add(payloads.value());
    return result;
  }

  /**
   * Extracts status code and status message in order to set them later as
   * attributes of <http:response/>.
   * @param conn http connection
   * @return node cache with attributes
   * @throws IOException I/O Exception
   */
  private static NodeCache extractAttrs(final HttpURLConnection conn) throws IOException {
    final NodeCache a = new NodeCache();
    a.add(new FAttr(Q_STATUS, token(conn.getResponseCode())));
    a.add(new FAttr(Q_MESSAGE, token(conn.getResponseMessage())));
    return a;
  }

  /**
   * Extracts response headers in order to set them later as children of
   * <http:response/>.
   * @param conn HTTP connection
   * @return node cache with http:header elements
   */
  private static NodeCache extractHdrs(final HttpURLConnection conn) {
    final NodeCache h = new NodeCache();
    for(final String headerName : conn.getHeaderFields().keySet()) {
      if(headerName != null) {
        final FElem hdr = new FElem(HTTP_HEADER, new Atts(HTTP, HTTPURI));
        hdr.add(new FAttr(Q_NAME, token(headerName)));
        hdr.add(new FAttr(Q_VALUE, token(conn.getHeaderField(headerName))));
        h.add(hdr);
      }
    }
    return h;
  }

  /**
   * Creates a <http:body/> element.
   * @param mediaType content type
   * @return body
   */
  private static FElem createBody(final String mediaType) {
    final FElem b = new FElem(HTTP_BODY, new Atts(HTTP, HTTPURI));
    b.add(new FAttr(Q_MEDIA_TYPE, token(mediaType)));
    return b;
  }

  /**
   * Extracts payload from HTTP message and returns it as a byte array encoded
   * in UTF-8.
   * @param io connection input stream
   * @param c content type
   * @param ce response content charset
   * @return payload as byte array
   * @throws IOException I/O Exception
   */
  private static byte[] extractPayload(final InputStream io, final String c,
      final String ce) throws IOException {

    final BufferedInputStream bis = new BufferedInputStream(io);
    try {
      final ByteList bl = new ByteList();
      for(int i; (i = bis.read()) != -1;) bl.add(i);
      // In case of XML, HTML or text content type, use supplied character set
      if(MimeTypes.isXML(c) || c.equals(MimeTypes.TEXT_HTML) ||
          c.startsWith(MimeTypes.MIME_TEXT_PREFIX))
        return new NewlineInput(new IOContent(bl.toArray()), ce).content();

      // In case of binary data, do not encode anything
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
   * @param ct content type
   * @return interpreted payload
   */
  private Item interpretPayload(final byte[] p, final String ct) {
    try {
      final IOContent io = new IOContent(p);
      io.name(PAYLOAD + IO.XMLSUFFIX);
      return Parser.item(io, prop, ct);
    } catch(final IOException ex) {
      // automagic (to be discussed): return as binary content
      return new B64(p);
    }
  }

  /**
   * Extracts the parts from a multipart message.
   * @param io connection input stream
   * @param status indicates if content is required
   * @param payloads item cache for part payloads
   * @param sep separation boundary
   * @return array list will all parts
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private NodeCache extractParts(final InputStream io, final boolean status,
      final ItemCache payloads, final byte[] sep)
          throws IOException, QueryException {

    try {
      // read first line of multipart content
      byte[] next = readLine(io);
      // RFC 1341: Preamble shall be ignored -> read till 1st boundary
      while(next != null && !eq(sep, next))
        next = readLine(io);
      if(next == null) REQINV.thrw(input, "No body specified for http:part");

      final byte[] end = concat(sep, token("--"));
      FElem nextPart = extractNextPart(io, status, payloads, sep, end);
      final NodeCache p = new NodeCache();
      while(nextPart != null) {
        p.add(nextPart);
        nextPart = extractNextPart(io, status, payloads, sep, end);
      }
      return p;
    } finally {
      io.close();
    }
  }

  /**
   * Extracts a part from a multipart message.
   * @param io connection input stream
   * @param status indicates if content is required
   * @param payloads item cache for part payloads
   * @param sep separation boundary
   * @param end closing boundary
   * @return part
   * @throws IOException I/O Exception
   */
  private FElem extractNextPart(final InputStream io, final boolean status,
      final ItemCache payloads, final byte[] sep, final byte[] end) throws IOException {

    // content type of part payload - if not defined by header 'Content-Type',
    // it is equal to 'text/plain' (RFC 1341)
    String partCType = MimeTypes.TEXT_PLAIN;
    String charset = null;
    final byte[] firstLine = readLine(io);
    // last line is reached:
    if(firstLine == null || eq(firstLine, end)) return null;

    final FElem root = new FElem(Q_PART, new Atts(HTTP, HTTPURI));

    //final NodeCache partCh = new NodeCache();
    if(firstLine.length == 0) {
      // part has no headers
      final byte[] p = extractPartPayload(io, sep, end, null);
      if(!status) payloads.add(interpretPayload(p, partCType));
    } else {
      // extract headers:
      byte[] nextHdr = firstLine;
      while(nextHdr != null && nextHdr.length > 0) {
        // extract charset from header 'Content-Type'
        if(startsWith(lc(nextHdr), CONTENT_TYPE_LC))
          charset = extractCharset(string(nextHdr));
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
            final FElem hdr = new FElem(HTTP_HEADER);
            hdr.add(new FAttr(Q_NAME, name));
            hdr.add(new FAttr(Q_VALUE, value));
            root.add(hdr);
            if(eq(lc(name), CONTENT_TYPE_LC)) partCType = string(value);
          }
        }
        nextHdr = readLine(io);
      }
      final byte[] p = extractPartPayload(io, sep, end, charset);
      if(!status) {
        payloads.add(interpretPayload(p, partCType));
      }
    }
    root.add(createBody(partCType));
    return root;
  }

  /**
   * Reads a line of HTTP multipart content.
   * @param in connection input stream
   * @return line
   * @throws IOException I/O Exception
   */
  private static byte[] readLine(final InputStream in) throws IOException {
    final TokenBuilder tb = new TokenBuilder();
    for(int b; (b = in.read()) != -1;) {
      // RFC 1341: a line ends with CRLF
      if(b == '\r') {
        while(true) {
          final int b2 = in.read();
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
    return tb.isEmpty() ? null : tb.finish();
  }

  /**
   * Reads the payload of a part.
   * @param io connection input stream
   * @param sep separation boundary
   * @param end closing boundary
   * @param ce part content encoding
   * @return payload part content
   * @throws IOException I/O Exception
   */
  private static byte[] extractPartPayload(final InputStream io,
      final byte[] sep, final byte[] end, final String ce) throws IOException {

    final ByteList bl = new ByteList();
    while(true) {
      final byte[] next = readLine(io);
      if(next == null || eq(next, sep)) break;
      if(eq(next, end)) {
        // RFC 1341: Epilogue shall be ignored
        while(readLine(io) != null);
        break;
      }
      bl.add(next).add('\n');
    }
    return new NewlineInput(new IOContent(bl.toArray()), ce).content();
  }

  /**
   * Extracts the content from a "Content-type" header.
   * @param c value for "Content-type" header
   * @return result
   */
  private static String extractContentType(final String c) {
    if(c == null) return MimeTypes.APP_OCTET;
    final int end = c.indexOf(';');
    return end == -1 ? c : c.substring(0, end);
  }

  /**
   * Extracts the encapsulation boundary from the content type.
   * @param c content type
   * @return boundary
   * @throws QueryException query exception
   */
  private byte[] extractBoundary(final String c) throws QueryException {
    int index = c.toLowerCase(Locale.ENGLISH).lastIndexOf("boundary=");
    if(index == -1) REQINV.thrw(input, "No separation boundary specified");
    String b = c.substring(index + 9); // 9 for "boundary="
    if(b.charAt(0) == '"') {
      // if the boundary is enclosed in quotes, strip them
      index = b.lastIndexOf('"');
      b = b.substring(1, index);
    }
    return token(b);
  }

  /**
   * Extracts the charset from the 'Content-Type' header if present.
   * @param c Content-Type header
   * @return charset charset
   */
  private static String extractCharset(final String c) {
    // content type is unknown
    if(c == null) return null;
    final String cs = "charset=";
    final int i = c.toLowerCase(Locale.ENGLISH).lastIndexOf(cs);
    return i == -1 ? null : c.substring(i + cs.length());
  }
}
