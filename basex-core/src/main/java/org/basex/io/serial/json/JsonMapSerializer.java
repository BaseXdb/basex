package org.basex.io.serial.json;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * This class serializes map data as JSON. The input must conform to the rules
 * defined in the {@link JsonDirectConverter} class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class JsonMapSerializer extends JsonSerializer {
  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonMapSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(sep && level == 0) print(' ');

    try {
      if(item instanceof Map) {
        final Map map = (Map) item;
        level++;

        // check if keys of a map are strings (names of pairs) or positive integers (array offsets)
        final Value keys = map.keys();
        long num = 0;
        boolean object = false;
        for(final Item k : keys) {
          if(k.type == AtomType.ITR) {
            final long n = k.itr(null);
            if(n <= 0) BXJS_SERIAL.thrwIO("Integer key " + k + " ist not positive");
            num = Math.max(num, n);
          } else {
            object = true;
            if(k.type != AtomType.STR) num = -1;
          }
          if(object && num != 0) BXJS_SERIAL.thrwIO("Keys must either be strings or integers");
        }

        // print current object or array
        object = num == 0;
        print(object ? '{' : '[');
        boolean f = false;
        if(object) num = keys.size();
        for(int i = 0; i < num; i++) {
          final Item k = object ? keys.itemAt(i) : Int.get(i + 1);
          if(f) print(',');
          indent();
          if(object) {
            serialize(k);
            print(':');
            if(indent) print(' ');
          }
          final Value v = map.get(k, null);
          if(v.size() > 1) BXJS_SERIAL.thrwIO("More than one item specified for key " + k);
          serialize(v.isEmpty() ? null : (Item) v);
          f = true;
        }

        level--;
        indent();
        print(object ? '}' : ']');
      } else if(level == 0 && jopts.get(JsonOptions.SPEC) == JsonSpec.RFC4627) {
        BXJS_SERIAL.thrwIO("Top level must be a map; " + item.type + " found");
      } else if(item == null) {
        // empty sequence
        print(NULL);
      } else {
        final byte[] str = item.string(null);
        final boolean quote = item.type != AtomType.BLN &&
            (!item.type.isNumber() || eq(str, NAN, INF, NINF));
        if(quote) print('"');
        final byte[] atom = item.string(null);
        for(int a = 0; a < atom.length; a += cl(atom, a)) encode(cp(atom, a));
        if(quote) print('"');
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
  }

  @Override
  protected final void indent() throws IOException {
    if(!indent) return;
    print(nl);
    final int ls = level * indents;
    for(int l = 0; l < ls; ++l) print(tab);
  }
}
