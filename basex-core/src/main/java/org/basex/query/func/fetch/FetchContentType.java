package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.net.http.*;
import java.net.http.HttpResponse;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FetchContentType extends FetchDoc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IO io = toIO(0, qc);

    MediaType mt = null;
    if(io instanceof IOUrl) {
      try {
        final HttpHeaders headers = ((IOUrl) io).response(
            HttpResponse.BodyHandlers.discarding()).headers();
        final Optional<String> value = headers.firstValue(HTTPText.CONTENT_TYPE);
        if(value.isPresent()) mt = new MediaType(value.get());
      } catch(final IOException ex) {
        throw FETCH_OPEN_X.get(info, ex);
      }
    } else if(io instanceof IOContent) {
      mt = MediaType.APPLICATION_XML;
    }
    return Str.get((mt == null ? MediaType.get(io.path()) : mt).toString());
  }
}
