package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.options.Options.YesNo;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends StandardSerializer {
  /** JSON options. */
  final JsonSerialOptions jopts;
  /** Adaptive serializer. */
  AdaptiveSerializer adaptive;

  /** Escape special characters. */
  private final boolean escape;
  /** Allow duplicate names. */
  private final boolean nodups;

  /**
   * Constructor.
   * @param out print output
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  JsonSerializer(final PrintOutput out, final SerializerOptions opts) throws IOException {
    super(out, opts);
    jopts = opts.get(SerializerOptions.JSON);
    escape = jopts.get(JsonSerialOptions.ESCAPE);
    nodups = opts.get(SerializerOptions.ALLOW_DUPLICATE_NAMES) == YesNo.NO;
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(sep) throw SERJSON.getIO();
    if(item == null) {
      out.print(NULL);
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
    if(value.size() > 1) {
      if(adaptive != null) {
        adaptive.serialize(value);
      } else {
        throw SERJSONSEQ.getIO();
      }
    } else {
      sep = false;
      serialize(value.isEmpty() ? null : (Item) value);
    }
  }

  @Override
  public void function(final FItem item) throws IOException {
    try {
      if(item instanceof Map) {
        level++;
        out.print('{');

        boolean s = false;
        final TokenSet set = nodups ? new TokenSet() : null;
        final Map m = (Map) item;
        for(final Item key : m.keys()) {
          final byte[] name = key.string(null);
          if(nodups) {
            if(set.contains(name)) {
              if(adaptive == null) throw SERDUPL_X.getIO(name);
            } else {
              set.put(name);
            }
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

      } else if(item instanceof Array) {
        level++;
        out.print('[');

        boolean s = false;
        for(final Value v : ((Array) item).members()) {
          if(s) out.print(',');
          indent();
          serialize(v);
          s = true;
        }

        level--;
        indent();
        out.print(']');

      } else {
        if(adaptive != null) {
          adaptive.serialize(item);
        } else {
          throw SERJSONFUNC_X.getIO(item.type);
        }
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
        if(eq(str, NAN, INF, NINF) && adaptive == null) throw SERNUMBER_X.getIO(str);
        out.print(str);
      } else if(item.type == AtomType.BLN) {
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
    for(int s = 0; s < sl; s += cl(str, s)) encode(cp(str, s));
    out.print('"');
  }

  @Override
  protected final void encode(final int cp) throws IOException {
    if(map != null) {
      // character map
      final byte[] value = map.get(cp);
      if(value != null) {
        out.print(value);
        return;
      }
    }

    if(escape) {
      switch(cp) {
        case '\b': out.print("\\b"); break;
        case '\f': out.print("\\f"); break;
        case '\n': out.print("\\n"); break;
        case '\r': out.print("\\r"); break;
        case '\t': out.print("\\t"); break;
        case '"': out.print("\\\""); break;
        case '\\': out.print("\\\\"); break;
        default: out.print(cp); break;
      }
    } else {
      out.print(cp);
    }
  }

  @Override
  public void close() throws IOException {
    if(!sep) out.print(NULL);
    super.close();
  }
}
