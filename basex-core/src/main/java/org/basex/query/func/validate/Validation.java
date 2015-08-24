package org.basex.query.func.validate;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.query.*;
import org.xml.sax.*;

/** Abstract validator class. */
abstract class Validation {
  /** Temporary schema instance. */
  private IOFile schema;

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

  /**
   * Prepares validation. Creates a temporary file from the specified IO reference if it is
   * main-memory content or a streaming reference.
   * @param in schema file
   * @param handler error handler
   * @return resulting file
   * @throws IOException I/O exception
   */
  protected IO prepare(final IO in, final ErrorHandler handler) throws IOException {
    if(in instanceof IOContent || in instanceof IOStream) {
      // main-memory content, stream: cache contents to file
      final IOFile io = new IOFile(File.createTempFile("validate", IO.BASEXSUFFIX));
      io.write(in.read());
      schema = io;
      handler.schema(io);
      return io;
    }
    return in;
  }

  /**
   * Closes a temporary schema instance.
   */
  void finish() {
    if(schema != null) schema.delete();
  }
}
