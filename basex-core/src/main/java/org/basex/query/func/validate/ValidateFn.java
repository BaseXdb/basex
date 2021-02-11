package org.basex.query.func.validate;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * Functions for validating documents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Michael Seiferle
 * @author Marco Lettere (greedy/verbose validation)
 */
abstract class ValidateFn extends StandardFunc {
  /** Report element. */
  private static final String REPORT = "report";
  /** Error element. */
  private static final String MESSAGE = "message";
  /** Status. */
  private static final String STATUS = "status";
  /** Valid. */
  private static final String VALID = "valid";
  /** Invalid. */
  private static final String INVALID = "invalid";
  /** Line. */
  private static final String LINE = "line";
  /** Column. */
  private static final String COLUMN = "column";
  /** Type. */
  private static final String LEVEL = "level";
  /** File. */
  private static final String URL = "url";

  /**
   * Runs the validation process and returns an empty sequence or an error.
   * @param qc query context
   * @return empty sequence
   * @throws QueryException query exception
   */
  protected final Empty check(final QueryContext qc) throws QueryException {
    final ArrayList<ErrorInfo> errors = errors(qc);
    if(errors.isEmpty()) return Empty.VALUE;
    throw VALIDATE_ERROR_X.get(info, errors.get(0).toString());
  }

  /**
   * Runs the validation process and returns the errors as string sequence.
   * @param qc query context
   * @return string sequence
   * @throws QueryException query exception
   */
  protected final Value info(final QueryContext qc) throws QueryException {
    final ArrayList<ErrorInfo> errors = errors(qc);
    final TokenList tl = new TokenList(errors.size());
    for(final ErrorInfo error : errors) tl.add(error.toString());
    return StrSeq.get(tl);
  }

  /**
   * Runs the validation process and returns the errors as XML.
   * @param qc query context
   * @return XML
   * @throws QueryException query exception
   */
  protected final FElem report(final QueryContext qc) throws QueryException {
    final ArrayList<ErrorInfo> errors = errors(qc);
    final FElem report = new FElem(REPORT);
    report.add(new FElem(STATUS).add(errors.isEmpty() ? VALID : INVALID));
    for(final ErrorInfo ei : errors) {
      final FElem error = new FElem(MESSAGE);
      error.add(LEVEL, ei.level);
      if(ei.line != Integer.MIN_VALUE) error.add(LINE, token(ei.line));
      if(ei.column != Integer.MIN_VALUE) error.add(COLUMN, token(ei.column));
      if(ei.url != null) error.add(URL, ei.url);
      error.add(ei.message);
      report.add(error);
    }
    return report;
  }

  /**
   * Runs the validation process and returns the resulting errors.
   * @param qc query context
   * @return errors
   * @throws QueryException query exception
   */
  public abstract ArrayList<ErrorInfo> errors(QueryContext qc) throws QueryException;

  /**
   * Runs the specified validator.
   * @param v validator code
   * @return errors
   * @throws QueryException query exception
   */
  protected final ArrayList<ErrorInfo> process(final Validation v) throws QueryException {
    final ValidationHandler handler = new ValidationHandler();
    try {
      v.process(handler);
    } catch(final SAXException ex) {
      // fatal exception: send exceptions to debug output, ignore root exception
      Util.rootException(ex);
      handler.add(ex, ValidationHandler.FATAL);
    } catch(final IOException | ParserConfigurationException | Error ex) {
      throw VALIDATE_START_X.get(info, ex);
    } finally {
      v.finish();
    }
    return handler.getErrors();
  }

  /**
   * Returns an input reference (possibly cached) to the first argument.
   * @param item item
   * @param sopts serializer parameters
   * @return item
   * @throws QueryException query exception
   * @throws IOException exception
   */
  protected final IO read(final Item item, final SerializerOptions sopts)
      throws QueryException, IOException {

    if(item instanceof ANode) {
      // return node as main-memory string
      final IOContent io = new IOContent(item.serialize(sopts).finish());
      io.name(string(((ANode) item).baseURI()));
      return io;
    }

    if(item.type.isStringOrUntyped()) {
      IO io = checkPath(toToken(item));
      if(sopts != null) {
        // add doctype declaration if specified
        io = new IOContent(new DBNode(io).serialize(sopts).finish());
        io.name(io.path());
      }
      return io;
    }

    throw STRNOD_X_X.get(info, item.type, item);
  }
}
