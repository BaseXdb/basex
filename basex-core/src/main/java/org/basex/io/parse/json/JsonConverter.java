package org.basex.io.parse.json;

import java.io.*;

import org.basex.build.json.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter extends JsonHandler {
  /** Shared data references. */
  protected final SharedData shared = new SharedData();
  /** JSON options. */
  protected final JsonParserOptions jopts;

  /** Null value. */
  protected Value nullValue = Empty.VALUE;
  /** Input info. */
  protected InputInfo info;
  /** Interruptible job. */
  protected Job job;

  /**
   * Returns a JSON converter for the given configuration.
   * @param jopts options
   * @return JSON converter
   * @throws QueryException query exception
   */
  public static JsonConverter get(final JsonParserOptions jopts) throws QueryException {
    return get(jopts, null, null);
  }

  /**
   * Returns a JSON converter for the given configuration.
   * @param jopts options
   * @param info input info (can be {@code null})
   * @return JSON converter
   * @throws QueryException query exception
   */
  public static JsonConverter get(final JsonParserOptions jopts, final InputInfo info)
      throws QueryException {
    return get(jopts, info, null);
  }

  /**
   * Returns a JSON converter for the given configuration.
   * @param jopts options
   * @param info input info (can be {@code null})
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   * @return JSON converter
   * @throws QueryException query exception
   */
  public static JsonConverter get(final JsonParserOptions jopts, final InputInfo info,
      final XmlHandler handler) throws QueryException {
    jopts.check(info);
    return switch(jopts.get(JsonOptions.FORMAT)) {
      case ATTRIBUTES -> new JsonAttsConverter(jopts, handler);
      case JSONML     -> new JsonMLConverter(jopts, handler);
      case W3, W3_MAPPING -> new JsonW3Converter(jopts);
      case W3_XML     -> new JsonW3XmlConverter(jopts, handler);
      default         -> new JsonDirectConverter(jopts, handler);
    };
  }

  /**
   * Constructor.
   * @param jopts JSON options
   */
  protected JsonConverter(final JsonParserOptions jopts) {
    this.jopts = jopts;
  }

  /**
   * Assigns a value for 'null' values.
   * @param item null value
   */
  public final void nullValue(final Value item) {
    nullValue = item;
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @return result
   */
  public final Value convert(final IO input) throws QueryException, IOException {
    final String encoding = jopts.get(JsonParserOptions.ENCODING);
    try(NewlineInput ni = new NewlineInput(input, encoding)) {
      return convert(ni, input.url(), null, this);
    }
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @param uri uri (can be empty)
   * @param ii input info (can be {@code null})
   * @param jb interruptible job
   * @return result (empty if the XML events are sent to a handler)
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public final Value convert(final TextInput input, final String uri, final InputInfo ii,
      final Job jb) throws QueryException, IOException {
    job = jb;
    info = ii;
    final JsonParser parser = new JsonParser(input, jopts, this);
    if(!jopts.get(JsonParserOptions.JSON_LINES)) {
      init(uri);
      parser.parse(ii);
      return finish();
    }
    final ValueBuilder vb = new ValueBuilder(jb);
    while(true) {
      init(uri);
      if(!parser.next(ii)) return vb.value();
      vb.add(finish());
    }
  }

  /**
   * Initializes the conversion.
   * @param uri base URI
   */
  protected abstract void init(String uri);

  /**
   * Returns the resulting XQuery value.
   * @return result
   */
  protected abstract Value finish();
}
