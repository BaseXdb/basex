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
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * Functions for validating documents.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Michael Seiferle
 * @author Marco Lettere (greedy/verbose validation)
 */
abstract class ValidateFn extends StandardFunc {
  /** QName. */
  private static final QNm Q_REPORT = new QNm("report");
  /** QName. */
  private static final QNm Q_MESSAGE = new QNm("message");
  /** QName. */
  private static final QNm Q_STATUS = new QNm("status");
  /** QName. */
  private static final QNm Q_LINE = new QNm("line");
  /** QName. */
  private static final QNm Q_COLUMN = new QNm("column");
  /** QName. */
  private static final QNm Q_LEVEL = new QNm("level");
  /** QName. */
  private static final QNm Q_URL = new QNm("url");

  /** String: valid. */
  private static final String VALID = "valid";
  /** String: invalid. */
  private static final String INVALID = "invalid";

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
  protected final FNode report(final QueryContext qc) throws QueryException {
    final ArrayList<ErrorInfo> errors = errors(qc);
    final FBuilder report = FElem.build(Q_REPORT);
    report.add(FElem.build(Q_STATUS).add(errors.isEmpty() ? VALID : INVALID));
    for(final ErrorInfo ei : errors) {
      final FBuilder error = FElem.build(Q_MESSAGE).add(Q_LEVEL, ei.level);
      if(ei.line != Integer.MIN_VALUE) error.add(Q_LINE, ei.line);
      if(ei.column != Integer.MIN_VALUE) error.add(Q_COLUMN, ei.column);
      report.add(error.add(Q_URL, ei.url).add(ei.message));
    }
    return report.finish();
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

    final Type type = item.type;
    if(type.isStringOrUntyped()) {
      IO io = toIO(toString(item));
      if(sopts != null) {
        // add doctype declaration if specified
        io = new IOContent(new DBNode(io).serialize(sopts).finish());
        io.name(io.path());
      }
      return io;
    }

    throw STRNOD_X_X.get(info, type, item);
  }
}
