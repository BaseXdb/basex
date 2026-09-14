package org.basex.build.json;

import java.io.*;

import org.basex.build.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
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
    final JsonFormat format = jopts.get(JsonOptions.FORMAT);
    if(format == JsonFormat.W3) {
      throw new IOException(Util.info("Format '%' cannot be converted to XML.", format));
    }
    try {
      final boolean lines = jopts.get(JsonParserOptions.JSON_LINES);
      if(lines) builder.openElem(JsonConstants.JSON_LINES, atts, nsp);
      if(jopts.get(JsonOptions.MERGE) &&
          (format == JsonFormat.DIRECT || format == JsonFormat.ATTRIBUTES)) {
        // types can only be merged in complete documents
        final Serializer ser = new BuilderSerializer(builder);
        for(final Item item : JsonConverter.get(jopts).convert(source)) {
          for(final GNode child : ((XNode) item).childIter()) ser.serialize(child);
        }
      } else {
        final JsonConverter converter = JsonConverter.get(jopts, null, builder);
        final String encoding = jopts.get(JsonParserOptions.ENCODING);
        try(NewlineInput ni = new NewlineInput(source, encoding)) {
          converter.convert(ni, "", null, this);
        }
      }
      if(lines) builder.closeElem();
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
