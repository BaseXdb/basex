package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.Options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnXmlToJson extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(arg(0), qc);
    final JsonSerialOptions options = toOptions(arg(1), new JsonSerialOptions(), qc);
    if(node == null) return Empty.VALUE;

    options.set(JsonOptions.FORMAT, JsonFormat.W3_XML);
    final Boolean indent = options.get(JsonSerialOptions.INDENT);
    // no indentation specified: adopt module indentation
    if(indent == null) options.set(JsonSerialOptions.INDENT,
        qc.parameters().get(SerializerOptions.INDENT) == YesNo.YES);

    return Str.get(serialize(node.iter(), options(options), INVALIDOPTION_X, qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Creates parameters for options.
   * @param jopts JSON options
   * @return options
   */
  public static SerializerOptions options(final JsonSerialOptions jopts) {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);
    final Boolean indent = jopts.get(JsonSerialOptions.INDENT);
    if(indent != null) sopts.set(SerializerOptions.INDENT, indent ? YesNo.YES : YesNo.NO);
    return sopts;
  }
}
