package org.basex.query.func;

import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
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
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    final boolean header = map.contains(HEADER) && Util.yes(string(map.get(HEADER)));
    int sep = ',';
    final byte[] sp = map.get(SEPARATOR);
    if(sp != null) {
      final TokenParser tp = new TokenParser(sp);
      sep = tp.next();
      if(sep == -1 || tp.next() != -1) BXCS_CONFIG.thrw(info);
    }
    try {
      return new CsvParser(sep, header).convert(input);
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
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    final ArrayOutput ao = new ArrayOutput();
    final SerializerProp props = new SerializerProp();
    if(map.contains(HEADER)) props.set(S_CSV_HEADER, string(map.get(HEADER)));
    if(map.contains(SEPARATOR)) props.set(S_CSV_SEPARATOR, string(map.get(SEPARATOR)));

    try {
      // run serialization
      final Serializer ser = new CsvSerializer(ao, props);
      ser.serialize(node);
      ser.close();
    } catch(final SerializerException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      SERANY.thrw(info, ex);
    }
    return Str.get(delete(ao.toArray(), '\r'));
  }
}
