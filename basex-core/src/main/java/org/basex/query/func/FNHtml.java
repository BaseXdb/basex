package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Functions for converting HTML to XML.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNHtml extends StandardFunc {
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get("options", HTMLURI);

  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNHtml(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
      case _HTML_PARSER: return Str.get(HtmlParser.parser());
      case _HTML_PARSE:  return parse(ctx);
      default:           return super.item(ctx, ii);
    }
  }

  /**
   * Converts HTML input to XML and returns a document node.
   * @param ctx query context
   * @return document node
   * @throws QueryException query exception
   */
  private DBNode parse(final QueryContext ctx) throws QueryException {
    final byte[] in = checkStrBin(checkItem(exprs[0], ctx));
    final HtmlOptions opts = checkOptions(1, Q_OPTIONS, new HtmlOptions(), ctx);
    try {
      return new DBNode(new HtmlParser(new IOContent(in), ctx.context.options, opts));
    } catch(final IOException ex) {
      throw BXHL_IO.get(info, ex);
    }
  }
}
