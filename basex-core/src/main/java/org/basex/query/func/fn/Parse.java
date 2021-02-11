package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.xml.sax.*;

/**
 * Parse functions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Parse extends StandardFunc {
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

    checkCreate(qc);
    final byte[] path = toTokenOrNull(exprs[0], qc);
    if(path == null) return check ? Bln.FALSE : Empty.VALUE;

    String enc;
    IO io;
    try {
      enc = encoding ? toEncodingOrNull(1, ENCODING_X, qc) : null;

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

  /**
   * Returns a document node for the parsed XML input.
   * @param qc query context
   * @param frag parse fragments
   * @return result or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item parseXml(final QueryContext qc, final boolean frag) throws QueryException {
    final byte[] token = toTokenOrNull(exprs[0], qc);
    if(token == null) return Empty.VALUE;

    final IO io = new IOContent(token, string(sc.baseURI().string()));
    try {
      return new DBNode(frag ? new XMLParser(io, MainOptions.get(), true) : Parser.xmlParser(io));
    } catch(final IOException ex) {
      final QueryException qe = SAXERR_X.get(info, ex);
      final Throwable th = ex.getCause();
      if(th instanceof SAXException) qe.value(Str.get(th.toString()));
      throw qe;
    }
  }
}
