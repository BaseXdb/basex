package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.data.Data;
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
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.Put;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
final class FNGen extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNGen(final InputInfo ii, final FunDef f, final Expr[] e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case DATA:    return data(ctx);
      case COLL:    return collection(ctx);
      case URICOLL: return uriCollection(ctx);
      default:      return super.iter(ctx);
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
      case PARSE: // might get obsolete
      case PARSEXML:    return parseXml(ctx);
      case SERIALIZE:   return serialize(ctx);
      default:          return super.item(ctx, ii);
    }
  }

  @Override
  public Expr cmp(final QueryContext ctx) {
    if(def == FunDef.DATA &&  expr.length == 1) {
      final SeqType t = expr[0].type();
      type = t.type.node() ? SeqType.get(Type.ATM, t.occ) : t;
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
        return it != null ? atom(it) : null;
      }
    };
  }

  /**
   * Performs the collection function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter collection(final QueryContext ctx) throws QueryException {
    return ctx.resource.collection(expr.length != 0 ? checkStr(expr[0], ctx) :
      null, input);
  }

  /**
   * Performs the uri-collection function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter uriCollection(final QueryContext ctx) throws QueryException {
    final Iter coll = collection(ctx);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = coll.next();
        // all items will be nodes
        return it == null ? null : Uri.uri(((Nod) it).base());
      }
    };
  }

  /**
   * Performs the put function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Item put(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    final byte[] file = checkEStr(expr[1], ctx);
    final Item it = checkNode(checkEmpty(expr[0].item(ctx, input)));

    if(it == null || it.type != Type.DOC && it.type != Type.ELM)
      UPFOTYPE.thrw(input, expr[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.valid()) UPFOURI.thrw(input, file);
    ctx.updates.add(new Put(input, (Nod) it, u, ctx.serProp()), ctx);

    return null;
  }

  /**
   * Performs the doc function.
   * @param ctx query context
   * @return resulting node
   * @throws QueryException query exception
   */
  private Nod doc(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].item(ctx, input);
    if(it == null) return null;

    final byte[] in = checkEStr(it);
    if(contains(in, '<') || contains(in, '>')) INVDOC.thrw(input, in);

    final Data d = ctx.resource.data(in, false, input);
    if(!d.single()) EXPSINGLE.thrw(input);
    return new DBNode(d, 0, Data.DOC);
  }

  /**
   * Performs the doc-available function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln docAvailable(final QueryContext ctx) throws QueryException {
    try {
      return Bln.get(doc(ctx) != null);
    } catch(final QueryException ex) {
      // catch FODC0002 and FODC0004
      if(ex.type() == Err.ErrType.FODC) return Bln.FALSE;
      throw ex;
    }
  }

  /**
   * Performs the unparsed-text function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item unparsedText(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkEStr(expr[1], ctx));
    try {
      return Str.get(TextInput.content(io, enc).finish());
    } catch(final IOException ex) {
      UNDEF.thrw(input, ex);
      return null;
    }
  }

  /**
   * Performs the unparsed-text-available function.
   * @param ctx query context
   * @return resulting item
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
   * @return resulting item
   * @throws QueryException query exception
   */
  private Nod parseXml(final QueryContext ctx) throws QueryException {
    final byte[] cont = checkEStr(expr[0], ctx);
    Uri base = ctx.baseURI;
    if(expr.length == 2) {
      base = Uri.uri(checkEStr(expr[1], ctx));
      if(!base.valid()) DOCBASE.thrw(input, base);
    }

    final Prop prop = ctx.context.prop;
    final IO io = new IOContent(cont, string(base.atom()));
    try {
      final Parser p = Parser.fileParser(io, prop, "");
      return new DBNode(MemBuilder.build(p, prop, ""), 0);
    } catch(final IOException ex) {
      DOCWF.thrw(input, ex.toString());
      return null;
    }
  }

  /**
   * Performs the serialize function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final Nod nod = checkNode(checkItem(expr[0], ctx));
    final ArrayOutput ao = new ArrayOutput();
    try {
      // run serialization
      final XMLSerializer xml = new XMLSerializer(ao, serialPar(this, 1, ctx));
      nod.serialize(xml);
      xml.close();
    } catch(final IOException ex) {
      UNDEF.thrw(input, ex.toString());
    }
    return Str.get(ao.toArray());
  }

  /**
   * Creates a serializer.
   * @param fun calling function
   * @param arg argument with parameters
   * @param ctx query context
   * @return serialization parameters
   * @throws QueryException query exception
   */
  static SerializerProp serialPar(final Fun fun, final int arg,
      final QueryContext ctx) throws QueryException {

    if(arg >= fun.expr.length) return null;

    // interpret query parameters
    final TokenBuilder tb = new TokenBuilder();
    final Iter ir = fun.expr[arg].iter(ctx);
    Item it;
    while((it = ir.next()) != null) {
      final Nod n = fun.checkNode(it);
      if(tb.size() != 0) tb.add(',');
      tb.add(n.nname()).add('=').add(n.atom());
    }
    try {
      return new SerializerProp(tb.toString());
    } catch(final IOException ex) {
      UNDEF.thrw(fun.input, ex.toString());
      return null;
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.UPD && def == FunDef.PUT || u == Use.X30 && (
        def == FunDef.DATA && expr.length == 0 ||
        def == FunDef.PARSE || def == FunDef.PARSETXT ||
        def == FunDef.PARSETXTAVL || def == FunDef.PARSEXML ||
        def == FunDef.URICOLL || def == FunDef.SERIALIZE) ||
        u == Use.CTX && def == FunDef.DATA && expr.length == 0 ||
        super.uses(u);
  }

  @Override
  public boolean duplicates() {
    return def != FunDef.COLL && super.duplicates();
  }
}
