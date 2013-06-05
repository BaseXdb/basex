package org.basex.query.util.http;

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
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
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
  private final InputInfo info;
  /** Database properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param ii input info
   * @param pr database properties
   */
  public HTTPResponse(final InputInfo ii, final Prop pr) {
    info = ii;
    prop = pr;
  }

  /**
   * Constructs http:response element and reads HTTP response content.
   * @param conn HTTP connection
   * @param status indicates if content is required
   * @param ctype content type provided by the user to interpret the
   *          response content
   * @return result sequence of <http:response/> and content items
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  public ValueIter getResponse(final HttpURLConnection conn, final byte[] status,
      final byte[] ctype) throws IOException, QueryException {

    final String type = conn.getContentType();
    final ValueBuilder payloads = new ValueBuilder();
    final boolean s = status != null && Bln.parse(status, info);

    // multipart response
    final FElem body;
    InputStream is = conn.getErrorStream();

    final byte[] cType;
    if(is == null) {
      cType = ctype == null ? token(extractContentType(type)) : ctype;
      is = conn.getInputStream();
    } else {
      // error: use text/plain as content type
      cType = token(MimeTypes.TEXT_PLAIN);
    }

    if(startsWith(cType, MULTIPART)) {
      final byte[] boundary = extractBoundary(type);
      body = new FElem(Q_MULTIPART).add(MEDIA_TYPE, cType).add(BOUNDARY, boundary);
      final ANodeList list = extractParts(is, s, payloads, concat(token("--"), boundary));
      for(final ANode node : list) body.add(node);
      // single part response
    } else {
      body = createBody(cType);
      if(!s) {
        final byte[] payload = extractPayload(is, cType, extractCharset(type));
        payloads.add(interpretPayload(payload, cType));
      }
    }

    // construct <http:response/>
    final FElem responseEl = new FElem(Q_RESPONSE).declareNS();
    responseEl.add(STATUS, token(conn.getResponseCode()));
    responseEl.add(MESSAGE, token(conn.getResponseMessage()));

    for(final String header : conn.getHeaderFields().keySet()) {
      if(header != null) {
        final FElem hdr = new FElem(Q_HEADER);
        hdr.add(NAME, token(header));
        hdr.add(VALUE, token(conn.getHeaderField(header)));
        responseEl.add(hdr);
      }
    }
    responseEl.add(body);

    // result
    final ValueBuilder result = new ValueBuilder();
    result.add(responseEl);
    result.add(payloads.value());
    return result;
  }


  /**
   * Creates a <http:body/> element.
   * @param cType content type
   * @return body
   */
  private static FElem createBody(final byte[] cType) {
    return new FElem(Q_BODY).add(MEDIA_TYPE, cType);
  }

  /**
   * Extracts payload from HTTP message and returns it as a byte array encoded
   * in UTF-8.
   * @param io connection input stream
   * @param ct content type
   * @param ce response content charset
   * @return payload as byte array
   * @throws IOException I/O Exception
   */
  private static byte[] extractPayload(final InputStream io, final byte[] ct,
      final String ce) throws IOException {

    final BufferedInputStream bis = new BufferedInputStream(io);
    final String c = string(ct);
    try {
      final ByteList bl = new ByteList();
      for(int i; (i = bis.read()) != -1;) bl.add(i);
      // In case of XML, HTML or text content type, use supplied character set
      if(MimeTypes.isXML(c) || c.equals(MimeTypes.TEXT_HTML) ||
          c.startsWith(MimeTypes.MIME_TEXT_PREFIX))
        return new NewlineInput(new IOContent(bl.toArray())).encoding(ce).content();

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
   * @throws QueryException query exception
   */
  private Item interpretPayload(final byte[] p, final byte[] ct) throws QueryException {
    try {
      final IOContent io = new IOContent(p);
      io.name(PAYLOAD + IO.XMLSUFFIX);
      return Parser.item(io, prop, string(ct));
    } catch(final IOException ex) {
      throw HC_PARSE.thrw(info, ex);
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
  private ANodeList extractParts(final InputStream io, final boolean status,
      final ValueBuilder payloads, final byte[] sep) throws IOException, QueryException {

    try {
      // read first line of multipart content
      byte[] next = readLine(io);
      // RFC 1341: Preamble shall be ignored -> read till 1st boundary
      while(next != null && !eq(sep, next))
        next = readLine(io);
      if(next == null) HC_REQ.thrw(info, "No body specified for http:part");

      final byte[] end = concat(sep, token("--"));
      final ANodeList p = new ANodeList();
      while(extractNextPart(io, status, payloads, sep, end, p));
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
   * @param nl node list
   * @return part
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private boolean extractNextPart(final InputStream io, final boolean status,
      final ValueBuilder payloads, final byte[] sep, final byte[] end,
      final ANodeList nl) throws IOException, QueryException {

    // content type of part payload - if not defined by header 'Content-Type',
    // it is equal to 'text/plain' (RFC 1341)
    byte[] partCType = token(MimeTypes.TEXT_PLAIN);
    String charset = null;
    final byte[] firstLine = readLine(io);
    // last line is reached:
    if(firstLine == null || eq(firstLine, end)) return false;

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
            final byte[] value = trim(substring(nextHdr, pos + 1, nextHdr.length));
            // construct attributes
            nl.add(new FElem(Q_HEADER).add(NAME, name).add(VALUE, value));
            if(eq(lc(name), CONTENT_TYPE_LC)) partCType = value;
          }
        }
        nextHdr = readLine(io);
      }
      final byte[] p = extractPartPayload(io, sep, end, charset);
      if(!status) payloads.add(interpretPayload(p, partCType));
    }
    nl.add(createBody(partCType));
    return true;
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
  private static byte[] extractPartPayload(final InputStream io, final byte[] sep,
      final byte[] end, final String ce) throws IOException {

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
    return new NewlineInput(new IOContent(bl.toArray())).encoding(ce).content();
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
    if(index == -1) HC_REQ.thrw(info, "No separation boundary specified");
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
