package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Parse functions.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class Parse extends StandardFunc {
  /** Input reference. */
  IO input;

  /**
   * Converts the specified URI to a IO reference.
   * @param uri URI
   * @return io reference, or {@code null} if the URI is invalid
   */
  protected IO input(final byte[] uri) {
    return input != null ? input : Uri.get(uri).isValid() ? sc.resolve(string(uri)) : null;
  }

  /**
   * Performs the unparsed-text function.
   * @param qc query context
   * @param check only check if text is available
   * @param encoding parse encoding
   * @return content string, {@link Empty#VALUE} if no URL is supplied, or boolean success flag
   *   if availability is checked
   * @throws QueryException query exception
   */
  final Item unparsedText(final QueryContext qc, final boolean check, final boolean encoding)
      throws QueryException {
    try {
      IO io = input;
      if(io == null) {
        final Item href = exprs[0].atomItem(qc, info);
        if(href == Empty.VALUE) return check ? Bln.FALSE : Empty.VALUE;
        io = input(toToken(href));
        if(io == null) throw INVURL_X.get(info, href);
      }
      if(Strings.contains(io.path(), '#')) throw FRAGID_X.get(info, io);

      String enc = encoding ? toEncodingOrNull(1, ENCODING_X, qc) : null;

      // only required for test APIs
      final String[] pathEnc = qc.resources.text(io);
      if(pathEnc != null) {
        io = IO.get(pathEnc[0]);
        enc = pathEnc[1];
      }

      // parse text
      try(InputStream is = io.inputStream(); TextInput ti = new TextInput(io)) {
        ti.encoding(enc).validate(true);
        if(!check) return Str.get(ti.content());

        while(ti.read() != -1);
        return Bln.TRUE;
      } catch(final IOException ex) {
        if(check) return Bln.FALSE;
        if(ex instanceof DecodingException) throw WHICHCHARS_X.get(info, ex);
        if(ex instanceof InputException) throw INVCHARS_X.get(info, ex);
        throw RESNF_X.get(info, io);
      }

    } catch(final QueryException ex) {
      if(check && !ex.error().toString().startsWith(ErrType.XPTY.name())) return Bln.FALSE;
      throw ex;
    }
  }
}
