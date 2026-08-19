package org.basex.query.func.csv;

import static org.basex.query.QueryError.*;

import org.basex.build.csv.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvSerialize extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    try {
      final CsvOptions options = options(1, CsvOptions::new, qc);
      return Str.get(serialize(input, options(options), INVALIDOPTION_X, qc));
    } catch(final QueryException ex) {
      throw error(ex, ex.matches(ErrType.FOCV) ? CSV_SERIALIZE_X : null);
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    try {
      optOptions(1, CsvOptions::new, cc);
    } catch(final QueryException ex) {
      throw error(ex, ex.matches(ErrType.FOCV) ? CSV_SERIALIZE_X : null);
    }
    return this;
  }

  /**
   * Creates parameters for options.
   * @param copts CSV options
   * @return options
   */
  public static SerializerOptions options(final CsvOptions copts) {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.CSV);
    sopts.set(SerializerOptions.CSV, copts);
    return sopts;
  }
}
