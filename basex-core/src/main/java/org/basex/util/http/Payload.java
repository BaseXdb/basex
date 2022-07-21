package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.csv.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.Base64;
import org.basex.util.list.*;

/**
 * HTTP payload helper functions.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Payload {
  /** XML declaration (start). */
  private static final byte[] DECLSTART = token("<?xml");
  /** XML declaration (end). */
  private static final byte[] DECLEND = token("?>");

  /** Payloads (may be {@code null}). */
  private final ItemList payloads;
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
  public Payload(final InputStream input, final boolean body, final InputInfo info,
      final MainOptions options) {

    this.input = input;
    this.info = info;
    this.options = options;
    payloads = body ? new ItemList() : null;
  }

  /**
   * Parses the HTTP payload and returns a result body element.
   * @param type media type
   * @param encoding content encoding
   * @return body element
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  FElem parse(final MediaType type, final String encoding)
      throws IOException, QueryException {

    final FElem body;
    if(type.isMultipart()) {
      // multipart response
      final byte[] boundary = boundary(type);
      body = new FElem(Q_HTTP_MULTIPART).add(BOUNDARY, boundary);
      final ANodeList parts = new ANodeList();
      extractParts(concat(DASHES, boundary), parts);
      for(final ANode node : parts) body.add(node);
    } else {
      // single part response
      body = new FElem(Q_HTTP_BODY);
      if(payloads != null) {
        final InputStream in = GZIP.equals(encoding) ? new GZIPInputStream(input) : input;
        payloads.add(parse(BufferInput.get(in).content(), type));
      }
    }
    return body.add(SerializerOptions.MEDIA_TYPE.name(), type.type());
  }

  /**
   * Returns all payloads.
   * @return payloads
   */
  Value value() {
    return payloads.value();
  }

  /**
   * Interprets a payload according to content type and returns a corresponding value.
   * @param payload payload
   * @param type media type
   * @return interpreted payload
   * @throws QueryException query exception
   */
  private Value parse(final byte[] payload, final MediaType type) throws QueryException {
    try {
      return value(payload, type, options);
    } catch(final IOException ex) {
      throw HC_PARSE_X.get(info, ex);
    }
  }

  /**
   * Extracts the parts from a multipart message.
   * @param sep separation boundary
   * @param parts list with all parts (can be {@code null})
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private void extractParts(final byte[] sep, final ANodeList parts)
      throws IOException, QueryException {

    // RFC 1341: Preamble is to be ignored: read till 1st boundary
    while(true) {
      final byte[] l = readLine();
      if(l == null) throw HC_REQ_X.get(info, "No body specified for http:part");
      if(eq(sep, l)) break;
    }
    // parse part
    while(extractPart(sep, concat(sep, DASHES), parts));
  }

  /**
   * Extracts a part from a multipart message.
   * @param sep separation boundary
   * @param end closing boundary
   * @param parts list with all parts (can be {@code null})
   * @return success flag
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private boolean extractPart(final byte[] sep, final byte[] end, final ANodeList parts)
      throws IOException, QueryException {

    // check if last line is reached
    byte[] line = readLine();
    if(line == null || eq(line, end)) return false;

    // content type of part payload - if not defined by header 'Content-Type',
    // it is equal to 'text/plain' (RFC 1341)
    MediaType type = MediaType.TEXT_PLAIN;

    // extract headers
    boolean base64 = false;
    for(byte[] l = line; l != null && l.length > 0;) {
      final int pos = indexOf(l, ':');
      if(pos > 0) {
        final String key = string(substring(l, 0, pos)), val = string(trim(substring(l, pos + 1)));
        if(key.equalsIgnoreCase(CONTENT_TYPE)) {
          type = new MediaType(val);
        } else if(key.equalsIgnoreCase(CONTENT_TRANSFER_ENCODING)) {
          base64 = val.equals(BASE64);
        }
        if(!val.isEmpty() && parts != null)
          parts.add(new FElem(Q_HTTP_HEADER).add(NAME, key).add(VALUE, val));
      }
      l = readLine();
    }
    if(parts != null) {
      parts.add(new FElem(Q_HTTP_BODY).add(SerializerOptions.MEDIA_TYPE.name(), type.toString()));
    }

    // extract payload
    final ByteList bl = new ByteList();
    while(true) {
      line = readLine();
      if(line == null || eq(line, sep)) break;

      // RFC 1341: Epilogue is to be ignored
      if(eq(line, end)) {
        while(readLine() != null);
        break;
      }
      if(!bl.isEmpty()) bl.add(CRLF);
      bl.add(line);
    }

    if(payloads != null) {
      final String encoding = type.parameter(CHARSET);
      final byte[] part = new TextInput(bl.finish()).encoding(encoding).content();
      payloads.add(parse(base64 ? Base64.decode(part) : part, type));
    }
    return true;
  }

  /**
   * Reads the next line of an HTTP multipart content.
   * @return line, or {@code null} if end of stream is reached
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
   * Extracts the encapsulation boundary from the media type.
   * @param type media type
   * @return boundary or {@code null}
   * @throws QueryException query exception
   */
  private byte[] boundary(final MediaType type) throws QueryException {
    final String b = type.parameter(BOUNDARY);
    if(b == null) throw HC_REQ_X.get(info, "No separation boundary specified");
    return token(b);
  }

  /**
   * Returns a map with multipart form data.
   * @param type media type
   * @return map with file names and contents
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public HashMap<String, Value> multiForm(final MediaType type) throws IOException, QueryException {
    // parse boundary, create helper arrays
    final byte[] bound = concat(DASHES, boundary(type)), last = concat(bound, DASHES);

    final HashMap<String, Value> data = new HashMap<>();
    final ByteList cont = new ByteList();
    int lines = -1;
    String name = "", filename = null;
    for(byte[] line; (line = readLine()) != null;) {
      if(lines >= 0) {
        if(startsWith(line, bound)) {
          // get old value
          Value value = data.get(name);
          if(filename != null) {
            // assign file and contents, join multiple files
            final XQMap map = value instanceof XQMap ? (XQMap) value : XQMap.empty();
            final Str file = Str.get(filename);
            final B64 contents = B64.get(cont.next());
            final Value files = new ItemList().add(map.get(file, info)).add(contents).value();
            value = map.put(file, files, info);
          } else {
            // assign string, join multiple strings
            final Str v = Str.get(cont.next());
            value = value == null ? v : new ItemList().add(value).add(v).value();
          }

          if(!name.isEmpty()) data.put(name, value);
          lines = -1;
          if(eq(line, last)) break;
        } else {
          if(lines++ > 0) cont.add(CRLF);
          cont.add(line);
        }
      } else if(startsWith(lc(line), CONTENT_DISPOSITION)) {
        // get key and file name
        name = contains(line, token(NAME + '=')) ?
          string(line).replaceAll("^.*?" + NAME + "=\"|\".*", "").replaceAll("\\[]", "") : null;
        filename = contains(line, token(FILENAME + '=')) ?
          string(line).replaceAll("^.*" + FILENAME + "=\"|\"$", "") : null;
      } else if(line.length == 0) {
        lines = 0;
      }
    }

    return data;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Returns an XQuery value for the specified body.
   * @param body body
   * @param type type of the body
   * @param options database options
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Value value(final byte[] body, final MediaType type,
      final MainOptions options) throws IOException, QueryException {

    final IOContent io = prepare(body, type);
    if(io.length() == 0) {
      return Empty.VALUE;
    } else if(type.isJSON()) {
      final JsonParserOptions opts = new JsonParserOptions(options.get(MainOptions.JSONPARSER));
      opts.assign(type);
      return JsonConverter.get(opts).convert(io);
    } else if(type.isCSV()) {
      final CsvParserOptions opts = new CsvParserOptions(options.get(MainOptions.CSVPARSER));
      opts.assign(type);
      return CsvConverter.get(opts).convert(io);
    } else if(type.is(MediaType.TEXT_HTML)) {
      final HtmlOptions opts = new HtmlOptions(options.get(MainOptions.HTMLPARSER));
      opts.assign(type);
      return new DBNode(new HtmlParser(io, options, opts));
    } else if(type.isXML()) {
      return new DBNode(io);
    } else if(type.isText()) {
      return Str.get(io.read());
    } else if(type.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
      try {
        final byte[] decoded = decodeUri(io.read(), true);
        final int cp = XMLToken.invalid(decoded);
        if(cp != -1) throw new InputException(cp);
        return Str.get(decoded);
      } catch(final IllegalArgumentException ex) {
        Util.debug(ex);
        throw new IOException(ex.getMessage());
      }
    } else if(type.isMultipart()) {
      try(InputStream is = io.inputStream()) {
        final Payload payload = new Payload(is, true, null, options);
        payload.extractParts(concat(DASHES, payload.boundary(type)), null);
        return payload.value();
      }
    } else {
      return B64.get(io.read());
    }
  }

  /**
   * Returns a normalized payload.
   * @param body body
   * @param type media type
   * @return content
   * @throws IOException I/O exception
   */
  private static IOContent prepare(final byte[] body, final MediaType type) throws IOException {
    byte[] data = body;
    final boolean xml = type.isXML(), text = type.isText();
    if(xml || text) {
      // convert text to UTF8; skip redundant XML declaration
      data = new NewlineInput(body).encoding(type.parameter(CHARSET)).content();
      if(xml && startsWith(data, DECLSTART)) {
        final int d = indexOf(data, DECLEND, DECLSTART.length);
        if(d != -1) data = substring(data, d + DECLEND.length);
      }
    }
    return new IOContent(data);
  }
}
