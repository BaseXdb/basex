package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Functions for validating documents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Michael Seiferle
 */
public class FNValidate extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNValidate(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _VALIDATE_XSD: return xsd(ctx);
      case _VALIDATE_DTD: return dtd(ctx);
      default:            return super.item(ctx, ii);
    }
  }

  /**
   * Validates a document against a XML Schema.
   * There exist two variants:
   *
   * <ul>{@code validate:xsd($doc)}
   *  <li>Looks for {@code xsi:(noNamespace)schemaLocation} in {@code $doc} and
   *    uses this schema for validation.</li>
   *  <li>{@code $doc} must contain a schemaLocation declaration for validation
   *  to work.</li>
   *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
   *    xs:string} pointing to an URL or a local file that will then be parsed
   *    and validated.</li>
   *  </ul>
   *  <ul>{@code validate:xsd($doc, $schema)}
   *  <li>if {@code $doc} contains an {@code xsi:(noNamespace)schemaLocation} it
   *  will be ignored.</li>
   *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
   *    xs:string} pointing to an URL or a local file</li>
   *  <li>{@code $schema as xs:string} is expected to point to an URL or a local
   *  file containing the schema definitions. </li>
   *  </ul>
   *
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item xsd(final QueryContext ctx) throws QueryException {
    try {
      final IO in = read(ctx, null);
      final SchemaFactory sf = SchemaFactory.newInstance(
          XMLConstants.W3C_XML_SCHEMA_NS_URI);
      final Schema schema;
      if(expr.length < 2) { // the schema location is computed at runtime
        schema = sf.newSchema();
      } else { // schema explicitly given and passed to the SchemaFactory
        final IO sc = IO.get(string(checkStr(expr[1], ctx)));
        if(!sc.exists()) WHICHRES.thrw(info, sc);
        schema = sf.newSchema(new URL(sc.url()));
      }
      final Validator v = schema.newValidator();
      v.setErrorHandler(new SchemaHandler());
      v.validate(new StreamSource(in.inputStream()));
      return null;
    } catch(final Exception ex) {
      if(ex instanceof QueryException) throw (QueryException) ex;
      // may be IOException, SAXException; get original exception
      Throwable e = ex;
      while(e.getCause() != null) e = e.getCause();
      throw VALFAIL.thrw(info, e.getMessage());
    }
  }

  /**
   * Validates a document against a DTD.
   * There exist two variants:
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

   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item dtd(final QueryContext ctx) throws QueryException {
    try {
      final IO in;
      if(expr.length == 2) {
        // integrate doctype declaration via serialization properties
        final SerializerProp sp = new SerializerProp();
        final String dtd = string(checkStr(expr[1], ctx));
        if(!IO.get(dtd).exists()) WHICHRES.thrw(info, dtd);
        sp.set(SerializerProp.S_DOCTYPE_SYSTEM, dtd);
        in = read(ctx, sp);
      } else {
        // assume that doctype declaration is included in document
        in = read(ctx, null);
      }
      final SAXParserFactory sf = SAXParserFactory.newInstance();
      sf.setValidating(true);
      final InputSource is = in.inputSource();
      sf.newSAXParser().parse(is, new SchemaHandler());
      return null;
    } catch(final Exception ex) {
      if(ex instanceof QueryException) throw (QueryException) ex;
      // may be IOException, SAXException, ParserConfigurationException
      Util.debug(ex);
      throw VALFAIL.thrw(info, ex.getMessage());
    }
  }

  /** Schema error handler. */
  static class SchemaHandler extends DefaultHandler {
    @Override
    public void fatalError(final SAXParseException ex) throws SAXException {
      error(ex);
    }

    @Override
    public void error(final SAXParseException ex) throws SAXException {
      // may be recursively called if external validator (e.g. Saxon) is used
      final String msg = ex.getMessage();
      if(msg.contains("Exception:")) {
        Throwable e = ex;
        while(e.getCause() != null) e = e.getCause();
        throw e instanceof SAXException ? (SAXException) e : new SAXException(msg);
      }

      final TokenBuilder report = new TokenBuilder();
      final String id = ex.getSystemId();
      if(id != null) report.add(IO.get(id).name()).add(", ");
      report.addExt(ex.getLineNumber()).add(':').addExt(ex.getColumnNumber());
      report.add(": ").add(msg);
      throw new SAXException(report.toString());
    }
  }

  /**
   * Returns an input reference (possibly cached) to the first argument.
   * @param ctx query context
   * @param sp serializer properties
   * @return item
   * @throws QueryException query exception
   * @throws IOException exception
   */
  private IO read(final QueryContext ctx, final SerializerProp sp)
      throws QueryException, IOException {

    final Item it = checkItem(expr[0], ctx);
    if(it.isEmpty()) STRNODTYPE.thrw(info, this);
    final Type ip = it.type;

    final ArrayOutput ao = new ArrayOutput();
    if(ip.isNode()) {
      // return node in string representation
      Serializer.get(ao, sp).serialize((ANode) it);
      return new IOContent(ao.toArray());
    }
    if(ip.isString()) {
      final String path = string(it.string(info));
      IO io = IO.get(path);
      if(!io.exists()) WHICHRES.thrw(info, path);

      if(sp != null) {
        // add doctype declaration if specified
        Serializer.get(ao, sp).serialize(new DBNode(io, ctx.context.prop));
        io = new IOContent(ao.toArray());
        io.name(path);
      }
      return io;
    }
    throw STRNODTYPE.thrw(info, this, ip);
  }
}
