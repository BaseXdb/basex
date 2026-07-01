package org.basex.build.json;

import static org.basex.build.json.JsonOptions.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;

/**
 * Streams a JSON file as events directly to a database builder, bypassing in-memory
 * tree construction.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class JsonStreamingParser extends SingleParser {
  /** JSON parser options. */
  private final JsonParserOptions jopts;
  /** Factory that creates the appropriate converter for the given builder. */
  private final QueryFunction<Builder, JsonBuilderConverter> converterFactory;

  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @param jopts JSON parser options
   * @param converterFactory converter factory
   */
  JsonStreamingParser(final IO source, final MainOptions options, final JsonParserOptions jopts,
      final QueryFunction<Builder, JsonBuilderConverter> converterFactory) {
    super(source, options);
    this.jopts = jopts;
    this.converterFactory = converterFactory;
  }

  /**
   * Returns a parser for the given JSON options, using the streaming path where applicable.
   * @param source document source
   * @param options main options
   * @return JSON parser (streaming or classic)
   * @throws IOException I/O exception
   */
  public static SingleParser get(final IO source, final MainOptions options) throws IOException {
    final JsonParserOptions jopts = options.get(MainOptions.JSONPARSER);
    final JsonOptions.JsonFormat fmt = jopts.get(FORMAT);
    final boolean merge = jopts.get(MERGE);
    final QueryFunction<Builder, JsonBuilderConverter> conv = switch(fmt) {
      case W3_XML, BASIC -> b -> new JsonW3XmlBuilderConverter(jopts, b);
      case DIRECT -> merge ? null : b -> new JsonDirectBuilderConverter(jopts, b);
      case ATTRIBUTES -> merge ? null : b -> new JsonAttsBuilderConverter(jopts, b);
      case JSONML -> null; // not yet supported; fall back to non-streaming
      default -> null;
    };
    // fallback to non-streaming parser if streaming path is not applicable
    return conv == null
        ? new JsonParser(source, options, jopts)
        : new JsonStreamingParser(source, options, jopts, conv);
  }

  @Override
  protected void parse() throws IOException {
    try {
      final JsonBuilderConverter conv = converterFactory.apply(builder);
      final String encoding = jopts.get(JsonParserOptions.ENCODING);
      try(NewlineInput ni = new NewlineInput(source, encoding)) {
        new org.basex.io.parse.json.JsonParser(ni, jopts, conv).parse(null);
      }
    } catch(final UncheckedIOException ex) {
      throw ex.getCause();
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
