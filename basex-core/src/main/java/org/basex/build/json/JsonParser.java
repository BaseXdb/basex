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
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
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
      if(format == JsonFormat.W3_MAPPING) {
        // convert parsed values with a conversion plan
        final JsonMappingOptions mopts = mapping();
        final String root = mopts.get(JsonMappingOptions.ROOT);
        final MapToElement converter = new MapToElement(mopts, PlanFn.XML_PREFIX,
            new SharedData(), options, null);
        for(final Item item : JsonConverter.get(jopts).convert(source)) {
          converter.convert(item, root, builder);
        }
      } else if(jopts.merge()) {
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

  /**
   * Returns the options for the w3-mapping format. If a string is assigned, it is interpreted as
   * the path to a JSON file with the options.
   * @return options
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private JsonMappingOptions mapping() throws IOException, QueryException {
    Value mapping = jopts.get(JsonOptions.MAPPING);
    if(mapping instanceof final Str path) {
      final JsonParserOptions opts = new JsonParserOptions();
      opts.set(JsonOptions.FORMAT, JsonFormat.W3);
      mapping = JsonConverter.get(opts).convert(IO.get(Token.string(path.string())));
    }
    return JsonMappingOptions.get(mapping, null, null);
  }
}
