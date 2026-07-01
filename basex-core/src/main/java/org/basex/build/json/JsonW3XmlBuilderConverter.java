package org.basex.build.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Converts a JSON document to W3C XML representation and emits the result as events to a
 * database builder, equivalent to {@link JsonW3XmlConverter} but without building an
 * in-memory tree.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class JsonW3XmlBuilderConverter extends JsonBuilderConverter {
  /** Default namespace declaration for the XPath functions namespace, emitted on the root. */
  private static final Atts FN_NSP = new Atts().add(EMPTY, QueryText.FN_URI);

  /** Whether to add escape attributes for values containing backslashes. */
  private final boolean escape;
  /** Key of the next value element; null inside arrays or at root. */
  private byte[] pendingKey;
  /** Whether the root element has been opened; controls namespace declaration. */
  private boolean rootOpened;

  /**
   * Constructor.
   * @param jopts JSON options
   * @param builder database builder
   * @throws QueryException query exception
   */
  JsonW3XmlBuilderConverter(final JsonParserOptions jopts, final Builder builder)
      throws QueryException {
    super(jopts, builder);
    escape = jopts.get(JsonParserOptions.ESCAPE);
  }

  @Override
  protected void openObject() {
    openContainer(MAP);
  }

  @Override
  protected void openPair(final byte[] key) {
    pendingKey = key;
  }

  @Override
  protected void openArray() {
    openContainer(ARRAY);
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
   * @param elemName local element name in the XPath functions namespace
   * @param value text content, or {@code null} for the null literal
   */
  private void addValue(final byte[] elemName, final byte[] value) {
    try {
      atts.reset();
      addKeyAtts();
      if(escape && value != null && contains(value, '\\')) atts.add(ESCAPED, TRUE);
      builder.openElem(elemName, atts, nsDecl());
      if(value != null) builder.text(value);
      builder.closeElem();
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * Opens a container element (map or array).
   * @param elemName local element name in the XPath functions namespace
   */
  private void openContainer(final byte[] elemName) {
    try {
      atts.reset();
      addKeyAtts();
      builder.openElem(elemName, atts, nsDecl());
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * Adds key and escaped-key attributes from {@link #pendingKey} if one is set.
   */
  private void addKeyAtts() {
    if(pendingKey != null) {
      atts.add(KEY, pendingKey);
      if(escape && contains(pendingKey, '\\')) atts.add(ESCAPED_KEY, TRUE);
      pendingKey = null;
    }
  }

  /**
   * Returns the namespace declaration for the current element.
   * The XPath functions namespace is declared as default on the root element only.
   * @return namespace Atts
   */
  private Atts nsDecl() {
    if(!rootOpened) {
      rootOpened = true;
      return FN_NSP;
    }
    return EMPTY_NSP;
  }
}
