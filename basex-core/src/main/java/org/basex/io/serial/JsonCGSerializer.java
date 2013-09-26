package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.util.json.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class serializes data as JSON. The input must conform to the rules
 * defined in the {@link JsonCGConverter} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JsonCGSerializer extends JsonSerializer {
  /** Plural. */
  private static final byte[] S = { 's' };
  /** Global data type attributes. */
  private static final byte[][] ATTRS = {
    concat(T_BOOLEAN, S), concat(T_NUMBER, S),
    concat(NULL, S), concat(T_ARRAY, S),
    concat(T_OBJECT, S), concat(T_STRING, S) };
  /** Supported data types. */
  private static final byte[][] TYPES = {
    T_BOOLEAN, T_NUMBER, NULL, T_ARRAY, T_OBJECT, T_STRING };

  /** Cached data types. */
  private final TokenSet[] typeCache = new TokenSet[TYPES.length];
  /** Comma flags. */
  private final BoolList comma = new BoolList();
  /** Types. */
  private final TokenList types = new TokenList();

  /**
   * Constructor.
   * @param os output stream reference
   * @param props serialization properties
   * @throws IOException I/O exception
   */
  protected JsonCGSerializer(final OutputStream os, final SerializerProp props)
      throws IOException {
    super(os, props);
    for(int t = 0; t < typeCache.length; t++) typeCache[t] = new TokenMap();
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    if(level == 0 && !eq(name, T_JSON)) error("<%> expected as root node", T_JSON);
    types.set(level, null);
    comma.set(level + 1, false);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    if(level == 0) {
      final int tl = typeCache.length;
      for(int t = 0; t < tl; t++) {
        if(eq(name, ATTRS[t])) {
          for(final byte[] b : split(value, ' ')) typeCache[t].add(b);
          return;
        }
      }
    }
    if(eq(name, T_TYPE)) {
      if(!eq(value, TYPES)) error("Element <%> has invalid type \"%\"", elem, value);
      types.set(level, value);
    } else if(!eq(name, XMLNS) && !startsWith(name, XMLNSC)) {
      error("Element <%> has invalid attribute \"%\"", elem, name);
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    if(comma.get(level)) print(',');
    else comma.set(level, true);

    if(level > 0) {
      indent(level);
      final byte[] par = types.get(level - 1);
      if(eq(par, T_OBJECT)) {
        print('"');
        print(XMLToken.decode(elem));
        print("\": ");
      } else if(!eq(par, T_ARRAY)) {
        error("Element <%> is typed as \"%\" and cannot be nested",
            tags.get(level - 1), par);
      }
    }

    byte[] type = types.get(level);
    if(type == null) {
      int t = -1;
      final int tl = typeCache.length;
      while(++t < tl && !typeCache[t].contains(elem));
      if(t != tl) type = TYPES[t];
      else type = T_STRING;
      types.set(level,  type);
    }

    if(eq(type, T_OBJECT)) {
      print('{');
    } else if(eq(type, T_ARRAY)) {
      print('[');
    } else if(level == 0) {
      error("Element <%> must be typed as \"%\" or \"%\"",
          T_JSON, T_OBJECT, T_ARRAY);
    }
  }

  @Override
  protected void finishText(final byte[] text) throws IOException {
    final byte[] type = types.get(level - 1);
    if(eq(type, T_STRING)) {
      print('"');
      for(final byte ch : text) code(ch);
      print('"');
    } else if(eq(type, T_BOOLEAN, T_NUMBER)) {
      print(text);
    } else if(trim(text).length != 0) {
      error("Element <%> is typed as \"%\" and cannot have a value",
          tags.get(level - 1), type);
    }
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    final byte[] type = types.get(level);
    if(eq(type, T_STRING)) {
      print("\"\"");
    } else if(eq(type, NULL)) {
      print(NULL);
    } else if(!eq(type, T_OBJECT, T_ARRAY)) {
      error("Value expected for type \"%\"", type);
    }
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    final byte[] type = types.get(level);
    if(eq(type, T_ARRAY)) {
      indent(level);
      print(']');
    } else if(eq(type, T_OBJECT)) {
      indent(level);
      print('}');
    }
  }

  @Override
  protected void code(final int ch) throws IOException {
    switch(ch) {
      case '\b': print("\\b");  break;
      case '\f': print("\\f");  break;
      case '\n': print("\\n");  break;
      case '\r': print("\\r");  break;
      case '\t': print("\\t");  break;
      case '"':  print("\\\""); break;
      case '/':  print("\\/");  break;
      case '\\': print("\\\\"); break;
      default:   print(ch);     break;
    }
  }

  @Override
  protected void finishComment(final byte[] value) throws IOException {
    error("Comments cannot be serialized");
  }

  @Override
  protected void finishPi(final byte[] name, final byte[] value) throws IOException {
    error("Processing instructions cannot be serialized");
  }

  @Override
  protected void atomic(final Item value) throws IOException {
    error("Atomic values cannot be serialized");
  }

  /**
   * Prints some indentation.
   * @param lvl level
   * @throws IOException I/O exception
   */
  void indent(final int lvl) throws IOException {
    if(!indent) return;
    print(nl);
    final int ls = lvl * indents;
    for(int l = 0; l < ls; ++l) print(tab);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws IOException I/O exception
   */
  private static void error(final String msg, final Object... ext) throws IOException {
    throw BXJS_SERIAL.thrwSerial(Util.inf(msg, ext));
  }
}
