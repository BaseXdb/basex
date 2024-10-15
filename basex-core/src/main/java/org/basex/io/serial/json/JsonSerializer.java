package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

import java.io.*;

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
import org.basex.util.hash.*;
import org.basex.util.options.Options.*;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-24, BSD License
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
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  JsonSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts);
    jopts = sopts.get(SerializerOptions.JSON);
    escape = jopts.get(JsonSerialOptions.ESCAPE);
    escapeSolidus = escape && sopts.get(SerializerOptions.ESCAPE_SOLIDUS) == YesNo.YES;
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
    if(item == null || item instanceof QNm && ((QNm) item).eq(FN_NULL)) {
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
      if(item instanceof XQMap) {
        level++;
        out.print('{');

        boolean s = false;
        final TokenSet set = nodups ? new TokenSet() : null;
        final XQMap m = (XQMap) item;
        for(final Item key : m.keys()) {
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
          serialize(m.get(key));
          s = true;
        }

        level--;
        indent();
        out.print('}');

      } else if(item instanceof XQArray) {
        level++;
        out.print('[');

        boolean s = false;
        for(final Value value : ((XQArray) item).iterable()) {
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

  @Override
  protected void atomic(final Item item) throws IOException {
    try {
      final Type type = item.type;
      if(type.isNumber()) {
        final byte[] str = item.string(null);
        if(eq(str, NAN, INF, NEGATVE_INF)) throw SERNUMBER_X.getIO(str);
        out.print(str);
      } else if(type == AtomType.BOOLEAN) {
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
