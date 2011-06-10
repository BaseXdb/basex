package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.SerializerException;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.TextInput;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.AtomType;
import org.basex.query.item.Str;
import org.basex.query.item.Uri;
import org.basex.query.item.Value;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.Put;
import org.basex.query.util.Err;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNGen extends Fun {
  /** Output namespace. */
  private static final Uri U_ZIP = Uri.uri(OUTPUTURI);
  /** Element: output:serialization-parameter. */
  private static final QNm E_PARAM =
    new QNm(token("serialization-parameters"), U_ZIP);

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNGen(final InputInfo ii, final FunDef f, final Expr[] e) {
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
    return def == FunDef.COLL ? collection(ctx) : super.value(ctx);
  }

  @Override
  public Expr cmp(final QueryContext ctx) {
    if(def == FunDef.DATA &&  expr.length == 1) {
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
    ctx.updates.add(new Put(input, nd, u, ctx), ctx);

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
    if(contains(in, '<') || contains(in, '>')) INVDOC.thrw(input, in);

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
    try {
      byte[] txt = TextInput.content(io, enc).finish();
      if(contains(txt, '\r')) txt = contains(txt, '\n') ?
          delete(txt, '\r') : replace(txt, '\r', '\n');
      return Str.get(txt);
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
      if(!base.valid()) DOCBASE.thrw(input, base);
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
    final ANode node = checkNode(checkItem(expr[0], ctx));
    final ArrayOutput ao = new ArrayOutput();
    try {
      // run serialization
      final XMLSerializer xml = new XMLSerializer(ao, serialPar(this, 1, ctx));
      node.serialize(xml);
      xml.close();
    } catch(final SerializerException ex) {
      throw new QueryException(input, ex);
    } catch(final IOException ex) {
      SERANY.thrw(input, ex);
    }
    return Str.get(ao.toArray());
  }

  /**
   * Creates serializer properties.
   * @param fun calling function
   * @param arg argument with parameters
   * @param ctx query context
   * @return serialization parameters
   * @throws SerializerException serializer exception
   * @throws QueryException query exception
   */
  static SerializerProp serialPar(final Fun fun, final int arg,
      final QueryContext ctx) throws SerializerException, QueryException {

    final TokenBuilder tb = new TokenBuilder();

    // check if enough arguments are available
    if(arg < fun.expr.length) {
      // retrieve parameters
      final Item it = fun.expr[arg].item(ctx, fun.input);
      if(it != null) {
        // check root node
        ANode node = (ANode) fun.checkType(it, NodeType.ELM);
        if(!node.qname().eq(E_PARAM)) SERUNKNOWN.thrw(fun.input, node.qname());

        // interpret query parameters
        final AxisIter ai = node.children();
        while((node = ai.next()) != null) {
          if(tb.size() != 0) tb.add(',');
          final QNm name = node.qname();
          if(!name.uri().eq(U_ZIP)) SERUNKNOWN.thrw(fun.input, name);
          tb.add(name.ln()).add('=').add(node.atom());
        }
      }
    }
    // use default parameters if no parameters have been assigned
    return tb.size() == 0 ? ctx.serProp(true) :
      new SerializerProp(tb.toString());
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.UPD && def == FunDef.PUT || u == Use.X30 && (
        def == FunDef.DATA && expr.length == 0 ||
        def == FunDef.PARSETXT || def == FunDef.PARSETXTLIN ||
        def == FunDef.PARSETXTAVL || def == FunDef.PARSEXML ||
        def == FunDef.URICOLL || def == FunDef.SERIALIZE) ||
        u == Use.CTX && def == FunDef.DATA && expr.length == 0 ||
        super.uses(u);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return def == FunDef.COLL || super.iterable();
  }
}
