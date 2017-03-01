package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Parse functions.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Parse extends StandardFunc {
  /**
   * Performs the unparsed-text function.
   * @param qc query context
   * @param check only check if text is available
   * @param encoding parse encoding
   * @return content string or boolean success flag
   * @throws QueryException query exception
   */
  Item unparsedText(final QueryContext qc, final boolean check, final boolean encoding)
      throws QueryException {

    checkCreate(qc);
    final Item it = exprs[0].atomItem(qc, info);
    if(it == null) return check ? Bln.FALSE : null;

    final byte[] path = toToken(it);

    String enc = null;
    IO io = null;
    try {
      enc = encoding ? toEncoding(1, ENCODING_X, qc) : null;

      final String p = string(path);
      if(p.indexOf('#') != -1) throw FRAGID_X.get(info, p);
      final Uri uri = Uri.uri(p);
      if(!uri.isValid()) throw INVURL_X.get(info, p);

      if(uri.isAbsolute()) {
        io = IO.get(p);
      } else {
        if(sc.baseURI() == Uri.EMPTY) throw STBASEURI.get(info);
        io = sc.resolve(p);
      }

      // overwrite path with global resource files
      String[] rp = qc.resources.text(p);
      if(rp == null) rp = qc.resources.text(io.path());
      if(rp != null && rp.length > 0) {
        io = IO.get(rp[0]);
        if(rp.length > 1) enc = rp[1];
      }

      try(InputStream is = io.inputStream()) {
        final TextInput ti = new TextInput(io).encoding(enc).validate(true);
        if(!check) return Str.get(ti.content());
        while(ti.read() != -1);
        return Bln.TRUE;
      }
    } catch(final QueryException ex) {
      if(check && !ex.error().is(ErrType.XPTY)) return Bln.FALSE;
      throw ex;
    } catch(final IOException ex) {
      if(check) return Bln.FALSE;
      if(ex instanceof InputException) {
        final boolean inv = ex instanceof EncodingException || enc != null;
        throw (inv ? INVCHARS_X : WHICHCHARS_X).get(info, ex);
      }
      throw RESNF_X.get(info, io);
    }
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param qc query context
   * @param frag parse fragments
   * @return result
   * @throws QueryException query exception
   */
  ANode parseXml(final QueryContext qc, final boolean frag) throws QueryException {
    final Item it = exprs[0].item(qc, info);
    if(it == null) return null;

    final IO io = new IOContent(toToken(it), string(sc.baseURI().string()));
    try {
      final Parser parser;
      if(frag) {
        parser = new XMLParser(io, MainOptions.get(), true);
      } else {
        parser = Parser.xmlParser(io);
      }
      return new DBNode(parser);
    } catch(final IOException ex) {
      throw SAXERR_X.get(info, ex);
    }
  }

  /**
   * Returns the specified text as lines.
   * @param str text input
   * @return result
   */
  public static Iter textIter(final byte[] str) {
    try {
      final NewlineInput nli = new NewlineInput(new ArrayInput(str));
      final TokenBuilder tb = new TokenBuilder();
      return new Iter() {
        @Override
        public Item next() {
          try {
            return nli.readLine(tb) ? Str.get(tb.toArray()) : null;
          } catch(final IOException ex) {
            // no I/O exception expected, as input is a main-memory array
            throw Util.notExpected(ex);
          }
        }
      };
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }
}
