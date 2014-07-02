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
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.util.Err.ErrType;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNGen extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNGen(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DATA:                return data(ctx);
      case COLLECTION:          return collection(ctx).iter();
      case URI_COLLECTION:      return uriCollection(ctx);
      case UNPARSED_TEXT_LINES: return unparsedTextLines(ctx);
      default:                  return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
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
    switch(func) {
      case COLLECTION: return collection(ctx);
      default:         return super.value(ctx);
    }
  }

  @Override
  protected Expr opt(final QueryContext ctx, final VarScope scp) {
    if(func == DATA && exprs.length == 1) {
      final SeqType t = exprs[0].type();
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
    final Iter ir = ctx.iter(exprs.length == 0 ? checkCtx(ctx) : exprs[0]);

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        if(it instanceof FItem) throw FIATOM.get(info, it.type);
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
    final Item it = exprs.length == 0 ? null : exprs[0].item(ctx, info);
    if(it == null) return ctx.resources.collection(info);

    // check if reference is valid
    final byte[] in = checkEStr(it);
    if(!Uri.uri(in).isValid()) throw INVCOLL.get(info, in);
    return ctx.resources.collection(new QueryInput(string(in)), sc.baseIO(), info);
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
    final byte[] file = checkEStr(exprs[1], ctx);
    final ANode nd = checkNode(exprs[0], ctx);
    if(nd.type != NodeType.DOC && nd.type != NodeType.ELM) throw UPFOTYPE.get(info, exprs[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.isValid()) throw UPFOURI.get(info, file);
    final Updates updates = ctx.resources.updates();
    final DBNode target = updates.determineDataRef(nd, ctx);

    final String uri = IO.get(string(u.string())).path();
    // check if all target paths are unique
    if(!updates.putPaths.add(uri)) throw UPURIDUP.get(info, uri);

    updates.add(new Put(target.pre, target.data, uri, info), ctx);
    return null;
  }

  /**
   * Performs the doc function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private ANode doc(final QueryContext ctx) throws QueryException {
    final Item it = exprs[0].item(ctx, info);
    if(it == null) return null;
    final byte[] in = checkEStr(it);
    if(!Uri.uri(in).isValid()) throw INVDOC.get(info, in);
    return ctx.resources.doc(new QueryInput(string(in)), sc.baseIO(), info);
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
      if(err != null) {
        final String num = err.code.length() == 8 ? err.code.substring(4) : "";
        if(err.is(ErrType.FODC) && (num.equals("0002") || num.equals("0004")) ||
           err.is(ErrType.BXDB) && num.equals("0006")) return Bln.FALSE;
      }
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
  private Item unparsedText(final QueryContext ctx, final boolean check) throws QueryException {
    checkCreate(ctx);
    final byte[] path = checkStr(exprs[0], ctx);
    final IO base = sc.baseIO();
    if(base == null) throw STBASEURI.get(info);

    String enc = null;
    try {
      enc = encoding(1, WHICHENC, ctx);

      final String p = string(path);
      if(p.indexOf('#') != -1) throw FRAGID.get(info, p);
      if(!Uri.uri(p).isValid()) throw INVURL.get(info, p);

      IO io = base.merge(p);
      final String[] rp = ctx.resources.texts.get(io.path());
      if(rp != null && rp.length > 0) {
        io = IO.get(rp[0]);
        if(rp.length > 1) enc = rp[1];
      }
      if(!io.exists()) throw RESNF.get(info, p);

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
      if(check && !ex.err().is(ErrType.XPTY)) return Bln.FALSE;
      throw ex;
    } catch(final IOException ex) {
      if(check) return Bln.FALSE;
      if(ex instanceof InputException) {
        final boolean inv = ex instanceof EncodingException || enc != null;
        throw (inv ? INVCHARS : WHICHCHARS).get(info, ex);
      }
      throw RESNF.get(info, path);
    }
  }

  /**
   * Performs the unparsed-text-lines function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter unparsedTextLines(final QueryContext ctx) throws QueryException {
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
            throw Util.notExpected(ex);
          }
        }
      };
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param ctx query context
   * @param frag parse fragment
   * @return result
   * @throws QueryException query exception
   */
  private ANode parseXml(final QueryContext ctx, final boolean frag) throws QueryException {
    final Item item = exprs[0].item(ctx, info);
    if(item == null) return null;
    try {
      final IO io = new IOContent(checkStr(item), string(sc.baseURI().string()));
      return parseXml(io, ctx.context, frag);
    } catch(final IOException ex) {
      throw SAXERR.get(info, ex);
    }
  }

  /**
   * Performs the serialize function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final Item it = exprs.length > 1 ? exprs[1].item(ctx, info) : null;
    final SerializerOptions sopts = FuncOptions.serializer(it, info);
    return Str.get(serialize(exprs[0].iter(ctx), sopts, SERANY));
  }

  @Override
  public boolean has(final Flag flag) {
    return (flag == Flag.X30 || flag == Flag.CTX) && func == DATA && exprs.length == 0 ||
        super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(oneOf(func, DATA) && exprs.length == 0) {
      if(!visitor.lock(DBLocking.CTX)) return false;
    } else if(oneOf(func, DOC_AVAILABLE, DOC, COLLECTION, URI_COLLECTION)) {
      if(exprs.length == 0) {
        if(oneOf(func, COLLECTION, URI_COLLECTION) && !visitor.lock(DBLocking.COLL))
          return false;
      } else if(!(exprs[0] instanceof Str)) {
        if(!visitor.lock(null)) return false;
      } else {
        final QueryInput qi = new QueryInput(string(((Str) exprs[0]).string()));
        if(qi.db == null && !visitor.lock(null)) return false;
        if(!visitor.lock(qi.db)) return false;
      }
    }
    return super.accept(visitor);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return func == COLLECTION || super.iterable();
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

    final MainOptions opts = ctx.options;
    final boolean chop = opts.get(MainOptions.CHOP);
    try {
      opts.set(MainOptions.CHOP, false);
      return new DBNode(frag || opts.get(MainOptions.INTPARSE) ?
        new XMLParser(input, ctx.options, frag) : new SAXWrapper(input, ctx.options));
    } finally {
      opts.set(MainOptions.CHOP, chop);
    }
  }
}
