package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.util.json.JSONConverter;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.BoolList;
import org.basex.util.list.TokenList;

/**
 * This class serializes data as JSON. The input must conform to the rules
 * defined in the {@link JSONConverter} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JSONSerializer extends OutputSerializer {
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
  /** Comma flag. */
  private final BoolList comma = new BoolList();
  /** Types. */
  private final TokenList types = new TokenList();

  /**
   * Constructor.
   * @param os output stream reference
   * @param props serialization properties
   * @throws IOException I/O exception
   */
  public JSONSerializer(final OutputStream os, final SerializerProp props)
      throws IOException {
    super(os, props);
    for(int t = 0; t < typeCache.length; t++) typeCache[t] = new TokenMap();
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    if(level == 0 && !eq(name, T_JSON))
      error("<%> expected as root node", T_JSON);
    types.set(level, null);
    comma.set(level + 1, false);
  }

  @Override
  public void attribute(final byte[] name, final byte[] value)
      throws IOException {

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
      types.set(level, value);
      if(!eq(value, TYPES))
        error("Element <%> has invalid type \"%\"", tag, value);
    } else {
      error("Element <%> has invalid attribute \"%\"", tag, name);
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
        print(name(tag));
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
      while(++t < tl && typeCache[t].id(tag) == 0);
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
  public void finishText(final byte[] text) throws IOException {
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
  public void finishComment(final byte[] value) throws IOException {
    error("Comments cannot be serialized");
  }

  @Override
  public void finishPi(final byte[] name, final byte[] value)
      throws IOException {
    error("Processing instructions cannot be serialized");
  }

  @Override
  public void finishItem(final Item value) throws IOException {
    error("Items cannot be serialized");
  }

  /**
   * Prints some indentation.
   * @param lvl level
   * @throws IOException I/O exception
   */
  protected void indent(final int lvl) throws IOException {
    print(nl);
    final int ls = lvl * indents;
    for(int l = 0; l < ls; ++l) print(tab);
  }

  /**
   * Converts an XML element name to a JSON name.
   * @param name name
   * @return cached QName
   */
  private byte[] name(final byte[] name) {
    // convert name to valid XML representation
    final TokenBuilder tb = new TokenBuilder();
    int uc = 0;
    // mode: 0=normal, 1=unicode, 2=underscore, 3=building unicode
    int mode = 0;
    for(int n = 0; n < name.length;) {
      final int cp = cp(name, n);
      if(mode >= 3) {
        uc = (uc << 4) + cp - (cp >= '0' && cp <= '9' ? '0' : 0x37);
        if(++mode == 7) {
          tb.add(uc);
          mode = 0;
        }
      } else if(cp == '_') {
        // limit underscore counter
        if(++mode == 3) {
          tb.add('_');
          mode = 0;
          continue;
        }
      } else if(mode == 1) {
        // unicode
        mode = 3;
        continue;
      } else if(mode == 2) {
        // underscore
        tb.add('_');
        mode = 0;
        continue;
      } else {
        // normal character
        tb.add(cp);
        mode = 0;
      }
      n += cl(name, n);
    }
    if(mode == 2) {
      tb.add('_');
    } else if(mode > 0 && tb.size() != 0) {
      tb.add('?');
    }
    return tb.finish();
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return build exception
   * @throws IOException I/O exception
   */
  private QueryException error(final String msg, final Object... ext)
      throws IOException {
    throw JSONSER.thrwSerial(Util.inf(msg, ext));
  }
}
