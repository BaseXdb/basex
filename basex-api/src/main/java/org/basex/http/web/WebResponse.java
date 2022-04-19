package org.basex.http.web;

import java.io.*;

import javax.servlet.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;

/**
 * This abstract class defines common methods of Web responses.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class WebResponse {
  /** Response type. */
  public enum Response {
    /** No response.       */ NONE,
    /** Standard response. */ STANDARD,
    /** Custom response.   */ CUSTOM
  }

  /** Database context. */
  protected final Context ctx;
  /** Query context. */
  protected QueryContext qc;

  /**
   * Constructor.
   * @param ctx database context
   */
  protected WebResponse(final Context ctx) {
    this.ctx = ctx;
  }

  /**
   * Creates the Response.
   * @param function function to be evaluated
   * @param data additional data (result, function, error, can be {@code null})
   * @param body serialize body
   * @return response type
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   * @throws ServletException servlet exception
   */
  public final Response create(final WebFunction function, final Object data, final boolean body)
      throws QueryException, IOException, ServletException {

    final StaticFunc sf = function.function;
    try {
      final Expr[] args = init(function, data);
      qc.assign(sf, args);
      qc.jc().description("(: " + sf.info + " :) " + qc.root);

      return serialize(body);
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(sf.info);
      throw ex;
    } finally {
      if(qc != null) qc.close();
    }
  }

  /**
   * Initializes the evaluation of the specified function and binds function arguments.
   * @param function web function
   * @param data additional data (result, function, error, can be {@code null})
   * @return function arguments
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract Expr[] init(WebFunction function, Object data)
      throws QueryException, IOException;

  /**
   * Serializes the response.
   * @param body serialize body (if {@code false}, only the headers will be assigned)
   * @return response type
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @throws ServletException servlet exception
   */
  protected abstract Response serialize(boolean body) throws QueryException, IOException,
      ServletException;
}
