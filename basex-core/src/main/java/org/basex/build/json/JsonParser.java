package org.basex.build.json;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

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
public final class JsonParser extends SingleParser {
  /** Parser options. */
  private final JsonParserOptions jopts;

  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @param jopts parser options
   */
  public JsonParser(final IO source, final MainOptions options, final JsonParserOptions jopts) {
    super(source, options);
    this.jopts = jopts;
  }

  @Override
  protected void parse() throws IOException {
    try {
      final Value value = JsonConverter.get(jopts).convert(source);
      final Serializer ser = new BuilderSerializer(builder);
      if(jopts.get(JsonParserOptions.JSON_LINES)) {
        // wrap documents in a root element
        builder.openElem(JsonConstants.JSON_LINES, atts, nsp);
        for(final Item item : value) add((XNode) item, ser);
        builder.closeElem();
      } else {
        add((XNode) value, ser);
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  /**
   * Adds the children of a document node to the database builder.
   * @param doc document node
   * @param ser builder serializer
   * @throws IOException I/O exception
   */
  private static void add(final XNode doc, final Serializer ser) throws IOException {
    for(final GNode child : doc.childIter()) ser.serialize(child);
  }
}
