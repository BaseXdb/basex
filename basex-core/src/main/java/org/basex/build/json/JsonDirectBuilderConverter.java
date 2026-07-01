package org.basex.build.json;

import static org.basex.io.parse.json.JsonConstants.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Converts a JSON document to XML and emits the result as events to a database builder,
 * equivalent to {@link JsonDirectConverter} but without building an in-memory tree.
 * Supports {@link JsonOptions#MERGE}{@code =false} only.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class JsonDirectBuilderConverter extends JsonBuilderConverter {
  /** Whether to emit explicit {@code type="string"} attributes. */
  private final boolean strings;
  /** Whether to use lax QName encoding for object keys. */
  private final boolean lax;
  /** Name to use for the next element to be opened. */
  private byte[] name;

  /**
   * Constructor.
   * @param jopts JSON options
   * @param builder database builder
   * @throws QueryException query exception
   */
  JsonDirectBuilderConverter(final JsonParserOptions jopts, final Builder builder)
      throws QueryException {
    super(jopts, builder);
    strings = jopts.get(JsonOptions.STRINGS);
    lax = jopts.get(JsonOptions.LAX);
    name = JSON;
  }

  @Override
  protected void openObject() {
    openElem(OBJECT);
  }

  @Override
  protected void openPair(final byte[] key) {
    name = XMLToken.encode(key, lax);
  }

  @Override
  protected void openArray() {
    openElem(ARRAY);
  }

  @Override
  protected void openItem() {
    name = VALUE;
  }

  @Override
  protected void numberLit(final byte[] value) {
    addValue(NUMBER, value);
  }

  @Override
  protected void stringLit(final byte[] value) {
    addValue(STRING, value);
  }

  @Override
  protected void nullLit() {
    addValue(NULL, null);
  }

  @Override
  protected void booleanLit(final byte[] value) {
    addValue(BOOLEAN, value);
  }

  /**
   * Emits a leaf value element with optional text content.
   * @param type JSON type token
   * @param value text content, or {@code null} for the null literal
   */
  private void addValue(final byte[] type, final byte[] value) {
    try {
      atts.reset();
      if(strings || type != STRING) atts.add(TYPE, type);
      builder.openElem(name, atts, EMPTY_NSP);
      if(value != null) builder.text(value);
      builder.closeElem();
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * Opens a container element for an object or array.
   * @param type JSON type token ({@code OBJECT} or {@code ARRAY})
   */
  private void openElem(final byte[] type) {
    try {
      atts.reset();
      atts.add(TYPE, type);
      builder.openElem(name, atts, EMPTY_NSP);
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
