package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseFn extends StandardFunc {
  /** Parse Options. */
  public static final class ParseOptions extends Options {
    /** Normalize-newlines option. */
    public static final BooleanOption NORMALIZE_NEWLINES = new BooleanOption("normalize-newlines");
    /** Encoding option. */
    public static final StringOption ENCODING = CommonOptions.ENCODING;
  }

  /** Input reference. */
  IO input;

  /**
   * Converts the specified URI to a IO reference.
   * @param uri URI
   * @return io reference, or {@code null} if the URI is invalid
   */
  protected final IO input(final byte[] uri) {
    return input != null ? input : Uri.get(uri).isValid() ? info.sc().resolve(string(uri)) : null;
  }

  /**
   * Reads the specified source and invokes {@link #parse(TextInput, Object, QueryContext)}.
   * @param source input source
   * @param nl normalize newlines
   * @param options options (custom format; can be {@code null})
   * @param error custom error code for invalid input
   * @param qc query context
   * @return parsed result
   * @throws QueryException query exception
   */
  final Value parse(final Item source, final boolean nl, final Object options,
      final QueryError error, final QueryContext qc) throws QueryException {

    IO io = input;
    if(input == null) {
      io = input(toToken(source));
      if(io == null) throw INVURL_X.get(info, source);
    }
    if(Strings.contains(io.path(), '#')) throw FRAGID_X.get(info, io);

    final ParseOptions po = new ParseOptions();
    if(options instanceof Expr) {
      if(options instanceof XQMap) {
        toOptions((XQMap) options, po, qc);
      } else {
        po.set(ParseOptions.ENCODING, toStringOrNull((Expr) options, qc));
      }
    }
    Boolean normalize = po.get(ParseOptions.NORMALIZE_NEWLINES);
    if(normalize != null) {
      if(nl) throw INVALIDOPTION_X.get(info, Options.unknown(ParseOptions.NORMALIZE_NEWLINES));
    } else {
      normalize = nl;
    }
    String encoding = toEncodingOrNull(po.get(ParseOptions.ENCODING), ENCODING_X);

    // only required for test APIs
    final String[] pathEnc = qc.resources.text(io);
    if(pathEnc != null) {
      io = IO.get(pathEnc[0]);
      encoding = pathEnc[1];
    }

    // parse text
    try(InputStream is = io.inputStream(); TextInput ti = normalize ? new NewlineInput(io) :
      new TextInput(io)) {
      return parse(ti.encoding(encoding).validate(true), options, qc);
    } catch(final IOException ex) {
      if(ex instanceof DecodingException) throw WHICHCHARS_X.get(info, ex);
      if(ex instanceof InputException) throw error.get(info, ex);
      throw RESNF_X.get(info, io);
    }
  }

  /**
   * Parses the input and returns a custom result.
   * @param ti text input
   * @param options options (custom format; can be {@code null})
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  abstract Value parse(TextInput ti, Object options, QueryContext qc)
      throws QueryException, IOException;
}
