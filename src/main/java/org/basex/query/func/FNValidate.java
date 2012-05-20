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
   * Validates a documents against a given XML Schema.
   * {@code xsi:(noNamespace)schemaLocation} will be ignored.
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
      if(expr.length < 2) {
        schema = sf.newSchema();
      } else {
        final IO sc = IO.get(string(checkStr(expr[1], ctx)));
        if(!sc.exists()) RESFNF.thrw(info, sc);
        schema = sf.newSchema(new URL(sc.url()));
      }
      schema.newValidator().validate(new StreamSource(in.inputStream()));
      return null;
    } catch(final Exception ex) {
      // may be IOException, SAXException
      throw DOCVAL.thrw(info, ex.getMessage());
    }
  }

  /**
   * Validates a document against a DTD.
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
        if(!IO.get(dtd).exists()) RESFNF.thrw(info, dtd);
        sp.set(SerializerProp.S_DOCTYPE_SYSTEM, dtd);
        in = read(ctx, sp);
      } else {
        // assume that doctype declaration is included in document
        in = read(ctx, null);
      }
      final SAXParserFactory sf = SAXParserFactory.newInstance();
      sf.setValidating(true);
      final InputSource is = in.inputSource();
      sf.newSAXParser().parse(is, new DTDHandler());
      return null;
    } catch(final Exception ex) {
      if(ex instanceof QueryException) throw (QueryException) ex;
      // may be IOException, SAXException, ParserConfigurationException
      Util.debug(ex);
      throw DOCVAL.thrw(info, ex.getMessage());
    }
  }

  /** DTD handler. */
  static class DTDHandler extends DefaultHandler {
    @Override
    public void fatalError(final SAXParseException ex) throws SAXException {
      error(ex);
    }
    @Override
    public void warning(final SAXParseException ex) throws SAXException {
      error(ex);
    }
    @Override
    public void error(final SAXParseException ex) throws SAXException {
      final TokenBuilder report = new TokenBuilder();
      if(ex.getSystemId() != null) report.add(IO.get(ex.getSystemId()).name()).add(", ");
      report.addExt(ex.getLineNumber()).add(':').addExt(ex.getColumnNumber());
      report.add(": ").add(ex.getMessage());
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
      if(!io.exists()) RESFNF.thrw(info, path);

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
