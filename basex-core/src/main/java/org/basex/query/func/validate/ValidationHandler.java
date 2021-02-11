package org.basex.query.func.validate;

import java.util.*;

import org.basex.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Error handler.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ValidationHandler extends DefaultHandler {
  /** Fatal error. */
  static final String FATAL = "Fatal";
  /** Error. */
  static final String ERROR = "Error";
  /** Warning. */
  static final String WARNING = "Warning";

  /** Errors. */
  private final ArrayList<ErrorInfo> errors = new ArrayList<>();

  /** Schema URL. */
  private IO schema;

  @Override
  public void fatalError(final SAXParseException ex) {
    add(ex, FATAL);
  }

  @Override
  public void error(final SAXParseException ex) {
    add(ex, ERROR);
  }

  @Override
  public void warning(final SAXParseException ex) {
    add(ex, WARNING);
  }

  /**
   * Adds a new error info.
   * @param ex exception
   * @param level level
   */
  void add(final SAXException ex, final String level) {
    errors.add(new ErrorInfo(ex, level, schema));
  }

  /**
   * Assigns the schema reference.
   * @param io schema reference
   */
  void schema(final IO io) {
    schema = io;
  }

  /**
   * Returns the errors.
   * @return errors
   */
  ArrayList<ErrorInfo> getErrors() {
    return errors;
  }
}
