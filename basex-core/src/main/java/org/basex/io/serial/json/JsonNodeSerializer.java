package org.basex.io.serial.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.JsonFormat;
import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.Options.YesNo;

/**
 * This class serializes items as JSON. The input must conform to the rules
 * defined in the {@link JsonDirectConverter} and {@link JsonAttsConverter} class.
 *
 * @author BaseX Team 2005-15, BSD License
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
  /** Node serializer. */
  private final Serializer ser;
  /** Node output cache. */
  private final ArrayOutput cache = new ArrayOutput();

  /** Current name of a pair. */
  private byte[] key;
  /** Custom serialization. */
  private boolean custom;

  /**
   * Constructor.
   * @param out print output
   * @param opts serialization parameters
   * @param adaptive adaptive serializer (can be {@code null})
   * @throws IOException I/O exception
   */
  public JsonNodeSerializer(final PrintOutput out, final SerializerOptions opts,
      final AdaptiveSerializer adaptive) throws IOException {
    this(out, opts);
    this.adaptive = adaptive;
  }

  /**
   * Constructor.
   * @param out print output
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonNodeSerializer(final PrintOutput out, final SerializerOptions opts)
      throws IOException {

    super(out, opts);

    final SerializerOptions so = new SerializerOptions();
    so.set(SerializerOptions.METHOD, opts.get(SerializerOptions.JSON_NODE_OUTPUT_METHOD));
    so.set(SerializerOptions.OMIT_XML_DECLARATION, YesNo.YES);
    so.set(SerializerOptions.INDENT, YesNo.NO);
    ser = Serializer.get(cache, so);

    final int tl = typeCache.length;
    for(int t = 0; t < tl; t++) typeCache[t] = new TokenMap();
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
      ser.reset();
      string(cache.toArray());
      cache.reset();
    }
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
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
      if(!eq(value, TYPES)) throw error("<%> has invalid type \"%\"", elem, value);
      types.set(level, value);
    } else if(atts && eq(name, NAME)) {
      key = value;
      if(!eq(elem, PAIR)) throw error("<%> found, <pair> expected", elem);
    } else if(!eq(name, XMLNS) && !startsWith(name, XMLNSC)) {
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
        if(atts && !eq(elem, PAIR)) throw error("<%> found, <%> expected", elem, PAIR);
        if(key == null) throw error("<%> has no name attribute", elem);
        out.print('"');
        final byte[] name = atts ? key : XMLToken.decode(key, lax);
        if(name == null) throw error("Name of element <%> is invalid", key);
        out.print(name);
        out.print("\":");
      } else if(eq(ptype, ARRAY)) {
        if(atts) {
          if(!eq(elem, ITEM)) throw error("<%> found, <%> expected", elem, ITEM);
          if(key != null) throw error("<%> must have no name attribute", elem);
        } else {
          if(!eq(elem, VALUE)) throw error("<%> found, <%> expected", elem, VALUE);
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
      for(final byte ch : norm(value)) encode(ch);
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
    return BXJS_SERIAL_X.getIO(Util.inf(msg, ext));
  }
}
