package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.json.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Project specific functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNJson extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNJson(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _JSON_PARSE:
        return new JSONConverter(info).parse(checkStr(expr[0], ctx));
      case _JSON_PARSE_ML:
        return new JsonMLConverter(info).parse(checkStr(expr[0], ctx));
      case _JSON_SERIALIZE:
        return serialize(false, ctx);
      case _JSON_SERIALIZE_ML:
        return serialize(true, ctx);
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Serializes the specified XML document to JSON/JsonML.
   * @param ml ml flag
   * @param ctx query context
   * @return serialized document
   * @throws QueryException query exception
   */
  private Str serialize(final boolean ml, final QueryContext ctx) throws QueryException {
    final ANode node = checkNode(checkItem(expr[0], ctx));
    final ArrayOutput ao = new ArrayOutput();
    try {
      // run serialization
      final SerializerProp props = new SerializerProp();
      final Serializer json = ml ? new JsonMLSerializer(ao, props) :
          new JSONSerializer(ao, props);
      json.serialize(node);
      json.close();
    } catch(final SerializerException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      SERANY.thrw(info, ex);
    }
    return Str.get(delete(ao.toArray(), '\r'));
  }
}
