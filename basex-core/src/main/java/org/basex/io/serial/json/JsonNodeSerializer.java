package org.basex.io.serial.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.JsonFormat;
import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.Options.YesNo;

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
  /** Escape special characters. */
  private final Serializer ser;
  /** Node output cache. */
  private final ArrayOutput cache = new ArrayOutput();

  /** Current name of a pair. */
  private byte[] key;
  /** Custom serialization. */
  private boolean custom;

  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonNodeSerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {

    super(os, opts);

    final SerializerOptions so = new SerializerOptions();
    so.set(SerializerOptions.METHOD, opts.get(SerializerOptions.JSON_NODE_OUTPUT_METHOD));
    so.set(SerializerOptions.OMIT_XML_DECLARATION, YesNo.YES);
    ser = Serializer.get(cache, so);

    for(int t = 0; t < typeCache.length; t++) typeCache[t] = new TokenMap();
    atts = jopts.get(JsonOptions.FORMAT) == JsonFormat.ATTRIBUTES;
    lax = jopts.get(JsonOptions.LAX) || atts;
  }

  @Override
  protected void serialize(final ANode node) throws IOException {
    final boolean doc = node.type == NodeType.DOC;
    final boolean elm = node.type == NodeType.ELM && eq(JSON, node.name());
    if(custom || doc || elm) {
      final boolean c = custom;
      if(!custom) custom = elm;
      super.serialize(node);
      custom = c;
    } else {
      ser.serialize(node);
      ser.close();
      string(cache.toArray());
      cache.reset();
    }
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
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
      if(!eq(value, TYPES)) throw error("<%> has invalid type \"%\"", elem, value);
      types.set(lvl, value);
    } else if(atts && eq(name, NAME)) {
      key = value;
      if(!eq(elem, PAIR)) throw error("<%> found, <pair> expected", elem);
    } else if(!eq(name, XMLNS) && !startsWith(name, XMLNSC)) {
      throw error("<%> has invalid attribute \"%\"", elem, name);
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    if(comma.get(lvl)) print(',');
    else comma.set(lvl, true);

    if(lvl > 0) {
      indent();
      final byte[] ptype = types.get(lvl - 1);
      if(eq(ptype, OBJECT)) {
        if(atts && !eq(elem, PAIR)) throw error("<%> found, <%> expected", elem, PAIR);
        if(key == null) throw error("<%> has no name attribute", elem);
        print('"');
        final byte[] name = atts ? key : XMLToken.decode(key, lax);
        if(name == null) throw error("Name of element <%> is invalid", key);
        print(name);
        print("\":");
      } else if(eq(ptype, ARRAY)) {
        if(atts) {
          if(!eq(elem, ITEM)) throw error("<%> found, <%> expected", elem, ITEM);
          if(key != null) throw error("<%> must have no name attribute", elem);
        } else {
          if(!eq(elem, VALUE)) throw error("<%> found, <%> expected", elem, VALUE);
        }
      } else {
        throw error("<%> is typed as \"%\" and cannot be nested", elems.get(lvl - 1), ptype);
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
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    final byte[] type = types.get(lvl - 1);
    if(eq(type, STRING)) {
      print('"');
      for(final byte ch : value) encode(ch);
      print('"');
    } else if(eq(type, BOOLEAN)) {
      if(!eq(value, TRUE, FALSE))
        throw error("Value of <%> is no boolean: \"%\"", elems.get(lvl - 1), value);
      print(value);
    } else if(eq(type, NUMBER)) {
      if(Double.isNaN(toDouble(value)))
        throw error("Value of <%> is no number: \"%\"", elems.get(lvl - 1), value);
      print(value);
    } else if(trim(value).length != 0) {
      throw error("<%> is typed as \"%\" and cannot have a value", elems.get(lvl - 1), type);
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
      throw error("Value expected for type \"%\"", type);
    }
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    final byte[] type = types.get(lvl);
    if(eq(type, ARRAY)) {
      indent();
      print(']');
    } else if(eq(type, OBJECT)) {
      indent();
      print('}');
    }
  }

  @Override
  protected void comment(final byte[] value) throws IOException {
    throw error("Comments cannot be serialized");
  }

  @Override
  protected void pi(final byte[] name, final byte[] value) throws IOException {
    throw error("Processing instructions cannot be serialized");
  }

  @Override
  protected void atomic(final Item value, final boolean iter) throws IOException {
    throw error("Atomic values cannot be serialized");
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return I/O exception
   */
  private static QueryIOException error(final String msg, final Object... ext) {
    return BXJS_SERIAL_X.getIO(Util.inf(msg, ext));
  }
}
