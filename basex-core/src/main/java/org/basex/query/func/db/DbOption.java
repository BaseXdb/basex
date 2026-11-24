package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbOption extends DbOptionMap {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] key = toToken(arg(0), qc);

    final Context ctx = qc.context;
    final boolean admin = qc.has(Perm.ADMIN, null, info);
    final MainOptions mopts = qc.options();
    final Object value = ctx.option(Token.string(key), mopts, admin);
    if(value == null) {
      final StringList names = new StringList().add(mopts.names());
      if(admin) names.add(ctx.soptions.names());
      throw DB_OPTION_X.get(info, QueryError.similar(key, Levenshtein.similar(key, names.finish()),
          name -> name.toLowerCase(Locale.ENGLISH)));
    }
    return toValue(value);
  }
}
