package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.csv.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Functions for parsing CSV input.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class FNCsv extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("options", CSVURI);
  /** The {@code header} key. */
  private static final byte[] HEADER = token("header");
  /** The {@code separator} key. */
  private static final byte[] SEPARATOR = token("separator");

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
      case _CSV_PARSE: return parse(ctx);
      default:         return super.item(ctx, ii);
    }
  }

  /**
   * Converts CSV input to an element node.
   * @param ctx query context
   * @return element node
   * @throws QueryException query exception
   */
  private FElem parse(final QueryContext ctx) throws QueryException {
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    final boolean header = map.contains(HEADER) && eq(map.get(HEADER), TRUE);
    byte sep = CsvParser.SEPMAPPINGS[0];
    final byte[] s = map.get(SEPARATOR);
    if(s != null) {
      if(s.length != 1) BXCS_SEP.thrw(info);
      sep = s[0];
    }
    final CsvParser parser = new CsvParser(sep, header);

    try {
      return parser.convert(checkStr(expr[0], ctx));
    } catch(final IOException ex) {
      throw BXCS_ERROR.thrw(info, ex);
    }
  }
}
