package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

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
      final Schema s = expr.length < 2 ? sf.newSchema() :
        sf.newSchema(new URL(string(checkStr(expr[1], ctx))));

      s.newValidator().validate(new StreamSource(in.inputStream()));
      return null;
    } catch(final Exception ex) {
      System.out.println("? " + ex.getClass());
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
        // include doctype declaration via serialization properties
        final SerializerProp sp = new SerializerProp();
        sp.set(SerializerProp.S_DOCTYPE_SYSTEM, string(checkStr(expr[1], ctx)));
        in = read(ctx, sp);
      } else {
        in = read(ctx, null);
      }
      final SAXParserFactory sf = SAXParserFactory.newInstance();
      sf.setValidating(true);
      sf.newSAXParser().parse(in.inputStream(), new DefaultHandler());
      return null;
    } catch(final Exception ex) {
      throw DOCVAL.thrw(info, ex.getMessage());
    }
  }

  /**
   * Returns an input reference (possibly cached) to the first argument.
   * @param ctx query context
   * @param sp serializer properties
   * @return item
   * @throws QueryException query exception
   * @throws Exception exception
   */
  private IO read(final QueryContext ctx, final SerializerProp sp) throws Exception {
    final Item it = checkItem(expr[0], ctx);
    if(it.isEmpty()) throw STRNODTYPE.thrw(info, this);
    final Type ip = it.type;

    final ArrayOutput ao = new ArrayOutput();
    if(ip.isNode()) {
      // return node in string representation
      Serializer.get(ao, sp).serialize((ANode) it);
      return new IOContent(ao.toArray());
    }
    if(ip.isString()) {
      IO io = IO.get(string(it.string(info)));
      if(sp != null) {
        // add doctype declaration if specified
        Serializer.get(ao, sp).serialize(new DBNode(io, ctx.context.prop));
        io = IO.get(ao.toString());
      }
      return io;
    }
    throw STRNODTYPE.thrw(info, this, ip);
  }
}
