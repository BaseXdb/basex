package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinInferEncoding extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBin(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), BIN_UE_X, qc);

    try(TextInput ti = new TextInput(value.input(info), encoding)) {
      final String enc = ti.encoding();
      final MapBuilder map = new MapBuilder();
      map.put("encoding", Str.get(enc));
      map.put("offset", Itr.get(ti.position()));
      return map.map();
    } catch(final IOException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }
}
