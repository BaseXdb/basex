package org.basex.query.util.json;

import org.basex.build.*;
import org.basex.build.JsonOptions.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Writes the parsed JSON file to the given {@link TokenBuilder}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class JsonStringConverter implements JsonHandler {
  /** The token builder. */
  private final TokenBuilder tb;
  /** Flag for first array entry, object member or constructor argument. */
  private boolean first = true;

  /**
   * Constructor.
   * @param builder the token builder
   */
  private JsonStringConverter(final TokenBuilder builder) {
    tb = builder;
  }

  /**
   * Writes a pretty-printed representation of the given JSON string to the given builder.
   * @param json JSON string
   * @param spec JSON spec for parsing
   * @param un unescape flag
   * @param tb token builder
   * @return the token builder
   * @throws QueryException parse exception
   */
  public static TokenBuilder print(final String json, final JsonSpec spec,
      final boolean un, final TokenBuilder tb) throws QueryException {

    final JsonOptions jopts = new JsonOptions();
    jopts.set(JsonOptions.SPEC, spec.toString());
    jopts.set(JsonOptions.UNESCAPE, un);
    JsonParser.parse(json, jopts, new JsonStringConverter(tb), null);
    return tb;
  }

  @Override
  public void openObject() throws QueryException {
    tb.add("{ ");
    first = true;
  }

  @Override
  public void openPair(final byte[] key) throws QueryException {
    if(!first) tb.add(", ");
    stringLit(key);
    tb.add(": ");
  }

  @Override
  public void closePair() throws QueryException {
    first = false;
  }

  @Override
  public void closeObject() throws QueryException {
    tb.add(first ? "}" : " }");
  }

  @Override
  public void openArray() throws QueryException {
    tb.add("[ ");
    first = true;
  }

  @Override
  public void openItem() throws QueryException {
    if(!first) tb.add(", ");
  }

  @Override
  public void closeItem() throws QueryException {
    first = false;
  }

  @Override
  public void closeArray() throws QueryException {
    tb.add(first ? "]" : " ]");
  }

  @Override
  public void openConstr(final byte[] name) throws QueryException {
    tb.add("new ").add(name).addByte((byte) '(');
    first = true;
  }

  @Override
  public void openArg() throws QueryException {
    if(!first) tb.add(", ");
  }

  @Override
  public void closeArg() throws QueryException {
    first = false;
  }

  @Override
  public void closeConstr() throws QueryException {
    tb.addByte((byte) ')');
  }

  @Override
  public void numberLit(final byte[] value) throws QueryException {
    tb.add(value);
  }

  @Override
  public void stringLit(final byte[] value) {
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
  public void nullLit() throws QueryException {
    tb.add(Token.NULL);
  }

  @Override
  public void booleanLit(final byte[] b) throws QueryException {
    tb.add(b);
  }
}
