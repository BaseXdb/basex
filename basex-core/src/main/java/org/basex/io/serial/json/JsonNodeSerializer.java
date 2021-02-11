package org.basex.io.serial.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.out.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.Options.*;

/**
 * This class serializes items as JSON. The input must conform to the rules
 * defined in the {@link JsonDirectConverter} and {@link JsonAttsConverter} class.
 *
 * @author BaseX Team 2005-21, BSD License
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
  /** Node output cache. */
  private final ArrayOutput cache = new ArrayOutput();

  /** Current name of a pair. */
  private byte[] key;
  /** BaseX JSON serialization. */
  private boolean custom;
  /** Node serializer. */
  private Serializer nodeSerializer;

  /**
   * Constructor.
   * @param os output stream
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonNodeSerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {

    super(os, opts);
    final int tl = typeCache.length;
    for(int t = 0; t < tl; t++) typeCache[t] = new TokenMap();
    atts = jopts.get(JsonOptions.FORMAT) == JsonFormat.ATTRIBUTES;
    lax = jopts.get(JsonOptions.LAX) || atts;
  }

  @Override
  protected void node(final ANode node) throws IOException {
    if(node.type == NodeType.DOCUMENT_NODE || custom) {
      super.node(node);
    } else if(level == 0 && node.type == NodeType.ELEMENT && eq(JSON, node.name())) {
      final boolean c = custom;
      custom = true;
      super.node(node);
      custom = c;
    } else {
      try(Serializer ser = nodeSerializer()) {
        ser.serialize(node);
        ser.reset();
      }
      string(cache.next());
    }
  }

  @Override
  protected void startOpen(final QNm name) {
    types.set(level, null);
    comma.set(level + 1, false);
    key = atts ? null : name.string();
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {

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
      if(!eq(value, TYPES)) throw error("<%> has invalid type \"%\"", elem, value);
      types.set(level, value);
    } else if(atts && eq(name, NAME)) {
      key = value;
      if(!eq(elem.string(), PAIR)) throw error("<%> found, <pair> expected", elem);
    } else if(!eq(name, XMLNS) && !startsWith(name, XMLNS_COLON)) {
      throw error("<%> has invalid attribute \"%\"", elem, name);
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    if(comma.get(level)) out.print(',');
    else comma.set(level, true);

    if(level > 0) {
      indent();
      final byte[] ptype = types.get(level - 1);
      if(eq(ptype, OBJECT)) {
        if(atts && !eq(elem.string(), PAIR)) throw error("<%> found, <%> expected", elem, PAIR);
        if(key == null) throw error("<%> has no name attribute", elem);
        out.print('"');
        final byte[] name = atts ? key : XMLToken.decode(key, lax);
        if(name == null) throw error("Name of element <%> is invalid", key);
        out.print(norm(name));
        out.print("\":");
      } else if(eq(ptype, ARRAY)) {
        if(atts) {
          if(!eq(elem.string(), ITEM)) throw error("<%> found, <%> expected", elem, ITEM);
          if(key != null) throw error("<%> must have no name attribute", elem);
        } else {
          if(!eq(elem.string(), VALUE)) throw error("<%> found, <%> expected", elem, VALUE);
        }
      } else {
        throw error("<%> is typed as \"%\" and cannot be nested", elems.get(level - 1), ptype);
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
      out.print('{');
    } else if(eq(type, ARRAY)) {
      out.print('[');
    }
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    final byte[] type = types.get(level - 1);
    if(eq(type, STRING)) {
      out.print('"');
      for(final byte ch : norm(value)) printChar(ch);
      out.print('"');
    } else if(eq(type, BOOLEAN)) {
      if(!eq(value, TRUE, FALSE))
        throw error("Value of <%> is no boolean: \"%\"", elems.get(level - 1), value);
      out.print(value);
    } else if(eq(type, NUMBER)) {
      if(Double.isNaN(toDouble(value)))
        throw error("Value of <%> is no number: \"%\"", elems.get(level - 1), value);
      out.print(value);
    } else if(trim(value).length != 0) {
      throw error("<%> is typed as \"%\" and cannot have a value", elems.get(level - 1), type);
    }
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    final byte[] type = types.get(level);
    if(eq(type, STRING)) {
      out.print("\"\"");
    } else if(eq(type, NULL)) {
      out.print(NULL);
    } else if(!eq(type, OBJECT, ARRAY)) {
      throw error("Value expected for type \"%\"", type);
    }
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    final byte[] type = types.get(level);
    if(eq(type, ARRAY)) {
      indent();
      out.print(']');
    } else if(eq(type, OBJECT)) {
      indent();
      out.print('}');
    }
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return I/O exception
   */
  private static QueryIOException error(final String msg, final Object... ext) {
    return JSON_SERIALIZE_X.getIO(Util.inf(msg, ext));
  }

  /**
   * Returns a node serializer.
   * @return serializer
   * @throws IOException I/O exception
   */
  private Serializer nodeSerializer() throws IOException {
    if(nodeSerializer == null) {
      final SerializerOptions so = new SerializerOptions();
      so.set(SerializerOptions.METHOD, sopts.get(SerializerOptions.JSON_NODE_OUTPUT_METHOD));
      so.set(SerializerOptions.OMIT_XML_DECLARATION, YesNo.YES);
      so.set(SerializerOptions.INDENT, YesNo.NO);
      nodeSerializer = Serializer.get(cache, so);
    }
    return nodeSerializer;
  }
}
