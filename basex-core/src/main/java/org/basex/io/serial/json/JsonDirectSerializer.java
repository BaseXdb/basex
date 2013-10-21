package org.basex.io.serial.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class serializes data as JSON. The input must conform to the rules
 * defined in the {@link JsonDirectConverter} class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class JsonDirectSerializer extends JsonSerializer {
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
  public JsonDirectSerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {

    super(os, opts);
    for(int t = 0; t < typeCache.length; t++) typeCache[t] = new TokenMap();
    lax = jopts.get(JsonOptions.LAX);
    atts = jopts.get(JsonOptions.FORMAT) == JsonFormat.ATTRIBUTES;
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    if(level == 0 && !eq(name, JSON)) error("<%> expected as root node", JSON);
    types.set(level, null);
    comma.set(level + 1, false);
    key = atts ? null : name;
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    // parse merged types on root level
    if(level == 0) {
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
      if(!eq(value, TYPES)) error("<%> has invalid type \"%\"", tag, value);
      types.set(level, value);
    } else if(atts && eq(name, NAME)) {
      key = value;
      if(!eq(tag, PAIR)) error("<%> found, <pair> expected", tag);
    } else if(!eq(name, XMLNS) && !startsWith(name, XMLNSC)) {
      error("<%> has invalid attribute \"%\"", tag, name);
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    if(comma.get(level)) print(',');
    else comma.set(level, true);

    if(level > 0) {
      indent(level);
      final byte[] ptype = types.get(level - 1);
      if(eq(ptype, OBJECT)) {
        if(atts && !eq(tag, PAIR)) error("<%> found, <%> expected", tag, PAIR);
        if(key == null) error("<%> has no name attribute", tag);
        print('"');
        print(XMLToken.decode(key, lax));
        print("\":");
        if(indent) print(' ');
      } else if(eq(ptype, ARRAY)) {
        if(atts) {
          if(!eq(tag, ITEM)) error("<%> found, <%> expected", tag, ITEM);
          if(key != null) error("<%> must have no name attribute", tag);
        } else {
          if(!eq(tag, VALUE)) error("<%> found, <%> expected", tag, VALUE);
        }
      } else {
        error("<%> is typed as \"%\" and cannot be nested", tags.get(level - 1), ptype);
      }
    }

    byte[] type = types.get(level);
    if(type == null) {
      if(key != null) {
        final int tl = typeCache.length;
        for(int t = 0; t < tl && type == null; t++) {
          if(typeCache[t].contains(key)) type = TYPES[t];
        }
      }
      if(type == null) type = STRING;
      types.set(level,  type);
    }

    if(eq(type, OBJECT)) {
      print('{');
    } else if(eq(type, ARRAY)) {
      print('[');
    } else if(level == 0 && spec == JsonSpec.RFC4627) {
      error("<%> must be typed as \"%\" or \"%\"", JSON, OBJECT, ARRAY);
    }
  }

  @Override
  protected void finishText(final byte[] text) throws IOException {
    final byte[] type = types.get(level - 1);
    if(eq(type, STRING)) {
      print('"');
      for(final byte ch : text) encode(ch);
      print('"');
    } else if(eq(type, BOOLEAN)) {
      if(!eq(text, TRUE, FALSE))
        error("Value of <%> is no boolean: \"%\"", tags.get(level - 1), text);
      print(text);
    } else if(eq(type, NUMBER)) {
      if(Double.isNaN(toDouble(text)))
        error("Value of <%> is no number: \"%\"", tags.get(level - 1), text);
      print(text);
    } else if(trim(text).length != 0) {
      error("<%> is typed as \"%\" and cannot have a value", tags.get(level - 1), type);
    }
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    final byte[] type = types.get(level);
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
    final byte[] type = types.get(level);
    if(eq(type, ARRAY)) {
      indent(level);
      print(']');
    } else if(eq(type, OBJECT)) {
      indent(level);
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
    throw BXJS_SERIAL.thrwIO(Util.inf(msg, ext));
  }
}
