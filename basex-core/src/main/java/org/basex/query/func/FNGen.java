package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.core.Context;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.util.Err.ErrType;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

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
      case DATA:                return dataa(ctx);
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
      case UNPARSED_TEXT:           return unparsedText(ctx, false);
      case UNPARSED_TEXT_AVAILABLE: return unparsedText(ctx, true);
      case PUT:                     return put(ctx);
      case PARSE_XML:               return parseXml(ctx, false);
      case PARSE_XML_FRAGMENT:      return parseXml(ctx, true);
      case SERIALIZE:               return serialize(ctx);
      default:                      return super.item(ctx, ii);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case COLLECTION: return collection(ctx);
      default:         return super.value(ctx);
    }
  }

  @Override
  protected Expr opt(final QueryContext ctx) throws QueryException {
    if(sig == Function.DATA && expr.length == 1) {
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
  private Iter dataa(final QueryContext ctx) throws QueryException {
    final Iter ir = ctx.iter(expr.length != 0 ? expr[0] : checkCtx(ctx));

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        if(it instanceof FItem) FIATOM.thrw(info, FNGen.this);
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
    final ANode nd = checkNode(expr[0], ctx);
    if(nd.type != NodeType.DOC && nd.type != NodeType.ELM) UPFOTYPE.thrw(info, expr[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.isValid()) UPFOURI.thrw(info, file);
    final DBNode target = ctx.updates.determineDataRef(nd, ctx);

    final String uri = IO.get(u.toJava()).path();
    // check if all target paths are unique
    if(!ctx.updates.putPaths.add(uri)) UPURIDUP.thrw(info, uri);

    ctx.updates.add(new Put(info, target.pre, target.data, uri), ctx);
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
    return ctx.resource.doc(new QueryInput(string(in)), info);
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
   * @param check only check if text is available
   * @return content
   * @throws QueryException query exception
   */
  private Item unparsedText(final QueryContext ctx, final boolean check)
      throws QueryException {

    checkCreate(ctx);
    final byte[] path = checkStr(expr[0], ctx);
    final IO base = ctx.sc.baseIO();
    if(base == null) throw STBASEURI.thrw(info);

    String enc = null;
    try {
      enc = encoding(1, WHICHENC, ctx);

      final String p = string(path);
      if(p.indexOf('#') != -1) FRAGID.thrw(info, p);
      if(!Uri.uri(p).isValid()) INVURL.thrw(info, p);

      IO io = base.merge(p);
      final String[] rp = ctx.resource.resources.get(io.path());
      if(rp != null && rp.length > 0) {
        io = IO.get(rp[0]);
        if(rp.length > 1) enc = rp[1];
      }
      if(!io.exists()) RESNF.thrw(info, p);

      final InputStream is = io.inputStream();
      try {
        final TextInput ti = new TextInput(io).encoding(enc).validate(true);
        if(!check) return Str.get(ti.content());
        while(ti.read() != -1);
        return Bln.TRUE;
      } finally {
        is.close();
      }
    } catch(final QueryException ex) {
      if(check && ex.err().type != ErrType.XPTY) return Bln.FALSE;
      throw ex;
    } catch(final IOException ex) {
      if(check) return Bln.FALSE;
      if(ex instanceof InputException) {
        if(ex instanceof EncodingException || enc != null) INVCHARS.thrw(info, ex);
        else WHICHCHARS.thrw(info, ex);
      }
      throw RESNF.thrw(info, path);
    }
  }

  /**
   * Performs the unparsed-text-lines function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  Iter unparsedTextLines(final QueryContext ctx) throws QueryException {
    return textIter(unparsedText(ctx, false).string(info));
  }

  /**
   * Returns the specified text as lines.
   * @param str text input
   * @return result
   */
  static Iter textIter(final byte[] str) {
    // not I/O exception expected, as input is a main-memory array
    try {
      final NewlineInput nli = new NewlineInput(new ArrayInput(str));
      final TokenBuilder tb = new TokenBuilder();
      return new Iter() {
        @Override
        public Item next() {
          try {
            return nli.readLine(tb) ? Str.get(tb.finish()) : null;
          } catch(final IOException ex) {
            throw Util.notexpected(ex);
          }
        }
      };
    } catch(final IOException ex) {
      throw Util.notexpected(ex);
    }
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param ctx query context
   * @param frag parse fragment
   * @return result
   * @throws QueryException query exception
   */
  private ANode parseXml(final QueryContext ctx, final boolean frag)
      throws QueryException {

    final Item item = expr[0].item(ctx, info);
    if(item == null) return null;
    try {
      final IO io = new IOContent(checkStr(item), string(ctx.sc.baseURI().string()));
      return parseXml(io, ctx.context, frag);
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
    Item it = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final SerializerProp props = FuncParams.serializerProp(it, info);
    return Str.get(serialize(expr[0].iter(ctx), props));
  }

  @Override
  public boolean has(final Flag flag) {
    return (flag == Flag.X30 || flag == Flag.CTX) && sig == DATA && expr.length == 0 ||
        super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(oneOf(sig, DATA) && expr.length == 0) {
      if(!visitor.lock(DBLocking.CTX)) return false;
    } else if(oneOf(sig, DOC_AVAILABLE, DOC, COLLECTION, URI_COLLECTION)) {
      if(expr.length == 0) {
        if(oneOf(sig, COLLECTION, URI_COLLECTION) && !visitor.lock(DBLocking.COLL))
          return false;
      } else if(!(expr[0] instanceof Str)) {
        if(!visitor.lock(null)) return false;
      } else {
        final QueryInput qi = new QueryInput(string(((Str) expr[0]).string()));
        if(qi.db == null && !visitor.lock(null)) return false;
        if(!visitor.lock(qi.db)) return false;
      }
    }
    return super.accept(visitor);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return sig == COLLECTION || super.iterable();
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param input string to be parsed
   * @param ctx query context
   * @param frag parse fragment
   * @return result
   * @throws IOException I/O exception
   */
  public static ANode parseXml(final IO input, final Context ctx, final boolean frag)
      throws IOException {

    final Prop prop = ctx.prop;
    final boolean chop = prop.is(Prop.CHOP);
    try {
      prop.set(Prop.CHOP, false);
      return new DBNode(frag || prop.is(Prop.INTPARSE) ?
        new XMLParser(input, ctx.prop, frag) : new SAXWrapper(input, ctx.prop));
    } finally {
      prop.set(Prop.CHOP, chop);
    }
  }
}
