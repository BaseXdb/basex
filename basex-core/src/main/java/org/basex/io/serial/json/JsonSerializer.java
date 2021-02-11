package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.XQMap;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.options.Options.YesNo;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends StandardSerializer {
  /** JSON options. */
  final JsonSerialOptions jopts;

  /** Escape special characters. */
  private final boolean escape;
  /** Allow duplicate names. */
  private final boolean nodups;

  /**
   * Constructor.
   * @param os output stream
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  JsonSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
    jopts = opts.get(SerializerOptions.JSON);
    escape = jopts.get(JsonSerialOptions.ESCAPE);
    nodups = opts.get(SerializerOptions.ALLOW_DUPLICATE_NAMES) == YesNo.NO;
    final Boolean ji = jopts.get(JsonSerialOptions.INDENT);
    if(ji != null) indent = ji;
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(sep) throw SERJSON.getIO();
    if(item == null) {
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
          serialize(m.get(key, null));
          s = true;
        }

        level--;
        indent();
        out.print('}');

      } else if(item instanceof XQArray) {
        level++;
        out.print('[');

        boolean s = false;
        for(final Value value : ((XQArray) item).members()) {
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
      if(item.type.isNumber()) {
        final byte[] str = item.string(null);
        if(eq(str, NAN, INF, NEGATVE_INF)) throw SERNUMBER_X.getIO(str);
        out.print(str);
      } else if(item.type == AtomType.BOOLEAN) {
        out.print(item.string(null));
      } else {
        string(item.string(null));
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  /**
   * Serializes a JSON string.
   * @param string string
   * @throws IOException I/O exception
   */
  protected final void string(final byte[] string) throws IOException {
    out.print('"');
    final byte[] str = norm(string);
    final int sl = str.length;
    for(int s = 0; s < sl; s += cl(str, s)) printChar(cp(str, s));
    out.print('"');
  }

  @Override
  protected final void print(final int cp) throws IOException {
    if(escape) {
      switch(cp) {
        case '\b': out.print("\\b");  break;
        case '\f': out.print("\\f");  break;
        case '\n': out.print("\\n");  break;
        case '\r': out.print("\\r");  break;
        case '\t': out.print("\\t");  break;
        case '"' : out.print("\\\""); break;
        case '/' : out.print("\\/");  break;
        case '\\': out.print("\\\\"); break;
        default  : out.print(cp);     break;
      }
    } else {
      super.print(cp);
    }
  }

  @Override
  public void close() throws IOException {
    if(!sep) out.print(JsonConstants.NULL);
    super.close();
  }
}
