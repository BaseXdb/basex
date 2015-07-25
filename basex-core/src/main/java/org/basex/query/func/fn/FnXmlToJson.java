package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.Options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class FnXmlToJson extends FnParseJson {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toEmptyNode(exprs[0], qc);
    final JsonSerialOptions opts = toOptions(1, null, new JsonSerialOptions(), qc);
    if(node == null) return null;

    final JsonSerialOptions jopts = new JsonSerialOptions();
    jopts.set(JsonOptions.FORMAT, JsonFormat.BASIC);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);
    sopts.set(SerializerOptions.INDENT, opts.get(JsonSerialOptions.INDENT) ? YesNo.YES : YesNo.NO);
    return Str.get(serialize(node.iter(), sopts, INVALIDOPT_X));
  }
}
