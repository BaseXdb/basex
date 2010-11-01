package org.basex.query;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTOptions.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.data.SerializerProp;
import org.basex.io.IO;
import org.basex.query.expr.And;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CComm;
import org.basex.query.expr.CDoc;
import org.basex.query.expr.CElem;
import org.basex.query.expr.CPI;
import org.basex.query.expr.CTxt;
import org.basex.query.expr.Arith;
import org.basex.query.expr.OrderByExpr;
import org.basex.query.expr.OrderByStable;
import org.basex.query.expr.TypeCase;
import org.basex.query.expr.Cast;
import org.basex.query.expr.Castable;
import org.basex.query.expr.Catch;
import org.basex.query.expr.Calc;
import org.basex.query.expr.CmpG;
import org.basex.query.expr.CmpN;
import org.basex.query.expr.CmpV;
import org.basex.query.expr.Context;
import org.basex.query.expr.Except;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Extension;
import org.basex.query.expr.FLWOR;
import org.basex.query.expr.FLWR;
import org.basex.query.expr.For;
import org.basex.query.expr.ForLet;
import org.basex.query.expr.Func;
import org.basex.query.expr.GFLWOR;
import org.basex.query.expr.Group;
import org.basex.query.expr.If;
import org.basex.query.expr.Instance;
import org.basex.query.expr.InterSect;
import org.basex.query.expr.Let;
import org.basex.query.expr.List;
import org.basex.query.expr.Or;
import org.basex.query.expr.OrderBy;
import org.basex.query.expr.Order;
import org.basex.query.expr.Pragma;
import org.basex.query.expr.Filter;
import org.basex.query.expr.Range;
import org.basex.query.expr.Root;
import org.basex.query.expr.Quantifier;
import org.basex.query.expr.Scored;
import org.basex.query.expr.Switch;
import org.basex.query.expr.Treat;
import org.basex.query.expr.Try;
import org.basex.query.expr.TypeSwitch;
import org.basex.query.expr.Unary;
import org.basex.query.expr.Union;
import org.basex.query.expr.VarRef;
import org.basex.query.ft.FTAnd;
import org.basex.query.ft.FTContains;
import org.basex.query.ft.FTContent;
import org.basex.query.ft.FTExtensionSelection;
import org.basex.query.ft.FTScope;
import org.basex.query.ft.FTDistance;
import org.basex.query.ft.FTExpr;
import org.basex.query.ft.FTMildNot;
import org.basex.query.ft.FTNot;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.FTOptions;
import org.basex.query.ft.FTOr;
import org.basex.query.ft.FTOrder;
import org.basex.query.ft.FTWeight;
import org.basex.query.ft.FTWindow;
import org.basex.query.ft.FTWords;
import org.basex.query.ft.ThesQuery;
import org.basex.query.ft.Thesaurus;
import org.basex.query.ft.FTWords.FTMode;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Empty;
import org.basex.query.item.Itr;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.SeqType.Occ;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.KindTest;
import org.basex.query.path.MixedPath;
import org.basex.query.path.NameTest;
import org.basex.query.path.AxisStep;
import org.basex.query.path.Test;
import org.basex.query.up.Delete;
import org.basex.query.up.Insert;
import org.basex.query.up.Rename;
import org.basex.query.up.Replace;
import org.basex.query.up.Transform;
import org.basex.query.util.Err;
import org.basex.query.util.Namespaces;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.Atts;
import org.basex.util.InputParser;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.XMLToken;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.StopWords;
import org.basex.util.ft.FTLexer.FTUnit;

