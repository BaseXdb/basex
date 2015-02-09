package org.basex.query.func.validate;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.query.*;
import org.xml.sax.*;

/** Abstract validator class. */
abstract class Validate {
  /** Temporary file instance. */
  IOFile tmp;

  /**
   * Starts the validation.
   * @param h error handler
   * @throws IOException I/O exception
   * @throws ParserConfigurationException parser configuration exception
   * @throws SAXException SAX exception
   * @throws QueryException query exception
   */
  abstract void process(ErrorHandler h)
      throws IOException, ParserConfigurationException, SAXException, QueryException;
}
