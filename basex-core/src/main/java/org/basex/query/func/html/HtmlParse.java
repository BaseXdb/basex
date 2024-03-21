package org.basex.query.func.html;

import static org.basex.build.html.HtmlOptions.*;
import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.html.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class HtmlParse extends StandardFunc {
  /** Class needed for option heuristics=ICU. */
  private static final String ICU_CLASS_NAME = "com.ibm.icu.text.CharsetDetector";
  /** Class needed for option heuristics=CHARDET. */
  private static final String CHARDET_CLASS_NAME =
      "org.mozilla.intl.chardet.nsICharsetDetectionObserver";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if (value.isEmpty()) return Empty.VALUE;
    final IO io = value instanceof Bin ? new IOContent(toBytes(value))
                                       : new IOContent(toBytes(value), "", Strings.UTF8);
    return parse(io, qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the input and creates an XML document.
   * @param io input data
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected final Item parse(final IO io, final QueryContext qc) throws QueryException {
    final HtmlOptions options = toOptions(arg(1), new HtmlOptions(), INVHTMLOPT_X, qc);
    if(options.contains(HEURISTICS)) {
      switch (options.get(HEURISTICS)) {
      case ALL:
        ensureAvailable(ICU_CLASS_NAME);
        ensureAvailable(CHARDET_CLASS_NAME);
        break;
      case ICU:
        ensureAvailable(ICU_CLASS_NAME);
        break;
      case CHARDET:
        ensureAvailable(CHARDET_CLASS_NAME);
        break;
      default:
      }
    }
    try {
      return new DBNode(new org.basex.build.html.HtmlParser(io, new MainOptions(), options));
    } catch(final IOException ex) {
      throw INVHTML_X.get(info, ex);
    }
  }

  /**
   * Ensure that a required class is available on the class path.
   * @param className the class name
   * @throws QueryException query exception,
   */
  private void ensureAvailable(final String className) throws QueryException {
    if(!Reflect.available(className))
      throw BASEX_CLASSPATH_X_X.get(info, definition.local(), className);
  }
}
