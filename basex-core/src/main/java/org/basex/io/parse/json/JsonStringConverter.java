package org.basex.io.parse.json;

import org.basex.build.JsonOptions.JsonSpec;
import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Writes the parsed JSON file to the given {@link TokenBuilder}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class JsonStringConverter extends JsonConverter {
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
   * Writes a pretty-printed representation of the given JSON string to the given builder.
   *
   * @param json JSON string
   * @param spec JSON spec for parsing
   * @param un unescape flag
   * @param tb token builder
   * @throws QueryIOException query I/O exception
   */
  public static void print(final String json, final JsonSpec spec, final boolean un,
      final TokenBuilder tb) throws QueryIOException {

    final JsonParserOptions jopts = new JsonParserOptions();
    jopts.set(JsonOptions.SPEC, spec);
    jopts.set(JsonParserOptions.UNESCAPE, un);
    JsonParser.parse(json, null, jopts, new JsonStringConverter(jopts, tb));
  }

  @Override
  public void openObject() {
    tb.add("{ ");
    first = true;
  }

  @Override
  public void openPair(final byte[] key) {
    if(!first) tb.add(", ");
    stringLit(key);
    tb.add(": ");
  }

  @Override
  public void closePair() {
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
  public void openConstr(final byte[] name) {
    tb.add("new ").add(name).addByte((byte) '(');
    first = true;
  }

  @Override
  public void openArg() {
    if(!first) tb.add(", ");
  }

  @Override
  public void closeArg() {
    first = false;
  }

  @Override
  public void closeConstr() {
    tb.addByte((byte) ')');
  }

  @Override
  public void numberLit(final byte[] value) {
    tb.add(value);
  }

  @Override
  void stringLit(final byte[] value) {
    tb.addByte((byte) '"');
    for(int i = 0; i < value.length; i += Token.cl(value, i)) {
      final int cp = Token.cp(value, i);
      switch(cp) {
        case '\\':
        case '"':
          tb.addByte((byte) '\\').addByte((byte) cp);
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
            tb.addByte((byte) '\\').addByte((byte) 'u');
            for(int j = 4; --j >= 0;) {
              final int hex = cp >>> (j << 2) & 0xF;
              tb.addByte((byte) (hex + (hex > 9 ? 'A' - 10 : '0')));
            }
          } else {
            tb.add(cp);
          }
      }
    }
    tb.addByte((byte) '"');
  }

  @Override
  public void nullLit() {
    tb.add(Token.NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    tb.add(value);
  }

  @Override
  public Item finish() {
    return Str.get(tb.finish());
  }
}
