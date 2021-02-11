package org.basex.query.func.validate;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Abstract validator class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class Validation {
  /** Temporary schema instance. */
  private IOFile schema;

  /**
   * Starts the validation.
   * @param handler error handler
   * @throws IOException I/O exception
   * @throws ParserConfigurationException parser configuration exception
   * @throws SAXException SAX exception
   * @throws QueryException query exception
   */
  abstract void process(ValidationHandler handler)
      throws IOException, ParserConfigurationException, SAXException, QueryException;

  /**
   * Prepares validation. Creates a temporary file from the specified IO reference if it is
   * main-memory content or a streaming reference.
   * @param in schema file
   * @param handler error handler
   * @return resulting file
   * @throws IOException I/O exception
   */
  final IO prepare(final IO in, final ValidationHandler handler) throws IOException {
    if(in instanceof IOContent || in instanceof IOStream) {
      // cache main-memory content or stream to file
      schema = new IOFile(File.createTempFile(Prop.NAME + '-', IO.TMPSUFFIX));
      schema.write(in.read());
      handler.schema(schema);
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
}
