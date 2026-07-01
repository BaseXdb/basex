package org.basex.build.json;

import java.io.*;

import org.basex.build.*;
import org.basex.build.json.JsonParserOptions.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract base for JSON converters that emit events directly to a database builder,
 * bypassing in-memory tree construction.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
abstract class JsonBuilderConverter extends JsonConverter {
  /** Empty namespace declarations. */
  static final Atts EMPTY_NSP = new Atts();

  /** Database builder. */
  final Builder builder;
  /** Reusable attribute list. */
  final Atts atts = new Atts();

  /**
   * Constructor.
   * @param jopts JSON parser options
   * @param builder database builder
   * @throws QueryException if the USE_LAST duplicates option is set
   */
  JsonBuilderConverter(final JsonParserOptions jopts, final Builder builder)
      throws QueryException {
    super(jopts);
    this.builder = builder;
    final JsonDuplicates dupl = jopts.get(JsonParserOptions.DUPLICATES);
    if(dupl == JsonDuplicates.USE_LAST) {
      throw optionError(JsonParserOptions.DUPLICATES.name(), dupl);
    }
  }

  @Override
  protected final void init(final String uri) { }

  @Override
  protected final Item finish() {
    return null;
  }

  @Override
  protected void closePair() { }

  @Override
  protected void closeObject() {
    closeElem();
  }

  @Override
  protected void closeArray() {
    closeElem();
  }

  @Override
  protected void openItem() { }

  @Override
  protected void closeItem() { }

  /**
   * Closes the current builder element, wrapping any I/O exception.
   */
  final void closeElem() {
    try {
      builder.closeElem();
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
