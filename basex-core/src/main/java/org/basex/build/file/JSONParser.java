package org.basex.build.file;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.json.*;
import org.basex.query.util.json.JsonParser.Spec;
import org.basex.query.value.item.*;

/**
 * This class parses files in the JSON format
 * and sends events to the specified database builder.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link Prop#JSONPARSER} option.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JSONParser extends XMLParser {
  /**
   * Constructor.
   * @param source document source
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public JSONParser(final IO source, final Prop pr) throws IOException {
    this(source, pr, pr.get(Prop.JSONPARSER));
  }

  /**
   * Constructor.
   * @param source document source
   * @param pr database properties
   * @param options parser options
   * @throws IOException I/O exception
   */
  public JSONParser(final IO source, final Prop pr, final String options)
      throws IOException {
    super(toXML(source, options), pr);
  }

  /**
   * Converts a JSON document to XML.
   * @param io io reference
   * @param options parsing options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IO toXML(final IO io, final String options) throws IOException {
    // set parser properties
    final JsonProp props = new JsonProp(options);
    final byte[] format = token(props.get(JsonProp.FORMAT));
    final String encoding = props.get(JsonProp.ENCODING);

    // parse input, using specified encoding
    final byte[] content = new NewlineInput(io).encoding(encoding).content();

    // parse input and convert to XML node
    final Item node;
    try {
      final JsonConverter conv = JsonConverter.get(format, Spec.RFC4627, true, null);
      node = conv.convert(string(content));

      // create XML input container from serialized node
      final IOContent xml = new IOContent(node.serialize().toArray());
      xml.name(io.name());
      return xml;
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }
}
