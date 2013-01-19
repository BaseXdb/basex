package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.net.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions for fetching resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class FNFetch extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFetch(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _FETCH_TEXT:         return text(ctx);
      case _FETCH_BINARY:       return binary(ctx);
      case _FETCH_CONTENT_TYPE: return contentType(ctx);
      default:                  return super.item(ctx, ii);
    }
  }

  /**
   * Fetches a resource identified by a URI and returns a string representation.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private StrStream text(final QueryContext ctx) throws QueryException {
    final byte[] uri = checkStr(expr[0], ctx);
    final String enc = encoding(1, BXFE_ENCODING, ctx);
    return new StrStream(IO.get(Token.string(uri)), enc, BXFE_IO);
  }

  /**
   * Fetches a resource identified by a URI and returns a binary representation.
   * @param ctx query context
   * @return Base64Binary
   * @throws QueryException query exception
   */
  private B64Stream binary(final QueryContext ctx) throws QueryException {
    final byte[] uri = checkStr(expr[0], ctx);
    return new B64Stream(IO.get(Token.string(uri)), BXFE_IO);
  }

  /**
   * Fetches the content type of a resource.
   * @param ctx query context
   * @return content type
   * @throws QueryException query exception
   */
  private Str contentType(final QueryContext ctx) throws QueryException {
    final byte[] uri = checkStr(expr[0], ctx);
    final IO io = IO.get(Token.string(uri));

    final String path = io.path();
    final String mt;
    if(io instanceof IOUrl) {
      try {
        final URL url = new URL(path);
        final URLConnection hc = url.openConnection();
        mt = hc.getContentType();
      } catch(final IOException ex) {
        Util.debug(ex);
        throw BXFE_IO.thrw(info, ex);
      }
    } else if(io instanceof IOContent) {
      mt = MimeTypes.APP_XML;
    } else {
      mt = io.exists() ? MimeTypes.get(path) : null;
    }
    if(mt == null) throw BXFE_IO.thrw(info, new FileNotFoundException(path));
    return Str.get(mt);
  }
}
