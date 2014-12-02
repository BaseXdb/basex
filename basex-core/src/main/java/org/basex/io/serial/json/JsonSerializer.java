package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.options.Options.YesNo;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends OutputSerializer {
  /** JSON options. */
  final JsonSerialOptions jopts;
  /** Escape special characters. */
  private final boolean escape;
  /** Allow duplicate names. */
  private final boolean nodups;

  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  JsonSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
    jopts = opts.get(SerializerOptions.JSON);
    escape = jopts.get(JsonSerialOptions.ESCAPE);
    nodups = opts.get(SerializerOptions.ALLOW_DUPLICATE_NAMES) == YesNo.NO;
  }

  @Override
  public final void serialize(final Item item) throws IOException {
    if(sep) throw SERJSON.getIO();
    if(lvl == 0) openResult();

    try {
      if(item instanceof Map) {
        lvl++;
        print('{');

        boolean s = false;
        final TokenSet set = nodups ? new TokenSet() : null;
        final Map map = (Map) item;
        for(final Item key : map.keys()) {
          final byte[] name = key.string(null);
          if(set != null) {
            if(set.contains(name)) throw SERDUPL_X.getIO(name);
            set.put(name);
          }
          if(s) print(',');
          indent();
          string(name);
          print(':');
          if(indent) print(' ');
          final Value v = map.get(key, null);
          if(v.size() > 1) throw SERMAPSEQ.getIO();
          sep = false;
          serialize(v.isEmpty() ? null : (Item) v);
          s = true;
        }

        lvl--;
        indent();
        print('}');

      } else if(item instanceof Array) {
        lvl++;
        print('[');

        boolean s = false;
        for(final Value v : ((Array) item).members()) {
          if(s) print(',');
          indent();
          if(v.size() > 1) throw SERARRAYSEQ.getIO();
          sep = false;
          serialize(v.isEmpty() ? null : (Item) v);
          s = true;
        }

        lvl--;
        indent();
        print(']');

      } else if(item instanceof ANode) {
        serialize((ANode) item);

      } else if(item == null) {
        // empty sequence
        print(NULL);

      } else if(item.type.isNumber()) {
        final byte[] str = item.string(null);
        if(eq(str, NAN, INF, NINF)) throw SERNUMBER_X.getIO(str);
        print(str);

      } else if(item.type == AtomType.BLN) {
        print(item.string(null));

      } else {
        string(item.string(null));
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }

    sep = true;
    if(lvl == 0) closeResult();
  }

  /**
   * Serialize a JSON string.
   * @param string string
   * @throws IOException I/O exception
   */
  protected final void string(final byte[] string) throws IOException {
    print('"');
    final byte[] str = norm(string);
    final int sl = str.length;
    for(int s = 0; s < sl; s += cl(str, s)) encode(cp(str, s));
    print('"');
  }

  @Override
  protected final void encode(final int cp) throws IOException {
    if(escape) {
      switch(cp) {
        case '\b': print("\\b"); break;
        case '\f': print("\\f"); break;
        case '\n': print("\\n"); break;
        case '\r': print("\\r"); break;
        case '\t': print("\\t"); break;
        case '"': print("\\\""); break;
        case '\\': print("\\\\"); break;
        default: print(cp); break;
      }
    } else {
      print(cp);
    }
  }

  @Override
  protected final void indent() throws IOException {
    if(!indent) return;
    print(nl);
    final int ls = lvl * indents;
    for(int l = 0; l < ls; ++l) print(tab);
  }

  @Override
  public void close() throws IOException {
    if(!sep) print(NULL);
  }
}
