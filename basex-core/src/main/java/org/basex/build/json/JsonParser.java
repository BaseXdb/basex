package org.basex.build.json;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.parse.json.*;

/**
 * This class parses files in the JSON format
 * and converts them to XML.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link MainOptions#JSONPARSER} option.</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JsonParser extends XMLParser {
  /**
   * Constructor.
   * @param source document source
   * @param opts database options
   * @throws IOException I/O exception
   */
  public JsonParser(final IO source, final MainOptions opts) throws IOException {
    this(source, opts, opts.get(MainOptions.JSONPARSER));
  }

  /**
   * Constructor.
   * @param source document source
   * @param opts database options
   * @param jopts parser options
   * @throws IOException I/O exception
   */
  public JsonParser(final IO source, final MainOptions opts, final JsonParserOptions jopts)
      throws IOException {
    super(toXML(source, jopts), opts);
  }

  /**
   * Converts a JSON document to XML.
   * @param io input
   * @param options parser options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IOContent toXML(final IO io, final JsonParserOptions options) throws IOException {
    final JsonConverter conv = JsonConverter.get(options);
    final IOContent xml = new IOContent(conv.convert(io).serialize().finish());
    xml.name(io.name());
    return xml;
  }
}
