package org.basex.query.func.validate;

import org.basex.query.func.validate.ErrorInfo.Level;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Abstract validator class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class Validation extends DefaultHandler {
  /** Errors. */
  private final ArrayList<ErrorInfo> errors = new ArrayList<>();
  /** Schema URL. */
  private IOFile schema;

  /**
   * Starts the validation.
   * @throws IOException I/O exception
   * @throws ParserConfigurationException parser configuration exception
   * @throws SAXException SAX exception
   * @throws QueryException query exception
   */
  abstract void validate() throws IOException, ParserConfigurationException, SAXException,
    QueryException;

  /**
   * Prepares validation. Creates a temporary file from the specified IO reference if it is
   * main-memory content or a streaming reference.
   * @param in schema file
   * @return resulting file
   * @throws IOException I/O exception
   */
  final IO prepare(final IO in) throws IOException {
    if(in instanceof IOContent || in instanceof IOStream) {
      // cache main-memory content or stream to file
      schema = new IOFile(File.createTempFile(Prop.NAME + '-', IO.TMPSUFFIX));
      schema.write(in.read());
      return schema;
    }
    return in;
  }

  /**
   * Closes a temporary schema instance.
   */
  final void finish() {
    if(schema != null) schema.delete();
  }

  @Override
  public void fatalError(final SAXParseException ex) {
    add(ex, Level.FATAL);
  }

  @Override
  public void error(final SAXParseException ex) {
    add(ex, Level.ERROR);
  }

  @Override
  public void warning(final SAXParseException ex) {
    add(ex, Level.WARNING);
  }

  /**
   * Adds a new error info.
   * @param ex exception
   * @param level level
   */
  void add(final SAXException ex, final Level level) {
    errors.add(new ErrorInfo(ex, level, schema));
  }

  /**
   * Returns the errors.
   * @return errors
   */
  ArrayList<ErrorInfo> getErrors() {
    return errors;
  }
}
