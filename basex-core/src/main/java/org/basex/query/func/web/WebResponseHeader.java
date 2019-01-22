package org.basex.query.func.web;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class WebResponseHeader extends StandardFunc {
  /** Response options. */
  public static class ResponseOptions extends Options {
    /** Status. */
    public static final NumberOption STATUS = new NumberOption("status");
    /** Message. */
    public static final StringOption MESSAGE = new StringOption("message");
  }


  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final HashMap<String, String> output = toOptions(0, new Options(), qc).free();
    final HashMap<String, String> headers = toOptions(1, new Options(), qc).free();
    final ResponseOptions response = toOptions(2, new ResponseOptions(), qc);

    // check keys
    final SerializerOptions so = SerializerMode.DEFAULT.get();
    for(final String entry : output.keySet())
      if(so.option(entry) == null) throw INVALIDOPTION_X.get(info, entry);

    final FElem hresp = new FElem(new QNm(HTTP_PREFIX, "response", HTTP_URI)).declareNS();
    for(final Option<?> o : response) {
      if(response.contains(o)) hresp.add(o.name(), response.get(o).toString());
    }

    headers.forEach((name, value) -> {
      if(!value.isEmpty()) hresp.add(new FElem(new QNm(HTTP_PREFIX, "header", HTTP_URI)).
          add("name", name).add("value", value));
    });

    final FElem oseri = new FElem(FuncOptions.Q_SPARAM).declareNS();
    output.forEach((name, value) -> {
      if(!value.isEmpty()) oseri.add(new FElem(new QNm(OUTPUT_PREFIX, name, OUTPUT_URI)).
          add("value", value));
    });

    // REST response
    final FElem rest = new FElem(new QNm(REST_PREFIX, "response", REST_URI)).declareNS();

    return rest.add(hresp).add(oseri);
  }
}
