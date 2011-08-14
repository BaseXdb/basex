package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.JSONSerializer;
import org.basex.io.serial.JsonMLSerializer;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.util.json.JSONConverter;
import org.basex.util.InputInfo;

/**
 * Project specific functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNJson extends FuncCall {
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case JPARSE:
        return new JSONConverter().parse(checkStr(expr[0], ctx), input);
      case JSERIALIZE:
        return serialize(false, ctx);
      case JSERIALIZEML:
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
  private Str serialize(final boolean ml, final QueryContext ctx)
      throws QueryException {

    final ANode node = checkNode(checkItem(expr[0], ctx));
    final ArrayOutput ao = new ArrayOutput();
    try {
      // run serialization
      final SerializerProp props = ctx.serProp(false);
      final Serializer json = ml ? new JsonMLSerializer(ao, props) :
          new JSONSerializer(ao, props);
      node.serialize(json);
      json.close();
    } catch(final SerializerException ex) {
      throw new QueryException(input, ex);
    } catch(final IOException ex) {
      SERANY.thrw(input, ex);
    }
    return Str.get(delete(ao.toArray(), '\r'));
  }
}
