package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.json.*;
import org.basex.query.util.json.JsonParser.Spec;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Functions for parsing and serializing JSON objects.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNJson extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("options", JSONURI);
  /** The {@code unescape} key. */
  private static final byte[] UNESCAPE = token("unescape");
  /** The {@code spec} key. */
  private static final byte[] SPEC = token("spec");
  /** The {@code type} key. */
  private static final byte[] FORMAT = token("format");

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
        return parse(ctx);
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
   * Converts a JSON object to an item according to the given configuration.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  private Item parse(final QueryContext ctx) throws QueryException {
    final byte[] input = checkStr(expr[0], ctx);
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    final boolean unesc = !map.contains(UNESCAPE) || eq(map.get(UNESCAPE), TRUE);
    final Spec spec;
    final byte[] sp = map.get(SPEC);
    if(sp != null) {
      Spec spc = null;
      for(final Spec s : Spec.values()) if(eq(sp, s.desc)) spc = s;
      if(spc == null) BXJS_PARSE_CFG.thrw(info, "Unknown spec '" + string(sp) + "'");
      spec = spc;
    } else {
      spec = Spec.RFC4627;
    }

    final byte[] form = map.get(FORMAT);
    final JsonConverter conv = JsonConverter.newInstance(form, spec, unesc, info);
    return conv.convert(string(input)).item(ctx, info);
  }

  /**
   * Serializes the specified XML document to JSON/JsonML.
   * @param ml ml flag
   * @param ctx query context
   * @return serialized document
   * @throws QueryException query exception
   */
  private Str serialize(final boolean ml, final QueryContext ctx) throws QueryException {
    final ANode node = checkNode(expr[0], ctx);
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
