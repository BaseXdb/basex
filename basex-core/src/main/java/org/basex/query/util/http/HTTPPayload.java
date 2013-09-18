package org.basex.query.util.http;

import static org.basex.io.MimeTypes.*;
import static org.basex.query.util.Err.*;
import static org.basex.query.util.http.HTTPText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.build.file.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * HTTP payload helper functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class HTTPPayload {
  /** Payloads (may be {@code null}). */
  private final ValueBuilder payloads;
  /** Input stream. */
  private final InputStream in;
  /** Input info. */
  private final InputInfo info;
  /** Database properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param is input stream
   * @param st only create status
   * @param ii input info
   * @param pr database properties
   */
  public HTTPPayload(final InputStream is, final boolean st, final InputInfo ii,
      final Prop pr) {
    in = is;
    info = ii;
    prop = pr;
    payloads = st ? null : new ValueBuilder();
  }

  /**
   * Parses the HTTP payload and returns a result body element.
   * @param error error flag
   * @param ctype content type defined in the connection
   * @param utype content type specified by the user
   * @return body element
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public FElem parse(final boolean error, final String ctype, final String utype)
      throws IOException, QueryException {

    // error: use text/plain as content type
    final String ct = error ? MimeTypes.TEXT_PLAIN :
      utype != null ? utype : contentType(ctype);

    final FElem body;
    if(MimeTypes.isMultipart(ct)) {
      // multipart response
      final byte[] boundary = boundary(ctype);
      if(boundary == null) HC_REQ.thrw(info, "No separation boundary specified");

      body = new FElem(Q_MULTIPART).add(MEDIA_TYPE, ct).add(BOUNDARY, boundary);
      final ANodeList parts = new ANodeList();
      extractParts(concat(DASHES, boundary), parts);
      for(final ANode node : parts) body.add(node);
    } else {
      // single part response
      body = new FElem(Q_BODY).add(MEDIA_TYPE, ct);
      if(payloads != null) {
        final byte[] pl = extract(ct, charset(ctype));
        payloads.add(parse(pl, ct));
      }
    }
    return body;
  }

  /**
   * Returns all payloads.
   * @return payloads
   */
  public Value payloads() {
    return payloads.value();
  }

  /**
   * Extracts payload from HTTP message and returns it as a byte array encoded
   * in UTF-8.
   * @param ctype content type
   * @param ce response content charset
   * @return payload as byte array
   * @throws IOException I/O Exception
   */
  private byte[] extract(final String ctype, final String ce) throws IOException {
    final BufferedInputStream bis = new BufferedInputStream(in);
    try {
      final ByteList bl = new ByteList();
      for(int i; (i = bis.read()) != -1;) bl.add(i);
      // In case of XML, HTML or text content type, use supplied character set
      if(MimeTypes.isXML(ctype) || MimeTypes.isText(ctype))
        return new TextInput(new IOContent(bl.toArray())).encoding(ce).content();

      // In case of binary data, do not encode anything
      return bl.toArray();
    } finally {
      bis.close();
    }
  }

  /**
   * Interprets a payload according to content type and returns a corresponding value.
   * @param payload payload
   * @param ctype content type
   * @return interpreted payload
   * @throws QueryException query exception
   */
  private Value parse(final byte[] payload, final String ctype) throws QueryException {
    try {
      return value(new IOContent(payload), prop, ctype, null);
    } catch(final IOException ex) {
      throw HC_PARSE.thrw(info, ex);
    }
  }

  /**
   * Extracts the parts from a multipart message.
   * @param sep separation boundary
   * @param parts list with all parts (may be {@code null})
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private void extractParts(final byte[] sep, final ANodeList parts)
      throws IOException, QueryException {

    try {
      // RFC 1341: Preamble is to be ignored -> read till 1st boundary
      while(true) {
        final byte[] l = readLine();
        if(l == null) HC_REQ.thrw(info, "No body specified for http:part");
        if(eq(sep, l)) break;
      }
      while(extractPart(sep, concat(sep, DASHES), parts));
    } finally {
      in.close();
    }
  }

  /**
   * Extracts a part from a multipart message.
   * @param sep separation boundary
   * @param end closing boundary
   * @param parts list with all parts (may be {@code null})
   * @return part
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private boolean extractPart(final byte[] sep, final byte[] end, final ANodeList parts)
      throws IOException, QueryException {

    // check if last line is reached
    final byte[] line = readLine();
    if(line == null || eq(line, end)) return false;

    // content type of part payload - if not defined by header 'Content-Type',
    // it is equal to 'text/plain' (RFC 1341)
    String ctype = MimeTypes.TEXT_PLAIN, enc = null;

    // extract headers
    for(byte[] l = line; l != null && l.length > 0;) {
      final int pos = indexOf(l, ':');
      if(pos > 0) {
        final byte[] key = substring(l, 0, pos);
        final byte[] val = trim(substring(l, pos + 1));
        if(eq(lc(key), CONTENT_TYPE_LC)) {
          ctype = string(val);
          enc = charset(ctype);
        }
        if(val.length != 0 && parts != null)
          parts.add(new FElem(Q_HEADER).add(NAME, key).add(VALUE, val));
      }
      l = readLine();
    }
    if(parts != null) parts.add(new FElem(Q_BODY).add(MEDIA_TYPE, ctype));

    final byte[] pl = extractPart(sep, end, enc);
    if(payloads != null) payloads.add(parse(pl, ctype));
    return true;
  }

  /**
   * Reads the next line of an HTTP multipart content.
   * @return line, or {@code null} if end of stream is reached
   * @throws IOException I/O Exception
   */
  private byte[] readLine() throws IOException {
    final ByteList bl = new ByteList();
    for(int b; (b = in.read()) != -1;) {
      // RFC 1341: a line ends with CRLF
      while(b == '\r') {
        b = in.read();
        if(b == '\n') return bl.toArray();
        bl.add('\r');
        if(b == -1) return bl.toArray();
      }
      bl.add(b);
    }
    return bl.isEmpty() ? null : bl.toArray();
  }

  /**
   * Reads the payload of a part.
   * @param sep separation boundary
   * @param end closing boundary
   * @param enc part content encoding
   * @return payload part content
   * @throws IOException I/O Exception
   */
  private byte[] extractPart(final byte[] sep, final byte[] end, final String enc)
      throws IOException {

    final ByteList bl = new ByteList();
    while(true) {
      final byte[] next = readLine();
      if(next == null || eq(next, sep)) break;
      // RFC 1341: Epilogue is to be ignored
      if(eq(next, end)) {
        while(readLine() != null);
        break;
      }
      bl.add(next).add('\n');
    }
    return new TextInput(new IOContent(bl.toArray())).encoding(enc).content();
  }

  /**
   * Extracts the encapsulation boundary from the content type.
   * @param ct content type
   * @return boundary, or {@code null}
   * @throws QueryException query exception
   */
  private byte[] boundary(final String ct) throws QueryException {
    int i = ct.toLowerCase(Locale.ENGLISH).indexOf(BOUNDARY_IS);
    if(i == -1) HC_REQ.thrw(info, "No separation boundary specified");

    String b = ct.substring(i + BOUNDARY_IS.length());
    if(b.charAt(0) == '"') {
      // if the boundary is enclosed in quotes, strip them
      i = b.lastIndexOf('"');
      b = b.substring(1, i);
    }
    return token(b);
  }

  /**
   * Returns a map with multipart form data.
   * @param ext content type extension (may be {@code null})
   * @return map, or {@code null}
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public HashMap<String, Value> multiForm(final String ext)
      throws IOException, QueryException {

    // parse boundary, create helper arrays
    final byte[] bound = concat(DASHES, boundary(ext)), last = concat(bound, DASHES);

    final HashMap<String, Value> map = new HashMap<String, Value>();

    final ByteList cont = new ByteList();
    int lines = -1;
    String name = null, fn = null;
    for(byte[] line; (line = readLine()) != null;) {
      if(lines >= 0) {
        if(startsWith(line, bound)) {
          Value val = map.get(name);
          if(val == null && fn != null) val = Map.EMPTY;
          if(fn != null && val instanceof Map) {
            final Map m = (Map) val;
            final Str k = Str.get(fn);
            final Value v = new ValueBuilder().add(m.get(k, info)).add(
                new B64(cont.toArray())).value();
            val = m.insert(k, v, info);
          } else {
            val = Str.get(cont.toArray());
          }
          map.put(name, val);
          cont.reset();
          lines = -1;
          if(eq(line, last)) break;
        } else {
          if(lines++ > 0) cont.add(CRLF);
          cont.add(line);
        }
      } else if(startsWith(line, CONTENT_DISPOSITION)) {
        name = !contains(line, token(NAME_IS)) ? null : string(line).
          replaceAll("^.*?" + NAME_IS + "\"|\".*", "").replaceAll("\\[\\]", "");
        fn = !contains(line, token(FILENAME_IS)) ? null :
          string(line).replaceAll("^.*" + FILENAME_IS + "\"|\"$", "");
      } else if(line.length == 0) {
        lines = 0;
      }
    }
    return map;
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns an XQuery value for the specified content type.
   * @param in input source
   * @param prop database properties
   * @param ctype content type
   * @param ext content type extension (may be {@code null})
   * @return xml parser
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Value value(final IO in, final Prop prop, final String ctype,
      final String ext) throws IOException, QueryException {

    Value val = null;
    if(ctype != null) {
      if(Token.eq(ctype, APP_JSON, APP_JSONML)) {
        final String options = ParserProp.JSONML[0] + "=" + eq(ctype, APP_JSONML);
        val = new DBNode(new JSONParser(in, prop, options));
      } else if(TEXT_CSV.equals(ctype)) {
        val = new DBNode(new CSVParser(in, prop));
      } else if(TEXT_HTML.equals(ctype)) {
        val = new DBNode(new HTMLParser(in, prop));
      } else if(APP_FORM_URLENCODED.equals(ctype)) {
        final String enc = charset(ext);
        val = Str.get(URLDecoder.decode(string(in.read()), enc == null ? UTF8 : enc));
      } else if(MimeTypes.isXML(ctype)) {
        val = new DBNode(in, prop);
      } else if(MimeTypes.isText(ctype)) {
        val = Str.get(new TextInput(in).content());
      } else if(MimeTypes.isMultipart(ctype)) {
        final HTTPPayload hp = new HTTPPayload(in.inputStream(), false, null, prop);
        hp.extractParts(concat(DASHES, hp.boundary(ext)), null);
        val = hp.payloads();
      }
    }
    return val == null ? new B64(in.read()) : val;
  }

  /**
   * Extracts the content from a "Content-type" header.
   * @param ctype value for "Content-type" header
   * @return result
   */
  public static String contentType(final String ctype) {
    if(ctype == null) return MimeTypes.APP_OCTET;
    final int end = ctype.indexOf(';');
    return end == -1 ? ctype : ctype.substring(0, end);
  }

  /**
   * Extracts the charset from the 'Content-Type' header if present.
   * @param ctype Content-Type header
   * @return charset charset
   */
  private static String charset(final String ctype) {
    // content type is unknown
    if(ctype == null) return null;
    final int i = ctype.toLowerCase(Locale.ENGLISH).indexOf(CHARSET_IS);
    return i == -1 ? null : ctype.substring(i + CHARSET_IS.length());
  }
}
