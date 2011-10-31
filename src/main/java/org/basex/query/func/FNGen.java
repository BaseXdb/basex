package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.nio.charset.Charset;

import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.in.TextInput;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Uri;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.Put;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.list.ByteList;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNGen extends FuncCall {
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
    switch(def) {
      case DATA:        return data(ctx);
      case COLL:        return collection(ctx).iter();
      case URICOLL:     return uriCollection(ctx);
      case PARSETXTLIN: return unparsedTextLines(ctx);
      default:          return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case DOC:         return doc(ctx);
      case DOCAVL:      return docAvailable(ctx);
      case PARSETXT:    return unparsedText(ctx);
      case PARSETXTAVL: return unparsedTextAvailable(ctx);
      case PUT:         return put(ctx);
      case PARSEXML:    return parseXml(ctx);
      case SERIALIZE:   return serialize(ctx);
      default:          return super.item(ctx, ii);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return def == Function.COLL ? collection(ctx) : super.value(ctx);
  }

  @Override
  public Expr cmp(final QueryContext ctx) {
    if(def == Function.DATA &&  expr.length == 1) {
      final SeqType t = expr[0].type();
      type = t.type.node() ? SeqType.get(AtomType.ATM, t.occ) : t;
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
        if(it.func()) FNATM.thrw(input, FNGen.this);
        return atom(it);
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
    final Item it = expr.length != 0 ? expr[0].item(ctx, input) : null;
    return ctx.resource.collection(it != null ? checkEStr(it) : null, input);
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
        return it == null ? null : Uri.uri(((ANode) it).base());
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
    checkAdmin(ctx);
    final byte[] file = checkEStr(expr[1], ctx);
    final ANode nd = checkNode(checkEmpty(expr[0].item(ctx, input)));

    if(nd == null || nd.type != NodeType.DOC && nd.type != NodeType.ELM)
      UPFOTYPE.thrw(input, expr[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.valid()) UPFOURI.thrw(input, file);
    final DBNode target = ctx.updates.determineDataRef(nd, ctx);
    ctx.updates.add(new Put(input, target.pre, target.data, u, ctx), ctx);

    return null;
  }

  /**
   * Performs the doc function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private ANode doc(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].item(ctx, input);
    if(it == null) return null;

    final byte[] in = checkEStr(it);
    final Data d = ctx.resource.data(in, false, input);
    if(!d.single()) EXPSINGLE.thrw(input, in);
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
      if(err != null && err.type == Err.ErrType.FODC &&
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
  private Str unparsedText(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    if(enc != null && !Charset.isSupported(enc)) WHICHENC.thrw(input, enc);
    try {
      return Str.get(TextInput.content(io, enc).finish());
    } catch(final IOException ex) {
      throw WRONGINPUT.thrw(input, io, ex);
    }
  }

  /**
   * Performs the unparsed-text-lines function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  Iter unparsedTextLines(final QueryContext ctx) throws QueryException {
    return textIter(unparsedText(ctx).atom());
  }

  /**
   * Returns the specified text as lines.
   * @param str input text
   * @return result
   */
  public static Iter textIter(final byte[] str) {
    return new Iter() {
      int p = -1;
      @Override
      public Item next() {
        final ByteList bl = new ByteList();
        while(++p < str.length && str[p] != 0x0a) bl.add(str[p]);
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
  private Bln unparsedTextAvailable(final QueryContext ctx)
      throws QueryException {

    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkEStr(expr[1], ctx));
    try {
      TextInput.content(io, enc);
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
    Uri base = ctx.baseURI;
    if(expr.length == 2) {
      base = Uri.uri(checkEStr(expr[1], ctx));
      if(!base.valid()) BASEINV.thrw(input, base);
    }

    final IO io = new IOContent(cont, string(base.atom()));
    try {
      return new DBNode(io, ctx.context.prop);
    } catch(final IOException ex) {
      throw SAXERR.thrw(input, ex);
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
      throw new QueryException(input, ex);
    } catch(final IOException ex) {
      SERANY.thrw(input, ex);
    }
    return Str.get(delete(ao.toArray(), '\r'));
  }

  @Override
  public boolean uses(final Use u) {
    return
      u == Use.CNS && def == Function.PARSEXML ||
      u == Use.UPD && def == Function.PUT ||
      u == Use.X30 && (def == Function.DATA && expr.length == 0 ||
        def == Function.PARSETXT || def == Function.PARSETXTLIN ||
        def == Function.PARSETXTAVL || def == Function.PARSEXML ||
        def == Function.URICOLL || def == Function.SERIALIZE) ||
      u == Use.CTX && (def == Function.DATA && expr.length == 0 ||
        def == Function.PUT) && expr.length == 0 || super.uses(u);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return def == Function.COLL || super.iterable();
  }
}
