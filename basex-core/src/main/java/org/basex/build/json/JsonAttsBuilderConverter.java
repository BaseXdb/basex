package org.basex.build.json;

import static org.basex.io.parse.json.JsonConstants.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;

/**
 * Converts a JSON document to XML and emits the result as events to a database builder,
 * equivalent to {@link JsonAttsConverter} but without building an in-memory tree.
 * Supports {@link JsonOptions#MERGE}{@code =false} only.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class JsonAttsBuilderConverter extends JsonBuilderConverter {
  /** Whether to emit explicit {@code type="string"} attributes. */
  private final boolean strings;
  /** Key of the next pair element; null when not inside an object pair. */
  private byte[] pendingKey;
  /** Whether the root element has been opened. */
  private boolean rootOpened;

  /**
   * Constructor.
   * @param jopts JSON options
   * @param builder database builder
   * @throws QueryException query exception
   */
  JsonAttsBuilderConverter(final JsonParserOptions jopts, final Builder builder)
      throws QueryException {
    super(jopts, builder);
    strings = jopts.get(JsonOptions.STRINGS);
  }

  @Override
  protected void openObject() {
    if(!skipOpen()) openElem(OBJECT);
  }

  @Override
  protected void closeObject() {
    if(!skipClose()) closeElem();
  }

  @Override
  protected void openPair(final byte[] key, final boolean add) {
    if(!skipPair(add)) pendingKey = key;
  }

  @Override
  protected void closePair(final boolean add) {
    skipClose();
  }

  @Override
  protected void openArray() {
    if(!skipOpen()) openElem(ARRAY);
  }

  @Override
  protected void closeArray() {
    if(!skipClose()) closeElem();
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
    if(skip()) return;
    try {
      atts.reset();
      final byte[] en = prepareElem();
      if(strings || type != STRING) atts.add(TYPE, type);
      builder.openElem(en, atts, EMPTY_NSP);
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
      final byte[] en = prepareElem();
      atts.add(TYPE, type);
      builder.openElem(en, atts, EMPTY_NSP);
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * Returns the element name for the next container or value element.
   * @return element name
   */
  private byte[] prepareElem() {
    if(pendingKey != null) {
      atts.add(NAME, pendingKey);
      pendingKey = null;
      return PAIR;
    }
    if(rootOpened) return ITEM;
    rootOpened = true;
    return JSON;
  }
}
