package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.json.*;
import org.basex.query.util.json.JsonParser.Spec;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Project specific functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNJson extends StandardFunc {
  /** The {@code unescape} key. */
  private static final Str UNESC = Str.get("unescape");
  /** The {@code spec} key. */
  private static final Str SPEC = Str.get("spec");
  /** The {@code type} key. */
  private static final Str TYPE = Str.get(DataText.T_TYPE);

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
        final Map cfg = expr.length < 2 ? Map.EMPTY : checkMap(checkItem(expr[1], ctx));
        final JsonConverter conv = getConverter(cfg, ctx);
        return conv.convert(string(checkStr(expr[0], ctx))).item(ctx, ii);
      case _JSON_PARSE_ML:
        return new JsonMLConverter(info).convert(string(checkStr(expr[0], ctx)));
      case _JSON_SERIALIZE:
        return serialize(false, ctx);
      case _JSON_SERIALIZE_ML:
        return serialize(true, ctx);
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Creates a JSON converter according to the given configuration.
   * @param cfg configuration map
   * @param ctx query context
   * @return JSON converter
   * @throws QueryException query exception
   */
  private JsonConverter getConverter(final Map cfg, final QueryContext ctx)
      throws QueryException {
    final boolean unesc = !cfg.contains(UNESC, info).bool(info)
        || cfg.get(UNESC, info).ebv(ctx, info).bool(info);
    final Spec spec;
    if(cfg.contains(SPEC, info).bool(info)) {
      final byte[] sp = checkStr(cfg.get(SPEC, info), ctx);
      Spec spc = null;
      for(final Spec s : Spec.values()) if(eq(sp, s.desc)) spc = s;
      if(spc == null) BXJS_PARSE_CFG.thrw(info, "Unknown spec '" + string(sp) + "'");
      spec = spc;
    } else {
      spec = Spec.RFC4627;
    }

    final byte[] typ = checkEStr(cfg.get(TYPE, info), ctx);
    return JsonConverter.newInstance(typ, spec, unesc, info);
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
