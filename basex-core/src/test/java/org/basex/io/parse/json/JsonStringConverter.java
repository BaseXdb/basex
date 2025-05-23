package org.basex.io.parse.json;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Writes the parsed JSON file to the given {@link TokenBuilder}.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class JsonStringConverter extends JsonConverter {
  /** The token builder. */
  private final TokenBuilder tb;
  /** Flag for first array entry, object member or constructor argument. */
  private boolean first = true;

  /**
   * Constructor.
   * @param opts options
   * @param builder the token builder
   */
  private JsonStringConverter(final JsonParserOptions opts, final TokenBuilder builder) {
    super(opts);
    tb = builder;
  }

  /**
   * Writes a pretty-printed representation of the given JSON string.
   *
   * @param json JSON string
   * @param liberal liberal parsing
   * @param escape escape flag
   * @return resulting string
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public static String toString(final String json, final boolean liberal, final boolean escape)
      throws QueryException, IOException {

    final JsonParserOptions jopts = new JsonParserOptions();
    jopts.set(JsonParserOptions.LIBERAL, liberal);
    jopts.set(JsonParserOptions.ESCAPE, escape);
    final JsonStringConverter jsc = new JsonStringConverter(jopts, new TokenBuilder());
    return (String) jsc.convert(new IOContent(json)).toJava();
  }

  @Override
  public void openObject() {
    tb.add("{ ");
    first = true;
  }

  @Override
  public void openPair(final byte[] key, final boolean add) {
    if(!first) tb.add(", ");
    stringLit(key);
    tb.add(": ");
  }

  @Override
  public void closePair(final boolean add) {
    first = false;
  }

  @Override
  public void closeObject() {
    tb.add(first ? "}" : " }");
  }

  @Override
  public void openArray() {
    tb.add("[ ");
    first = true;
  }

  @Override
  public void openItem() {
    if(!first) tb.add(", ");
  }

  @Override
  public void closeItem() {
    first = false;
  }

  @Override
  public void closeArray() {
    tb.add(first ? "]" : " ]");
  }

  @Override
  public void numberLit(final byte[] value) {
    tb.add(value);
  }

  @Override
  protected void stringLit(final byte[] value) {
    tb.add('"');
    final TokenParser tp = new TokenParser(value);
    while(tp.more()) {
      final int cp = tp.next();
      switch(cp) {
        case '\\':
        case '"':
          tb.add('\\').add(cp);
          break;
        case '\b':
          tb.add("\\b");
          break;
        case '\f':
          tb.add("\\f");
          break;
        case '\t':
          tb.add("\\t");
          break;
        case '\r':
          tb.add("\\r");
          break;
        case '\n':
          tb.add("\\n");
          break;
        default:
          if(Character.isISOControl(cp)) {
            tb.add('\\').add('u');
            for(int j = 4; --j >= 0;) {
              final int hex = cp >>> (j << 2) & 0xF;
              tb.add(hex + (hex > 9 ? 'A' - 10 : '0'));
            }
          } else {
            tb.add(cp);
          }
      }
    }
    tb.add('"');
  }

  @Override
  public void nullLit() {
    tb.add(JsonConstants.NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    tb.add(value);
  }

  @Override
  protected void init(final String uri) {
  }

  @Override
  protected Item finish() {
    return Str.get(tb.toArray());
  }
}
