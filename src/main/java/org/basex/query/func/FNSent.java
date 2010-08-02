package org.basex.query.func;

import static org.basex.util.Token.*;
import java.util.HashMap;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.util.SentList;
import org.basex.util.InputInfo;
import org.basex.util.Tokenizer;

/**
 * This class defines the sentiment functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Oliver Egli
 */
public final class FNSent extends Fun {
  /** Word lists. */
  private static final HashMap<String, SentList> LISTS =
    new HashMap<String, SentList>();

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNSent(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(func) {
      case SENT:     return polarity(ctx, false);
      case NORMSENT: return polarity(ctx, true);
      default:       return super.atomic(ctx, ii);
    }
  }

  /**
   * Calculates the polarity value of a string.
   * @param ctx context
   * @param norm flag for normalizing results
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item polarity(final QueryContext ctx, final boolean norm)
      throws QueryException {

    // text to be analyzed
    final byte[] str = checkStr(expr[0], ctx);

    // if necessary, add new sentiment list to hash map
    final String uri = string(checkStr(expr[1], ctx));
    SentList list = LISTS.get(uri);
    if(list == null) {
      list = new SentList(input, uri);
      LISTS.put(uri, list);
    }

    double pos = 0, neg = 0;
    byte[] token1 = EMPTY, token2 = EMPTY, token3 = EMPTY, token4 = EMPTY;

    final Tokenizer tk = new Tokenizer(str, null);
    // if stemming is activated, it should be performed on word lists as well...
    //tk.st = true;

    // loop through all tokens
    while(tk.more()) {
      token4 = token3;
      token3 = token2;
      token2 = token1;
      token1 = tk.get();

      // calculate polarity
      double v = list.polarity(token1);
      if(list.negates(token2) || list.negates(token3) || list.negates(token4))
        v = -v;

      if(v > 0) pos += v;
      else if(v < 0) neg -= v;
    }

    // summarize result
    double result = pos - neg;
    // normalize result
    if(norm && result != 0) result = -1 + pos * 2 / (pos + neg);

    // return result
    return Dbl.get(result);
  }
}
