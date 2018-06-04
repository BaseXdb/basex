package org.basex.http.util;

import java.util.*;
import java.util.regex.*;

import org.basex.http.restxq.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 *  This abstract class defines common Methods of WebFunctions.
 *
 *  @author BaseX Team 2005-18, BSD License
 */
public abstract class WebFunction {
  /** Single template pattern. */
  protected static final Pattern TEMPLATE = Pattern.compile("\\s*\\{\\s*\\$(.+?)\\s*}\\s*");
  /** Associated module. */
  protected final RestXqModule module;
  /** Associated function. */
  public final StaticFunc function;
  /** Serialization parameters. */
  public final SerializerOptions output;
  /** Header Parameters. */
  public final ArrayList<RestXqParam> headerParams = new ArrayList<>();

  /**
   * Constructor.
   * @param function associated user function
   * @param qc query context
   * @param module associated module
   */
  protected WebFunction(final StaticFunc function,
      final QueryContext qc, final RestXqModule module) {
    this.function = function;
    this.module = module;
    output = qc.serParams();
  }

  /**
   * Returns the specified item as a string.
   * @param item item
   * @return string
   */
  protected static String toString(final Item item) {
    return ((Str) item).toJava();
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  protected abstract QueryException error(String msg, Object... ext);
}
