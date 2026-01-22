package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

import java.io.*;
import java.util.*;

import org.basex.build.json.*;
import org.basex.io.out.PrintOutput.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.Options.*;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends StandardSerializer {
  /** QName: xml:base. */
  private static final QNm FN_NULL = new QNm(JsonConstants.NULL, QueryText.FN_URI);

  /** JSON options. */
  final JsonSerialOptions jopts;
  /** Escape special characters. */
  final boolean escape;
  /** Escape special solidus. */
  final boolean escapeSolidus;
  /** Lines. */
  final boolean lines;
  /** Allow duplicate names. */
  final boolean nodups;

  /**
   * Returns a JSON serializer for the given serialization options.
   * @param os output stream reference
   * @param so serialization options
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os, final SerializerOptions so)
      throws IOException {
    return switch(so.get(SerializerOptions.JSON).get(JsonOptions.FORMAT)) {
      case JSONML        -> new JsonMLSerializer(os, so);
      case W3_XML, BASIC -> new JsonBasicSerializer(os, so);
      default            -> new JsonNodeSerializer(os, so);
    };
  }

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  JsonSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts);
    jopts = sopts.get(SerializerOptions.JSON);
    escape = jopts.get(JsonSerialOptions.ESCAPE);
    escapeSolidus = escape && !canonical && jopts.get(JsonSerialOptions.ESCAPE_SOLIDUS) &&
        sopts.get(SerializerOptions.ESCAPE_SOLIDUS) == YesNo.YES;
    nodups = sopts.get(SerializerOptions.ALLOW_DUPLICATE_NAMES) == YesNo.NO;
    lines = sopts.get(SerializerOptions.JSON_LINES) == YesNo.YES;
    final Boolean ji = jopts.get(JsonSerialOptions.INDENT);
    if(ji != null) indent = ji;
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(sep) {
      if(!lines) throw SERJSON.getIO();
      out.print('\n');
    }
    if(item == null || item instanceof final QNm qnm && qnm.eq(FN_NULL)) {
      out.print(JsonConstants.NULL);
    } else {
      super.serialize(item);
    }
    sep = true;
  }

  /**
   * Serializes a value.
   * @param value value
   * @throws IOException I/O exception
   */
  private void serialize(final Value value) throws IOException {
    if(value.size() > 1) throw SERJSONSEQ.getIO();
    sep = false;
    serialize(value.isEmpty() ? null : (Item) value);
  }

  @Override
  public void function(final FItem item) throws IOException {
    try {
      if(item instanceof final XQMap map) {
        level++;
        out.print('{');

        boolean s = false;
        final TokenSet set = nodups ? new TokenSet() : null;
        for(final Item key : keys(map)) {
          final byte[] name = key.string(null);
          if(nodups) {
            if(set.contains(name)) throw SERDUPL_X.getIO(name);
            set.put(name);
          }
          if(s) out.print(',');
          indent();
          string(name);
          out.print(':');
          if(indent) out.print(' ');
          serialize(map.get(key));
          s = true;
        }

        level--;
        indent();
        out.print('}');
      } else if(item instanceof final XQArray array) {
        level++;
        out.print('[');

        boolean s = false;
        for(final Value value : array.iterable()) {
          if(s) out.print(',');
          indent();
          serialize(value);
          s = true;
        }

        level--;
        indent();
        out.print(']');
      } else {
        throw SERJSONFUNC_X.getIO(item.type);
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
  }

  /**
   * Returns the keys of the given map, in canonical order, if required. Per RFC8785, "property name
   * strings to be sorted are formatted as arrays of UTF-16 [UNICODE] code units. The sorting is
   * based on pure value comparisons, where code units are treated as unsigned integers, independent
   * of locale settings". This is achieved here by sorting the keys as strings.
   * @param map map
   * @return keys
   * @throws QueryException query exception
   */
  private Iterable<Item> keys(final XQMap map) throws QueryException {
    final Value ks = map.keys();
    if(!canonical || ks.size() < 2) return ks;

    record Key(String string, Item item) { }
    final ArrayList<Key> list = new ArrayList<>();
    for(final Item k : ks) list.add(new Key(Token.string(k.string(null)), k));
    return list.stream().sorted(Comparator.comparing(Key::string)).map(Key::item).toList();
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    try {
      final Type type = item.type;
      if(type.oneOf(AtomType.DOUBLE, AtomType.FLOAT) || canonical && type.isNumber()) {
        final double d = item.dbl(null);
        if(Double.isFinite(d)) {
          out.print(Dbl.string(d));
        } else {
          if(canonical) throw SERNUMBER_X.getIO(d);
          out.print(d == Double.POSITIVE_INFINITY ? JsonConstants.INF :
            d == Double.NEGATIVE_INFINITY ? JsonConstants.NINF : JsonConstants.NULL);
        }
      } else if(type == AtomType.BOOLEAN || type.isNumber()) {
        out.print(item.string(null));
      } else {
        string(item.string(null));
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  protected void indent() throws IOException {
    if(!lines) super.indent();
    else if(indent) out.print(' ');
  }

  @Override
  protected boolean separate() {
    return false;
  }

  /**
   * Serializes a JSON string.
   * @param string string
   * @throws IOException I/O exception
   */
  protected final void string(final byte[] string) throws IOException {
    out.print('"');
    final byte[] norm = normalize(string, form);
    final int nl = norm.length;
    for(int n = 0; n < nl; n += cl(norm, n)) printChar(cp(norm, n));
    out.print('"');
  }

  /** Fallback function. */
  private final Fallback fallback = cp -> {
    if(Character.isBmpCodePoint(cp)) {
      out.print('\\');
      out.print('u');
      out.print(hex(cp, 4));
    } else {
      out.print('\\');
      out.print('u');
      out.print(hex(Character.highSurrogate(cp), 4));
      out.print('\\');
      out.print('u');
      out.print(hex(Character.lowSurrogate(cp), 4));
    }
  };

  @Override
  protected final void print(final int cp) throws IOException {
    if(escape) {
      switch(cp) {
        case '\b':
          out.print('\\');
          out.print('b');
          break;
        case '\f':
          out.print('\\');
          out.print('f');
          break;
        case '\n':
          out.print('\\');
          out.print('n');
          break;
        case '\r':
          out.print('\\');
          out.print('r');
          break;
        case '\t':
          out.print('\\');
          out.print('t');
          break;
        case '"' :
          out.print('\\');
          out.print('"');
          break;
        case '/' :
          if(escapeSolidus) out.print('\\');
          out.print('/');
          break;
        case '\\':
          out.print('\\');
          out.print('\\');
          break;
        default:
          out.print(cp, fallback);
          break;
      }
    } else {
      out.print(cp, fallback);
    }
  }

  @Override
  public void close() throws IOException {
    if(!sep && !lines) out.print(JsonConstants.NULL);
    super.close();
  }
}
