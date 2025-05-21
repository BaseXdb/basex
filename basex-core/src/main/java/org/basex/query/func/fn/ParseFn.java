package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseFn extends StandardFunc {
  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optFirst();
  }

  /**
   * Considers or normalizes newlines.
   * @return boolean flag
   */
  boolean nl() {
    return false;
  }

  /**
   * Returns a format-specific error code for invalid input.
   * @return error code
   */
  QueryError error() {
    return null;
  }

  /**
   * Returns parse options.
   * @param qc query context
   * @return parse options
   * @throws QueryException query exception
   */
  protected abstract Options options(QueryContext qc) throws QueryException;

  /** Input reference. */
  IO input;

  /**
   * Reads the specified value and invokes {@link #parse(TextInput, Options, QueryContext)}.
   * Returns an XQuery value for the parsed data.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  protected Value parse(final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    final IO io = new IOContent(toBytes(value));
    try(TextInput ti = nl() ? new NewlineInput(io) : new TextInput(io)) {
      return parse(ti, options(qc), qc);
    } catch(final IOException ex) {
      throw error().get(info, ex);
    }
  }

  /**
   * Reads the source and invokes {@link #parse(TextInput, Options, QueryContext)}.
   * @param qc query context
   * @return parsed result
   * @throws QueryException query exception
   */
  protected Value doc(final QueryContext qc) throws QueryException {
    final String source = toStringOrNull(arg(0), qc);
    if(source == null) return Empty.VALUE;

    // input
    final String[] testResources = qc.resources.text(source, info.sc());
    final IO io = testResources != null ? IO.get(testResources[0]) :
      input != null ? input : toIO(source, false);

    // encoding
    final Options options = options(qc);
    final String enc = toEncodingOrNull(options.get(CommonOptions.ENCODING), RESENCODING_X);
    final String encoding = enc != null ? enc : testResources != null ? testResources[1] : null;

    // newline normalization
    Boolean normalize = options.get(CommonOptions.NORMALIZE_NEWLINES);
    if(normalize != null) {
      if(nl()) throw INVALIDOPTION_X.get(info, Options.unknown(CommonOptions.NORMALIZE_NEWLINES));
    } else {
      normalize = nl();
    }

    // parse text
    try(InputStream is = io.inputStream(); TextInput ti = normalize ? new NewlineInput(io) :
      new TextInput(io)) {
      return parse(ti.encoding(encoding).validate(true), options, qc);
    } catch(final DecodingException ex) {
      throw RECDECODING_X.get(info, ex);
    } catch(final InputException ex) {
      throw error().get(info, ex);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw RESWHICH_X.get(info, io);
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
  abstract Value parse(TextInput ti, Options options, QueryContext qc)
      throws QueryException, IOException;
}
