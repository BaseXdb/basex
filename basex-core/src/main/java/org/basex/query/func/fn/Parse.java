package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Parse functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Parse extends StandardFunc {
  /** Parse Options. */
  public static final class ParseOptions extends Options {
    /** Normalize-newlines option. */
    public static final BooleanOption NORMALIZE_NEWLINES =
        new BooleanOption("normalize-newlines");
    /** Encoding option. */
    public static final StringOption ENCODING =
        new StringOption("encoding");
  }

  /** Input reference. */
  IO input;

  /**
   * Converts the specified URI to a IO reference.
   * @param uri URI
   * @return io reference, or {@code null} if the URI is invalid
   */
  protected IO input(final byte[] uri) {
    return input != null ? input : Uri.get(uri).isValid() ? info.sc().resolve(string(uri)) : null;
  }

  /**
   * Performs the unparsed-text function.
   * @param qc query context
   * @param check only check if text is available
   * @param options options argument or {@code null}
   * @param lines parse lines
   * @return content string, {@link Empty#VALUE} if no URL is supplied, or boolean success flag
   *   if availability is checked
   * @throws QueryException query exception
   */
  final Item unparsedText(final QueryContext qc, final boolean check, final boolean lines,
      final Expr options) throws QueryException {
    try {
      IO io = input;
      if(io == null) {
        final Item source = arg(0).atomItem(qc, info);
        if(source.isEmpty()) return check ? Bln.FALSE : Empty.VALUE;
        io = input(toToken(source));
        if(io == null) throw INVURL_X.get(info, source);
      }
      if(Strings.contains(io.path(), '#')) throw FRAGID_X.get(info, io);

      final ParseOptions po = new ParseOptions();
      if(options != null) {
        if(arg(1) instanceof XQMap) {
          toOptions(options, po, qc);
        } else {
          po.set(ParseOptions.ENCODING, toStringOrNull(options, qc));
        }
      }
      final Boolean normalize = po.get(ParseOptions.NORMALIZE_NEWLINES);
      if(normalize != null && lines) {
        throw OPTION_X.get(info, Options.unknown(ParseOptions.NORMALIZE_NEWLINES));
      }

      String encoding = toEncodingOrNull(po.get(ParseOptions.ENCODING), ENCODING_X);

      // only required for test APIs
      final String[] pathEnc = qc.resources.text(io);
      if(pathEnc != null) {
        io = IO.get(pathEnc[0]);
        encoding = pathEnc[1];
      }

      // parse text
      try(InputStream is = io.inputStream(); TextInput ti = normalize == Boolean.TRUE ||
          this instanceof FnUnparsedTextLines ? new NewlineInput(io) : new TextInput(io)) {
        ti.encoding(encoding).validate(true);
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
