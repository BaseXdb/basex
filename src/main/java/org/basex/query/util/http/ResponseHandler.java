package org.basex.query.util.http;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.basex.build.Parser;
import org.basex.build.file.HTMLParser;
import org.basex.core.Prop;
import org.basex.io.IOContent;
import org.basex.io.in.TextInput;
import org.basex.query.QueryException;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.FNode;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.ValueIter;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.list.ByteList;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * <http:response/> element.
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
  /** Media type application/octet-stream. */
  private static final byte[] OCT_STREAM = token("application/octet-stream");
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
   * Constructs http:response element and reads HTTP response content.
   * @param conn HTTP connection
   * @param statusOnly indicates if content is required
   * @param mediaTypeOvr content type provided by the user to interpret the
   *          response content
   * @param prop query context properties
   * @param ii input info
   * @return result sequence of <http:response/> and content items
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  public static ValueIter getResponse(final HttpURLConnection conn,
      final byte[] statusOnly, final byte[] mediaTypeOvr, final Prop prop,
      final InputInfo ii) throws IOException, QueryException {

    final NodeCache attrs = extractAttrs(conn);
    final NodeCache hdrs = extractHdrs(conn);
    final byte[] contentType = mediaTypeOvr == null ?
        extractContentType(conn.getContentType()) : mediaTypeOvr;
    final ItemCache payloads = new ItemCache();
    final FNode body;
    final boolean s = statusOnly != null && Bln.parse(statusOnly, ii);
    // multipart response
    if(startsWith(contentType, token("multipart"))) {
      final byte[] boundary = extractBoundary(conn.getContentType(), ii);
      final NodeCache a = new NodeCache();
      a.add(new FAttr(new QNm(MEDIATYPE, EMPTY), contentType));
      a.add(new FAttr(new QNm(BOUNDARY, EMPTY), boundary));
      body = new FElem(new QNm(MULTIPART, HTTPURI), extractParts(
          conn.getInputStream(), s, payloads, concat(token("--"), boundary),
          prop, ii), a, new Atts().add(HTTP, HTTPURI));
      // single part response
    } else {
      body = createBody(contentType);
      if(!s) payloads.add(
          interpretPayload(
              extractPayload(
                  conn.getInputStream(),
                  extractCharset(conn.getContentType())),
              contentType, prop, ii));
    }

    // construct <http:response/>
    final FElem responseEl = new FElem(new QNm(RESPONSE, HTTPURI), hdrs, attrs,
        new Atts().add(HTTP, HTTPURI));
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
  private static NodeCache extractAttrs(final HttpURLConnection conn)
      throws IOException {
    final NodeCache a = new NodeCache();
    a.add(new FAttr(new QNm(STATUS, EMPTY), token(conn.getResponseCode())));
    a.add(new FAttr(new QNm(MSG, EMPTY), token(conn.getResponseMessage())));

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
        final FElem hdr = new FElem(new QNm(HEADER, HTTPURI),
            new Atts().add(HTTP, HTTPURI));
        hdr.add(new FAttr(new QNm(HDR_NAME, EMPTY), token(headerName)));
        hdr.add(new FAttr(new QNm(HDR_VALUE, EMPTY),
            token(conn.getHeaderField(headerName))));
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
  private static FElem createBody(final byte[] mediaType) {
    final FElem b = new FElem(new QNm(BODY, HTTPURI),
        new Atts().add(HTTP, HTTPURI));
    b.add(new FAttr(new QNm(MEDIATYPE, EMPTY), mediaType));
    return b;
  }

  /**
   * Extracts payload from HTTP message and returns it as a byte array encoded
   * in UTF-8.
   * @param io connection input stream
   * @param cs response content charset
   * @return payload as byte array
   * @throws IOException I/O Exception
   */
  private static byte[] extractPayload(final InputStream io, final String cs)
    throws IOException {
    final BufferedInputStream bis = new BufferedInputStream(io);
    try {
      final ByteList bl = new ByteList();
      for(int i = 0; (i = bis.read()) != -1;) bl.add(i);
      return TextInput.content(new IOContent(bl.toArray()), cs).finish();
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
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private static Item interpretPayload(final byte[] p, final byte[] c,
      final Prop prop, final InputInfo ii) throws IOException, QueryException {

    if(eq(c, TXT_XML) || eq(c, TXT_EXT_XML) || eq(c, APPL_XML) ||
        eq(c, APPL_EXT_XML) || endsWith(c, MIME_XML_SUFFIX)) {
      return new DBNode(Parser.xmlParser(new IOContent(p), prop), prop);
    }
    if(eq(c, TXT_HTML)) {
      if(!HTMLParser.available()) throw HTMLERR.thrw(ii);
      return new DBNode(new HTMLParser(new IOContent(p), "", prop), prop);
    }
    return startsWith(c, MIME_TEXT_PREFIX) ? Str.get(p) : new B64(p);
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
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private static NodeCache extractParts(final InputStream io,
      final boolean statusOnly, final ItemCache payloads, final byte[] sep,
      final Prop prop, final InputInfo ii) throws IOException, QueryException {

    try {
      // read first line of multipart content
      byte[] next = readLine(io);
      // RFC 1341: Preamble shall be ignored -> read till 1st boundary
      while(next != null && !eq(sep, next))
        next = readLine(io);
      if(next == null) REQINV.thrw(ii, "No body specified for http:part");

      final byte[] end = concat(sep, token("--"));
      FElem nextPart = extractNextPart(io, statusOnly, payloads, sep, end,
          prop, ii);
      final NodeCache p = new NodeCache();
      while(nextPart != null) {
        p.add(nextPart);
        nextPart =
          extractNextPart(io, statusOnly, payloads, sep, end, prop, ii);
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
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private static FElem extractNextPart(final InputStream io,
      final boolean statusOnly, final ItemCache payloads, final byte[] sep,
      final byte[] end, final Prop prop, final InputInfo ii)
      throws IOException, QueryException {

    // content type of part payload - if not defined by header 'Content-Type',
    // it is equal to 'text/plain' (RFC 1341)
    byte[] partContType = TXT_PLAIN;
    String charset = null;
    final byte[] firstLine = readLine(io);
    // last line is reached:
    if(firstLine == null || eq(firstLine, end)) return null;

    final FElem root = new FElem(new QNm(PART, EMPTY),
        new Atts().add(HTTP, HTTPURI));

    //final NodeCache partCh = new NodeCache();
    if(firstLine.length == 0) {
      // part has no headers
      final byte[] p = extractPartPayload(io, sep, end, null);
      if(!statusOnly) payloads.add(interpretPayload(p, partContType, prop, ii));
    } else {
      // extract headers:
      byte[] nextHdr = firstLine;
      while(nextHdr != null && nextHdr.length > 0) {
        // extract charset from header 'Content-Type'
        if(startsWith(lc(nextHdr), CONT_TYPE_LC))
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
            final FElem hdr = new FElem(new QNm(HEADER, HTTPURI));
            hdr.add(new FAttr(new QNm(HDR_NAME, EMPTY), name));
            hdr.add(new FAttr(new QNm(HDR_VALUE, EMPTY), value));
            root.add(hdr);
            if(eq(lc(name), CONT_TYPE_LC)) partContType = value;
          }
        }
        nextHdr = readLine(io);
      }
      final byte[] p = extractPartPayload(io, sep, end, charset);
      if(!statusOnly) {
        payloads.add(interpretPayload(p, partContType, prop, ii));
      }
    }
    root.add(createBody(partContType));
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
    return tb.size() == 0 ? null : tb.finish();
  }

  /**
   * Reads the payload of a part.
   * @param io connection input stream
   * @param sep separation boundary
   * @param end closing boundary
   * @param cs part content encoding
   * @return payload part content
   * @throws IOException I/O Exception
   */
  private static byte[] extractPartPayload(final InputStream io,
      final byte[] sep, final byte[] end, final String cs) throws IOException {

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
    return TextInput.content(new IOContent(bl.toArray()), cs).finish();
  }

  /**
   * Extracts the content from a "Content-type" header.
   * @param c value for "Content-type" header
   * @return result
   */
  private static byte[] extractContentType(final String c) {
    if(c == null) return OCT_STREAM;
    final int end = c.indexOf(';');
    return end == -1 ? token(c) : subtoken(token(c), 0, end);
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
    int index = c.toLowerCase().lastIndexOf("boundary=");
    if(index == -1) REQINV.thrw(info, "No separation boundary specified");
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
    final int i = c.toLowerCase().lastIndexOf(cs);
    return i == -1 ? null : c.substring(i + cs.length());
  }
}
