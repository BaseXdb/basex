package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.json.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.Options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnXmlToJson extends FnParseJson {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(arg(0), qc);
    final JsonSerialOptions options = toOptions(arg(1), new JsonSerialOptions(), false, qc);
    if(node == null) return Empty.VALUE;

    options.set(JsonOptions.FORMAT, JsonFormat.BASIC);
    final Boolean indent = options.get(JsonSerialOptions.INDENT);
    // no indentation specified: adopt module indentation
    if(indent == null) options.set(JsonSerialOptions.INDENT,
        qc.parameters().get(SerializerOptions.INDENT) == YesNo.YES);

    return Str.get(serialize(node.iter(), JsonSerialize.options(options), INVALIDOPT_X, qc));
  }
}
