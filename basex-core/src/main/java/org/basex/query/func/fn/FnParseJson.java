package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class FnParseJson extends Parse {
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(FN_PREFIX, "options", FN_URI);

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    if(it == null) return null;
    return parse(toToken(it), qc, ii);
  }

  /**
   * Parses the specified JSON string.
   * @param json json string
   * @param qc query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  final Item parse(final byte[] json, final QueryContext qc, final InputInfo ii)
      throws QueryException {

    final JsonParserOptions opts = toOptions(1, Q_OPTIONS, new JsonParserOptions(), qc);
    opts.set(JsonOptions.FORMAT, JsonFormat.MAP);
    try {
      final JsonConverter conv = JsonConverter.get(opts);
      conv.convert(json, null);
      return conv.finish();
    } catch(final QueryIOException ex) {
      Util.debug(ex);
      final QueryException qe = ex.getCause(info);
      final Err err = qe.err();
      final String msg = ex.getLocalizedMessage();
      if(err == BXJS_PARSE_X_X_X) throw JSON_PARSE_X.get(ii, msg);
      if(err == BXJS_INVALID_X) throw JSON_INVALID_X.get(ii, msg);
      if(err == BXJS_DUPLICATE_X) throw JSON_DUPLICATE_X.get(ii, msg);
      throw qe;
    }
  }
}
