package org.basex.query.func.fn;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.web.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCollation extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap options = toMap(arg(0), qc);

    final MapBuilder map = new MapBuilder();
    options.forEach((k, v) -> {
      final String key = Strings.camelCase(Token.string(k.string(info)));
      final Value value = v == Bln.TRUE ? Str.get(Text.YES) : v == Bln.FALSE ? Str.get(Text.NO) : v;
      map.put(key, value);
    });

    // generate and check collation URI
    final byte[] href = Prop.ICU ? Collation.ICU : Collation.BASEX;
    return Str.get(WebFn.createUrl(href, map.map(), ';', info).finish());
  }
}
