package org.basex.query.util.http;

import static org.basex.io.MimeTypes.*;
import static org.basex.query.util.http.HTTPText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.file.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * HTTP payload helper functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class HTTPPayload {
  /** Hidden constructor. */
  private HTTPPayload() { }

  /**
   * Extracts the encapsulation boundary from the content type.
   * @param ct content type
   * @return boundary, or {@code null}
   */
  public static byte[] boundary(final String ct) {
    int i = ct.toLowerCase(Locale.ENGLISH).indexOf(BOUNDARY_IS);
    if(i == -1) return null;

    String b = ct.substring(i + BOUNDARY_IS.length());
    if(b.charAt(0) == '"') {
      // if the boundary is enclosed in quotes, strip them
      i = b.lastIndexOf('"');
      b = b.substring(1, i);
    }
    return token(b);
  }

  /**
   * Extracts the content from a "Content-type" header.
   * @param ct value for "Content-type" header
   * @return result
   */
  public static String contentType(final String ct) {
    if(ct == null) return MimeTypes.APP_OCTET;
    final int end = ct.indexOf(';');
    return end == -1 ? ct : ct.substring(0, end);
  }

  /**
   * Extracts the charset from the 'Content-Type' header if present.
   * @param ct Content-Type header
   * @return charset charset
   */
  public static String charset(final String ct) {
    // content type is unknown
    if(ct == null) return null;
    final int i = ct.toLowerCase(Locale.ENGLISH).indexOf(CHARSET_IS);
    return i == -1 ? null : ct.substring(i + CHARSET_IS.length());
  }


  /**
   * Returns an XQuery item for the specified content type.
   * @param in input source
   * @param prop database properties
   * @param ct content type
   * @param ext content type extension (may be {@code null})
   * @return xml parser
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Item item(final IO in, final Prop prop, final String ct,
      final String ext) throws IOException, QueryException {

    Item it = null;
    if(ct != null) {
      if(Token.eq(ct, APP_JSON, APP_JSONML)) {
        final String options = ParserProp.JSONML[0] + "=" + eq(ct, APP_JSONML);
        it = new DBNode(new JSONParser(in, prop, options));
      } else if(TEXT_CSV.equals(ct)) {
        it = new DBNode(new CSVParser(in, prop));
      } else if(TEXT_HTML.equals(ct)) {
        it = new DBNode(new HTMLParser(in, prop));
      } else if(MimeTypes.isXML(ct)) {
        it = new DBNode(in, prop);
      } else if(MimeTypes.isText(ct)) {
        it = Str.get(new TextInput(in).content());
      } else if(MULTI_FORM.equals(ct)) {
        it = multiForm(in, ext);
      }
    }
    return it == null ? new B64(in.read()) : it;
  }

  /**
   * Returns a map with multipart form data.
   * @param in input source
   * @param ext content type extension (may be {@code null})
   * @return map, or {@code null}
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Item multiForm(final IO in, final String ext)
      throws IOException, QueryException {

    // parse boundary
    final byte[] b = boundary(ext);
    if(b == null) throw new IOException("multipart/form-data: no boundary specified.");

    // helper arrays
    final byte[] boundary = concat(DASHES, b);
    final byte[] end = concat(CRLF, boundary), last = concat(boundary, DASHES);

    Map map = Map.EMPTY;
    final byte[] cont = in.read();

    final int cl = cont.length;
    String fn = null;
    for(int s = 0, i; s < cl; s = i + 2) {
      i = indexOf(cont, CRLF, s);
      if(i == -1) throw new IOException("multipart/form-data: CRLF expected.");
      final byte[] line = substring(cont, s, i);

      if(startsWith(line, boundary)) {
        if(eq(line, last)) break;
      } else if(startsWith(line, CONTENT_DISPOSITION)) {
        fn = string(line).replaceAll("^.*filename=\"?|\"?$", "");
      } else if(line.length == 0) {
        s = i + 2;
        i = indexOf(cont, end, s);
        if(i == -1) throw new IOException("multipart/form-data: no closing boundary.");

        map = map.insert(Str.get(fn), new B64(substring(cont, s, i)), null);
      } else {
        Util.debug("multipart/form-data: ignored: " + string(line));
      }
    }
    return map;
  }
}
