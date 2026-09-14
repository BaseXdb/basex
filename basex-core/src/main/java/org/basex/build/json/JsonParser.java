package org.basex.build.json;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class parses files in the JSON format
 * and converts them to XML.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link MainOptions#JSONPARSER} option.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonParser extends XMLParser {
  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @param jopts parser options
   * @throws IOException I/O exception
   */
  public JsonParser(final IO source, final MainOptions options, final JsonParserOptions jopts)
      throws IOException {
    super(toXml(source, jopts), options);
  }

  /**
   * Converts a JSON document to XML.
   * @param io input
   * @param jopts parser options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IOContent toXml(final IO io, final JsonParserOptions jopts) throws IOException {
    try {
      final Value value = JsonConverter.get(jopts).convert(io);
      final byte[] bytes;
      if(jopts.get(JsonParserOptions.JSON_LINES)) {
        // wrap documents in a root element
        final TokenBuilder tb = new TokenBuilder().add('<').add(JsonConstants.JSON_LINES).add('>');
        for(final Item item : value) tb.add(item.serialize().finish());
        bytes = tb.add("</").add(JsonConstants.JSON_LINES).add('>').finish();
      } else {
        bytes = value.serialize().finish();
      }
      final IOContent xml = new IOContent(bytes);
      xml.name(io.name());
      return xml;
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
