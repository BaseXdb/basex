package org.basex.query.func;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.csv.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Functions for parsing CSV input.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class FNCsv extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("options", CSVURI);

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNCsv(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _CSV_PARSE:     return parse(ctx);
      case _CSV_SERIALIZE: return serialize(ctx);
      default:             return super.item(ctx, ii);
    }
  }

  /**
   * Converts CSV input to an element node.
   * @param ctx query context
   * @return element node
   * @throws QueryException query exception
   */
  private FElem parse(final QueryContext ctx) throws QueryException {
    final byte[] input = checkStr(expr[0], ctx);
    final CsvOptions opts = checkOptions(1, Q_OPTIONS, new CsvOptions(), ctx);

    try {
      return new CsvConverter(opts).convert(input);
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      throw BXCS_PARSE.thrw(info, ex);
    }
  }

  /**
   * Serializes the specified XML document as CSV.
   * @param ctx query context
   * @return string representation
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final ANode node = checkNode(expr[0], ctx);
    final CsvOptions opts = checkOptions(1, Q_OPTIONS, new CsvOptions(), ctx);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(S_METHOD, M_CSV);
    sopts.set(S_CSV, opts.toString());
    return Str.get(delete(serialize(node.iter(), sopts), '\r'));
  }
}
