package org.basex.io.serial.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.JsonFormat;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class serializes data as JSON. The input must conform to the rules
 * defined in the {@link JsonDirectConverter} and {@link JsonAttsConverter} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class JsonNodeSerializer extends JsonSerializer {
  /** Cached data types. */
  private final TokenSet[] typeCache = new TokenSet[TYPES.length];
  /** Comma flags. */
  private final BoolList comma = new BoolList();
  /** Types. */
  private final TokenList types = new TokenList();
  /** Lax flag. */
  private final boolean lax;
  /** Attributes flag. */
  private final boolean atts;

  /** Current name of a pair. */
  private byte[] key;

  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonNodeSerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {

    super(os, opts);
    for(int t = 0; t < typeCache.length; t++) typeCache[t] = new TokenMap();
    atts = jopts.get(JsonOptions.FORMAT) == JsonFormat.ATTRIBUTES;
    lax = jopts.get(JsonOptions.LAX) || atts;
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    if(lvl == 0 && !eq(name, JSON)) error("<%> expected as root node", JSON);
    types.set(lvl, null);
    comma.set(lvl + 1, false);
    key = atts ? null : name;
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    // parse merged types on root level
    if(lvl == 0) {
      final int tl = typeCache.length;
      for(int t = 0; t < tl; t++) {
        if(eq(name, ATTRS[t])) {
          for(final byte[] b : split(value, ' ')) typeCache[t].add(b);
          return;
        }
      }
    }

    if(eq(name, TYPE)) {
      // add single type
      if(!eq(value, TYPES)) error("<%> has invalid type \"%\"", elem, value);
      types.set(lvl, value);
    } else if(atts && eq(name, NAME)) {
      key = value;
      if(!eq(elem, PAIR)) error("<%> found, <pair> expected", elem);
    } else if(!eq(name, XMLNS) && !startsWith(name, XMLNSC)) {
      error("<%> has invalid attribute \"%\"", elem, name);
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    if(comma.get(lvl)) print(',');
    else comma.set(lvl, true);

    if(lvl > 0) {
      indent(lvl);
      final byte[] ptype = types.get(lvl - 1);
      if(eq(ptype, OBJECT)) {
        if(atts && !eq(elem, PAIR)) error("<%> found, <%> expected", elem, PAIR);
        if(key == null) error("<%> has no name attribute", elem);
        print('"');
        final byte[] name = atts ? key : XMLToken.decode(key, lax);
        if(name == null) error("Name of element <%> is invalid", key);
        print(name);
        print("\":");
        if(indent) print(' ');
      } else if(eq(ptype, ARRAY)) {
        if(atts) {
          if(!eq(elem, ITEM)) error("<%> found, <%> expected", elem, ITEM);
          if(key != null) error("<%> must have no name attribute", elem);
        } else {
          if(!eq(elem, VALUE)) error("<%> found, <%> expected", elem, VALUE);
        }
      } else {
        error("<%> is typed as \"%\" and cannot be nested", elems.get(lvl - 1), ptype);
      }
    }

    byte[] type = types.get(lvl);
    if(type == null) {
      if(key != null) {
        final int tl = typeCache.length;
        for(int t = 0; t < tl && type == null; t++) {
          if(typeCache[t].contains(key)) type = TYPES[t];
        }
      }
      if(type == null) type = STRING;
      types.set(lvl,  type);
    }

    if(eq(type, OBJECT)) {
      print('{');
    } else if(eq(type, ARRAY)) {
      print('[');
    }
  }

  @Override
  protected void finishText(final byte[] value) throws IOException {
    final byte[] type = types.get(lvl - 1);
    if(eq(type, STRING)) {
      print('"');
      for(final byte ch : value) encode(ch);
      print('"');
    } else if(eq(type, BOOLEAN)) {
      if(!eq(value, TRUE, FALSE))
        error("Value of <%> is no boolean: \"%\"", elems.get(lvl - 1), value);
      print(value);
    } else if(eq(type, NUMBER)) {
      if(Double.isNaN(toDouble(value)))
        error("Value of <%> is no number: \"%\"", elems.get(lvl - 1), value);
      print(value);
    } else if(trim(value).length != 0) {
      error("<%> is typed as \"%\" and cannot have a value", elems.get(lvl - 1), type);
    }
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    final byte[] type = types.get(lvl);
    if(eq(type, STRING)) {
      print("\"\"");
    } else if(eq(type, NULL)) {
      print(NULL);
    } else if(!eq(type, OBJECT, ARRAY)) {
      error("Value expected for type \"%\"", type);
    }
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    final byte[] type = types.get(lvl);
    if(eq(type, ARRAY)) {
      indent(lvl);
      print(']');
    } else if(eq(type, OBJECT)) {
      indent(lvl);
      print('}');
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
  protected void atomic(final Item value, final boolean iter) throws IOException {
    error("Atomic values cannot be serialized");
  }

  /**
   * Prints some indentation.
   * @param level level
   * @throws IOException I/O exception
   */
  private void indent(final int level) throws IOException {
    if(!indent) return;
    print(nl);
    final int ls = level * indents;
    for(int l = 0; l < ls; ++l) print(tab);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws IOException I/O exception
   */
  private static void error(final String msg, final Object... ext) throws IOException {
    throw BXJS_SERIAL_X.getIO(Util.inf(msg, ext));
  }
}
