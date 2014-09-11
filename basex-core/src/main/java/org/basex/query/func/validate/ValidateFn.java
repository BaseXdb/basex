package org.basex.query.func.validate;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Functions for validating documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Michael Seiferle
 * @author Marco Lettere (greedy/verbose validation)
 */
public abstract class ValidateFn extends StandardFunc {
  /**
   * Runs the specified validator.
   * @param v validator code
   * @return string sequence with warnings and errors
   * @throws QueryException query exception
   */
  final Value process(final Validate v) throws QueryException {
    final ErrorHandler handler = new ErrorHandler();
    try {
      v.process(handler);
    } catch(final IOException | ParserConfigurationException ex) {
      throw BXVA_START_X.get(info, ex);
    } catch(final SAXException ex) {
      // fatal exception: get original message
      Throwable e = ex;
      while(e.getCause() != null) {
        Util.debug(e);
        e = e.getCause();
      }
      return Str.get("Fatal" + Text.COL + ex.getLocalizedMessage());
    } finally {
      if(v.tmp != null) v.tmp.delete();
    }
    // return error strings
    return StrSeq.get(handler.getExceptions());
  }

  /**
   * Returns an input reference (possibly cached) to the first argument.
   * @param it item
   * @param qc query context
   * @param sopts serializer parameters
   * @return item
   * @throws QueryException query exception
   * @throws IOException exception
   */
  final IO read(final Item it, final QueryContext qc, final SerializerOptions sopts)
      throws QueryException, IOException {

    if(it instanceof ANode) {
      // return node in string representation
      final ArrayOutput ao = new ArrayOutput();
      Serializer.get(ao, sopts).serialize(it);
      final IOContent io = new IOContent(ao.finish());
      io.name(string(((ANode) it).baseURI()));
      return io;
    }

    if(it.type.isStringOrUntyped()) {
      IO io = checkPath(it, qc);
      if(sopts != null) {
        // add doctype declaration if specified
        final ArrayOutput ao = new ArrayOutput();
        Serializer.get(ao, sopts).serialize(new DBNode(io));
        io = new IOContent(ao.finish());
        io.name(io.path());
      }
      return io;
    }
    throw STRNOD_X_X.get(info, it.type, it);
  }

  /**
   * Creates a temporary file with the contents of the specified IO reference.
   * {@code null} is returned if the IO reference refers to an existing file.
   * @param in input file
   * @return resulting file
   * @throws IOException I/O exception
   */
  static IOFile createTmp(final IO in) throws IOException {
    if(!(in instanceof IOContent || in instanceof IOStream)) return null;
    final IOFile tmp = new IOFile(File.createTempFile("validate", IO.BASEXSUFFIX));
    tmp.write(in.read());
    return tmp;
  }
}
