package org.basex.query.func.http;

import static org.basex.io.MimeTypes.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.http.HttpText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * HTTP payload helper functions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class HttpPayload {
  /** Payloads (may be {@code null}). */
  private final ValueBuilder payloads;
  /** Input stream. */
  private final InputStream input;
  /** Input info. */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;

  /**
   * Constructor.
   * @param input input stream
   * @param body create body
   * @param info input info
   * @param options database options
   */
  public HttpPayload(final InputStream input, final boolean body, final InputInfo info,
      final MainOptions options) {

    this.input = input;
    this.info = info;
    this.options = options;
    payloads = body ? new ValueBuilder() : null;
  }

  /**
   * Parses the HTTP payload and returns a result body element.
   * @param type content type
   * @return body element
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  FElem parse(final String type) throws IOException, QueryException {
    final String main = MimeTypes.type(type);
    final FElem body;
    if(isMultipart(type)) {
      // multipart response
      final byte[] boundary = boundary(type);
      if(boundary == null) throw HC_REQ_X.get(info, "No separation boundary specified");

      body = new FElem(Q_MULTIPART).add(SerializerOptions.MEDIA_TYPE.name(), main);
      body.add(BOUNDARY, boundary);
      final ANodeList parts = new ANodeList();
      extractParts(concat(DASHES, boundary), parts);
      for(final ANode node : parts) body.add(node);
    } else {
      // single part response
      body = new FElem(Q_BODY).add(SerializerOptions.MEDIA_TYPE.name(), main);
      if(payloads != null) {
        final byte[] pl = extract(type);
        payloads.add(parse(pl, type));
      }
    }
    return body;
  }

  /**
   * Returns all payloads.
   * @return payloads
   */
  Value payloads() {
    return payloads.value();
  }

  /**
   * Extracts payload from HTTP message and returns it as a byte array encoded in UTF-8.
   * @param ctype content type
   * @return payload as byte array
   * @throws IOException I/O Exception
   */
  private byte[] extract(final String ctype) throws IOException {
    // In case of XML, HTML or text content type, use supplied character set
    return (isXML(ctype) || isText(ctype)
      ? new TextInput(input).encoding(charset(ctype))
      : new BufferInput(input)
    ).content();
  }

  /**
   * Interprets a payload according to content type and returns a corresponding value.
   * @param payload payload
   * @param contentType content type
   * @return interpreted payload
   * @throws QueryException query exception
   */
  private Value parse(final byte[] payload, final String contentType) throws QueryException {
    if(payload.length == 0) return Empty.SEQ;
    try {
      return value(new IOContent(payload), options, contentType);
    } catch(final IOException ex) {
      throw HC_PARSE_X.get(info, ex);
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
        if(l == null) throw HC_REQ_X.get(info, "No body specified for http:part");
        if(eq(sep, l)) break;
      }
      while(extractPart(sep, concat(sep, DASHES), parts));
    } finally {
      input.close();
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
    String ctype = TEXT_PLAIN;

    // extract headers
    for(byte[] l = line; l != null && l.length > 0;) {
      final int pos = indexOf(l, ':');
      if(pos > 0) {
        final byte[] key = substring(l, 0, pos);
        final byte[] val = trim(substring(l, pos + 1));
        if(eq(lc(key), CONTENT_TYPE_LC)) ctype = string(val);
        if(val.length != 0 && parts != null)
          parts.add(new FElem(Q_HEADER).add(NAME, key).add(VALUE, val));
      }
      l = readLine();
    }
    if(parts != null) parts.add(new FElem(Q_BODY).add(SerializerOptions.MEDIA_TYPE.name(), ctype));

    final byte[] pl = extractPart(sep, end, charset(ctype));
    if(payloads != null) payloads.add(parse(pl, ctype));
    return true;
  }

  /**
   * Reads the next line of an HTTP multipart content.
   * @return line or {@code null} if end of stream is reached
   * @throws IOException I/O Exception
   */
  private byte[] readLine() throws IOException {
    final ByteList bl = new ByteList();
    for(int b; (b = input.read()) != -1;) {
      // RFC 1341: a line ends with CRLF
      while(b == '\r') {
        b = input.read();
        if(b == '\n') return bl.finish();
        bl.add('\r');
        if(b == -1) return bl.finish();
      }
      bl.add(b);
    }
    return bl.isEmpty() ? null : bl.finish();
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
    return new TextInput(new IOContent(bl.finish())).encoding(enc).content();
  }

  /**
   * Extracts the encapsulation boundary from the content type.
   * @param params content type parameters
   * @return boundary or {@code null}
   * @throws QueryException query exception
   */
  private byte[] boundary(final String params) throws QueryException {
    int i = params.toLowerCase(Locale.ENGLISH).indexOf(BOUNDARY_IS);
    if(i == -1) throw HC_REQ_X.get(info, "No separation boundary specified");

    String b = params.substring(i + BOUNDARY_IS.length());
    if(b.charAt(0) == '"') {
      // if the boundary is enclosed in quotes, strip them
      i = b.lastIndexOf('"');
      b = b.substring(1, i);
    }
    return token(b);
  }

  /**
   * Returns a map with multipart form data.
   * @param params content type parameters
   * @return map or {@code null}
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public HashMap<String, Value> multiForm(final String params) throws IOException, QueryException {
    // parse boundary, create helper arrays
    final byte[] bound = concat(DASHES, boundary(params)), last = concat(bound, DASHES);

    final HashMap<String, Value> map = new HashMap<>();
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
                new B64(cont.next())).value();
            val = m.put(k, v, info);
          } else {
            val = Str.get(cont.next());
          }
          if(!name.isEmpty()) map.put(name, val);
          lines = -1;
          if(eq(line, last)) break;
        } else {
          if(lines++ > 0) cont.add(CRLF);
          cont.add(line);
        }
      } else if(startsWith(line, CONTENT_DISPOSITION)) {
        name = contains(line, token(NAME_IS)) ? string(line).
          replaceAll("^.*?" + NAME_IS + "\"|\".*", "").replaceAll("\\[\\]", "") : null;
        fn = contains(line, token(FILENAME_IS)) ? string(line).replaceAll("^.*" + FILENAME_IS +
            "\"|\"$", "") : null;
      } else if(line.length == 0) {
        lines = 0;
      }
    }
    return map;
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns an XQuery value for the specified content type.
   * @param input input source
   * @param options database options
   * @param contentType content type
   * @return xml parser
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Value value(final IO input, final MainOptions options, final String contentType)
      throws IOException, QueryException {

    final String ctype = MimeTypes.type(contentType);
    final String ext = MimeTypes.parameters(contentType);
    Value val = null;
    if(ctype != null) {
      if(APP_JSON.equals(ctype)) {
        final JsonParserOptions opts = new JsonParserOptions(options.get(MainOptions.JSONPARSER));
        opts.parse(ext);
        val = new DBNode(new JsonParser(input, options, opts));
      } else if(TEXT_CSV.equals(ctype)) {
        final CsvParserOptions opts = new CsvParserOptions(options.get(MainOptions.CSVPARSER));
        opts.parse(ext);
        val = new DBNode(new CsvParser(input, options, opts));
      } else if(TEXT_HTML.equals(ctype)) {
        final HtmlOptions opts = new HtmlOptions(options.get(MainOptions.HTMLPARSER));
        opts.parse(ext);
        val = new DBNode(new HtmlParser(input, options, opts));
      } else if(APP_FORM_URLENCODED.equals(ctype)) {
        final String enc = charset(ext);
        val = Str.get(URLDecoder.decode(string(input.read()), enc == null ? Strings.UTF8 : enc));
      } else if(isXML(ctype)) {
        val = new DBNode(input);
      } else if(isText(ctype)) {
        val = Str.get(new TextInput(input).content());
      } else if(isMultipart(ctype)) {
        try(final InputStream is = input.inputStream()) {
          final HttpPayload hp = new HttpPayload(is, true, null, options);
          hp.extractParts(concat(DASHES, hp.boundary(ext)), null);
          val = hp.payloads();
        }
      }
    }
    return val == null ? new B64(input.read()) : val;
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
