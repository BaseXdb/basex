package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.net.http.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FetchContentType extends FetchDoc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IO source = toIO(arg(0), qc);

    MediaType mt = null;
    if(source instanceof final IOUrl url) {
      try {
        final HttpHeaders headers = url.response().headers();
        final Optional<String> value = headers.firstValue(HTTPText.CONTENT_TYPE);
        if(value.isPresent()) mt = new MediaType(value.get());
      } catch(final IOException ex) {
        throw FETCH_OPEN_X.get(info, ex);
      }
    } else if(source instanceof IOContent) {
      mt = MediaType.APPLICATION_XML;
    }
    return Str.get((mt == null ? MediaType.get(source.path()) : mt).toString());
  }
}