/**
 * Simple query parser; can be overwritten to support more complex parsings.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class QueryParser extends InputParser {
  /** Query context. */
  public final QueryContext ctx;

  /** Temporary token builder. */
  private final TokenBuilder tok = new TokenBuilder();
  /** List of loaded modules. */
  private final TokenList modLoaded = new TokenList();
  /** Module name. */
  private QNm module;

  /** Alternative error output. */
  private Err alter;
  /** Alternative error description. */
  private QNm alterFunc;
  /** Alternative position. */
  private int ap;

  /** Declared serialization options. */
  private final StringList serial = new StringList();
  /** Declaration flag. */
  private boolean declElem;
  /** Declaration flag. */
  private boolean declFunc;
  /** Declaration flag. */
  private boolean declColl;
  /** Declaration flag. */
  private boolean declConstr;
  /** Declaration flag. */
  private boolean declSpaces;
  /** Declaration flag. */
  private boolean declOrder;
  /** Declaration flag. */
  private boolean declReval;
  /** Declaration flag. */
  private boolean declGreat;
  /** Declaration flag. */
  private boolean declPres;
  /** Declaration flag. */
  private boolean declBase;

  /***
   * Constructor.
   * @param q query
   * @param c query context
   */
  public QueryParser(final String q, final QueryContext c) {
    super(q);
    ctx = c;
    file = c.file();
  }

  /**
   * Parses the specified query.
   * If {@code u != null}, the query is treated as a module
   * @param f optional input file
   * @param u module uri
   * @return resulting expression
   * @throws QueryException query exception
   */
  public final Expr parse(final IO f, final Uri u) throws QueryException {
    file = f;
    if(!more()) error(QUERYEMPTY);
    final int v = valid();
    if(v != -1) {
      qp = v; // correct position for error message
      error(QUERYINV, query.codePointAt(v));
    }
    return parse(u, true);
  }

  /**
   * Parses the specified query.
   * If {@code u != null}, the query is treated as a module
   * [  1] Parses a Module.
   * @param u module uri
   * @param c if true, input must be completely evaluated
   * @return resulting expression
   * @throws QueryException query exception
   */
  public final Expr parse(final Uri u, final boolean c) throws QueryException {
    Expr expr = null;
    try {
      versionDecl();
      if(u == null) {
        expr = mainModule();
        if(expr == null) if(alter != null) error(); else error(EXPREMPTY);
      } else {
        moduleDecl(u);
      }

      if(c && more()) {
        if(alter != null) error();
        error(QUERYEND, rest());
      }
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
    ctx.funcs.check();
    ctx.vars.check();
    ctx.ns.finish(ctx.nsElem);
    return expr;
  }

  /**
   * [  2] Parses a VersionDecl.
   * @throws QueryException query exception
   */
  private void versionDecl() throws QueryException {
    final int p = qp;
    if(!consumeWS(XQUERY) || !consumeWS2(VERSION)) {
      qp = p;
      return;
    }
    final String ver = string(stringLiteral());

    final byte[] enc = consumeWS2(ENCODING) ? lc(stringLiteral()) : null;
    if(enc != null) {
      // actual encoding is ignored, as input comes in as string
      boolean v = true;
      for(final byte e : enc) v &= ftChar(e) || e == '-';
      if(!v) error(XQUERYENC2, enc);
    }
    skipWS();
    check(';');
    if(ver.equals(XQ10)) ctx.xquery30 = false;
    else if(ver.equals(XQ11) || ver.equals(XQ30)) ctx.xquery30 = true;
    else error(XQUERYVER, ver);
  }

  /**
   * [  3] Parses a MainModule.
   * [  7] Parses a Setter.
   * [ 30] Parses a QueryBody ( = Expr).
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr mainModule() throws QueryException {
    prolog1();
    prolog2();
    if(declColl) {
      final byte[] coll = ctx.baseURI.resolve(ctx.collation).atom();
      if(!eq(URLCOLL, coll)) error(COLLWHICH, coll);
    }
    return expr();
  }

  /**
   * [  4] Parses a ModuleDecl.
   * @param u module uri
   * @throws QueryException query exception
   */
  private void moduleDecl(final Uri u) throws QueryException {
    check(MODULE);
    check(NSPACE);
    module = new QNm(ncName(XPNAME));
    check(IS);
    module.uri(stringLiteral());
    if(module.uri() == Uri.EMPTY) error(NSMODURI);
    // skip uri check for empty input uri...
    if(u != Uri.EMPTY && !u.eq(module.uri()))
      error(WRONGMODULE, module.uri(), file);
    ctx.ns.add(module, input());
    skipWS();
    check(';');
    prolog1();
    prolog2();
  }

  /**
   * [  6] Parses a Prolog.
   * [  7] Parses a Setter.
   * @throws QueryException query exception
   */
  private void prolog1() throws QueryException {
    while(true) {
      final int p = qp;
      if(consumeWS(DECLARE)) {
        if(consumeWS(DEFAULT)) {
          if(!defaultNamespaceDecl() && !defaultCollationDecl() &&
              !emptyOrderDecl()) error(DECLINCOMPLETE);
        } else if(consumeWS(BOUNDARY)) {
          boundarySpaceDecl();
        } else if(consumeWS(BASEURI)) {
          baseURIDecl();
        } else if(consumeWS(CONSTRUCTION)) {
          constructionDecl();
        } else if(consumeWS(ORDERING)) {
          orderingModeDecl();
        } else if(consumeWS(REVALIDATION)) {
          revalidationDecl();
        } else if(consumeWS(COPYNS)) {
          copyNamespacesDecl();
        } else if(consumeWS(NSPACE)) {
          namespaceDecl();
        } else if(consumeWS(FTOPTION)) {
          final FTOpt opt = new FTOpt(ctx.context.prop);
          while(ftMatchOption(opt));
          ctx.ftopt.init(opt);
        } else {
          qp = p;
          return;
        }
      } else if(consumeWS(IMPORT)) {
        if(consumeWS(SCHEMA)) {
          schemaImport();
        } else if(consumeWS(MODULE)) {
          moduleImport();
        } else {
          qp = p;
          return;
        }
      } else {
        return;
      }
      skipWS();
      check(';');
    }
  }

  /**
   * [  6] Parses a Prolog.
   * [  7] Parses a Setter.
   * @throws QueryException query exception
   */
  private void prolog2() throws QueryException {
    while(true) {
      final int p = qp;
      if(!consumeWS(DECLARE)) return;

      if(consumeWS(VARIABLE)) {
        varDecl();
      } else if(consumeWS(UPDATING)) {
        ctx.updating = true;
        check(FUNCTION);
        functionDecl(true);
      } else if(consumeWS(FUNCTION)) {
        functionDecl(false);
      } else if(consumeWS(OPTION)) {
        optionDecl();
      } else if(consumeWS(DEFAULT)) {
        error(PROLOGORDER);
      } else {
        qp = p;
        return;
      }
      skipWS();
      check(';');
    }
  }

  /**
   * [ 10] Parses a namespaceDecl.
   * @throws QueryException query exception
   */
  private void namespaceDecl() throws QueryException {
    final QNm name = new QNm(ncName(XPNAME));
    check(IS);
    name.uri(stringLiteral());
    if(ctx.ns.find(name.ln()) != null) error(DUPLNSDECL, name);
    ctx.ns.add(name, input());
  }

  /**
   * [UP141] Parses a RevalidationDecl.
   * @throws QueryException query exception
   */
  private void revalidationDecl() throws QueryException {
    if(declReval) error(DUPLREVAL);
    declReval = consumeWS2(STRICT) || consumeWS2(LAX) || consumeWS2(SKIP);
    error(NOREVAL);
  }

  /**
   * [ 11] Parses a BoundarySpaceDecl.
   * @throws QueryException query exception
   */
  private void boundarySpaceDecl() throws QueryException {
    if(declSpaces) error(DUPLBOUND);
    final boolean spaces = consumeWS2(PRESERVE);
    if(!spaces) check(STRIP);
    ctx.spaces = spaces;
    declSpaces = true;
  }

  /**
   * [ 12] Parses a DefaultNamespaceDecl.
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean defaultNamespaceDecl() throws QueryException {
    final boolean elem = consumeWS(ELEMENT);
    if(!elem && !consumeWS(FUNCTION)) return false;
    check(NSPACE);
    final byte[] ns = stringLiteral();
    if(elem) {
      if(declElem) error(DUPLNS);
      ctx.nsElem = ns;
      declElem = true;
    } else {
      if(declFunc) error(DUPLNS);
      ctx.nsFunc = ns;
      declFunc = true;
    }
    return true;
  }

  /**
   * [ 13] Parses an OptionDecl.
   * @throws QueryException query exception
   */
  private void optionDecl() throws QueryException {
    // ignore option declarations
    skipWS();
    final QNm name = new QNm(qName(QNAMEINV), ctx, input());
    final byte[] val = stringLiteral();
    if(!name.ns()) error(NSMISS, name);

    // output declaration
    if(ctx.xquery30 && eq(name.pref(), OUTPUT)) {
      final String key = string(name.ln());
      if(module != null) error(MODOUT);

      if(ctx.serProp == null) ctx.serProp = new SerializerProp();
      if(ctx.serProp.get(key) == null) error(OUTWHICH, key);
      if(serial.contains(key)) error(OUTDUPL, key);

      ctx.serProp.set(key, string(val));
      serial.add(key);
    }
  }

  /**
   * [ 14] Parses an OrderingModeDecl.
   * @throws QueryException query exception
   */
  private void orderingModeDecl() throws QueryException {
    if(declOrder) error(DUPLORD);
    final boolean ordered = consumeWS2(ORDERED);
    if(!ordered) check(UNORDERED);
    ctx.ordered = ordered;
    declOrder = true;
  }

  /**
   * [ 15] Parses an emptyOrderDecl.
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean emptyOrderDecl() throws QueryException {
    if(!consumeWS2(ORDER)) return false;
    check(EMPTYORD);
    if(declGreat) error(DUPLORDEMP);
    final boolean order = consumeWS2(GREATEST);
    if(!order) check(LEAST);
    ctx.orderGreatest = order;
    declGreat = true;
    return true;
  }

  /**
   * [ 16] Parses a copyNamespacesDecl.
   * [ 17] Parses an PreserveMode.
   * [ 18] Parses an InheritMode.
   * @throws QueryException query exception
   */
  private void copyNamespacesDecl() throws QueryException {
    if(declPres) error(DUPLCOPYNS);
    boolean nsp = consumeWS(PRESERVE);
    if(!nsp) check(NOPRESERVE);
    ctx.nsPreserve = nsp;
    declPres = true;
    consume(',');
    nsp = consumeWS(INHERIT);
    if(!nsp) check(NOINHERIT);
    ctx.nsInherit = nsp;
  }

  /**
   * [ 19] Parses a DefaultCollationDecl.
   * @return query expression
   * @throws QueryException query exception
   */
  private boolean defaultCollationDecl() throws QueryException {
    if(!consumeWS2(COLLATION)) return false;
    ctx.collation = Uri.uri(stringLiteral());
    if(declColl) error(DUPLCOLL);
    declColl = true;
    return true;
  }

  /**
   * [ 20] Parses a BaseURIDecl.
   * @throws QueryException query exception
   */
  private void baseURIDecl() throws QueryException {
    if(declBase) error(DUPLBASE);
    ctx.baseURI = Uri.uri(stringLiteral());
    declBase = true;
  }

  /**
   * [ 21] Parses a SchemaImport.
   * [ 22] Parses a SchemaPrefix.
   * @throws QueryException query exception
   */
  private void schemaImport() throws QueryException {
    if(consumeWS(NSPACE)) {
      ncName(XPNAME);
      check(IS);
    } else if(consumeWS(DEFAULT)) {
      check(ELEMENT);
      check(NSPACE);
    }
    final byte[] ns = stringLiteral();
    if(ns.length == 0) error(NSEMPTY);
    if(consumeWS(AT)) do stringLiteral(); while(consumeWS(COMMA));
    error(IMPLSCHEMA);
  }

  /**
   * [ 23] Parses a ModuleImport.
   * @throws QueryException query exception
   */
  private void moduleImport() throws QueryException {
    QNm name = null;
    if(consumeWS(NSPACE)) {
      name = new QNm(ncName(XPNAME));
      check(IS);
    } else {
      name = new QNm();
    }
    final byte[] uri = stringLiteral();
    if(uri.length == 0) error(NSMODURI);
    name.uri(uri);
    ctx.ns.add(name, input());

    final TokenList fl = new TokenList();
    if(consumeWS(AT)) do fl.add(stringLiteral()); while(consumeWS(COMMA));

    if(modLoaded.contains(uri)) error(DUPLMODULE, name.uri());
    try {
      if(fl.size() == 0) {
        boolean found = false;
        final int ns = ctx.modules.size();
        for(int n = 0; n < ns; n += 2) {
          if(ctx.modules.get(n).equals(string(uri))) {
            module(ctx.modules.get(n + 1), name.uri());
            modLoaded.add(uri);
            found = true;
          }
        }
        if(!found) error(NOMODULE, uri);
      }
      for(int n = 0; n < fl.size(); ++n) {
        module(string(fl.get(n)), name.uri());
        modLoaded.add(uri);
      }
    } catch(final StackOverflowError ex) {
      error(CIRCMODULE);
    }
  }

  /**
   * Parses the specified module.
   * @param f file name
   * @param u module uri
   * @throws QueryException query exception
   */
  private void module(final String f, final Uri u) throws QueryException {
    if(ctx.modLoaded.contains(f)) return;
    // check specified path and path relative to query file
    IO fl = IO.get(f);
    if(!fl.exists() && file != null) fl = file.merge(f);

    String qu = null;
    try {
      qu = string(fl.content());
    } catch(final IOException ex) {
      error(NOMODULEFILE, fl);
    }

    final Namespaces ns = ctx.ns;
    ctx.ns = new Namespaces();
    new QueryParser(qu, ctx).parse(fl, u);
    ctx.ns = ns;
    ctx.modLoaded.add(f);
  }

  /**
   * [ 24] Parses a VarDecl.
   * @throws QueryException query exception
   */
  private void varDecl() throws QueryException {
    final QNm name = varName();
    if(module != null && !name.uri().eq(module.uri())) error(MODNS, name);

    final SeqType t = consumeWS(AS) ? sequenceType() : null;
    final Var v = new Var(input(), name, t);

    // check if variable has already been declared
    final Var o = ctx.vars.get(v);
    // throw no error if a variable has been externally bound
    if(o != null && o.declared) error(VARDEFINE, o);
    (o != null ? o : v).declared = true;

    if(consumeWS2(EXTERNAL)) {
      if(o != null && t != null) {
        // bind value with new type
        o.type = t;
        o.value = null;
      }
      // bind default value
      if(ctx.xquery30 && consumeWS(ASSIGN)) {
        v.bind(check(single(), VARMISSING), ctx);
      }
    } else {
      check(ASSIGN);
      v.bind(check(single(), VARMISSING), ctx);
    }

    // bind variable if not done yet
    if(o == null) ctx.vars.setGlobal(v);
  }

  /**
   * [ 25] Parses a ConstructionDecl.
   * @throws QueryException query exception
   */
  private void constructionDecl() throws QueryException {
    if(declConstr) error(DUPLCONS);
    final boolean cons = consumeWS2(PRESERVE);
    if(!cons) check(STRIP);
    ctx.construct = cons;
    declConstr = true;
  }

  /**
   * [ 26] Parses a FunctionDecl.
   * @param up updating flag
   * @throws QueryException query exception
   */
  private void functionDecl(final boolean up) throws QueryException {
    skipWS();
    final QNm name = new QNm(qName(FUNCNAME));
    name.uri(name.ns() ? ctx.ns.uri(name.pref(), false, input()) : ctx.nsFunc);
    if(Type.find(name, false) != null) error(FUNCRES, name);
    if(module != null && !name.uri().eq(module.uri())) error(MODNS, name);

    check(PAR1);
    skipWS();
    Var[] args = {};
    final int s = ctx.vars.size();
    while(curr() == '$') {
      final QNm arg = varName();
      final SeqType argType = consumeWS(AS) ? sequenceType() : null;
      final Var var = new Var(input(), arg, argType);
      ctx.vars.add(var);
      for(final Var v : args) if(v.name.eq(arg)) error(FUNCDUPL, arg.atom());

      args = Array.add(args, var);
      if(!consume(',')) break;
      skipWS();
    }
    check(PAR2);

    final SeqType type = consumeWS(AS) ? sequenceType() : null;
    final Func func = new Func(input(),
        new Var(input(), name, type), args, true);
    func.updating = up;

    ctx.funcs.add(func, this);
    if(!consumeWS(EXTERNAL)) func.expr = enclosed(NOFUNBODY);
    ctx.vars.reset(s);
  }

  /**
   * [ 29] Parses an EnclosedExpr.
   * @param err error message
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr enclosed(final Err err) throws QueryException {
    check(BRACE1);
    final Expr e = check(expr(), err);
    check(BRACE2);
    return e;
  }

  /**
   * [ 31] Parses an Expr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr expr() throws QueryException {
    final Expr e = single();
    if(e == null) {
      if(more()) return null;
      if(alter != null) error(); else error(NOEXPR);
    }

    if(!consumeWS2(COMMA)) return e;
    Expr[] l = { e };
    do l = add(l, single()); while(consumeWS2(COMMA));
    return new List(input(), l);
  }

  /**
   * [ 32] Parses an ExprSingle.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr single() throws QueryException {
    alter = null;
    Expr e = flwor();
    if(e == null) e = quantified();
    if(e == null) e = switchh();
    if(e == null) e = typeswitch();
    if(e == null) e = iff();
    if(e == null) e = tryCatch();
    if(e == null) e = insert();
    if(e == null) e = deletee();
    if(e == null) e = rename();
    if(e == null) e = replace();
    if(e == null) e = transform();
    if(e == null) e = or();
    return e;
  }

  /**
   * [ 33] Parses a FLWORExpr.
   * [ 37] Parses a WhereClause.
   * [ 38] Parses an OrderByClause.
   * [ 39] Parses an OrderSpecList.
   * [ 57]* Parses GroupByClause
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr flwor() throws QueryException {
    final int s = ctx.vars.size();

    final ForLet[] fl = forLet();
    if(fl == null) return null;

    Expr where = null;
    if(consumeWS(WHERE)) {
      ap = qp;
      where = check(single(), NOWHERE);
      alter = NOWHERE;
    }

    Var[] group = null;
    if(ctx.xquery30 && consumeWS(GROUP)) {
      check(BY);
      ap = qp;
      do group = groupSpec(group); while(consumeWS2(COMMA));
      alter = GRPBY;
    }

    OrderBy[] order = null;
    final boolean stable = consumeWS(STABLE);
    if(stable) check(ORDER);

    if(stable || consumeWS(ORDER)) {
      check(BY);
      ap = qp;
      do order = orderSpec(order); while(consumeWS2(COMMA));
      if(order != null) order = Array.add(order, new OrderByStable(input()));
      alter = ORDERBY;
    }

    if(!consumeWS(RETURN)) {
      if(alter != null) error();
      error(where == null ? FLWORWHERE : order == null ? FLWORORD : FLWORRET);
    }
    final Expr ret = check(single(), NORETURN);
    ctx.vars.reset(s);
    return ret == Empty.SEQ ? ret : order == null && group == null ?
      new FLWR(fl, where, ret, input()) : group == null ?
      new FLWOR(fl, where, new Order(input(), order), ret, input()) :
      new GFLWOR(fl, where, order == null ? null : new Order(input(), order),
        new Group(input(), group), ret, input());
  }

  /**
   * [ 34] Parses a ForClause.
   * [ 35] Parses a PositionalVar.
   * [ 36] Parses a LetClause.
   * [FT37] Parses an FTScoreVar.
   * @return query expression
   * @throws QueryException query exception
   */
  private ForLet[] forLet() throws QueryException {
    ForLet[] fl = null;
    boolean comma = false;

    do {
      final boolean fr = consumeWS(FOR, DOLLAR, NOFOR);
      boolean score = !fr && consumeWS(LET, SCORE, NOLET);
      if(score) check(SCORE);
      else if(!fr && !consumeWS(LET, DOLLAR, NOLET)) return fl;

      do {
        if(comma && !fr) score = consumeWS(SCORE);

        final QNm name = varName();
        final SeqType type = score ? SeqType.DBL :
          consumeWS(AS) ? sequenceType() : null;
        final Var var = new Var(input(), name, type);

        final Var at = fr && consumeWS(AT) ?
            new Var(input(), varName(), SeqType.ITR) : null;
        final Var sc = fr && consumeWS(SCORE) ?
            new Var(input(), varName(), SeqType.DBL) : null;

        check(fr ? IN : ASSIGN);
        final Expr e = check(single(), VARMISSING);
        ctx.vars.add(var);

        if(fl == null) fl = new ForLet[1];
        else fl = Arrays.copyOf(fl, fl.length + 1);
        if(sc != null) {
          if(sc.name.eq(name) || at != null && sc.name.eq(at.name))
            error(VARDEFINED, sc);
          ctx.vars.add(sc);
        }
        if(at != null) {
          if(name.eq(at.name)) error(VARDEFINED, at);
          ctx.vars.add(at);
        }
        fl[fl.length - 1] = fr ? new For(input(), e, var, at, sc) :
          new Let(input(), e, var, score);
        score = false;
        comma = true;
      } while(consumeWS2(COMMA));
      comma = false;
    } while(true);
  }

  /**
   * [ 40] Parses an OrderSpec.
   * [ 41] Parses an OrderModifier.
   * @param order order array
   * @return new order array
   * @throws QueryException query exception
   */
  private OrderBy[] orderSpec(final OrderBy[] order) throws QueryException {
    final Expr e = check(single(), ORDERBY);

    boolean desc = false;
    if(!consumeWS(ASCENDING)) desc = consumeWS(DESCENDING);
    boolean least = !ctx.orderGreatest;
    if(consumeWS(EMPTYORD)) {
      least = !consumeWS(GREATEST);
      if(least) check(LEAST);
    }
    if(consumeWS(COLLATION)) {
      final byte[] coll = stringLiteral();
      if(!eq(URLCOLL, coll)) error(INVCOLL, coll);
    }
    if(e.empty()) return order;
    final OrderBy ord = new OrderByExpr(input(), e, desc, least);
    return order == null ? new OrderBy[] { ord } : Array.add(order, ord);
  }

  /**
   * [59]* GroupingSpec.
   * @param group grouping specification
   * @return new group array
   * @throws QueryException query exception
   */
  private Var[] groupSpec(final Var[] group) throws QueryException {
    final Var v = new Var(input(), varName());
    if(null == ctx.vars.get(v)) error(GVARNOTDEFINED, v);
    if(consumeWS(COLLATION)) {
      final byte[] coll = stringLiteral();
      if(!eq(URLCOLL, coll)) error(INVCOLL, coll);
    }
    return group == null ? new Var[] { v } : Array.add(group, v);
  }

  /**
   * [ 42] Parses a QuantifiedExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr quantified() throws QueryException {
    final boolean some = consumeWS(SOME, DOLLAR, NOSOME);
    if(!some && !consumeWS(EVERY, DOLLAR, NOSOME)) return null;

    final int s = ctx.vars.size();
    For[] fl = {};
    do {
      final Var var = new Var(input(), varName(), consumeWS(AS) ?
          sequenceType() : null);
      check(IN);
      final Expr e = check(single(), NOSOME);
      ctx.vars.add(var);
      fl = Array.add(fl, new For(input(), e, var, null, null));
    } while(consumeWS2(COMMA));

    check(SATISFIES);
    final Expr e = check(single(), NOSOME);
    ctx.vars.reset(s);
    return new Quantifier(input(), fl, e, !some);
  }

  /**
   * [ 42] Parses a SwitchExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!consumeWS(SWITCH, PAR1, TYPEPAR)) return null;
    check(PAR1);
    Expr[] exprs = { check(expr(), NOSWITCH) };
    check(PAR2);

    // collect all cases
    Expr[] cases;
    while(true) {
      cases = new Expr[0];
      while(consumeWS(CASE)) cases = add(cases, single());
      if(cases.length == 0) break;

      check(RETURN);
      final Expr ret = single();
      for(final Expr c : cases) exprs = add(add(exprs, c), ret);
    }

    // add default case
    if(exprs.length == 1) error(WRONGEND, CASE);
    check(DEFAULT);
    check(RETURN);
    exprs = add(exprs, single());

    return new Switch(input(), exprs);
  }

  /**
   * [ 42] Parses a TypeswitchExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr typeswitch() throws QueryException {
    if(!consumeWS(TYPESWITCH, PAR1, TYPEPAR)) return null;
    check(PAR1);
    final Expr ts = check(expr(), NOTYPESWITCH);
    check(PAR2);

    TypeCase[] cases = {};
    final int s = ctx.vars.size();
    boolean cs = true;
    do {
      cs = consumeWS(CASE);
      if(!cs) check(DEFAULT);
      skipWS();
      QNm name = null;
      if(curr() == '$') {
        name = varName();
        if(cs) check(AS);
      }
      final Var var = new Var(input(), name, cs ? sequenceType() : null);
      if(name != null) ctx.vars.add(var);
      check(RETURN);
      final Expr ret = check(single(), NOTYPESWITCH);
      cases = Array.add(cases, new TypeCase(input(), var, ret));
      ctx.vars.reset(s);
    } while(cs);
    if(cases.length == 1) error(NOTYPESWITCH);
    return new TypeSwitch(input(), ts, cases);
  }

  /**
   * [ 45] Parses an IfExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr iff() throws QueryException {
    if(!consumeWS(IF, PAR1, IFPAR)) return null;
    check(PAR1);
    final Expr e = check(expr(), NOIF);
    check(PAR2);
    if(!consumeWS(THEN)) error(NOIF);
    final Expr thn = check(single(), NOIF);
    if(!consumeWS(ELSE)) error(NOIF);
    final Expr els = check(single(), NOIF);
    return new If(input(), e, thn, els);
  }

  /**
   * [ 46] Parses an OrExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr or() throws QueryException {
    final Expr e = and();
    if(!consumeWS(OR)) return e;

    Expr[] list = { e };
    do list = add(list, and()); while(consumeWS(OR));
    return new Or(input(), list);
  }

  /**
   * [ 47] Parses an AndExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr and() throws QueryException {
    final Expr e = comparison();
    if(!consumeWS(AND)) return e;

    Expr[] list = { e };
    do list = add(list, comparison()); while(consumeWS(AND));
    return new And(input(), list);
  }

  /**
   * [ 48] Parses an ComparisonExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr comparison() throws QueryException {
    final Expr e = ftContains();
    if(e != null) {
      for(final CmpV.Op c : CmpV.Op.values()) if(consumeWS(c.name))
        return new CmpV(input(), e, check(ftContains(), CMPEXPR), c);
      for(final CmpN.Op c : CmpN.Op.values()) if(consumeWS(c.name))
        return new CmpN(input(), e, check(ftContains(), CMPEXPR), c);
      for(final CmpG.Op c : CmpG.Op.values()) if(consumeWS2(c.name))
        return new CmpG(input(), e, check(ftContains(), CMPEXPR), c);
    }
    return e;
  }

  /**
   * [FT51] Parses a FTContainsExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr ftContains() throws QueryException {
    final Expr e = range();

    final int p = qp;
    if(!consumeWS(CONTAINS) || !consumeWS(TEXT)) {
      qp = p;
      return e;
    }

    // [CG] XQFT: FTIgnoreOption
    final FTExpr select = ftSelection(false);
    //Expr ignore = null;
    if(consumeWS2(WITHOUT)) {
      check(CONTENT);
      //ignore = union();
      union();
      error(FTIGNORE);
    }
    return new FTContains(e, select, input());
  }

  /**
   * [ 49] Parses a RangeExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr range() throws QueryException {
    final Expr e = additive();
    if(!consumeWS(TO)) return e;
    return new Range(input(), e, check(additive(), INCOMPLETE));
  }

  /**
   * [ 50] Parses an AdditiveExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr additive() throws QueryException {
    Expr e = multiplicative();

    while(true) {
      final Calc c = consume('+') ? Calc.PLUS : consume('-') ?
          Calc.MINUS : null;
      if(c == null) break;
      e = new Arith(input(), e, check(multiplicative(), CALCEXPR), c);
    }
    return e;
  }

  /**
   * [ 51] Parses a MultiplicativeExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr multiplicative() throws QueryException {
    Expr e = union();
    while(e != null) {
      final Calc c = consume('*') ? Calc.MULT : consumeWS(DIV) ?
        Calc.DIV : consumeWS(IDIV) ? Calc.IDIV : consumeWS(MOD) ?
        Calc.MOD : null;
      if(c == null) break;
      e = new Arith(input(), e, check(union(), CALCEXPR), c);
    }
    return e;
  }

  /**
   * [ 52] Parses a UnionExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr union() throws QueryException {
    final Expr e = intersect();
    if(!consumeWS(UNION) && !consumeWS2(PIPE)) return e;

    Expr[] list = { e };
    do list = add(list, intersect());
    while(consumeWS(UNION) || consumeWS2(PIPE));
    return new Union(input(), list);
  }

  /**
   * [ 52] Parses an IntersectExceptExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr intersect() throws QueryException {
    final Expr e = instanceoff();

    if(consumeWS(INTERSECT)) {
      Expr[] list = { e };
      do list = add(list, instanceoff()); while(consumeWS(INTERSECT));
      return new InterSect(input(), list);
    } else if(consumeWS(EXCEPT)) {
      Expr[] list = { e };
      do list = add(list, instanceoff()); while(consumeWS(EXCEPT));
      return new Except(input(), list);
    } else {
      return e;
    }
  }

  /**
   * [ 54] Parses an InstanceofExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr instanceoff() throws QueryException {
    final Expr e = treat();
    if(!consumeWS(INSTANCE)) return e;
    check(OF);
    return new Instance(input(), e, sequenceType());
  }

  /**
   * [ 55] Parses a TreatExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr treat() throws QueryException {
    final Expr e = castable();
    if(!consumeWS(TREAT)) return e;
    check(AS);
    return new Treat(input(), e, sequenceType());
  }

  /**
   * [ 56] Parses a CastableExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr castable() throws QueryException {
    final Expr e = cast();
    if(!consumeWS(CASTABLE)) return e;
    check(AS);
    return new Castable(input(), e, simpleType());
  }

  /**
   * [ 57] Parses a CastExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr cast() throws QueryException {
    final Expr e = scored();
    if(!consumeWS(CAST)) return e;
    check(AS);
    return new Cast(input(), e, simpleType());
  }

  /**
   * Parses a ScoredExpr.
   * This is a proprietary extension to XQuery FT for adding full-text scores
   * to non-FT expressions.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr scored() throws QueryException {
    final Expr e = unary();
    if(!consumeWS(SCORED)) return e;
    return new Scored(input(), e, single());
  }

  /**
   * [ 58] Parses a UnaryExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr unary() throws QueryException {
    boolean minus = false;
    boolean found = false;
    do {
      skipWS();
      if(consume('-')) {
        minus ^= true;
        found = true;
      } else if(consume('+')) {
        found = true;
      } else {
        final Expr e = value();
        return found ? new Unary(input(), check(e, EVALUNARY), minus) : e;
      }
    } while(true);
  }

  /**
   * [ 59] Parses a ValueExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr value() throws QueryException {
    final boolean lax = consumeWS(VALIDATE, LAX, NOVALIDATE);
    if(lax || consumeWS(VALIDATE, STRICT, NOVALIDATE)) validate();

    final Expr e = path();
    return e != null ? e : extension();
  }

  /**
   * [ 65] Parses an ExtensionExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr extension() throws QueryException {
    final Expr[] pragmas = pragma();
    return pragmas.length == 0 ? null :
      new Extension(input(), pragmas, enclosed(NOPRAGMA));
  }

  /**
   * [ 63] Parses a ValidateExpr.
   * @throws QueryException query exception
   */
  private void validate() throws QueryException {
    if(!consumeWS2(LAX)) consumeWS2(STRICT);
    check(BRACE1);
    check(single(), NOVALIDATE);
    check(BRACE2);
    error(IMPLVAL);
  }

  /**
   * [ 66] Parses a Pragma.
   * @return array of pragmas
   * @throws QueryException query exception
   */
  private Expr[] pragma() throws QueryException {
    Expr[] pragmas = { };
    while(consumeWS(PRAGMA)) {
      skipWS();
      final QNm name = new QNm(qName(PRAGMAINV), ctx, input());
      if(!name.ns()) error(NSMISS, name);
      char c = curr();
      if(c != '#' && !ws(c)) error(PRAGMAINV);

      tok.reset();
      while(c != '#' || next() != ')') {
        if(c == 0) error(PRAGMAINV);
        tok.add(consume());
        c = curr();
      }
      tok.trim();
      pragmas = add(pragmas, new Pragma(name, tok.finish(), input()));
      qp += 2;
    }
    return pragmas;
  }

  /**
   * [ 68] Parses a PathExpr.
   * [ 69] Parses a RelativePathExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr path() throws QueryException {
    checkInit();
    final int s = consume('/') ? consume('/') ? 2 : 1 : 0;
    if(s > 0) checkAxis(s == 2 ? Axis.DESC : Axis.CHILD);
    qm = qp;

    final Expr ex = step();
    if(ex == null) {
      if(s == 2) {
        if(more()) checkInit();
        error(PATHMISS);
      }
      return s == 1 ? new Root(input()) : null;
    }

    final boolean slash = consume('/');
    final boolean step = ex instanceof AxisStep;
    if(!slash && s == 0 && !step) return ex;

    Expr[] list = {};
    if(s == 2) list = add(list, descOrSelf());

    final Expr root = s > 0 ? new Root(input()) : !step ? ex : null;
    if(root != ex) list = add(list, ex);

    if(slash) {
      do {
        final boolean desc = consume('/');
        qm = qp;
        if(desc) list = add(list, descOrSelf());
        checkAxis(desc ? Axis.DESC : Axis.CHILD);

        final Expr st = step();
        if(st == null) error(PATHMISS);
        if(!(st instanceof Context)) list = add(list, st);
      } while(consume('/'));
    }
    if(list.length == 0)
      return new MixedPath(input(), root, new Context(input()));

    // check if all steps are axis steps
    boolean axes = true;
    final AxisStep[] tmp = new AxisStep[list.length];
    for(int l = 0; l < list.length; ++l) {
      axes &= list[l] instanceof AxisStep;
      if(axes) tmp[l] = (AxisStep) list[l];
    }

    return axes ? AxisPath.get(input(), root, tmp) :
      new MixedPath(input(), root, list);
  }

  /**
   * Returns a standard descendant-or-self::node() step.
   * @return step
   */
  private AxisStep descOrSelf() {
    return AxisStep.get(input(), Axis.DESCORSELF, Test.NOD);
  }

  // Methods for query suggestions

  /**
   * Performs an optional check init.
   */
  protected void checkInit() { }

  /**
   * Performs an optional axis check.
   * @param axis axis
   */
  @SuppressWarnings("unused")
  protected void checkAxis(final Axis axis) { }

  /**
   * Performs an optional test check.
   * @param test node test
   * @param attr attribute flag
   */
  @SuppressWarnings("unused")
  protected void checkTest(final Test test, final boolean attr) { }

  /**
   * Checks a predicate.
   * @param open open flag
   */
  @SuppressWarnings("unused")
  protected void checkPred(final boolean open) { }

  /**
   * [ 70] Parses a StepExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr step() throws QueryException {
    final Expr e = filter();
    return e != null ? e : axis();
  }

  /**
   * [ 71] Parses an AxisStep.
   * @return query expression
   * @throws QueryException query exception
   */
  private AxisStep axis() throws QueryException {
    Axis ax = null;
    Test test = null;
    if(consumeWS2(DOT2)) {
      ax = Axis.PARENT;
      test = Test.NOD;
      checkTest(test, false);
    } else if(consume('@')) {
      ax = Axis.ATTR;
      test = test(true);
      checkTest(test, true);
      if(test == null) {
        --qp;
        error(NOATTNAME);
      }
    } else {
      for(final Axis a : Axis.values()) {
        if(consumeWS(a.name, COL2, NOLOCSTEP)) {
          consumeWS2(COL2);
          alter = NOLOCSTEP;
          ap = qp;
          ax = a;
          test = test(a == Axis.ATTR);
          break;
        }
      }
    }
    if(ax == null) {
      ax = Axis.CHILD;
      test = test(false);
      if(test != null && test.type == Type.ATT) ax = Axis.ATTR;
      checkTest(test, ax == Axis.ATTR);
    }
    if(test == null) return null;

    Expr[] pred = {};
    while(consumeWS2(BR1)) {
      checkPred(true);
      pred = add(pred, expr());
      check(BR2);
      checkPred(false);
    }
    return AxisStep.get(input(), ax, test, pred);
  }

  /**
   * [ 78] Parses a NodeTest.
   * [ 79] Parses a NameTest.
   * [123] Parses a KindTest.
   * @param att attribute flag
   * @return query expression
   * @throws QueryException query exception
   */
  private Test test(final boolean att) throws QueryException {
    final int p = qp;
    final char ch = curr();
    if(XMLToken.isNCStartChar(ch)) {
      final byte[] name = qName(null);
      final int p2 = qp;
      if(consumeWS(PAR1)) {
        final Type type = Type.node(new QNm(name));
        if(type != null) {
          tok.reset();
          while(!consume(PAR2)) {
            if(curr() == 0) error(TESTINCOMPLETE);
            tok.add(consume());
          }
          skipWS();
          tok.trim();
          return tok.size() == 0 ? Test.get(type) :
            kindTest(type, tok.finish());
        }
      } else {
        qp = p2;
        // name test "pre:tag"
        if(contains(name, ':')) {
          skipWS();
          return new NameTest(new QNm(name, ctx, input()),
              NameTest.Name.STD, att, input());
        }
        // name test "tag"
        if(!consume(':')) {
          skipWS();
          final QNm nm = new QNm(name, att ? Uri.EMPTY : null);
          return new NameTest(nm, NameTest.Name.STD, att, input());
        }
        // name test "pre:*"
        if(consume('*')) {
          final QNm nm = new QNm(EMPTY, ctx.ns.uri(name, false, input()));
          return new NameTest(nm, NameTest.Name.NS, att, input());
        }
      }
    } else if(consume('*')) {
      // name test "*"
      if(!consume(':')) return new NameTest(att, input());
      // name test "*:tag"
      return new NameTest(new QNm(qName(QNAMEINV)), NameTest.Name.NAME, att,
          input());
    }
    qp = p;
    return null;
  }

  /**
   * [ 81] Parses a FilterExpr.
   * [ 82] Parses a PredicateList.
   * [ 83] Parses a Predicate.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr filter() throws QueryException {
    final Expr e = primary();
    if(!consumeWS2(BR1)) return e;

    if(e == null) error(PREDMISSING);
    Expr[] pred = {};
    do { pred = add(pred, expr()); check(BR2); } while(consumeWS2(BR1));
    return new Filter(input(), e, pred);
  }

  /**
   * [ 84] Parses a PrimaryExpr.
   * [ 87] Parses a VarRef.
   * [ 90] Parses a ContextItem.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr primary() throws QueryException {
    skipWS();
    // literals
    Expr e = literal();
    if(e != null) return e;
    // variables
    final char c = curr();
    if(c == '$') {
      final Var v = new Var(input(), varName());
      final Var var = ctx.vars.get(v);
      if(var == null) error(VARUNDEF, v);
      return new VarRef(input(), var);
    }
    // parentheses
    if(c == '(' && next() != '#') return parenthesized();
    // direct constructor
    if(c == '<') return constructor();
    // function calls and computed constructors
    if(letter(c)) {
      e = functionCall();
      if(e != null) return e;
      e = compConstructor();
      if(e != null) return e;
      // ordered expression
      if(consumeWS(ORDERED, BRACE1, INCOMPLETE) ||
         consumeWS(UNORDERED, BRACE1, INCOMPLETE))
        return enclosed(NOENCLEXPR);
    }
    return null;
  }

  /**
   * [ 85] Parses a Literal.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr literal() throws QueryException {
    final char c = curr();
    if(digit(c)) return numericLiteral();
    // decimal/double values or context item
    if(c == '.' && next() != '.') {
      consume('.');
      if(!digit(curr())) return new Context(input());
      tok.reset();
      tok.add('.');
      return decimalLiteral();
    }
    // strings
    return quote(c) ? Str.get(stringLiteral()) : null;
  }

  /**
   * [ 86] Parses a NumericLiteral.
   * [141] Parses an IntegerLiteral.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr numericLiteral() throws QueryException {
    tok.reset();
    while(digit(curr())) tok.add(consume());
    if(letter(curr())) return checkDbl();
    if(!consume('.')) {
      final long l = toLong(tok.finish());
      if(l == Long.MIN_VALUE) error(BOUNDS, tok);
      return Itr.get(l);
    }
    tok.add('.');
    return decimalLiteral();
  }

  /**
   * [ 88] Parses a VarName.
   * @return query expression
   * @throws QueryException query exception
   */
  private QNm varName() throws QueryException {
    check(DOLLAR);
    skipWS();
    final QNm name = new QNm(qName(NOVARNAME));
    if(name.ns()) name.uri(ctx.ns.uri(name.pref(), false, input()));
    ctx.ns.uri(name);
    return name;
  }

  /**
   * [ 89] Parses a ParenthesizedExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr parenthesized() throws QueryException {
    check(PAR1);
    final Expr e = expr();
    check(PAR2);
    return e == null ? Empty.SEQ : e;
  }

  /**
   * [ 93] Parses a FunctionCall.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr functionCall() throws QueryException {
    final int p = qp;
    final QNm name = new QNm(qName(null));
    if(!consumeWS2(PAR1) || Type.node(name) != null) {
      qp = p;
      return null;
    }

    // name and opening bracket found
    Expr[] exprs = {};
    while(curr() != 0) {
      if(consumeWS2(PAR2)) {
        alter = FUNCUNKNOWN;
        alterFunc = name;
        ap = qp;
        ctx.ns.uri(name);
        name.uri(name.ns() ? ctx.ns.uri(name.pref(), false, input())
            : ctx.nsFunc);
        final Expr func = ctx.funcs.get(name, exprs, ctx, this);
        if(func != null) {
          alter = null;
          return func;
        }
        qp = p;
        return null;
      }
      if(exprs.length != 0) check(COMMA);
      exprs = add(exprs, single());
    }
    error(FUNCMISS, name.atom());
    return null;
  }

  /**
   * [ 94] Parses a Constructor.
   * [ 95] Parses a DirectConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr constructor() throws QueryException {
    check('<');
    return consume('!') ? dirComment() : consume('?') ? dirPI() : dirElement();
  }

/**
   * [ 96] Parses a DirElemConstructor.
   * [ 97-100] Parses attributes.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElement() throws QueryException {
    if(skipWS()) error(NOTAGNAME);
    final QNm tag = new QNm(qName(NOTAGNAME));
    consumeWSS();

    Expr[] cont = {};
    final Atts ns = new Atts();
    final int s = ctx.ns.size();

    // parse attributes...
    boolean xmlDef = false; // xml prefix explicitly declared?
    while(XMLToken.isNCStartChar(curr())) {
      final byte[] atn = qName(null);
      Expr[] attv = {};

      consumeWSS();
      check('=');
      consumeWSS();
      final char delim = consume();
      if(!quote(delim)) error(NOQUOTE, found());
      final TokenBuilder tb = new TokenBuilder();

      boolean simple = true;
      do {
        while(!consume(delim)) {
          final char c = curr();
          if(c == '{') {
            if(next() == '{') {
              tb.add(consume());
              consume();
            } else {
              final byte[] text = tb.finish();
              if(text.length != 0) {
                attv = add(attv, Str.get(text));
              } else {
                attv = add(attv, enclosed(NOENCLEXPR));
                simple = false;
              }
              tb.reset();
            }
          } else if(c == '}') {
            ++qp;
            check('}');
            tb.add('}');
          } else if(c == '<' || c == 0) {
            error(NOQUOTE, found());
          } else if(c == 0x0A || c == 0x09) {
            ++qp;
            tb.add(' ');
          } else {
            entity(tb);
          }
        }
        if(!consume(delim)) break;
        tb.add(delim);
      } while(true);

      if(tb.size() != 0) attv = add(attv, Str.get(tb.finish()));

      if(eq(atn, XMLNS)) {
        if(!simple) error(NSCONS);
        final byte[] v = attv.length == 0 ? EMPTY : ((Str) attv[0]).atom();
        if(!tag.ns()) tag.uri(v);
        addNS(ns, EMPTY, v);
      } else if(startsWith(atn, XMLNSC)) {
        if(!simple) error(NSCONS);
        final byte[] v = attv.length == 0 ? EMPTY : ((Str) attv[0]).atom();
        if(v.length == 0) error(NSEMPTYURI);
        final QNm nsd = new QNm(atn, v);
        final byte[] pref = nsd.ln();
        if(!eq(pref, XML) || !eq(v, XMLURI)) {
          ctx.ns.add(nsd, input());
          addNS(ns, pref, v);
          if(eq(pref, tag.pref())) tag.uri(v);
        } else {
          if(xmlDef) error(DUPLNSDEF, XML);
          xmlDef = true;
        }
      } else {
        cont = add(cont, new CAttr(input(), false,
            new QNm(atn, contains(atn, ':') ? null : Uri.EMPTY), attv));
      }
      if(!consumeWSS()) break;
    }

    if(consume('/')) {
      check('>');
    } else {
      check('>');
      while(curr() != '<' || next() != '/') {
        final Expr e = dirElemContent(tag);
        if(e == null) continue;
        cont = add(cont, e);
      }
      qp += 2;

      if(skipWS()) error(NOTAGNAME);
      final byte[] close = qName(NOTAGNAME);
      consumeWSS();
      check('>');
      if(!eq(tag.atom(), close)) error(TAGWRONG, tag.atom(), close);
    }

    ctx.ns.size(s);
    return new CElem(input(), tag, ns, cont);
  }

  /**
   * Checks the uniqueness of the namespace and adds it to the attributes.
   * @param ns namespace array
   * @param k namespace prefix
   * @param v uri
   * @throws QueryException query exception
   */
  private void addNS(final Atts ns, final byte[] k, final byte[] v)
      throws QueryException {

    if(ns.get(k) != -1) error(DUPLNSDEF, k);
    ns.add(k, v);
  }

  /**
   * [101] Parses a DirElemContent.
   * @param tag opening tag
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElemContent(final QNm tag) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    do {
      final char c = curr();
      if(c == '<') {
        if(consumeWS2(CDATA)) {
          tb.add(cDataSection());
          tb.ent = true;
        } else {
          final Str txt = text(tb);
          return txt != null ? txt : next() == '/' ? null : constructor();
        }
      } else if(c == '{') {
        if(next() == '{') {
          tb.add(consume());
          consume();
        } else {
          final Str txt = text(tb);
          return txt != null ? txt : enclosed(NOENCLEXPR);
        }
      } else if(c == '}') {
        consume();
        check('}');
        tb.add('}');
      } else if(c != 0) {
        entity(tb);
      } else {
        error(NOCLOSING, tag.atom());
      }
    } while(true);
  }

  /**
   * Returns a string item.
   * @param tb token builder
   * @return text or null reference
   */
  private Str text(final TokenBuilder tb) {
    final byte[] t = tb.finish();
    return t.length == 0 || !tb.ent && !ctx.spaces && ws(t) ? null : Str.get(t);
  }

  /**
   * [103] Parses a DirCommentConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirComment() throws QueryException {
    check('-');
    check('-');
    final TokenBuilder tb = new TokenBuilder();
    do {
      while(not('-')) tb.add(consume());
      consume();
      if(consume('-')) {
        check('>');
        return new CComm(input(), Str.get(tb.finish()));
      }
      tb.add('-');
    } while(true);
  }

  /**
   * [105-106] Parses a DirPIConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirPI() throws QueryException {
    if(consumeWSS()) error(PIXML, EMPTY);
    final byte[] str = trim(qName(PIWRONG));
    final Str pi = Str.get(str);
    if(str.length == 0 || eq(lc(str), XML)) error(PIXML, pi);

    final boolean space = skipWS();
    final TokenBuilder tb = new TokenBuilder();
    do {
      while(not('?')) {
        if(!space) error(PIWRONG);
        tb.add(consume());
      }
      consume();
      if(consume('>')) {
        if(!XMLToken.isNCName(str)) INVALPI.thrw(input(), str);
        return new CPI(input(), pi, Str.get(tb.finish()));
      }
      tb.add('?');
    } while(true);
  }

  /**
   * [107] Parses a CDataSection.
   * @return CData
   * @throws QueryException query exception
   */
  private byte[] cDataSection() throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      while(not(']')) {
        final char c = consume();
        if(c != '\r') tb.add(c);
      }
      consume();
      if(curr() == ']' && next() == '>') {
        qp += 2;
        return tb.finish();
      }
      tb.add(']');
    }
  }

  /**
   * [109] Parses a ComputedConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compConstructor() throws QueryException {
    final int p = qp;
    if(consumeWS(DOCUMENT))       return consume(compDoc(), p);
    else if(consumeWS(ELEMENT))   return consume(compElemConstructor(), p);
    else if(consumeWS(ATTRIBUTE)) return consume(compAttribute(), p);
    else if(consumeWS(TEXT))      return consume(compText(), p);
    else if(consumeWS(COMMENT))   return consume(compComment(), p);
    else if(consumeWS(PI))        return consume(compPI(), p);
    return null;
  }

  /**
   * Consumes the specified expression or resets the query position.
   * @param expr expression
   * @param p query position
   * @return expression or null
   */
  private Expr consume(final Expr expr, final int p) {
    if(expr == null) qp = p;
    return expr;
  }

  /**
   * [110] Parses a CompDocConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compDoc() throws QueryException {
    if(!consumeWS2(BRACE1)) return null;
    final Expr e = check(expr(), NODOCCONS);
    check(BRACE2);
    return new CDoc(input(), e);
  }

  /**
   * [111] Parses a CompElemConstructor.
   * [112] Parses a ContextExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compElemConstructor() throws QueryException {
    skipWS();

    Expr name;
    if(XMLToken.isNCStartChar(curr())) {
      name = new QNm(qName(null));
    } else {
      if(!consumeWS2(BRACE1)) return null;
      name = check(expr(), NOTAGNAME);
      check(BRACE2);
    }

    if(!consumeWS2(BRACE1)) return null;
    final Expr e = expr();
    check(BRACE2);
    return new CElem(input(), name, new Atts(),
        e == null ? new Expr[0] : new Expr[] { e });
  }

  /**
   * [113] Parses a CompAttrConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compAttribute() throws QueryException {
    skipWS();

    Expr nm;
    if(XMLToken.isNCStartChar(curr())) {
      nm = new QNm(qName(null));
    } else {
      if(!consumeWS2(BRACE1)) return null;
      nm = expr();
      check(BRACE2);
    }

    if(!consumeWS2(BRACE1)) return null;
    final Expr e = expr();
    check(BRACE2);
    return new CAttr(input(), true, nm, e == null ? Empty.SEQ : e);
  }

  /**
   * [114] Parses a CompTextConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compText() throws QueryException {
    if(!consumeWS2(BRACE1)) return null;
    final Expr e = check(expr(), NOTXTCONS);
    check(BRACE2);
    return new CTxt(input(), e);
  }

  /**
   * [115] Parses a CompCommentConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compComment() throws QueryException {
    if(!consumeWS2(BRACE1)) return null;
    final Expr e = check(expr(), NOCOMCONS);
    check(BRACE2);
    return new CComm(input(), e);
  }

  /**
   * [116] Parses a CompPIConstructor.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compPI() throws QueryException {
    skipWS();
    Expr name;
    if(XMLToken.isNCStartChar(curr())) {
      name = Str.get(ncName(null));
    } else {
      if(!consumeWS2(BRACE1)) return null;
      name = check(expr(), PIWRONG);
      check(BRACE2);
    }

    if(!consumeWS2(BRACE1)) return null;
    final Expr e = expr();
    check(BRACE2);
    return new CPI(input(), name, e == null ? Empty.SEQ : e);
  }

  /**
   * [117] Parses a SimpleType.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType simpleType() throws QueryException {
    skipWS();
    final QNm type = new QNm(qName(TYPEINVALID));
    type.uri(ctx.ns.uri(type.pref(), false, input()));
    skipWS();
    final Type t = Type.find(type, true);
    if(t == Type.AAT || t == Type.NOT) error(CASTUNKNOWN, type);
    if(t == null) error(TYPEUNKNOWN, type);
    return SeqType.get(t, consume('?') ? Occ.ZO : Occ.O);
  }

  /**
   * [119] Parses a SequenceType.
   * [120] Parses an OccurrenceIndicator.
   * [123] Parses a KindTest.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType sequenceType() throws QueryException {
    skipWS();
    final QNm type = new QNm(qName(TYPEINVALID));
    type.uri(ctx.ns.uri(type.pref(), false, input()));
    // parse non-atomic types
    final boolean atom = !consumeWS(PAR1);
    tok.reset();
    while(!atom && !consumeWS(PAR2)) {
      if(curr() == 0) error(FUNCMISS, type.atom());
      tok.add(consume());
    }
    skipWS();
    // parse occurrence indicator
    final Occ occ = consume('?') ? Occ.ZO : consume('+') ? Occ.OM :
      consume('*') ? Occ.ZM : Occ.O;
    skipWS();

    final Type t = Type.find(type, atom);
    if(t == Type.EMP && occ != Occ.O) error(EMPTYSEQOCC, t);
    if(t == null) {
      if(atom) error(TYPEUNKNOWN, type);
      error(NOTYPE, new TokenBuilder("\"").add(
          type.atom()).add('(').add(tok.finish()).add(")\""));
    }

    final KindTest kt = tok.size() == 0 ? null : kindTest(t, tok.finish());
    // use empty name test if types are different
    return SeqType.get(t, occ, kt == null ? null :
      kt.extype == null || t == kt.extype ? kt.name : new QNm(EMPTY));
  }

  /**
   * Checks the arguments of the kind test.
   * @param t type
   * @param k kind arguments
   * @return arguments
   * @throws QueryException query exception
   */
  private KindTest kindTest(final Type t, final byte[] k)
      throws QueryException {

    byte[] nm = trim(k);
    if(t == Type.PI) {
      final boolean s = startsWith(k, '\'') || startsWith(k, '"');
      nm = trim(delete(delete(k, '\''), '"'));
      if(!XMLToken.isNCName(nm)) {
        if(s) error(XPINVNAME, k);
        error(TESTINVALID, t, k);
      }
      return new KindTest(t, new QNm(nm, ctx, input()), null);
    }
    if(t != Type.ELM && t != Type.ATT) error(TESTINVALID, t, k);

    Type tp = t;
    final int i = indexOf(nm, ',');
    if(i != -1) {
      final QNm test = new QNm(trim(substring(nm, i + 1)), ctx, input());
      if(!eq(test.uri().atom(), XSURI)) error(TYPEUNDEF, test);

      final byte[] ln = test.ln();
      tp = Type.find(test, true);
      if(tp == null && !eq(ln, ANYTYPE) && !eq(ln, ANYSIMPLE) &&
          !eq(ln, UNTYPED)) error(VARUNDEF, test);
      if(tp == Type.ATM || tp == Type.AAT) tp = null;
      nm = trim(substring(nm, 0, i));
    }
    if(nm.length == 1 && nm[0] == '*') return new KindTest(t, null, tp);
    if(!XMLToken.isQName(nm)) error(TESTINVALID, t, k);
    return new KindTest(t, new QNm(nm, ctx, input()), tp);
  }

  /**
   * [142] Parses a DecimalLiteral.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr decimalLiteral() throws QueryException {
    if(letter(curr())) return checkDbl();
    while(digit(curr())) tok.add(consume());
    return letter(curr()) ? checkDbl() : new Dec(tok.finish());
  }

  /**
   * [143] Parses a DoubleLiteral.
   * Checks if a number is followed by a whitespace.
   * @return expression
   * @throws QueryException query exception
   */
  private Expr checkDbl() throws QueryException {
    if(!consume('e') && !consume('E')) error(NUMBERWS);
    tok.add('e');
    if(curr() == '+' || curr() == '-') tok.add(consume());
    boolean dig = false;
    while(digit(curr())) {
      tok.add(consume());
      dig = true;
    }
    if(!dig) error(NUMBERINC);
    if(letter(curr())) error(NUMBERWS);
    return Dbl.get(tok.finish(), input());
  }

  /**
   * [144] Parses a StringLiteral.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] stringLiteral() throws QueryException {
    skipWS();
    final char delim = curr();
    if(!quote(delim)) error(NOQUOTE, found());
    consume();
    tok.reset();
    while(true) {
      while(!consume(delim)) {
        if(curr() == 0) error(NOQUOTE, found());
        entity(tok);
      }
      if(!consume(delim)) break;
      tok.add(delim);
    }
    return tok.finish();
  }

  /**
   * [11-169] Parses a TryClause.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!ctx.xquery30 || !consumeWS2(TRY)) return null;

    final Expr tr = enclosed(NOENCLEXPR);
    check(CATCH);

    Catch[] ct = {};
    do {
      QNm[] codes = {};
      do {
        skipWS();
        if(XMLToken.isNCStartChar(curr())) {
          codes = Array.add(codes, new QNm(qName(QNAMEINV)));
        } else {
          check("*");
          codes = Array.add(codes, (QNm) null);
        }
      } while(consumeWS(PIPE));

      Var[] var = {};
      final int s = ctx.vars.size();
      if(consumeWS2(PAR1)) {
        var = addVar(var);
        if(consumeWS2(COMMA)) {
          var = addVar(var);
          if(consumeWS2(COMMA)) {
            var = addVar(var);
          }
        }
        check(PAR2);
      }
      final Expr ex = enclosed(NOENCLEXPR);
      ctx.vars.reset(s);
      ct = Array.add(ct, new Catch(input(), ex, codes, var));
    } while(consumeWS(CATCH));

    return new Try(input(), tr, ct);
  }

  /**
   * Adds a variable to the specified array.
   * @param vars input variables
   * @return new variable array
   * @throws QueryException query exception
   */
  private Var[] addVar(final Var[] vars) throws QueryException {
    final Var v = new Var(input(), varName());
    for(final Var vr : vars) if(v.name.eq(vr.name)) error(VARDEFINED, v);
    ctx.vars.add(v);
    final Var[] var = Array.add(vars, v);
    return var;
  }

  /**
   * [FT144] Parses an FTSelection.
   * [FT157] Parses an FTPosFilter.
   * [FT158] Parses an FTOrder.
   * [FT159] Parses an FTWindow.
   * [FT160] Parses an FTDistance.
   * [FT162] Parses an FTScope.
   * [FT164] Parses an FTContent.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftSelection(final boolean prg) throws QueryException {
    FTExpr expr = ftOr(prg);
    FTExpr old;
    do {
      old = expr;
      if(consumeWS(ORDERED)) {
        expr = new FTOrder(input(), expr);
      } else if(consumeWS(WINDOW)) {
        expr = new FTWindow(input(), expr, additive(), ftUnit());
      } else if(consumeWS(DISTANCE)) {
        final Expr[] r = ftRange();
        if(r == null) error(FTRANGE);
        expr = new FTDistance(input(), expr, r, ftUnit());
      } else if(consumeWS(AT)) {
        final boolean start = consumeWS(START);
        final boolean end = !start && consumeWS(END);
        if(!start && !end) error(INCOMPLETE);
        expr = new FTContent(input(), expr, start, end);
      } else if(consumeWS(ENTIRE)) {
        check(CONTENT);
        expr = new FTContent(input(), expr, false, false);
      } else {
        final boolean same = consumeWS(SAME);
        final boolean diff = !same && consumeWS(DIFFERENT);
        if(same || diff) {
          FTUnit unit = null;
          if(consumeWS(SENTENCE)) unit = FTUnit.SENTENCE;
          else if(consumeWS(PARAGRAPH)) unit = FTUnit.PARAGRAPH;
          else error(INCOMPLETE);
          expr = new FTScope(input(), expr, unit, same);
        }
      }
    } while(old != expr);
    return expr;
  }

  /**
   * [FT145] Parses FTOr.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftOr(final boolean prg) throws QueryException {
    final FTExpr e = ftAnd(prg);
    if(!consumeWS(FTOR)) return e;

    FTExpr[] list = { e };
    do list = Array.add(list, ftAnd(prg)); while(consumeWS(FTOR));
    return new FTOr(input(), list);
  }

  /**
   * [FT146] Parses FTAnd.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftAnd(final boolean prg) throws QueryException {
    final FTExpr e = ftMildNot(prg);
    if(!consumeWS(FTAND)) return e;

    FTExpr[] list = { e };
    do list = Array.add(list, ftMildNot(prg)); while(consumeWS(FTAND));
    return new FTAnd(input(), list);
  }

  /**
   * [FT147] Parses FTMildNot.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftMildNot(final boolean prg) throws QueryException {
    final FTExpr e = ftUnaryNot(prg);
    if(!consumeWS(NOT)) return e;

    FTExpr[] list = { };
    do {
      check(IN); list = Array.add(list, ftUnaryNot(prg));
    } while(consumeWS(NOT));
    // convert "A not in B not in ..." to "A not in(B or ...)"
    return new FTMildNot(input(), e, list.length == 1 ? list[0] :
      new FTOr(input(), list));
  }

  /**
   * [FT148] Parses FTUnaryNot.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftUnaryNot(final boolean prg) throws QueryException {
    final boolean not = consumeWS(FTNOT);
    final FTExpr e = ftPrimaryWithOptions(prg);
    return not ? new FTNot(input(), e) : e;
  }

  /**
   * [FT149] Parses FTPrimaryWithOptions.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftPrimaryWithOptions(final boolean prg) throws QueryException {
    FTExpr expr = ftPrimary(prg);
    final FTOpt fto = new FTOpt(ctx.context.prop);
    boolean found = false;
    while(ftMatchOption(fto)) found = true;
    // check if the options are supported:
    FTLexer.checkFTOpt(fto);
    if(consumeWS(WEIGHT))
      expr = new FTWeight(input(), expr, enclosed(NOENCLEXPR));
    // skip options if none were specified...
    return found ? new FTOptions(input(), expr, fto) : expr;
  }

  /**
   * [FT150] Parses FTPrimary.
   * [FT151] Parses FTWords.
   * [FT152] Parses FTWordsValue.
   * [FT154] Parses FTAnyallOption.
   * [FT155] Parses FTTimes.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftPrimary(final boolean prg) throws QueryException {
    final Expr[] pragmas = pragma();
    if(pragmas.length != 0) {
      check(BRACE1);
      final FTExpr e = ftSelection(true);
      check(BRACE2);
      return new FTExtensionSelection(input(), pragmas, e);
    }

    if(consumeWS(PAR1)) {
      final FTExpr e = ftSelection(false);
      check(PAR2);
      return e;
    }

    skipWS();
    final Expr e = curr('{') ? enclosed(NOENCLEXPR) : quote(curr()) ?
        literal() : null;
    if(e == null) error(prg ? NOPRAGMA : NOENCLEXPR);

    // FTAnyAllOption
    FTMode mode = FTMode.M_ANY;
    if(consumeWS(ALL)) {
      mode = consumeWS(WORDS) ? FTMode.M_ALLWORDS : FTMode.M_ALL;
    } else if(consumeWS(ANY)) {
      mode = consumeWS(WORD) ? FTMode.M_ANYWORD : FTMode.M_ANY;
    } else if(consumeWS(PHRASE)) {
      mode = FTMode.M_PHRASE;
    }

    // FTTimes
    Expr[] occ = null;
    if(consumeWS(OCCURS)) {
      occ = ftRange();
      if(occ == null) error(FTRANGE);
      check(TIMES);
    }
    return new FTWords(input(), e, mode, occ);
  }

  /**
   * [FT156] Parses an FTRange.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr[] ftRange() throws QueryException {
    final Expr[] occ = { Itr.get(1), Itr.get(Long.MAX_VALUE) };
    if(consumeWS(EXACTLY)) {
      occ[0] = additive();
      occ[1] = occ[0];
    } else if(consumeWS(AT)) {
      if(consumeWS(LEAST)) {
        occ[0] = additive();
      } else {
        check(MOST);
        occ[0] = Itr.get(0);
        occ[1] = additive();
      }
    } else if(consumeWS(FROM)) {
      occ[0] = additive();
      check(TO);
      occ[1] = additive();
    } else {
      return null;
    }
    return occ;
  }

  /**
   * [FT156] Parses an FTUnit.
   * @return query expression
   * @throws QueryException query exception
   */
  private FTUnit ftUnit() throws QueryException {
    if(consumeWS(WORDS)) return FTUnit.WORD;
    if(consumeWS(SENTENCES)) return FTUnit.SENTENCE;
    if(consumeWS(PARAGRAPHS)) return FTUnit.PARAGRAPH;
    error(INCOMPLETE);
    return null;
  }

  /**
   * [FT154] Parses an FTMatchOption.
   * @param opt options instance
   * @return false if no options were found
   * @throws QueryException query exception
   */
  private boolean ftMatchOption(final FTOpt opt) throws QueryException {
    if(!consumeWS(USING)) return false;

    if(consumeWS(LOWERCASE)) {
      if(opt.isSet(LC) || opt.isSet(UC) || opt.isSet(CS))
        error(FTDUP, CASE);
      opt.set(CS, true);
      opt.set(LC, true);
    } else if(consumeWS(UPPERCASE)) {
      if(opt.isSet(LC) || opt.isSet(UC) || opt.isSet(CS))
        error(FTDUP, CASE);
      opt.set(CS, true);
      opt.set(UC, true);
    } else if(consumeWS(CASE)) {
      if(opt.isSet(LC) || opt.isSet(UC) || opt.isSet(CS))
        error(FTDUP, CASE);
      opt.set(CS, consumeWS(SENSITIVE));
      if(!opt.is(CS)) check(INSENSITIVE);
    } else if(consumeWS(DIACRITICS)) {
      if(opt.isSet(DC)) error(FTDUP, DIACRITICS);
      opt.set(DC, consumeWS(SENSITIVE));
      if(!opt.is(DC)) check(INSENSITIVE);
    } else if(consumeWS(LANGUAGE)) {
      if(opt.ln != null) error(FTDUP, LANGUAGE);
      opt.ln = lc(stringLiteral());
    } else if(consumeWS(OPTION)) {
      optionDecl();
    } else {
      final boolean using = !consumeWS(NO);

      if(consumeWS2(STEMMING)) {
        if(opt.isSet(ST)) error(FTDUP, STEMMING);
        opt.set(ST, using);
      } else if(consumeWS2(THESAURUS)) {
        if(opt.th != null) error(FTDUP, THESAURUS);
        opt.th = new ThesQuery();
        if(using) {
          final boolean par = consumeWS2(PAR1);
          if(!consumeWS2(DEFAULT)) ftThesaurusID(opt.th);
          while(par && consumeWS2(COMMA)) ftThesaurusID(opt.th);
          if(par) check(PAR2);
        }
      } else if(consumeWS(STOP)) {
        // add union/except
        check(WORDS);

        if(opt.sw != null) error(FTDUP, STOP + ' ' + WORDS);
        opt.sw = new StopWords();
        if(consumeWS(DEFAULT)) {
          if(!using) error(FTSTOP);
        } else {
          boolean union = false;
          boolean except = false;
          while(using) {
            if(consumeWS2(PAR1)) {
              do {
                final byte[] sl = stringLiteral();
                if(except) opt.sw.delete(sl);
                else if(!union || opt.sw.id(sl) == 0) opt.sw.add(sl);
              } while(consumeWS2(COMMA));
              check(PAR2);
            } else if(consumeWS2(AT)) {
              String fn = string(stringLiteral());
              if(ctx.stop != null) fn = ctx.stop.get(fn);

              IO fl = IO.get(fn);
              if(!fl.exists() && file != null) fl = file.merge(fn);
              if(!opt.sw.read(fl, except)) error(NOSTOPFILE, fl);
            } else if(!union && !except) {
              error(FTSTOP);
            }
            union = consumeWS2(UNION);
            except = !union && consumeWS2(EXCEPT);
            if(!union && !except) break;
          }
        }
      } else if(consumeWS2(WILDCARDS)) {
        if(opt.isSet(WC)) error(FTDUP, WILDCARDS);
        if(opt.is(FZ)) error(FTFZWC);
        opt.set(WC, using);
      } else if(consumeWS2(FUZZY)) {
        if(opt.isSet(FZ)) error(FTDUP, FUZZY);
        if(opt.is(WC)) error(FTFZWC);
        opt.set(FZ, using);
      } else {
        error(FTMATCH, consume());
        return false;
      }
    }
    return true;
  }

  /**
   * [FT171] Parses an FTThesaurusID.
   * @param thes link to thesaurus
   * @throws QueryException query exception
   */
  private void ftThesaurusID(final ThesQuery thes) throws QueryException {
    check(AT);

    String fn = string(stringLiteral());
    if(ctx.thes != null) fn = ctx.thes.get(fn);
    IO fl = IO.get(fn);

    if(!fl.exists() && file != null) fl = file.merge(file.path());
    final byte[] rel = consumeWS2(RELATIONSHIP) ? stringLiteral() : EMPTY;
    final Expr[] range = ftRange();
    long min = 0;
    long max = Long.MAX_VALUE;
    if(range != null) {
      check(LEVELS);
      if(range[0] instanceof Itr && range[1] instanceof Itr) {
        min = ((Itr) range[0]).itr(input());
        max = ((Itr) range[1]).itr(input());
      } else {
        error(THESRNG);
      }
    }
    thes.add(new Thesaurus(fl, rel, min, max, ctx.context));
  }

  /**
   * [UP143] Parses an InsertExpression.
   * [UP142] Parses an InsertTargetChoiceExpr.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr insert() throws QueryException {
    final int p = qp;
    if(!consumeWS(INSERT) || !consumeWS(NODE) && !consumeWS(NODES)) {
      qp = p;
      return null;
    }

    final Expr s = check(single(), INCOMPLETE);
    boolean first = false;
    boolean last = false;
    boolean before = false;
    boolean after = false;
    if(consumeWS(AS)) {
      first = consumeWS(FIRST);
      if(!first) {
        check(LAST);
        last = true;
      }
      check(INTO);
    } else if(!consumeWS(INTO)) {
      after = consumeWS(AFTER);
      before = !after && consumeWS(BEFORE);
      if(!after && !before) error(INCOMPLETE);
    }
    final Expr trg = check(single(), INCOMPLETE);
    ctx.updating = true;
    return new Insert(input(), s, first, last, before, after, trg);
  }

  /**
   * [UP144] Parses a DeleteExpression.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr deletee() throws QueryException {
    final int p = qp;
    if(!consumeWS(DELETE) || !consumeWS(NODES) && !consumeWS(NODE)) {
      qp = p;
      return null;
    }
    ctx.updating = true;
    return new Delete(input(), check(single(), INCOMPLETE));
  }

  /**
   * [UP146] Parses a RenameExpression.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr rename() throws QueryException {
    final int p = qp;
    if(!consumeWS(RENAME) || !consumeWS(NODE)) {
      qp = p;
      return null;
    }

    final Expr trg = check(single(), INCOMPLETE);
    check(AS);
    final Expr n = check(single(), INCOMPLETE);
    ctx.updating = true;
    return new Rename(input(), trg, n);
  }

  /**
   * [UP145] Parses a ReplaceExpression.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr replace() throws QueryException {
    final int p = qp;
    if(!consumeWS(REPLACE)) return null;

    final boolean v = consumeWS(VALUEE);
    if(v) check(OF);
    if(!consumeWS(NODE)) {
      qp = p;
      return null;
    }

    final Expr t = check(single(), INCOMPLETE);
    check(WITH);
    final Expr r = check(single(), INCOMPLETE);
    ctx.updating = true;
    return new Replace(input(), t, r, v);
  }

  /**
   * [UP150] Parses a TransformExpression.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr transform() throws QueryException {
    if(!consumeWS(COPY, DOLLAR, INCOMPLETE)) return null;
    final boolean u = ctx.updating;
    ctx.updating = false;

    Let[] fl = {};
    do {
      final Var v = new Var(input(), varName());
      check(ASSIGN);
      final Expr e = check(single(), INCOMPLETE);
      ctx.vars.add(v);
      fl = Array.add(fl, new Let(input(), e, v, false));
    } while(consumeWS(COMMA));
    check(MODIFY);

    final Expr m = check(single(), INCOMPLETE);
    check(RETURN);
    final Expr r = check(single(), INCOMPLETE);

    ctx.updating = u;
    return new Transform(input(), fl, m, r);
  }

  /**
   * Parses an NCName.
   * @param err optional error message
   * @return string
   * @throws QueryException query exception
   */
  private byte[] ncName(final Err err) throws QueryException {
    skipWS();
    tok.reset();
    if(ncName(true)) return tok.finish();
    if(err != null) error(err);
    return EMPTY;
  }

  /**
   * Parses a QName.
   * @param err optional error message. Will be thrown if no QName is found,
   * and ignored if set to {@code null}
   * @return string
   * @throws QueryException query exception
   */
  private byte[] qName(final Err err) throws QueryException {
    tok.reset();
    final boolean ok = ncName(true);
    if(ok && consume(':')) ncName(false);
    if(!ok && err != null) error(err);
    return tok.finish();
  }

  /**
   * Helper method for parsing NCNames.
   * @param first flag for first call
   * @return true for success
   */
  private boolean ncName(final boolean first) {
    char c = curr();
    if(!XMLToken.isNCStartChar(c)) {
      if(!first) --qp;
      return false;
    }
    if(!first) tok.add(':');
    do {
      tok.add(consume());
      c = curr();
    } while(XMLToken.isNCChar(c));
    return true;
  }

  /**
   * Parses and converts entities.
   * @param tb token builder
   * @throws QueryException query exception
   */
  private void entity(final TokenBuilder tb) throws QueryException {
    final int p = qp;
    if(consume('&')) {
      if(consume('#')) {
        final int b = consume('x') ? 16 : 10;
        int n = 0;
        do {
          final char c = curr();
          final boolean m = digit(c);
          final boolean h = b == 16 && (c >= 'a' && c <= 'f' ||
              c >= 'A' && c <= 'F');
          if(!m && !h) invalidEnt(p, INVENTITY);
          final long nn = n;
          n = n * b + (consume() & 15);
          if(n < nn) invalidEnt(p, INVCHARREF);
          if(!m) n += 9;
        } while(!consume(';'));
        if(!XMLToken.valid(n)) invalidEnt(p, INVCHARREF);
        tb.add(n);
      } else {
        if(consume("lt")) {
          tb.add('<');
        } else if(consume("gt")) {
          tb.add('>');
        } else if(consume("amp")) {
          tb.add('&');
        } else if(consume("quot")) {
          tb.add('"');
        } else if(consume("apos")) {
          tb.add('\'');
        } else {
          invalidEnt(p, INVENTITY);
        }
        if(!consume(';')) invalidEnt(p, INVENTITY);
      }
      tb.ent = true;
    } else {
      final char c = consume();
      int cp = c;
      if(Character.isHighSurrogate(c) && curr() != 0
          && Character.isLowSurrogate(curr())) {
        cp = Character.toCodePoint(c, consume());
      }
      if(cp == 0x0d) {
        cp = 0x0a;
        if(curr() == cp) consume();
      }
      tb.add(cp);
    }
  }

  /**
   * Returns the current entity snippet.
   * @param p start position
   * @param c error code
   * @throws QueryException query exception
   */
  private void invalidEnt(final int p, final Err c) throws QueryException {
    final String sub = query.substring(p, Math.min(p + 20, ql));
    final int sc = sub.indexOf(';');
    final String ent = sc != -1 ? sub.substring(0, sc + 1) : sub;
    error(c, ent);
  }

  /**
   * Throws an exception if the specified expression is empty.
   * @param expr expression
   * @param err error message
   * @return expression
   * @throws QueryException query exception
   */
  private Expr check(final Expr expr, final Err err)
      throws QueryException {
    if(expr == null) error(err);
    return expr;
  }

  /**
   * Checks for the specified character and throws an exception if
   * something else is found.
   * @param ch character to be found
   * @throws QueryException query exception
   */
  private void check(final int ch) throws QueryException {
    if(!consume(ch)) error(WRONGCHAR, (char) ch, found());
  }

  /**
   * Checks for the specified string and throws an exception if
   * something else is found.
   * @param s string to be found
   * @throws QueryException query exception
   */
  private void check(final String s) throws QueryException {
    if(!consumeWS2(s)) error(WRONGCHAR, s, found());
  }

  /**
   * Checks if the specified character is not found; if the input is
   * exhausted, throws an exception.
   * @param ch character to be found
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean not(final char ch) throws QueryException {
    final char c = curr();
    if(c == 0) error(WRONGEND, ch);
    return c != ch;
  }

  /**
   * Consumes the specified token and surrounding whitespaces.
   * @param t token to consume
   * @return true if token was found
   * @throws QueryException query exception
   */
  private boolean consumeWS(final String t) throws QueryException {
    final int p = qp;
    final boolean ok = consumeWS2(t);
    final int q = qp;
    final int c = curr();
    final boolean ok2 = skipWS() || !XMLToken.isNCStartChar(t.charAt(0)) ||
        !XMLToken.isNCChar(c);
    qp = ok2 ? q : p;
    return ok && ok2;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position.
   * If the strings are found, the cursor is set after the first token.
   * @param s1 string to be consumed
   * @param s2 second string
   * @param expr alternative error message
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean consumeWS(final String s1, final String s2,
      final Err expr) throws QueryException {
    final int p = qp;
    if(!consumeWS(s1)) return false;
    alter = expr;
    ap = qp;
    final int p2 = qp;
    final boolean ok = consumeWS2(s2);
    qp = ok ? p2 : p;
    return ok;
  }

  /**
   * Peeks forward and consumes the string if it equals the specified one.
   * @param str string to consume
   * @return true if string was found
   * @throws QueryException query exception
   */
  private boolean consumeWS2(final String str) throws QueryException {
    skipWS();
    return consume(str);
  }

  /**
   * Consumes all whitespace characters from the remaining query.
   * @return true if whitespaces were found
   * @throws QueryException query exception
   */
  private boolean skipWS() throws QueryException {
    final int p = qp;
    while(more()) {
      final int c = curr();
      if(c == '(' && next() == ':') {
        comment();
      } else {
        if(c <= 0 || c > ' ') return p != qp;
        ++qp;
      }
    }
    return p != qp;
  }

  /**
   * Consumes a comment.
   * @throws QueryException query exception
   */
  private void comment() throws QueryException {
    ++qp;
    while(++qp < ql) {
      if(curr('(') && next() == ':') comment();
      if(curr(':') && next() == ')') {
        qp += 2;
        return;
      }
    }
    error(COMCLOSE);
  }

  /**
   * Consumes all following whitespace characters.
   * @return true if whitespaces were found
   */
  private boolean consumeWSS() {
    final int p = qp;
    while(more()) {
      final int c = curr();
      if(c <= 0 || c > ' ') return p != qp;
      ++qp;
    }
    return true;
  }

  /**
   * Throws the alternative error message.
   * @throws QueryException query exception
   */
  private void error() throws QueryException {
    qp = ap;
    if(alter != FUNCUNKNOWN) error(alter);
    ctx.funcs.funError(alterFunc, this);
    error(alter, alterFunc.atom());
  }

  /**
   * Adds an expression to the specified array.
   * @param ar input array
   * @param e new expression
   * @return new array
   * @throws QueryException query exception
   */
  private Expr[] add(final Expr[] ar, final Expr e) throws QueryException {
    if(e == null) error(INCOMPLETE);
    final int a = ar.length;
    final Expr[] tmp = new Expr[a + 1];
    System.arraycopy(ar, 0, tmp, 0, a);
    tmp[a] = e;
    return tmp;
  }

  /**
   * Throws the specified error.
   * @param err error to be thrown
   * @param arg error arguments
   * @throws QueryException query exception
   */
  public void error(final Err err, final Object... arg)
      throws QueryException {
    err.thrw(input(), arg);
  }
}
