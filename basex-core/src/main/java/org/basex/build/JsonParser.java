package org.basex.build;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.json.*;
import org.basex.query.value.item.*;

/**
 * This class parses files in the JSON format
 * and converts them to XML.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link MainOptions#JSONPARSER} option.</p>
 *
 * @author BaseX Team 2005-13, BSD License
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
   * @param parser parser options
   * @throws IOException I/O exception
   */
  public JsonParser(final IO source, final MainOptions opts, final String parser)
      throws IOException {
    super(toXML(source, parser), opts);
  }

  /**
   * Converts a JSON document to XML.
   * @param io input
   * @param options parsing options
   * @return parser
   * @throws IOException I/O exception
   */
  public static IO toXML(final IO io, final String options) throws IOException {
    final JsonOptions jopts = new JsonOptions(options);
    final String encoding = jopts.get(JsonOptions.ENCODING);

    // parse input, using specified encoding
    final byte[] content = new NewlineInput(io).encoding(encoding).content();

    // parse input and convert to XML node
    try {
      // cache XML representation
      final JsonConverter conv = JsonConverter.get(jopts);
      final Item node = conv.convert(string(content));
      final IOContent xml = new IOContent(node.serialize().toArray());
      xml.name(io.name());
      return xml;
    } catch(final QueryIOException ex) {
      throw new BaseXException(ex.getLocalizedMessage());
    }
  }
}
