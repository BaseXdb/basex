package org.basex.http.web;

import static org.basex.http.web.WebText.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This abstract class defines common methods of Web functions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public abstract class WebFunction implements Comparable<WebFunction> {
  /** Single template pattern. */
  private static final Pattern TEMPLATE = Pattern.compile("\\s*\\{\\s*\\$(.+?)\\s*}\\s*");

  /** Associated function. */
  public final StaticFunc function;
  /** Serialization parameters. */
  public final SerializerOptions output;
  /** Web module. */
  public final WebModule module;
  /** Header Parameters. */
  public final ArrayList<WebParam> headerParams = new ArrayList<>();

  /**
   * Constructor.
   * @param function associated user function
   * @param qc query context
   * @param module web module
   */
  protected WebFunction(final StaticFunc function, final WebModule module, final QueryContext qc) {
    this.function = function;
    this.module = module;
    output = qc.serParams();
  }

  /**
   * Checks a function for REST and permission annotations. This function is called both
   * when a module is parsed, and when the function is prepared for evaluation.
   * @param ctx database context
   * @return {@code true} if function contains relevant annotations
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public abstract boolean parse(Context ctx) throws QueryException, IOException;

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  protected abstract QueryException error(String msg, Object... ext);

  /**
   * Checks the specified template and adds a variable.
   * @param tmp template string
   * @param declared variable declaration flags
   * @return resulting variable
   * @throws QueryException query exception
   */
  protected final QNm checkVariable(final String tmp, final boolean... declared)
      throws QueryException {

    final Matcher matcher = TEMPLATE.matcher(tmp);
    if(!matcher.find()) throw error(INV_TEMPLATE_X, tmp);
    final byte[] qname = Token.token(matcher.group(1));
    if(!XMLToken.isQName(qname)) throw error(INV_VARNAME_X, qname);
    return checkVariable(new QNm(qname), declared);
  }

  /**
   * Checks parsed meta data.
   * @param found found flag
   * @param list list of annotations
   * @param declared declared parameters
   * @return found flag
   * @throws QueryException query exception
   */
  protected final boolean checkParsed(final boolean found, final AnnList list,
      final boolean[] declared) throws QueryException {

    if(found) {
      if(list.isEmpty()) throw error(ANN_MISSING);
      if(list.size() > 1) {
        final StringList names = new StringList(list.size());
        for(final Ann ann : list) names.add('%' + Token.string(ann.sig.id()));
        throw error(ANN_CONFLICT_X, String.join(", ", names.finish()));
      }

      final int dl = declared.length;
      for(int d = 0; d < dl; d++) {
        if(!declared[d]) throw error(VAR_UNDEFINED_X, function.params[d].name.string());
      }
    }
    return found;
  }

  /**
   * Checks if the specified variable exists in the current function.
   * @param name variable
   * @param declared variable declaration flags
   * @return resulting variable
   * @throws QueryException query exception
   */
  protected final QNm checkVariable(final QNm name, final boolean[] declared)
      throws QueryException {

    if(name.hasPrefix()) name.uri(function.sc.ns.uri(name.prefix()));
    int p = -1;
    final Var[] params = function.params;
    final int pl = params.length;
    while(++p < pl && !params[p].name.eq(name));

    if(p == params.length) throw error(PARAM_MISSING_X, name.string());
    if(declared[p]) throw error(PARAM_DUPL_X, name.string());

    declared[p] = true;
    return name;
  }

  /**
   * Binds the specified value to a variable.
   * @param name variable name
   * @param args arguments
   * @param value value to be bound
   * @param qc query context
   * @param info info string
   * @throws QueryException query exception
   */
  protected final void bind(final QNm name, final Expr[] args, final Value value,
      final QueryContext qc, final String info) throws QueryException {

    // skip nulled values
    if(value == null) return;

    final Var[] params = function.params;
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      final Var var = params[p];
      if(var.name.eq(name)) {
        // casts and binds the value
        final SeqType st = var.declaredType();
        args[p] = value.seqType().instanceOf(st) ? value :
          st.cast(value, false, qc, function.sc, null);
        if(args[p] == null) throw error(ARG_TYPE_X_X_X, info, st, value);
        break;
      }
    }
  }

  /**
   * Returns the specified item as a string.
   * @param item item
   * @return string
   */
  protected static String toString(final Item item) {
    return ((Str) item).toJava();
  }

  @Override
  public String toString() {
    return Token.string(function.name.prefixString());
  }
}
