package org.basex.http.web;

import java.io.*;

import javax.servlet.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;

/**
 * This abstract class defines common methods of Web responses.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public abstract class WebResponse {
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
   * @return result flag (ignored)
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   * @throws ServletException servlet exception
   */
  public boolean create(final WebFunction function, final Object data)
      throws QueryException, IOException, ServletException {

    final WebModule module = function.module;
    try {
      qc = module.qc(ctx);
      init(function);
      final StaticFunc sf = function.module.find(qc, function.function);
      final Expr[] args = new Expr[sf.params.length];
      bind(args, data);
      qc.mainModule(MainModule.get(sf, args));
      return serialize();
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(function.function.info);
      throw ex;
    } finally {
      if(qc != null) qc.close();
    }
  }

  /**
   * Creates and returns a function instance that can be evaluated.
   * @param function function template
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void init(WebFunction function) throws QueryException, IOException;

  /**
   * Binds values to the function parameters.
   * @param args arguments
   * @param data additional data (result, function, error, can be {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void bind(Expr[] args, Object data)
      throws QueryException, IOException;

  /**
   * Serializes the response.
   * @return {@code true} if data was serialized
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @throws ServletException servlet exception
   */
  protected abstract boolean serialize() throws QueryException, IOException, ServletException;
}
