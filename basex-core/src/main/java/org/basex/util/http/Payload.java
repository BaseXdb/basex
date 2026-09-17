package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import org.basex.build.*;
import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.io.parse.csv.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.util.*;
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
 * Helper functions for payload of HTTP response.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Payload {
  /** XML declaration (start). */
  private static final byte[] DECLSTART = token("<?xml");
  /** XML declaration (end). */
  private static final byte[] DECLEND = token("?>");

  /** Parse the contents of the payload. */
  private final boolean body;
  /** Input stream. */
  private InputStream input;
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;

  /**
   * Constructor.
   * @param input input stream
   * @param body create body
   * @param info input info (can be {@code null})
   * @param options main options
   */
  public Payload(final InputStream input, final boolean body, final InputInfo info,
      final MainOptions options) {

    this.input = input;
    this.body = body;
    this.info = info;
    this.options = options;
  }

  /**
   * Parses the HTTP payload.
   * @param type media type
   * @param encoding content encoding
   * @param temp registry for temporary files (can be {@code null})
   * @return parsed body
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  ResponseBody parse(final MediaType type, final String encoding, final TempFiles temp)
      throws IOException, QueryException {

    // decompress before parsing (applies to multipart and single-part alike)
    input = decode(input, encoding);

    final ResponseBody result = new ResponseBody();
    result.type = type;
    if(type.isMultipart()) {
      // multipart response
      result.boundary = boundary(type);
      extractParts(concat(DASHES, result.boundary), result.parts);
    } else if(body) {
      // the stream is closed as before, releasing the inflater of a decompressed response
      try(InputStream is = input) {
        result.value = parse(SpillOutput.read(is, temp), type);
      }
    }
    return result;
  }

  /**
   * Returns a stream that decodes a response body with the given content coding.
   * @param input response body
   * @param encoding content encoding (case-insensitive, RFC 9110)
   * @return decoded stream
   * @throws IOException I/O exception
   */
  public static InputStream decode(final InputStream input, final String encoding)
      throws IOException {
    return GZIP.equalsIgnoreCase(encoding) ? new GZIPInputStream(input) : input;
  }

  /**
   * Interprets a payload according to content type and returns a corresponding value.
   * @param payload payload
   * @param type media type
   * @return interpreted payload
   * @throws QueryException query exception
   */
  private Value parse(final IO payload, final MediaType type) throws QueryException {
    try {
      return value(payload, type, options);
    } catch(final IOException ex) {
      throw HC_PARSE_X.get(info, ex);
    }
  }

  /**
   * Extracts the parts from a multipart message.
   * @param sep separation boundary
   * @param parts list with all parts
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private void extractParts(final byte[] sep, final ArrayList<ResponseBody> parts)
      throws IOException, QueryException {

    // RFC 1341: Preamble is to be ignored: read till 1st boundary
    while(true) {
      final byte[] l = readLine();
      if(l == null) throw HC_REQ_X.get(info, "No body specified for http:part");
      if(matchBoundary(sep, l)) break;
    }
    // parse part
    while(extractPart(sep, concat(sep, DASHES), parts));
  }

  /**
   * Extracts a part from a multipart message.
   * @param sep separation boundary
   * @param end closing boundary
   * @param parts list with all parts
   * @return success flag
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private boolean extractPart(final byte[] sep, final byte[] end,
      final ArrayList<ResponseBody> parts) throws IOException, QueryException {

    // check if last line is reached
    byte[] line = readLine();
    if(line == null || matchBoundary(end, line)) return false;

    // a part without 'Content-Type' header has the media type 'text/plain' (RFC 1341)
    final ResponseBody part = new ResponseBody();
    parts.add(part);

    // extract headers
    boolean base64 = false;
    for(byte[] l = line; l != null && l.length > 0;) {
      final int pos = indexOf(l, ':');
      if(pos > 0) {
        final String key = string(substring(l, 0, pos));
        final String value = string(trim(substring(l, pos + 1)));
        if(key.equalsIgnoreCase(CONTENT_TYPE)) {
          part.type = new MediaType(value);
        } else if(key.equalsIgnoreCase(CONTENT_TRANSFER_ENCODING)) {
          base64 = value.equalsIgnoreCase(BASE64);
        }
        part.headers.add(Map.entry(string(lc(token(key))), value));
      }
      l = readLine();
    }

    // extract payload
    final ByteList bl = new ByteList();
    while(true) {
      line = readLine();
      if(line == null || matchBoundary(sep, line)) break;

      // RFC 1341: Epilogue is to be ignored
      if(matchBoundary(end, line)) {
        while(readLine() != null);
        break;
      }
      if(!bl.isEmpty()) bl.add(CRLF);
      bl.add(line);
    }

    if(body) {
      final String encoding = part.type.parameter(CHARSET);
      final byte[] contents = new TextInput(new IOContent(bl.finish()), encoding).content();
      part.value = parse(new IOContent(base64 ? Base64.decode(contents) : contents), part.type);
    }
    return true;
  }

  /**
   * Reads the next line of an HTTP multipart content.
   * @return line, or {@code null} if end of stream is reached
   * @throws IOException I/O exception
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
   * Checks if a line is a boundary delimiter, tolerating trailing transport-padding
   * (spaces and tabs, RFC 2046 §5.1.1).
   * @param boundary expected boundary bytes
   * @param line line to check
   * @return result of check
   */
  private static boolean matchBoundary(final byte[] boundary, final byte[] line) {
    final int bl = boundary.length, ll = line.length;
    if(!startsWith(line, boundary)) return false;
    for(int i = bl; i < ll; i++) {
      final byte c = line[i];
      if(c != ' ' && c != '\t') return false;
    }
    return true;
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
   * @param temp registry for temporary files (can be {@code null})
   * @return map with file names and contents
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public XQMap multiForm(final MediaType type, final TempFiles temp)
      throws IOException, QueryException {
    return multiForm(type, temp, SpillOutput.THRESHOLD);
  }

  /**
   * Returns a map with multipart form data.
   * @param type media type
   * @param temp registry for temporary files (can be {@code null})
   * @param threshold spill threshold in bytes
   * @return map with file names and contents
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  XQMap multiForm(final MediaType type, final TempFiles temp, final int threshold)
      throws IOException, QueryException {
    // parse boundary, create helper arrays
    final byte[] bound = concat(DASHES, boundary(type)), last = concat(bound, DASHES);

    XQMap data = XQMap.empty();
    // contents of the current part; a part that outgrows the threshold is spilled to disk
    SpillOutput cont = null;
    int lines = -1;
    Str name = Str.EMPTY, filename = Str.EMPTY;
    for(byte[] line; (line = readLine()) != null;) {
      if(lines >= 0) {
        // the closing boundary carries the trailing dashes, so it never matches the separator
        final boolean closing = matchBoundary(last, line);
        if(closing || matchBoundary(bound, line)) {
          cont.close();
          // get old value
          Value value = data.get(name);
          if(filename.string().length != 0) {
            // assign file and contents, join multiple files
            final XQMap map = value instanceof final XQMap m ? m : XQMap.empty();
            final B64 contents = cont.finish(IOERR_X);
            final Value files = new ItemList().add(map.get(filename)).add(contents).value();
            value = map.put(filename, files);
          } else {
            // assign untyped value (allows implicit coercion), join multiple values
            final Atm v = Atm.get(cont.finish().read());
            value = value == null ? v : new ItemList().add(value).add(v).value();
          }

          if(!name.isEmpty()) data = data.put(name, value);
          lines = -1;
          if(closing) break;
        } else {
          if(lines++ > 0) cont.write(CRLF);
          cont.write(line);
        }
      } else if(startsWith(lc(line), CONTENT_DISPOSITION)) {
        // get key and file name; match each parameter on its own (the 'name' token also
        // occurs inside 'filename', so a plain search would confuse the two)
        final String ln = string(line);
        name = Str.get(disposition(ln, "name").replaceAll("\\[]", ""));
        filename = Str.get(disposition(ln, "filename"));
      } else if(line.length == 0) {
        cont = new SpillOutput(temp, threshold);
        lines = 0;
      }
    }
    return data;
  }

  /**
   * Extracts the quoted value of a Content-Disposition parameter. The parameter name must not be
   * preceded by a word character (so {@code name} is not matched inside {@code filename}).
   * @param string disposition line
   * @param key parameter name
   * @return value (empty string if the parameter is absent)
   */
  private static String disposition(final String string, final String key) {
    final Matcher m = Pattern.compile("(?<![\\w-])" + key + "=\"([^\"]*)\"").matcher(string);
    return m.find() ? m.group(1) : "";
  }

  // STATIC METHODS ===============================================================================

  /**
   * Checks if a media type is treated as binary data (see {@link #value}).
   * @param type media type
   * @return result of check
   */
  public static boolean binary(final MediaType type) {
    return !(type.isJSON() || type.isCSV() || type.is(MediaType.TEXT_HTML) || type.isXml() ||
        type.isText() || type.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED) ||
        type.isMultipart());
  }

  /**
   * Returns an XQuery value for the specified body.
   * @param body body
   * @param type type of the body
   * @param options main options
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Value value(final IO body, final MediaType type, final MainOptions options)
      throws IOException, QueryException {

    final IO io = prepare(body, type);
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
    } else if(type.isXml()) {
      // remote input: parse as untrusted
      return new DBNode(Parser.xmlParser(io, new MainOptions().trusted(false)));
    } else if(type.isText()) {
      return Str.get(io.read());
    } else if(type.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
      try {
        final byte[] decoded = XMLToken.decodeUri(io.read(), true);
        if(Token.contains(decoded, Token.REPLACEMENT)) throw new InputException(Token.REPLACEMENT);
        return Str.get(decoded);
      } catch(final IllegalArgumentException ex) {
        throw new IOException(ex.getMessage(), ex);
      }
    } else if(type.isMultipart()) {
      try(InputStream is = io.inputStream()) {
        final Payload payload = new Payload(is, true, null, options);
        final ResponseBody parsed = new ResponseBody();
        parsed.type = type;
        payload.extractParts(concat(DASHES, payload.boundary(type)), parsed.parts);
        return parsed.values();
      }
    } else {
      return B64.get(io, IOERR_X);
    }
  }

  /**
   * Returns a normalized payload. Only text and XML input is materialized.
   * @param body body
   * @param type media type
   * @return content
   * @throws IOException I/O exception
   */
  private static IO prepare(final IO body, final MediaType type) throws IOException {
    final boolean xml = type.isXml(), text = type.isText();
    if(!(xml || text)) return body;

    // convert text to UTF8; skip redundant XML declaration
    byte[] data = new NewlineInput(body, type.parameter(CHARSET)).content();
    // '<?xml' is only a declaration if followed by whitespace; otherwise it is a
    // processing instruction such as '<?xml-stylesheet?>', which must be kept
    if(xml && startsWith(data, DECLSTART) && data.length > DECLSTART.length &&
        ws(data[DECLSTART.length])) {
      final int d = indexOf(data, DECLEND, DECLSTART.length);
      if(d != -1) data = substring(data, d + DECLEND.length);
    }
    return new IOContent(data);
  }
}
