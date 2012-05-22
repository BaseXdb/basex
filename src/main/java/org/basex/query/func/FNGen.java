package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.util.Err.ErrType;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNGen extends StandardFunc {
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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
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
        if(it.type.isFunction()) FIATOM.thrw(info, FNGen.this);
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
    // return default collection
    if(expr.length == 0) return ctx.resource.collection(info);

    // check if reference is valid
    final byte[] in = checkEStr(expr[0].item(ctx, info));
    if(!Uri.uri(in).isValid()) INVCOLL.thrw(info, in);
    return ctx.resource.collection(string(in), info);
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
    ctx.updates.add(new Put(info, target.pre, target.data, u), ctx);

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
    final byte[] in = checkEStr(it);
    if(!Uri.uri(in).isValid()) INVDOC.thrw(info, in);
    return ctx.resource.doc(string(in), info);
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
    final IO io = checkIO(checkStr(expr[0], ctx), ctx);
    final String enc = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    if(enc != null && !Charset.isSupported(enc)) WHICHENC.thrw(info, enc);
    return new StrStream(io, enc, RESNF);
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
        return p + 1 < str.length || !bl.isEmpty() ? Str.get(bl.toArray()) : null;
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
    final byte[] path = checkStr(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkEStr(expr[1], ctx));
    try {
      final IO io = checkIO(path, ctx);
      final NewlineInput nli = new NewlineInput(io).encoding(enc);
      try {
        while(nli.read() != -1);
      } finally {
        nli.close();
      }
      return Bln.TRUE;
    } catch(final IOException ex) {
      return Bln.FALSE;
    } catch(final QueryException ex) {
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
      Item it = expr.length > 1 ? expr[1].item(ctx, info) : null;
      final Serializer ser = Serializer.get(ao, FuncParams.serializerProp(it));
      final Iter ir = expr[0].iter(ctx);
      while((it = ir.next()) != null) ser.serialize(it);
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
        sig == Function.UNPARSED_TEXT || sig == Function.UNPARSED_TEXT_LINES ||
        sig == Function.UNPARSED_TEXT_AVAILABLE || sig == Function.PARSE_XML ||
        sig == Function.URI_COLLECTION || sig == Function.SERIALIZE) ||
      u == Use.CTX && (sig == Function.DATA && expr.length == 0 ||
        sig == Function.PUT) && expr.length == 0 || super.uses(u);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return sig == Function.COLLECTION || super.iterable();
  }
}
