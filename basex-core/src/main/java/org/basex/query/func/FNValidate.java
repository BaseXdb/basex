package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Functions for validating documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Michael Seiferle
 * @author Marco Lettere (greedy/verbose validation)
 */
public final class FNValidate extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNValidate(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _VALIDATE_XSD_INFO: return xsdInfo(qc).iter();
      case _VALIDATE_DTD_INFO: return dtdInfo(qc).iter();
      default:                 return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _VALIDATE_XSD_INFO: return xsdInfo(qc);
      case _VALIDATE_DTD_INFO: return dtdInfo(qc);
      default:                 return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _VALIDATE_XSD: return xsd(qc);
      case _VALIDATE_DTD: return dtd(qc);
      default:            return super.item(qc, ii);
    }
  }

  /**
   * Validates a document against an XML Schema.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item xsd(final QueryContext qc) throws QueryException {
    final Value seq = xsdInfo(qc);
    if(seq == Empty.SEQ) return null;
    throw BXVA_FAIL.get(info, seq.iter().next());
  }

  /**
   * Validates a document against an XML Schema.
   * The following two variants exist:
   *
   * <div>{@code validate:xsd($doc)}:</div>
   * <ul>
   *  <li>Looks for {@code xsi:(noNamespace)schemaLocation} in {@code $doc} and
   *    uses this schema for validation.</li>
   *  <li>{@code $doc} must contain a schemaLocation declaration for validation
   *  to work.</li>
   *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
   *    xs:string} pointing to an URL or a local file that will then be parsed
   *    and validated.</li>
   * </ul>
   * <div>{@code validate:xsd($doc, $schema)}:</div>
   * <ul>
   *  <li>if {@code $doc} contains an {@code xsi:(noNamespace)schemaLocation} it
   *  will be ignored.</li>
   *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
   *    xs:string} pointing to an URL or a local file</li>
   *  <li>{@code $schema as xs:string} is expected to point to an URL or a local
   *  file containing the schema definitions. </li>
   * </ul>
   *
   * @param qc query context
   * @return info string sequence, or null
   * @throws QueryException query exception
   */
  private Value xsdInfo(final QueryContext qc) throws QueryException {
    return process(new Validate() {
      @Override
      void process(final ErrorHandler handler) throws IOException, SAXException, QueryException {
        final IO in = read(checkItem(exprs[0], qc), qc, null);
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema;
        if(exprs.length < 2) {
          // assume that schema declaration is included in document
          schema = sf.newSchema();
        } else {
          final Item it = checkItem(exprs[1], qc);
          // schema specified as string
          IO scio = read(it, qc, null);
          tmp = createTmp(scio);
          if(tmp != null) scio = tmp;
          schema = sf.newSchema(new URL(scio.url()));
        }

        final Validator v = schema.newValidator();
        v.setErrorHandler(handler);
        v.validate(new StreamSource(in.inputStream()));
      }
    });
  }

  /**
   * Validates a document against a DTD.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item dtd(final QueryContext qc) throws QueryException {
    final Value seq = dtdInfo(qc);
    if(seq == Empty.SEQ) return null;
    throw BXVA_FAIL.get(info, seq.iter().next());
  }

  /**
   * Validates a document against a DTD.
   * The following two variants exist:
   *
   * <ul>{@code validate:dtd($doc)}
   *  <li>Looks for the document type declaration in {@code $doc} and
   *    uses it for validation.</li>
   *  <li>{@code $doc} must contain a DTD for this to work.</li>
   *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
   *    xs:string} pointing to an URL or a local file that will then be parsed
   *    and validated.</li>
   *  </ul>
   *  <ul>{@code validate:dtd($doc, $dtd)}
   *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
   *    xs:string} pointing to an URL or a local file</li>
   *  <li>{@code $dtd as xs:string} is expected to point to an URL or a local
   *  file containing the document type definitions. </li>
   *  </ul>

   * @param qc query context
   * @return info string sequence, or null
   * @throws QueryException query exception
   */
  private Value dtdInfo(final QueryContext qc) throws QueryException {
    return process(new Validate() {
      @Override
      void process(final ErrorHandler handler)
          throws IOException, ParserConfigurationException, SAXException, QueryException {

        final Item it = checkItem(exprs[0], qc);
        SerializerOptions sp = null;

        // integrate doctype declaration via serialization parameters
        if(exprs.length > 1) {
          sp = new SerializerOptions();
          IO dtd = checkPath(exprs[1], qc);
          tmp = createTmp(dtd);
          if(tmp != null) dtd = tmp;
          sp.set(SerializerOptions.DOCTYPE_SYSTEM, dtd.url());
        }

        final IO in = read(it, qc, sp);
        final SAXParserFactory sf = SAXParserFactory.newInstance();
        sf.setValidating(true);
        sf.newSAXParser().parse(in.inputSource(), handler);
      }
    });
  }

  /**
   * Runs the specified validator.
   * @param v validator code
   * @return string sequence with warnings and errors
   * @throws QueryException query exception
   */
  private Value process(final Validate v) throws QueryException {
    final ErrorHandler handler = new ErrorHandler();
    try {
      v.process(handler);
    } catch(final IOException | ParserConfigurationException ex) {
      throw BXVA_START.get(info, ex);
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
   * Creates a temporary file with the contents of the specified IO reference.
   * {@code null} is returned if the IO reference refers to an existing file.
   * @param in input file
   * @return resulting file
   * @throws IOException I/O exception
   */
  private static IOFile createTmp(final IO in) throws IOException {
    if(!(in instanceof IOContent || in instanceof IOStream)) return null;
    final IOFile tmp = new IOFile(File.createTempFile("validate", IO.BASEXSUFFIX));
    tmp.write(in.read());
    return tmp;
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
  private IO read(final Item it, final QueryContext qc, final SerializerOptions sopts)
      throws QueryException, IOException {

    if(it.type.isNode()) {
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
    throw STRNODTYPE.get(info, it.type, it);
  }

  /** Schema error handler. */
  static class ErrorHandler extends DefaultHandler {
    /** Will contain all raised validation exception messages. */
    private final TokenList exceptions = new TokenList();

    @Override
    public void fatalError(final SAXParseException ex) {
      error(ex, "Fatal");
    }

    @Override
    public void error(final SAXParseException ex) {
      error(ex, "Error");
    }

    @Override
    public void warning(final SAXParseException ex) {
      error(ex, "Warning");
    }

    /**
     * Adds an error message.
     * @param ex exception
     * @param type type of error
     */
    private void error(final SAXParseException ex, final String type) {
      // may be recursively called if external validator (e.g. Saxon) is used
      String msg = ex.getMessage();
      if(msg.contains("Exception:")) {
        Throwable e = ex;
        while(e.getCause() != null) e = e.getCause();
        if(e instanceof SAXException) msg = e.getLocalizedMessage();
      } else {
        final TokenBuilder report = new TokenBuilder();
        final String id = ex.getSystemId();
        if(id != null) report.add(IO.get(id).name()).add(", ");
        report.addExt(ex.getLineNumber()).add(Text.COL).addExt(ex.getColumnNumber());
        report.add(": ").add(msg);
        msg = report.toString();
      }
      exceptions.add(type + Text.COL + msg);
    }

    /**
     * Returns the exception messages.
     * @return exception messages
     */
    TokenList getExceptions() {
      return exceptions;
    }
  }

  /** Abstract validator class. */
  abstract static class Validate {
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
}
