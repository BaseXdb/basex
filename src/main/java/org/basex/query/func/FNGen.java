package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.nio.charset.Charset;

import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.in.NewlineInput;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.*;
import org.basex.query.item.map.*;
import org.basex.query.iter.Iter;
import org.basex.query.path.*;
import org.basex.query.up.primitives.Put;
import org.basex.query.util.Err;
import org.basex.query.util.Err.ErrType;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.ByteList;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNGen extends StandardFunc {
  /** Element: output:serialization-parameters. */
  private static final QNm Q_SPARAM = new QNm("serialization-parameters", OUTPUTURI);
  /** Attribute: value. */
  private static final QNm A_VALUE = new QNm("value");
  /** Response node test. */
  public static final ExtTest OUTPUT_SERIAL = new ExtTest(NodeType.ELM, Q_SPARAM);

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNGen(final InputInfo ii, final Function f, final Expr[] e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case DATA:                return data(ctx);
      case COLLECTION:          return collection(ctx).iter();
      case URI_COLLECTION:      return uriCollection(ctx);
      case UNPARSED_TEXT_LINES: return unparsedTextLines(ctx);
      default:                  return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(sig) {
      case DOC:                     return doc(ctx);
      case DOC_AVAILABLE:           return docAvailable(ctx);
      case UNPARSED_TEXT:           return unparsedText(ctx);
      case UNPARSED_TEXT_AVAILABLE: return unparsedTextAvailable(ctx);
      case PUT:                     return put(ctx);
      case PARSE_XML:               return parseXml(ctx);
      case SERIALIZE:               return serialize(ctx);
      default:                      return super.item(ctx, ii);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return sig == Function.COLLECTION ? collection(ctx) : super.value(ctx);
  }

  @Override
  public Expr cmp(final QueryContext ctx) {
    if(sig == Function.DATA &&  expr.length == 1) {
      final SeqType t = expr[0].type();
      type = t.type.isNode() ? SeqType.get(AtomType.ATM, t.occ) : t;
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter data(final QueryContext ctx) throws QueryException {
    final Iter ir = ctx.iter(expr.length != 0 ? expr[0] : checkCtx(ctx));

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        if(it.type.isFunction()) FNATM.thrw(info, FNGen.this);
        return atom(it, info);
      }
    };
  }

  /**
   * Performs the collection function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Value collection(final QueryContext ctx) throws QueryException {
    final Item it = expr.length != 0 ? expr[0].item(ctx, info) : null;
    return ctx.resource.collection(
        it != null ? string(checkEStr(it)) : null, info);
  }

  /**
   * Performs the uri-collection function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter uriCollection(final QueryContext ctx) throws QueryException {
    final Iter coll = collection(ctx).iter();
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = coll.next();
        // all items will be nodes
        return it == null ? null : Uri.uri(((ANode) it).baseURI(), false);
      }
    };
  }

  /**
   * Performs the put function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item put(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final byte[] file = checkEStr(expr[1], ctx);
    final ANode nd = checkNode(checkNoEmpty(expr[0].item(ctx, info)));

    if(nd == null || nd.type != NodeType.DOC && nd.type != NodeType.ELM)
      UPFOTYPE.thrw(info, expr[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.isValid()) UPFOURI.thrw(info, file);
    final DBNode target = ctx.updates.determineDataRef(nd, ctx);
    ctx.updates.add(new Put(info, target.pre, target.data, u, ctx), ctx);

    return null;
  }

  /**
   * Performs the doc function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private ANode doc(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].item(ctx, info);
    if(it == null) return null;

    final String in = string(checkEStr(it));
    final Data d = ctx.resource.data(in, false, info);
    if(!d.single()) EXPSINGLE.thrw(info, in);
    return new DBNode(d, 0, Data.DOC);
  }

  /**
   * Performs the doc-available function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln docAvailable(final QueryContext ctx) throws QueryException {
    try {
      return Bln.get(doc(ctx) != null);
    } catch(final QueryException ex) {
      final Err err = ex.err();
      if(err != null && err.type == ErrType.FODC &&
          (err.num == 2 || err.num == 4)) return Bln.FALSE;
      throw ex;
    }
  }

  /**
   * Performs the unparsed-text function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private StrStream unparsedText(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    if(enc != null && !Charset.isSupported(enc)) WHICHENC.thrw(info, enc);
    return new StrStream(io, enc, WRONGINPUT);
  }

  /**
   * Performs the unparsed-text-lines function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  Iter unparsedTextLines(final QueryContext ctx) throws QueryException {
    return textIter(unparsedText(ctx), info);
  }

  /**
   * Returns the specified text as lines.
   * @param si text input
   * @param ii input info
   * @return result
   * @throws QueryException query exception
   */
  static Iter textIter(final StrStream si, final InputInfo ii) throws QueryException {
    final byte[] str = si.string(ii);
    return new Iter() {
      int p = -1;
      @Override
      public Item next() {
        final ByteList bl = new ByteList();
        while(++p < str.length && str[p] != '\n') bl.add(str[p]);
        return p + 1 < str.length || bl.size() != 0 ?
            Str.get(bl.toArray()) : null;
      }
    };
  }

  /**
   * Performs the unparsed-text-available function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln unparsedTextAvailable(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkEStr(expr[1], ctx));
    try {
      final NewlineInput nli = new NewlineInput(io).encoding(enc);
      try {
        while(nli.read() != -1);
      } finally {
        nli.close();
      }
      return Bln.TRUE;
    } catch(final IOException ex) {
      return Bln.FALSE;
    }
  }

  /**
   * Performs the parse-xml function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private ANode parseXml(final QueryContext ctx) throws QueryException {
    final byte[] cont = checkEStr(expr[0], ctx);
    Uri base = ctx.sc.baseURI();
    if(expr.length == 2) {
      base = Uri.uri(checkEStr(expr[1], ctx));
      if(!base.isValid()) BASEINV.thrw(info, base);
    }

    final IO io = new IOContent(cont, string(base.string()));
    try {
      return new DBNode(io, ctx.context.prop);
    } catch(final IOException ex) {
      throw SAXERR.thrw(info, ex);
    }
  }

  /**
   * Performs the serialize function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final ArrayOutput ao = new ArrayOutput();
    try {
      // run serialization
      final Serializer ser = Serializer.get(ao, serialPar(this, 1, ctx));
      final Iter ir = expr[0].iter(ctx);
      for(Item it; (it = ir.next()) != null;) it.serialize(ser);
      ser.close();
    } catch(final SerializerException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      SERANY.thrw(info, ex);
    }
    return Str.get(delete(ao.toArray(), '\r'));
  }

  @Override
  public boolean uses(final Use u) {
    return
      u == Use.CNS && sig == Function.PARSE_XML ||
      u == Use.UPD && sig == Function.PUT ||
      u == Use.X30 && (sig == Function.DATA && expr.length == 0 ||
        sig == Function.UNPARSED_TEXT ||
        sig == Function.UNPARSED_TEXT_LINES ||
        sig == Function.UNPARSED_TEXT_AVAILABLE ||
        sig == Function.PARSE_XML ||
        sig == Function.URI_COLLECTION || sig == Function.SERIALIZE) ||
      u == Use.CTX && (sig == Function.DATA && expr.length == 0 ||
        sig == Function.PUT) && expr.length == 0 || super.uses(u);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return sig == Function.COLLECTION || super.iterable();
  }

  /**
   * Creates serialization properties from the specified function argument.
   * @param fun calling function
   * @param arg argument with parameters
   * @param ctx query context
   * @return serialization parameters
   * @throws SerializerException serializer exception
   * @throws QueryException query exception
   */
  static SerializerProp serialPar(final StandardFunc fun, final int arg,
      final QueryContext ctx) throws SerializerException, QueryException {

    // check if enough arguments are available
    String params = "";
    if(arg < fun.expr.length) {
      // retrieve parameters
      final Item it = fun.expr[arg].item(ctx, fun.info);
      if(it != null) {
        if(it instanceof Map) {
          params = convert(((Map) it).tokenJavaMap(fun.info));
        } else {
          // check root node
          final ANode nd = (ANode) fun.checkType(it, NodeType.ELM);
          if(!OUTPUT_SERIAL.eq(nd)) SERUNKNOWN.thrw(fun.info, nd.qname());
          // retrieve query parameters
          params = parameters(nd, fun.info);
        }
      }
    }
    // use default parameters if no parameters have been assigned
    return params.isEmpty() ? ctx.serParams(true) : new SerializerProp(params);
  }

  /**
   * Returns all serialization options defined by a serialization element.
   * @param nd root node
   * @param ii input info
   * @return serialization tokens
   * @throws QueryException query exception
   */
  public static String parameters(final ANode nd, final InputInfo ii)
      throws QueryException {

    // interpret query parameters
    final TokenObjMap<Object> tm = new TokenObjMap<Object>();
    for(final ANode n : nd.children()) {
      final QNm qn = n.qname();
      if(!eq(qn.uri(), OUTPUTURI)) SERUNKNOWN.thrw(ii, qn);
      final byte[] val = n.attribute(A_VALUE);
      if(val == null) SERNOVAL.thrw(ii);
      tm.add(qn.local(), val);
    }
    return convert(tm);
  }

  /**
   * Converts a token map to a serialization string.
   * @param map map with serialization options
   * @return serialization string
   */
  private static String convert(final TokenObjMap<Object> map) {
    final TokenBuilder tb = new TokenBuilder();
    if(map != null) {
      for(final byte[] key : map) {
        if(!tb.isEmpty()) tb.add(',');
        tb.add(key).add('=').addExt(map.get(key));
      }
    }
    return tb.toString();
  }
}
