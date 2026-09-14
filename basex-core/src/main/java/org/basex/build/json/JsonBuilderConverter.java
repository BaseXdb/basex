package org.basex.build.json;

import java.io.*;

import org.basex.build.*;
import org.basex.io.parse.json.*;
import org.basex.util.*;

/**
 * Abstract base for JSON converters that emit events directly to a database builder,
 * bypassing in-memory tree construction.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
abstract class JsonBuilderConverter extends JsonHandler {
  /** Empty namespace declarations. */
  static final Atts EMPTY_NSP = new Atts();

  /** Database builder. */
  final Builder builder;
  /** Reusable attribute list. */
  final Atts atts = new Atts();

  /**
   * Constructor.
   * @param builder database builder
   */
  JsonBuilderConverter(final Builder builder) {
    this.builder = builder;
  }

  /**
   * Initializes the conversion of a JSON value.
   */
  abstract void init();

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
