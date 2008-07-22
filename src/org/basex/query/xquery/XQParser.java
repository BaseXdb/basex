package org.basex.query.xquery;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.query.FTOpt;
import org.basex.query.FTPos;
import org.basex.query.QueryParser;
import org.basex.query.FTOpt.FTMode;
import org.basex.query.FTPos.FTUnit;
import org.basex.query.xquery.expr.And;
import org.basex.query.xquery.expr.CAttr;
import org.basex.query.xquery.expr.CComm;
import org.basex.query.xquery.expr.CDoc;
import org.basex.query.xquery.expr.CElem;
import org.basex.query.xquery.expr.CPI;
import org.basex.query.xquery.expr.CText;
import org.basex.query.xquery.expr.Calc;
import org.basex.query.xquery.expr.Case;
import org.basex.query.xquery.expr.Cast;
import org.basex.query.xquery.expr.Castable;
import org.basex.query.xquery.expr.Clc;
import org.basex.query.xquery.expr.CmpG;
import org.basex.query.xquery.expr.CmpN;
import org.basex.query.xquery.expr.CmpV;
import org.basex.query.xquery.expr.Context;
import org.basex.query.xquery.expr.Except;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.FLWOR;
import org.basex.query.xquery.expr.FLWR;
import org.basex.query.xquery.expr.FTAnd;
import org.basex.query.xquery.expr.FTContains;
import org.basex.query.xquery.expr.FTMildNot;
import org.basex.query.xquery.expr.FTNot;
import org.basex.query.xquery.expr.FTOptions;
import org.basex.query.xquery.expr.FTOr;
import org.basex.query.xquery.expr.FTSelect;
import org.basex.query.xquery.expr.FTWords;
import org.basex.query.xquery.expr.For;
import org.basex.query.xquery.expr.ForLet;
import org.basex.query.xquery.expr.Func;
import org.basex.query.xquery.expr.If;
import org.basex.query.xquery.expr.Instance;
import org.basex.query.xquery.expr.InterSect;
import org.basex.query.xquery.expr.Let;
import org.basex.query.xquery.expr.List;
import org.basex.query.xquery.expr.Or;
import org.basex.query.xquery.expr.Ord;
import org.basex.query.xquery.expr.Order;
import org.basex.query.xquery.expr.Pred;
import org.basex.query.xquery.expr.Range;
import org.basex.query.xquery.expr.Root;
import org.basex.query.xquery.expr.Satisfy;
import org.basex.query.xquery.expr.Treat;
import org.basex.query.xquery.expr.Try;
import org.basex.query.xquery.expr.TypeSwitch;
import org.basex.query.xquery.expr.Unary;
import org.basex.query.xquery.expr.Union;
import org.basex.query.xquery.expr.VarCall;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Dec;
import org.basex.query.xquery.item.FAttr;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.path.Axis;
import org.basex.query.xquery.path.Path;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.path.Test;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.Namespaces;
import org.basex.query.xquery.util.Var;
import org.basex.util.Array;
import org.basex.util.Set;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.XMLToken;

/**
 * XQuery parser.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQParser extends QueryParser {
  /** Resulting XQuery expression. */
  private final XQContext ctx;
  /** Temporary token builder. */
  private final TokenBuilder tok = new TokenBuilder();

  /** List of loaded modules. */
  public TokenList modLoaded = new TokenList();
  /** Module name. */
  private QNm module;

  /** Alternative error output. */
  private Object[] alter;
  /** Alternative position. */
  private int ap;

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
  private boolean declGreat;
  /** Declaration flag. */
  private boolean declPres;
  /** Declaration flag. */
  private boolean declBase;

  /***
   * Constructor.
   * @param c query context
   */
  public XQParser(final XQContext c) {
    ctx = c;
  }

  /**
   * Parses the specified query.
   * @param q input query
   * @throws XQException xquery exception
   */
  public void parse(final String q) throws XQException {
    ctx.query = q;
    parse(q, ctx.file, null);
  }

  /**
   * Parses the specified module.
   * @param q input query
   * @throws XQException xquery exception
   */
  public void module(final String q) throws XQException {
    ctx.query = q;
    parse(q, ctx.file, Uri.EMPTY);
  }

  /**
   * Parses the specified query.
   * @param q input query
   * @param f optional input file
   * @param u module uri
   * @throws XQException xquery exception
   */
  public void parse(final String q, final IO f, final Uri u)
      throws XQException {
    init(q);
    file = f;
    if(!more()) Err.or(QUERYEMPTY);
    final int v = valid();
    if(v != -1) Err.or(QUERYINV, v);
    parse(u, true);
  }

  /**
   * Parses the specified query.
   * [  1] Parses a Module.
   * @param u module uri
   * @param end if true, input must be completely evaluated
   * @throws XQException xquery exception
   */
  public void parse(final Uri u, final boolean end) throws XQException {
    try {
      versionDecl();
      if(u == null) {
        ctx.root = mainModule();
        if(ctx.root == null) if(alter != null) error(); else Err.or(EXPREMPTY);
      } else {
        moduleDecl(u);
      }

      if(end && more()) {
        if(alter != null) error();
        if(ctx.root instanceof Step) {
          final Step step = (Step) ctx.root;
          if(step.axis == Axis.CHILD && step.test.type == null &&
              step.test.name != null) Err.or(QUERYSTEP, step);
        }
        Err.or(QUERYEND, rest());
      }
      ctx.fun.check();
    } catch(final XQException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * [  2] Parses a VersionDecl.
   * @throws XQException xquery exception
   */
  private void versionDecl() throws XQException {
    final int p = qp;
    if(!consumeWS(XQUERY) || !consumeWS2(VERSION)) {
      qp = p;
      return;
    }
    final String ver = string(stringLiteral());

    final byte[] enc = consumeWS2(ENCODING) ? lc(stringLiteral()) : null;
    if(enc != null) {
      boolean v = true;
      for(final byte e : enc)
        v &= letterOrDigit(e) || e == '-';
      if(!v) Err.or(XQUERYENC2, enc);
    }
    ctx.encoding = enc;
    skipWS();
    check(';');
    if(!ver.equals(ONEZERO)) Err.or(XQUERYVER, ver);
  }

  /**
   * [  3] Parses a MainModule.
   * [  7] Parses a Setter.
   * [ 30] Parses a QueryBody ( = Expr).
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr mainModule() throws XQException {
    prolog();
    if(declColl) {
      final byte[] coll = ctx.baseURI.resolve(ctx.collation).str();
      if(!Token.eq(URLCOLL, coll)) Err.or(NOCOLL, coll);
    }
    return expr();
  }

  /**
   * [  4] Parses a ModuleDecl.
   * @param u module uri
   * @throws XQException xquery exception
   */
  private void moduleDecl(final Uri u) throws XQException {
    check(MODULE);
    check(NAMESPACE);
    module = new QNm(ncName(null));
    check(IS);
    module.uri = Uri.uri(stringLiteral());
    if(module.uri == Uri.EMPTY) Err.or(NSMODURI);
    // skip uri check for empty input uri...
    if(u != Uri.EMPTY && !u.eq(module.uri))
      Err.or(WRONGMODULE, module.uri, file);
    ctx.ns.index(module);
    skipWS();
    check(';');
    prolog();
  }

  /**
   * [  6] Parses a Prolog.
   * [  7] Parses a Setter.
   * @throws XQException xquery exception
   */
  private void prolog() throws XQException {
    // [CG] XQuery/Prolog: separate prolog from setters
    while(true) {
      final int p = qp;
      if(consumeWS(DECLARE)) {
        if(consumeWS(VARIABLE)) {
          varDecl();
        } else if(consumeWS(FUNCTION)) {
          functionDecl();
        } else if(consumeWS(CONSTRUCTION)) {
          constructionDecl();
        } else if(consumeWS(NAMESPACE)) {
          namespaceDecl();
        } else if(consumeWS(BOUNDARY)) {
          boundarySpaceDecl();
        } else if(consumeWS(OPTION)) {
          optionDecl();
        } else if(consumeWS(ORDERING)) {
          orderingModeDecl();
        } else if(consumeWS(COPYNS)) {
          copyNamespacesDecl();
        } else if(consumeWS(BASEURI)) {
          baseURIDecl();
        } else if(consumeWS(DEFAULT)) {
          if(!defaultNamespaceDecl() && !defaultCollationDecl() &&
              !emptyOrderDecl()) Err.or(list(ELEMENT, COLLATION, EMPTYORDER),
                  DECLINCOMPLETE);
        } else if(consumeWS(FTOPTION)) {
          ftMatchOption(ctx.ftopt);
        } else {
          //alter = DECLINCOMPLETE;
          qp = p;
          return;
        }
      } else if(consumeWS(IMPORT)) {
        if(consumeWS(SCHEMA)) {
          schemaImport();
        } else if(consumeWS(MODULE)) {
          moduleImport();
        } else {
          //Err.or(DECLINCOMPLETE);
          qp = p;
          return;
        }
      } else {
        break;
      }
      skipWS();
      check(';');
    }
  }

  /**
   * [ 10] Parses a namespaceDecl.
   * @throws XQException xquery exception
   */
  private void namespaceDecl() throws XQException {
    final QNm name = new QNm(ncName(XPNAME));
    check(IS);
    name.uri = Uri.uri(stringLiteral());
    if(!ctx.ns.index(name)) Err.or(DUPLNSDECL, name);
  }

  /**
   * [ 11] Parses a BoundarySpaceDecl.
   * @throws XQException xquery exception
   */
  private void boundarySpaceDecl() throws XQException {
    if(declSpaces) Err.or(DUPLBOUND);
    final boolean spaces = consumeWS2(PRESERVE);
    if(!spaces) check(STRIP);
    ctx.spaces = Bln.get(spaces);
    declSpaces = true;
  }

  /**
   * [ 12] Parses a DefaultNamespaceDecl.
   * @return true if declaration was found
   * @throws XQException xquery exception
   */
  private boolean defaultNamespaceDecl() throws XQException {
    final boolean elem = consumeWS(ELEMENT);
    if(!elem && !consumeWS(FUNCTION)) return false;
    check(NAMESPACE);
    final byte[] ns = stringLiteral();
    if(elem) {
      if(declElem) Err.or(DUPLNS);
      ctx.nsElem = Uri.uri(ns);
      declElem = true;
    } else {
      if(declFunc) Err.or(DUPLNS);
      ctx.nsFunc = Uri.uri(ns);
      declFunc = true;
    }
    return true;
  }

  /**
   * [ 13] Parses an OptionDecl.
   * @throws XQException xquery exception
   */
  private void optionDecl() throws XQException {
    // [CG] XQuery/Option Declaration
    final QNm name = new QNm(qName(QNAMEINV));
    final byte[] ns = stringLiteral();
    name.check(ctx);
    if(!name.ns()) Err.or(NSMISS, name);
    // will never be null...
    if(ns == null) Err.or(DECLINCOMPLETE);
  }

  /**
   * [ 14] Parses an OrderingModeDecl.
   * @throws XQException xquery exception
   */
  private void orderingModeDecl() throws XQException {
    if(declOrder) Err.or(DUPLORD);
    final boolean ordered = consumeWS2(ORDERED);
    if(!ordered) check(UNORDERED);
    ctx.ordered = Bln.get(ordered);
    declOrder = true;
  }

  /**
   * [ 15] Parses an emptyOrderDecl.
   * @return true if declaration was found
   * @throws XQException xquery exception
   */
  private boolean emptyOrderDecl() throws XQException {
    if(!consumeWS2(EMPTYORDER)) return false;
    check(EMPTYORD);
    if(declGreat) Err.or(DUPLORDEMP);
    final boolean order = consumeWS2(GREATEST);
    if(!order) check(LEAST);
    ctx.orderGreatest = Bln.get(order);
    declGreat = true;
    return true;
  }

  /**
   * [ 16] Parses a copyNamespacesDecl.
   * [ 17] Parses an PreserveMode.
   * [ 18] Parses an InheritMode.
   * @throws XQException xquery exception
   */
  private void copyNamespacesDecl() throws XQException {
    if(declPres) Err.or(DUPLCOPYNS);
    boolean nsp = consumeWS(PRESERVE);
    if(!nsp) check(NOPRESERVE);
    ctx.nsPreserve = Bln.get(nsp);
    declPres = true;
    consume(',');
    nsp = consumeWS(INHERIT);
    if(!nsp) check(NOINHERIT);
    ctx.nsInherit = Bln.get(nsp);
  }

  /**
   * [ 19] Parses a DefaultCollationDecl.
   * @return query expression
   * @throws XQException xquery exception
   */
  private boolean defaultCollationDecl() throws XQException {
    if(!consumeWS2(COLLATION)) return false;
    ctx.collation = Uri.uri(stringLiteral());
    if(declColl) Err.or(DUPLCOLL);
    declColl = true;
    return true;
  }

  /**
   * [ 20] Parses a BaseURIDecl.
   * @throws XQException xquery exception
   */
  private void baseURIDecl() throws XQException {
    if(declBase) Err.or(DUPLBASE);
    ctx.baseURI = Uri.uri(stringLiteral());
    declBase = true;
  }

  /**
   * [ 21] Parses a SchemaImport.
   * [ 22] Parses a SchemaPrefix.
   * @throws XQException xquery exception
   */
  private void schemaImport() throws XQException {
    if(consumeWS(NAMESPACE)) {
      ncName(null);
      check(IS);
    } else if(consumeWS(DEFAULT)) {
      check(ELEMENT);
      check(NAMESPACE);
    }
    final byte[] ns = stringLiteral();
    if(ns.length == 0) Err.or(NSEMPTY);
    if(consumeWS(AT)) do stringLiteral(); while(consumeWS(COMMA));
    Err.or(IMPLSCHEMA);
  }

  /**
   * [ 23] Parses a ModuleImport.
   * @throws XQException xquery exception
   */
  private void moduleImport() throws XQException {
    QNm name = null;
    if(consumeWS(NAMESPACE)) {
      name = new QNm(ncName(null));
      check(IS);
    } else {
      name = new QNm(EMPTY);
    }
    final byte[] uri = stringLiteral();
    if(uri.length == 0) Err.or(NSMODURI);
    name.uri = Uri.uri(uri);
    ctx.ns.index(name);

    final TokenList fl = new TokenList();
    if(consumeWS(AT)) do fl.add(stringLiteral()); while(consumeWS(COMMA));

    if(modLoaded.contains(uri)) Err.or(DUPLMODULE, name.uri);
    try {
      if(fl.size == 0) {
        boolean found = false;
        for(int n = 0; n < ctx.modules.size; n += 2) {
          if(ctx.modules.list[n].equals(string(uri))) {
            module(ctx.modules.list[n + 1], name.uri);
            modLoaded.add(uri);
            found = true;
          }
        }
        if(!found) Err.or(NOMODULE, uri);
      }
      for(int n = 0; n < fl.size; n++) {
        module(string(fl.list[n]), name.uri);
        modLoaded.add(uri);
      }
    } catch(final StackOverflowError ex) {
      Err.or(CIRCMODULE);
    }
  }

  /**
   * Parses the specified module.
   * @param f file name
   * @param u module uri
   * @throws XQException xquery exception
   */
  private void module(final String f, final Uri u) throws XQException {
    if(ctx.modLoaded.contains(f)) return;
    // check specified path and path relative to query file
    IO fl = new IO(f);
    if(!fl.exists() && file != null) fl = file.merge(fl);

    String query = null;
    try {
      query = Token.string(fl.content());
    } catch(final IOException ex) {
      Err.or(NOMODULEFILE, fl);
    }

    final Namespaces ns = ctx.ns;
    ctx.ns = new Namespaces();
    new XQParser(ctx).parse(query, fl, u);
    ctx.ns = ns;
    ctx.modLoaded.add(f);
  }

  /**
   * [ 24] Parses a VarDecl.
   * @throws XQException xquery exception
   */
  private void varDecl() throws XQException {
    final QNm name = varName();
    if(module != null && !name.uri.eq(module.uri)) Err.or(MODNS, name);

    final SeqType type = consumeWS(AS) ? sequenceType() : null;
    final Var var = new Var(name, type);

    if(consumeWS2(EXTERNAL)) {
      final Var ext = ctx.vars.get(var);
      if(ext != null && type != null) {
        ext.type = type;
        ext.check();
      }
    } else {
      if(ctx.vars.get(var) != null) Err.or(VARDEFINE, var);
      check(ASSIGN);
      ctx.vars.addGlobal(var.expr(check(single(), VARMISSING)));
    }
  }

  /**
   * [ 25] Parses a ConstructionDecl.
   * @throws XQException xquery exception
   */
  private void constructionDecl() throws XQException {
    if(declConstr) Err.or(DUPLCONS);
    final boolean cons = consumeWS2(PRESERVE);
    if(!cons) check(STRIP);
    ctx.construct = Bln.get(cons);
    declConstr = true;
  }

  /**
   * [ 26] Parses a FunctionDecl.
   * @throws XQException xquery exception
   */
  private void functionDecl() throws XQException {
    final QNm name = new QNm(qName(DECLFUNC));
    name.uri = name.ns() ? ctx.ns.uri(name.pre()) : ctx.nsFunc;

    if(name.pre().length == 0 && Type.find(name, true) != null)
      Err.or(FUNCRES, name);
    if(module != null && !name.uri.eq(module.uri)) Err.or(MODNS, name);

    check(PAR1);
    skipWS();
    Var[] args = new Var[0];
    final int s = ctx.vars.size();
    while(curr() == '$') {
      final QNm arg = varName();
      final SeqType argType = consumeWS(AS) ? sequenceType() : null;
      final Var var = new Var(arg, argType);
      ctx.vars.add(var);

      for(final Var v : args) if(v.name.eq(arg)) Err.or(FUNCDUPL, arg);

      args = Array.add(args, var);
      if(!consume(',')) break;
      skipWS();
    }
    check(PAR2);

    final SeqType type = consumeWS(AS) ? sequenceType() : null;
    final Func func = new Func(new Var(name, type), args, true);

    ctx.fun.add(ctx, func);
    if(!consumeWS(EXTERNAL)) func.expr = enclosed(NOFUNBODY);
    ctx.vars.reset(s);
  }

  /**
   * [ 29] Parses an EnclosedExpr.
   * @param err error message
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr enclosed(final Object[] err) throws XQException {
    check(BRACE1);
    final Expr e = check(expr(), err);
    check(BRACE2);
    return e;
  }

  /**
   * [ 31] Parses an Expr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr expr() throws XQException {
    final Expr e = single();
    if(e == null) {
      if(more()) return null;
      if(alter != null) error(); else Err.or(NOEXPR);
    }

    if(!consumeWS2(COMMA)) return e;
    Expr[] list = { e };
    do list = add(list, single()); while(consumeWS2(COMMA));
    return new List(list);
  }

  /**
   * [ 32-0] Parses an ExprSingle (try/catch clause).
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr single() throws XQException {
    if(!consumeWS2(TRY)) return single2();

    final Expr tr = enclosed(NOENCLEXPR);
    check(CATCH);
    check(PAR1);
    final int s = ctx.vars.size();
    Var var1 = null;
    Var var2 = null;
    if(curr() == '$') {
      var1 = new Var(varName());
      ctx.vars.add(var1);
      if(consumeWS2(COMMA)) {
        var2 = new Var(varName());
        if(var1.name.eq(var2.name)) Err.or(VARDEFINED, var2);
        ctx.vars.add(var2);
      }
    }
    check(PAR2);
    final Expr ct = enclosed(NOENCLEXPR);
    ctx.vars.reset(s);
    return new Try(tr, ct, var1, var2);
  }

  /**
   * [ 32] Parses an ExprSingle.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr single2() throws XQException {
    alter = null;
    Expr e = flwor();
    if(e == null) e = quantified();
    if(e == null) e = typeswitch();
    if(e == null) e = iff();
    if(e == null) e = or();
    return e;
  }

  /**
   * [ 33] Parses a FLWORExpr.
   * [ 37] Parses a WhereClause.
   * [ 38] Parses an OrderByClause.
   * [ 39] Parses an OrderSpecList.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr flwor() throws XQException {
    final int s = ctx.vars.size();

    final ForLet[] fl = forLet();
    if(fl == null) return null;

    Expr where = null;
    if(consumeWS(WHERE)) {
      ap = qp;
      where = check(single(), NOWHERE);
      alter = NOWHERE;
    }

    Ord[] order = null;
    final boolean stable = consumeWS(STABLE);
    if(stable) check(EMPTYORDER);

    if(stable || consumeWS(EMPTYORDER)) {
      check(BY);
      ap = qp;
      do order = orderSpec(order); while(consumeWS2(COMMA));
      if(order != null) order = Array.add(order, new Ord());
      alter = ORDERBY;
    }

    if(!consumeWS(RETURN)) {
      if(alter != null) error();
      Err.or(where == null ? FLWORWHERE : order == null ? FLWORORD : FLWORRET);
    }
    final Expr ret = check(single(), NORETURN);
    ctx.vars.reset(s);

    return order == null ? new FLWR(fl, where, ret) :
      new FLWOR(fl, where, new Order(order), ret);
  }

  /**
   * [ 34] Parses a ForClause.
   * [ 35] Parses a PositionalVar.
   * [ 36] Parses a LetClause.
   * [FT37] Parses an FTScoreVar.
   * @return query expression
   * @throws XQException xquery exception
   */
  private ForLet[] forLet() throws XQException {
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
        final SeqType type = !score && consumeWS(AS) ? sequenceType() : null;
        final Var var = new Var(name, type);

        final Var at = fr && consumeWS(AT) ? new Var(varName()) : null;
        final Var sc = fr && consumeWS(SCORE) ? new Var(varName()) : null;

        check(fr ? IN : ASSIGN);
        final Expr e = check(single(), VARMISSING);
        ctx.vars.add(var);

        if(fl == null) fl = new ForLet[1];
        else fl = Array.resize(fl, fl.length, fl.length + 1);
        if(sc != null) {
          if(sc.name.eq(name) || at != null && sc.name.eq(at.name))
            Err.or(VARDEFINED, sc);
          ctx.vars.add(sc);
        }
        if(at != null) {
          if(name.eq(at.name)) Err.or(VARDEFINED, at);
          ctx.vars.add(at);
        }
        fl[fl.length - 1] = fr ? new For(e, var, at, sc) :
          new Let(e, var, score);
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
   * @throws XQException xquery exception
   */
  private Ord[] orderSpec(final Ord[] order) throws XQException {
    final Expr e = check(single(), ORDERBY);
    boolean desc = false;
    if(!consumeWS(ASCENDING)) desc = consumeWS(DESCENDING);
    boolean least = !ctx.orderGreatest.bool();
    if(consumeWS(EMPTYORD)) {
      least = !consumeWS(GREATEST);
      if(least) check(LEAST);
    }
    if(consumeWS(COLLATION)) {
      final byte[] coll = stringLiteral();
      if(!Token.eq(URLCOLL, coll)) Err.or(INVCOLL, coll);
    }
    if(e.e()) return order;
    final Ord ord = new Ord(e, desc, least);
    return order == null ? new Ord[] { ord } : Array.add(order, ord);
  }

  /**
   * [ 42] Parses a QuantifiedExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr quantified() throws XQException {
    final boolean some = consumeWS(SOME, DOLLAR, NOSOME);
    if(!some && !consumeWS(EVERY, DOLLAR, NOSOME)) return null;

    final int s = ctx.vars.size();
    For[] fl = new For[0];
    do {
      final Var var = new Var(varName(), consumeWS(AS) ? sequenceType() : null);
      check(IN);
      final Expr e = check(single(), NOSOME);
      ctx.vars.add(var);
      fl = Array.add(fl, new For(e, var, null, null));
    } while(consumeWS2(COMMA));

    check(SATISFIES);
    final Expr e = check(single(), NOSOME);
    ctx.vars.reset(s);
    return new Satisfy(fl, e, !some);
  }

  /**
   * [ 42] Parses a TypeswitchExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr typeswitch() throws XQException {
    if(!consumeWS(TYPESWITCH, PAR1, TYPEPAR)) return null;

    check(PAR1);
    final Expr e = check(expr(), NOTYPESWITCH);
    check(PAR2);

    Case[] list = new Case[0];
    while(consumeWS(CASE)) {
      final int s = ctx.vars.size();
      skipWS();
      QNm name = null;
      if(curr() == '$') {
        name = varName();
        check(AS);
      }
      final Var var = new Var(name, sequenceType());
      if(name != null) ctx.vars.add(var);
      check(RETURN);
      final Expr ret = check(single(), NOTYPESWITCH);
      list = Array.add(list, new Case(var, ret));
      ctx.vars.reset(s);
    }
    if(list.length == 0) Err.or(NOTYPESWITCH);

    check(DEFAULT);
    skipWS();
    final int s = ctx.vars.size();
    final QNm name = curr() == '$' ? varName() : null;
    final Var var = new Var(name, null);
    if(name != null) ctx.vars.add(var);
    check(RETURN);

    final Expr ret = check(single(), NOTYPESWITCH);
    ctx.vars.reset(s);
    return new TypeSwitch(e, list, var, ret);
  }

  /**
   * [ 45] Parses an IfExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr iff() throws XQException {
    if(!consumeWS(IF, PAR1, IFPAR)) return null;
    check(PAR1);
    final Expr e = check(expr(), NOIF);
    check(PAR2);
    if(!consumeWS(THEN)) Err.or(NOIF);
    final Expr thn = check(single(), NOIF);
    if(!consumeWS(ELSE)) Err.or(NOIF);
    final Expr els = check(single(), NOIF);
    return new If(e, thn, els);
  }

  /**
   * [ 46] Parses an OrExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr or() throws XQException {
    final Expr e = and();
    if(!consumeWS(OR)) return e;

    Expr[] list = { e };
    do list = add(list, and()); while(consumeWS(OR));
    return new Or(list);
  }

  /**
   * [ 47] Parses an AndExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr and() throws XQException {
    final Expr e = comparison();
    if(!consumeWS(AND)) return e;

    Expr[] list = { e };
    do list = add(list, comparison()); while(consumeWS(AND));
    return new And(list);
  }

  /**
   * [ 48] Parses an ComparisonExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr comparison() throws XQException {
    final Expr e = ftContains();
    if(e == null) return null;

    for(final CmpV.COMP c : CmpV.COMP.values()) if(consumeWS(c.name))
        return new CmpV(e, check(ftContains(), CMPEXPR), c);
    for(final CmpN.COMP c : CmpN.COMP.values()) if(consumeWS(c.name))
        return new CmpN(e, check(ftContains(), CMPEXPR), c);
    for(final CmpG.COMP c : CmpG.COMP.values()) if(consumeWS2(c.name))
        return new CmpG(e, check(ftContains(), CMPEXPR), c);

    return e;
  }

  /**
   * [FT51] Parses a FTContainsExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr ftContains() throws XQException {
    final Expr e = range();
    if(!consumeWS(FTCONTAINS)) return e;

    // [CG] XQuery/FTIgnoreOption
    final Expr select = ftSelection();
    //Expr ignore = null;
    if(consumeWS2(WITHOUT) && consumeWS2(CONTENT)) {
      //ignore = unionExpr();
      union();
      Err.or(FTIGNORE);
    }
    return new FTContains(e, select);
  }

  /**
   * [ 49] Parses a RangeExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr range() throws XQException {
    final Expr e = additive();
    if(!consumeWS(TO)) return e;
    return new Range(e, check(additive(), INCOMPLETE));
  }

  /**
   * [ 50] Parses an AdditiveExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr additive() throws XQException {
    Expr e = multiplicative();

    while(true) {
      final Calc c = consume('+') ? Calc.PLUS : consume('-') ?
          Calc.MINUS : null;
      if(c == null) break;
      e = new Clc(e, check(multiplicative(), CALCEXPR), c);
    }
    return e;
  }

  /**
   * [ 51] Parses a MultiplicativeExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr multiplicative() throws XQException {
    Expr e = union();
    if(e == null) return null;

    while(true) {
      final Calc c = consume('*') ? Calc.MULT : consumeWS(DIV) ?
        Calc.DIV : consumeWS(IDIV) ? Calc.IDIV : consumeWS(MOD) ?
        Calc.MOD : null;
      if(c == null) break;
      e = new Clc(e, check(union(), CALCEXPR), c);
    }
    return e;
  }

  /**
   * [ 52] Parses a UnionExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr union() throws XQException {
    final Expr e = intersect();
    if(!consumeWS(UNION) && !consumeWS2(PIPE)) return e;

    Expr[] list = { e };
    do list = add(list, intersect());
    while(consumeWS(UNION) || consumeWS2(PIPE));
    return new Union(list);
  }

  /**
   * [ 52] Parses an IntersectExceptExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr intersect() throws XQException {
    final Expr e = instanceoff();

    if(consumeWS(INTERSECT)) {
      Expr[] list = { e };
      do list = add(list, instanceoff()); while(consumeWS(INTERSECT));
      return new InterSect(list);
    } else if(consumeWS(EXCEPT)) {
      Expr[] list = { e };
      do list = add(list, instanceoff()); while(consumeWS(EXCEPT));
      return new Except(list);
    } else {
      return e;
    }
  }

  /**
   * [ 54] Parses an InstanceofExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr instanceoff() throws XQException {
    final Expr e = treat();
    if(!consumeWS(INSTANCE)) return e;
    check(OF);
    return new Instance(e, sequenceType());
  }

  /**
   * [ 55] Parses a TreatExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr treat() throws XQException {
    final Expr e = castable();
    if(!consumeWS(TREAT)) return e;
    check(AS);
    return new Treat(e, sequenceType());
  }

  /**
   * [ 56] Parses a CastableExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr castable() throws XQException {
    final Expr e = cast();
    if(!consumeWS(CASTABLE)) return e;
    check(AS);
    return new Castable(e, simpleType());
  }

  /**
   * [ 57] Parses a CastExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr cast() throws XQException {
    final Expr e = unary();
    if(!consumeWS(CAST)) return e;
    check(AS);
    return new Cast(e, simpleType());
  }

  /**
   * [ 58] Parses a UnaryExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr unary() throws XQException {
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
        return found ? new Unary(check(e, EVALUNARY), minus) : e;
      }
    } while(true);
  }

  /**
   * [ 59] Parses a ValueExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr value() throws XQException {
    final boolean lax = consumeWS(VALIDATE, LAX, NOVALIDATE);
    if(lax || consumeWS(VALIDATE, STRICT, NOVALIDATE)) validate();

    final Expr e = path();
    return e != null ? e : extension();
  }

  /**
   * [ 63] Parses a ValidateExpr.
   * @throws XQException xquery exception
   */
  private void validate() throws XQException {
    if(!consumeWS2(LAX)) consumeWS2(STRICT);
    check(BRACE1);
    check(single(), NOVALIDATE);
    check(BRACE2);
    Err.or(IMPLVAL);
  }

  /**
   * [ 65] Parses an ExtensionExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr extension() throws XQException {
    if(!consumeWS2(PRAGMA)) return null;

    do {
      // [CG] XQuery/Pragmas
      final QNm name = new QNm(qName(PRAGMAINCOMPLETE));
      if(!name.ns()) Err.or(NSMISS, name);
      name.check(ctx);
      char c = curr();
      if(c != '#' && !ws(c)) Err.or(PRAGMAINCOMPLETE);

      tok.reset();
      while(c != '#' || next() != ')') {
        if(c == 0) Err.or(PRAGMAINCOMPLETE);
        tok.add(consume());
        c = curr();
      }
      tok.finish();
      qp += 2;
    } while(consumeWS(PRAGMA));

    return enclosed(NOPRAGMA);
  }

  /**
   * [ 68] Parses a PathExpr.
   * [ 69] Parses a RelativePathExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr path() throws XQException {
    final int s = consume('/') ? consume('/') ? 2 : 1 : 0;

    final Expr ex = step();
    if(ex == null) {
      if(s > 1) Err.or(PATHMISS);
      return s == 0 ? null : new Root();
    }

    final boolean slash = consume('/');
    if(!slash && s == 0) return ex;

    Expr[] list = {};
    if(s == 2) list = add(list, new Step(Axis.DESCORSELF, Test.NODE,
        new Expr[0]));
    if(s != 0) list = add(list, ex);

    if(slash) {
      do {
        if(consume('/')) list = add(list, new Step(Axis.DESCORSELF,
            Test.NODE, new Expr[0]));
        list = add(list, check(step(), PATHMISS));
      } while(consume('/'));
    }
    return new Path(s > 0 ? new Root() : ex, list);
  }

  /**
   * [ 70] Parses a StepExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr step() throws XQException {
    final Expr e = filter();
    return e != null ? e : axis();
  }

  /**
   * [ 71] Parses an AxisStep.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Step axis() throws XQException {
    Axis ax = null;
    Test test = null;

    if(consumeWS2(DOT2)) {
      ax = Axis.PARENT;
      test = Test.NODE;
    } else if(consume('@')) {
      ax = Axis.ATTR;
      test = test();
    } else {
      for(final Axis a : Axis.values()) {
        if(consumeWS(a.name, COL2, NOLOCSTEP)) {
          consumeWS2(COL2);
          alter = NOLOCSTEP;
          ap = qp;
          ax = a;
          test = test();
          break;
        }
      }
    }
    if(ax == null) {
      ax = Axis.CHILD;
      test = test();
      if(test != null && test.type == Type.ATT) ax = Axis.ATTR;
    }
    if(test == null) return null;

    Expr[] pred = {};
    while(consumeWS2(BR1)) {
      pred = add(pred, expr());
      check(BR2);
    }
    return new Step(ax, test, pred);
  }

  /**
   * [ 78] Parses a NodeTest.
   * [ 79] Parses a NameTest.
   * [123] Parses a KindTest.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Test test() throws XQException {
    final int p = qp;
    final char ch = curr();
    if(XMLToken.isXMLLetter(ch)) {
      final byte[] name = qName(null);
      skipWS();
      if(consume('(')) {
        tok.reset();
        while(!consume(')')) {
          if(curr() == 0) Err.or(TESTINCOMPLETE);
          tok.add(consume());
        }
        final Type type = node(new QNm(name));
        if(type == Type.NOD) return Test.NODE;
        if(type != null) return new Test(type, trim(tok.finish()), ctx);
      } else {
        // nametest abcde:abcde
        if(contains(name, ':')) {
          skipWS();
          return new Test(name, false, ctx);
        }
        // nametest abcde
        if(!consume(':')) {
          skipWS();
          return new Test(name, false, ctx);
        }
        // nametest abcde:*
        if(consume('*')) {
          return new Test(name, false, ctx);
        }
      }
    } else if(consume('*')) {
      // nametest *
      if(!consume(':')) return new Test();
      // nametest *:abcde
      return new Test(qName(null), true, ctx);
    }
    qp = p;
    return null;
  }

  /**
   * [ 81] Parses a FilterExpr.
   * [ 82] Parses a PredicateList.
   * [ 83] Parses a Predicate.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr filter() throws XQException {
    final Expr e = primary();
    if(!consumeWS2(BR1)) return e;

    if(e == null) Err.or(PREDMISSING);
    Expr[] pred = {};
    do { pred = add(pred, expr()); check(BR2); } while(consumeWS2(BR1));
    return new Pred(e, pred);
  }

  /**
   * [ 84] Parses a PrimaryExpr.
   * [ 85] Parses a Literal.
   * [ 87] Parses a VarRef.
   * [ 90] Parses a ContextItem.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr primary() throws XQException {
    skipWS();
    // numbers
    final char c = curr();
    if(digit(c)) return numericLiteral();
    // decimal/double values or context item
    if(c == '.' && next() != '.') {
      consume('.');
      return !digit(curr()) ? new Context() : decimalLiteral(
          new TokenBuilder('.'));
    }
    // strings
    if(quote(c)) return Str.get(stringLiteral());
    // variables
    if(c == '$') {
      final Var var = new Var(varName());
      final Var v = ctx.vars.get(var);
      if(v == null) Err.or(VARNOTDEFINED, var);
      return new VarCall(var);
    }
    // parentheses
    if(c == '(' && next() != '#') return parenthesized();
    // direct constructor
    if(c == '<') return constructor();
    // function calls and computed constructors
    if(letter(c)) {
      Expr e = functionCall();
      if(e != null) return e;
      e = computedConstructor();
      if(e != null) return e;
      // ordered expression
      if(consumeWS(ORDERED) || consumeWS(UNORDERED))
        return enclosed(NOENCLEXPR);
    }
    return null;
  }

  /**
   * [ 86] Parses a NumericLiteral.
   * [141] Parses an IntegerLiteral.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr numericLiteral() throws XQException {
    tok.reset();
    while(digit(curr())) tok.add(consume());
    if(letter(curr())) return checkDbl(tok);
    if(!consume('.')) {
      final long l = toLong(tok.finish());
      if(l == Long.MIN_VALUE) Err.or(BOUNDS, tok);
      return Itr.get(l);
    }
    tok.add('.');
    return decimalLiteral(tok);
  }

  /**
   * Checks if a number is followed by a whitespace.
   * @param tb token builder
   * @return expression
   * @throws XQException xquery exception
   */
  private Expr checkDbl(final TokenBuilder tb) throws XQException {
    if(!consume('e') && !consume('E')) Err.or(NUMBERWS);
    return decimalDouble(tb);
  }

  /**
   * [ 88] Parses a VarName.
   * @return query expression
   * @throws XQException xquery exception
   */
  private QNm varName() throws XQException {
    check(DOLLAR);
    final QNm name = new QNm(qName(NOVARNAME));
    if(name.ns()) name.uri = ctx.ns.uri(name.pre());
    ctx.ns.uri(name);
    return name;
  }

  /**
   * [ 89] Parses a ParenthesizedExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr parenthesized() throws XQException {
    check(PAR1);
    final Expr e = expr();
    check(PAR2);
    return e == null ? Seq.EMPTY : e;
  }

  /**
   * [ 93] Parses a FunctionCall.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr functionCall() throws XQException {
    final int p = qp;
    final QNm name = new QNm(qName(null));
    if(!consumeWS2(PAR1) || node(name) != null) {
      qp = p;
      return null;
    }

    // name and opening bracket found
    Expr[] exprs = {};
    while(curr() != 0) {
      if(consumeWS2(PAR2)) {
        alter = new Object[] { name };
        ap = qp;
        ctx.ns.uri(name);
        name.uri = name.ns() ? ctx.ns.uri(name.pre()) : ctx.nsFunc;
        final Expr[] args = exprs;
        final Expr func = ctx.fun.get(ctx, name, args);
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
    Err.or(FUNCMISS, name.str());
    return null;
  }

  /**
   * [ 94] Parses a Constructor.
   * [ 95] Parses a DirectConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr constructor() throws XQException {
    check('<');
    return consume('!') ? dirCommentConstructor() :
      consume('?') ? dirPIConstructor() : dirElemConstructor();
  }

/**
   * [ 96] Parses a DirElemConstructor.
   * [ 97-100] Parses attributes.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr dirElemConstructor() throws XQException {
    if(skipWS()) Err.or(NOTAGNAME);
    final QNm open = new QNm(qName(NOTAGNAME));
    consumeWSS();

    Expr[] cont = {};
    Uri taguri = null;
    FAttr[] ns = {};

    // parse attributes...
    while(XMLToken.isXMLLetter(curr())) {
      final QNm atn = new QNm(qName(null));
      Expr[] attv = {};

      consumeWSS();
      check('=');
      consumeWSS();
      final char delim = consume();
      if(!quote(delim)) Err.or(NOQUOTE, found());
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
            qp++;
            check('}');
            tb.add('}');
          } else if(c == '<' || c == 0) {
            Err.or(NOQUOTE, found());
          } else if(c == 0x0A || c == 0x09) {
            qp++;
            tb.add(' ');
          } else {
            entity(tb);
          }
        }
        if(!consume(delim)) break;
        tb.add(delim);
      } while(true);

      if(tb.size != 0) attv = add(attv, Str.get(tb.finish()));

      if(eq(atn.str(), XMLNS)) {
        if(!simple) Err.or(NSCONS);
        if(taguri != null) Err.or(DUPLNSDEF, atn);
        final byte[] v = attv.length == 0 ? EMPTY : attv[0].str();
        taguri = Uri.uri(v);
      } else if(eq(atn.pre(), XMLNS)) {
        if(!simple) Err.or(NSCONS);
        final byte[] v = attv.length == 0 ? EMPTY : attv[0].str();
        if(v.length == 0) Err.or(NSEMPTYURI);
        atn.uri = Uri.uri(v);
        ctx.ns.index(atn);
        ns = checkNS(ns, atn, v);
      } else {
        cont = add(cont, new CAttr(atn, attv, false));
      }
      if(!consumeWSS()) break;
    }

    // ...parse function arguments
    if(taguri == null) ctx.ns.uri(open);
    else open.uri = taguri;

    if(consume('/')) {
      check('>');
    } else {
      check('>');
      while(curr() != '<' || next() != '/') {
        final Expr e = dirElemContent(open);
        if(e == null) continue;
        cont = add(cont, e);
      }
      qp += 2;

      if(skipWS()) Err.or(NOTAGNAME);
      final byte[] close = qName(NOTAGNAME);
      consumeWSS();
      check('>');
      if(!eq(open.str(), close)) Err.or(TAGWRONG, open.str(), close);
    }
    return new CElem(open, cont, ns);
  }

  /**
   * Checks the uniqueness of the namespace and adds it to the array.
   * @param ns namespace array
   * @param a namespace to be checked
   * @param v namespace value
   * @return new array
   * @throws XQException xquery exception
   */
  private FAttr[] checkNS(final FAttr[] ns, final QNm a,
      final byte[] v) throws XQException {
    final int xl = ns.length;
    for(int x = 0; x < xl; x++) if(ns[x].qname().eq(a)) Err.or(DUPLNSDEF, a);
    return Array.add(ns, new FAttr(a, v, null));
  }

  /**
   * [101] Parses a DirElemContent.
   * @param tag opening tag
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr dirElemContent(final QNm tag) throws XQException {
    final TokenBuilder tb = new TokenBuilder();
    do {
      final char c = curr();
      if(c == '<') {
        if(consumeWS2(CDATA)) {
          tb.add(cDataSection());
          tb.ent = true;
        } else {
          final byte[] txt = text(tb);
          if(txt != null) return Str.get(txt);
          return next() == '/' ? null : constructor();
        }
      } else if(c == '{') {
        if(next() == '{') {
          tb.add(consume());
          consume();
        } else {
          final byte[] txt = text(tb);
          return txt != null ? Str.get(txt) : enclosed(NOENCLEXPR);
        }
      } else if(c == '}') {
        consume();
        check('}');
        tb.add('}');
      } else if(c == 0) {
        Err.or(NOCLOSING, tag);
      } else {
        entity(tb);
      }
    } while(true);
  }

  /**
   * Returns a text array.
   * @param tb token builder
   * @return text or null reference
   */
  private byte[] text(final TokenBuilder tb) {
    final byte[] t = tb.finish();
    return t.length == 0 || !tb.ent && !ctx.spaces.bool() &&
      ws(t) ? null : t;
  }

  /**
   * [103] Parses a DirCommentConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr dirCommentConstructor() throws XQException {
    check('-');
    check('-');
    final TokenBuilder tb = new TokenBuilder();
    do {
      while(not('-')) tb.add(consume());
      consume();
      if(consume('-')) {
        check('>');
        return new CComm(Str.get(tb.finish()));
      }
      tb.add('-');
    } while(true);
  }

  /**
   * [105-106] Parses a DirPIConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr dirPIConstructor() throws XQException {
    if(consumeWSS()) Err.or(PIXML, EMPTY);
    final byte[] str = qName(PIWRONG);
    final Expr pi = Str.get(str);
    if(str.length == 0 || Token.eq(Token.lc(str), XML)) Err.or(PIXML, pi);

    final boolean space = skipWS();
    final TokenBuilder tb = new TokenBuilder();
    do {
      while(not('?')) {
        if(!space) Err.or(PIWRONG);
        tb.add(consume());
      }
      consume();
      if(consume('>')) return new CPI(pi, Str.get(tb.finish()));
      tb.add('?');
    } while(true);
  }

  /**
   * [107] Parses a CDataSection.
   * @return CData
   * @throws XQException xquery exception
   */
  private byte[] cDataSection() throws XQException {
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      while(not(']')) tb.add(consume());
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
   * @throws XQException xquery exception
   */
  private Expr computedConstructor() throws XQException {
    final int p = qp;
    if(consumeWS(DOCUMENT)) return consume(compDocConstructor(), p);
    else if(consumeWS(ELEMENT)) return consume(compElemConstructor(), p);
    else if(consumeWS(ATTRIBUTE)) return consume(compAttrConstructor(), p);
    else if(consumeWS(TEXT)) return consume(compTextConstructor(), p);
    else if(consumeWS(COMMENT)) return consume(compCommentConstructor(), p);
    else if(consumeWS(PI)) return consume(compPIConstructor(), p);
    return null;
  }

  /**
   * [110] Parses a CompDocConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr compDocConstructor() throws XQException {
    if(!consumeWS2(BRACE1)) return null;
    final Expr e = check(expr(), NODOCCONS);
    consumeWS2(BRACE2);
    return new CDoc(e);
  }

  /**
   * [111] Parses a CompElemConstructor.
   * [112] Parses a ContextExpr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr compElemConstructor() throws XQException {
    skipWS();

    Expr name;
    if(XMLToken.isXMLLetter(curr())) {
      name = Str.get(qName(null));
    } else {
      if(!consumeWS2(BRACE1)) return null;
      name = check(expr(), NOTAGNAME);
      check(BRACE2);
    }

    check(BRACE1);
    final Expr e = expr();
    check(BRACE2);
    return new CElem(name, e == null ? new Expr[0] : new Expr[] { e },
        new FAttr[] {});
  }

  /**
   * [113] Parses a CompAttrConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr compAttrConstructor() throws XQException {
    skipWS();

    Expr nm;
    if(XMLToken.isXMLLetter(curr())) {
      nm = Str.get(qName(null));
    } else {
      if(!consumeWS2(BRACE1)) return null;
      nm = expr();
      check(BRACE2);
    }

    check(BRACE1);
    final Expr e = expr();
    check(BRACE2);
    return new CAttr(nm, new Expr[] { e == null ? Seq.EMPTY : e }, true);
  }

  /**
   * [114] Parses a CompTextConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr compTextConstructor() throws XQException {
    if(!consumeWS2(BRACE1)) return null;
    final Expr e = check(expr(), NOTXTCONS);
    check(BRACE2);
    return new CText(e);
  }

  /**
   * [115] Parses a CompCommentConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr compCommentConstructor() throws XQException {
    if(!consumeWS2(BRACE1)) return null;
    final Expr e = check(expr(), NOCOMCONS);
    consumeWS2(BRACE2);
    return new CComm(e);
  }

  /**
   * [116] Parses a CompPIConstructor.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr compPIConstructor() throws XQException {
    skipWS();
    Expr name;
    if(XMLToken.isXMLLetter(curr())) {
      name = Str.get(ncName(null));
    } else {
      if(!consumeWS2(BRACE1)) return null;
      name = check(expr(), PIWRONG);
      check(BRACE2);
    }

    check(BRACE1);
    Expr e = expr();
    if(e == null) e = Seq.EMPTY;
    check(BRACE2);
    return new CPI(name, e);
  }

  /**
   * [117] Parses a SimpleType.
   * @return sequence type
   * @throws XQException xquery exception
   */
  private SeqType simpleType() throws XQException {
    final QNm type = new QNm(qName(TYPEINVALID));
    ctx.ns.uri(type);
    skipWS();
    final SeqType seq = new SeqType(type, consume('?') ? 1 : 0, null);

    if(seq.type == null) {
      final byte[] uri = type.uri.str();
      if(uri.length == 0 && type.ns()) Err.or(PREUNKNOWN, type.pre());
      final byte[] ln = type.ln();
      Err.or(Token.eq(uri, Type.NOT.uri) && (Token.eq(Type.NOT.name, ln) ||
        Token.eq(Type.AAT.name, ln))  ? CASTUNKNOWN : TYPEUNKNOWN, type);
    }
    return seq;
  }

  /**
   * [119] Parses a SequenceType.
   * [120] Parses an OccurrenceIndicator.
   * [123] Parses a KindTest.
   * @return sequence type
   * @throws XQException xquery exception
   */
  private SeqType sequenceType() throws XQException {
    final QNm type = new QNm(qName(TYPEINVALID));
    tok.reset();
    final boolean par = consumeWS2(PAR1);
    if(par) {
      while(!consumeWS2(PAR2)) {
        if(curr() == 0) Err.or(FUNCMISS, type.str());
        skipWS();
        tok.add(consume());
      }
    }
    skipWS();
    final int mode = consume('?') ? 1 : consume('+') ? 2 :
      consume('*') ? 3 : 0;
    if(type.ns()) type.uri = ctx.ns.uri(type.pre());

    final SeqType seq = new SeqType(type, mode, tok.finish());
    if(seq.type == Type.EMP && mode != 0) Err.or(EMPTYSEQOCC, seq.type);
    if(seq.type == null) Err.or(par ? NOTYPE : TYPEUNKNOWN, type, par);
    return seq;
  }

  /**
   * [142] Parses a DecimalLiteral.
   * @param tb start of number
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr decimalLiteral(final TokenBuilder tb) throws XQException {
    if(letter(curr())) return checkDbl(tb);
    while(digit(curr())) tb.add(consume());
    return letter(curr()) ? checkDbl(tb) : new Dec(tb.finish());
  }

  /**
   * [142] Parses a DecimalDouble.
   * @param tb start of number
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr decimalDouble(final TokenBuilder tb) throws XQException {
    tb.add('e');
    if(curr() == '+' || curr() == '-') tb.add(consume());
    boolean dig = false;
    while(digit(curr())) {
      tb.add(consume());
      dig = true;
    }
    if(!dig) Err.or(NUMBERINC);
    if(letter(curr())) Err.or(NUMBERWS);
    return Dbl.get(tb.finish());
  }

  /**
   * [144] Parses a StringLiteral.
   * @return query expression
   * @throws XQException xquery exception
   */
  private byte[] stringLiteral() throws XQException {
    skipWS();
    final char delim = curr();
    if(!quote(delim)) Err.or(NOQUOTE, found());
    consume();
    tok.reset();
    while(true) {
      while(!consume(delim)) {
        if(curr() == 0) Err.or(NOQUOTE, found());
        entity(tok);
      }
      if(!consume(delim)) break;
      tok.add(delim);
    }
    return tok.finish();
  }

  /**
   * [FT144] Parses an FTSelection.
   * [FT157] Parses an FTPosFilter.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr ftSelection() throws XQException {
    final Expr e = ftOr();
    final FTPos pos = new FTPos();
    final FTSelect sel = new FTSelect(e, pos);

    while(true) {
      if(consumeWS(ORDERED)) {
        pos.ordered = true;
      } else if(consumeWS(WINDOW)) {
        sel.window = additive();
        pos.wunit = ftUnit();
      } else if(consumeWS(DISTANCE)) {
        sel.dist = ftRange();
        if(sel.dist == null) Err.or(FTRANGE);
        pos.dunit = ftUnit();
      } else if(consumeWS(AT)) {
        pos.start = consumeWS(START);
        if(!pos.start) pos.end = consumeWS(END);
        if(!pos.start && !pos.end) Err.or(INCOMPLETE);
      } else if(consumeWS(ENTIRE)) {
        check(CONTENT);
        pos.content = true;
      } else {
        final boolean same = consumeWS(SAME);
        final boolean diff = !same && consumeWS(DIFFERENT);
        if(!same && !diff) break;
        pos.same = same;
        pos.different = diff;
        if(consumeWS(SENTENCE)) pos.sdunit = FTUnit.SENTENCES;
        else if(consumeWS(PARAGRAPH)) pos.sdunit = FTUnit.PARAGRAPHS;
        else Err.or(INCOMPLETE);
      }
    }

    sel.weight = consumeWS(WEIGHT) ? range() : Dbl.ONE;
    return sel;
  }

  /**
   * [FT145] Parses FTOr.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr ftOr() throws XQException {
    final Expr e = ftAnd();
    if(!consumeWS(FTOR)) return e;

    Expr[] list = { e };
    do list = add(list, ftAnd()); while(consumeWS(FTOR));
    return new FTOr(list);
  }

  /**
   * [FT146] Parses FTAnd.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr ftAnd() throws XQException {
    final Expr e = ftMildNot();
    if(!consumeWS(FTAND)) return e;

    Expr[] list = { e };
    do list = add(list, ftMildNot()); while(consumeWS(FTAND));
    return new FTAnd(list);
  }

  /**
   * [FT147] Parses FTMildNot.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr ftMildNot() throws XQException {
    final Expr e = ftUnaryNot();
    if(!consumeWS(NOT)) return e;

    Expr[] list = { e };
    do { check(IN); list = add(list, ftUnaryNot()); } while(consumeWS(NOT));
    Err.or(FTMILD);
    return new FTMildNot(list);
  }

  /**
   * [FT148] Parses FTUnaryNot.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr ftUnaryNot() throws XQException {
    final boolean not = consumeWS(FTNOT);
    final FTOptions e = ftPrimaryWithOptions();
    return not ? new FTNot(e) : e;
  }

  /**
   * [FT149] Parses FTPrimaryWithOptions.
   * @return query expression
   * @throws XQException xquery exception
   */
  private FTOptions ftPrimaryWithOptions() throws XQException {
    final FTOpt opt = new FTOpt();
    final FTOptions ftopt = new FTOptions(ftPrimary(), opt);
    while(ftMatchOption(opt));
    return ftopt;
  }

  /**
   * [FT150] Parses an FTWordsSelection.
   * [FT151] Parses FTWords.
   * [FT152] Parses an FTWordsValue.
   * [FT154] Parses FTAnyallOption.
   * [FT155] Parses FTTimes.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr ftPrimary() throws XQException {
    if(consumeWS(PAR1)) {
      final Expr e = ftSelection();
      check(PAR2);
      return e;
    }

    skipWS();
    final Expr e = quote(curr()) ? Str.get(stringLiteral()) :
      enclosed(NOENCLEXPR);

    // FTAnyAllOption
    FTMode mode = FTMode.ANY;
    if(consumeWS(ALL)) {
      mode = consumeWS(WORDS) ? FTMode.ALLWORDS : FTMode.ALL;
    } else if(consumeWS(ANY)) {
      mode = consumeWS(WORD) ? FTMode.ANYWORD : FTMode.ANY;
    } else if(consumeWS(PHRASE)) {
      mode = FTMode.PHRASE;
    }

    // FTTimes
    Expr[] occ = { Itr.get(1), Itr.get(Long.MAX_VALUE) };
    if(consumeWS(OCCURS)) {
      occ = ftRange();
      if(occ == null) Err.or(FTRANGE);
      consumeWS(TIMES);
    }
    return new FTWords(e, mode, occ);
  }

  /**
   * [FT156] Parses an FTRange.
   * @return query expression
   * @throws XQException xquery exception
   */
  private Expr[] ftRange() throws XQException {
    final Expr[] occ = { Itr.get(1), Itr.get(Long.MAX_VALUE) };
    if(consumeWS(EXACTLY)) {
      occ[0] = union();
      occ[1] = occ[0];
    } else if(consumeWS(AT)) {
      if(consumeWS(LEAST)) {
        occ[0] = union();
      } else {
        check(MOST);
        occ[0] = Itr.get(0);
        occ[1] = union();
      }
    } else if(consumeWS(FROM)) {
      occ[0] = union();
      check(TO);
      occ[1] = union();
    } else {
      return null;
    }
    return occ;
  }

  /**
   * [FT156] Parses an FTUnit.
   * @return query expression
   * @throws XQException xquery exception
   */
  private FTUnit ftUnit() throws XQException {
    if(consumeWS(WORDS)) return FTUnit.WORDS;
    if(consumeWS(SENTENCES)) return FTUnit.SENTENCES;
    if(consumeWS(PARAGRAPHS)) return FTUnit.PARAGRAPHS;
    Err.or(INCOMPLETE);
    return null;
  }

  /**
   * [FT154] Parses an FTMatchOption.
   * @param opt options instance
   * @return false if no options were found
   * @throws XQException xquery exception
   */
  private boolean ftMatchOption(final FTOpt opt) throws XQException {
    // [CG] XQuery/FTMatchOptions: thesaurus

    if(consumeWS(LOWERCASE)) {
      if(opt.lc || opt.uc || opt.cs) Err.or(FTCASE);
      opt.lc = true;
      opt.cs = true;
    } else if(consumeWS(UPPERCASE)) {
      if(opt.lc || opt.uc || opt.cs) Err.or(FTCASE);
      opt.uc = true;
      opt.cs = true;
    } else if(consumeWS(CASE)) {
      if(opt.lc || opt.uc || opt.cs) Err.or(FTCASE);
      opt.cs = consumeWS(SENSITIVE);
      if(!opt.cs) check(INSENSITIVE);
    } else if(consumeWS(DIACRITICS)) {
      if(opt.dc) Err.or(FTDIA);
      opt.dc = consumeWS(SENSITIVE);
      if(!opt.dc) check(INSENSITIVE);
    } else if(consumeWS(LANGUAGE)) {
      opt.ln = lc(stringLiteral());
      if(!eq(opt.ln, EN)) Err.or(FTLAN, opt.ln);
    } else {
      final int p = qp;
      final boolean with = consumeWS(WITH);
      if(!with && !consumeWS(WITHOUT)) return false;

      if(consumeWS2(STEMMING)) {
        opt.st = with;
      } else if(consumeWS2(THESAURUS)) {
        opt.ts = with;
        if(with) {
          final boolean par = consumeWS2(PAR1);
          if(consumeWS2(AT)) {
            ftThesaurusID();
          } else {
            check(DEFAULT);
          }
          while(par && consumeWS2(COMMA)) ftThesaurusID();
          if(par) check(PAR2);
          Err.or(FTTHES);
        }
      } else if(consumeWS(STOP)) {
        // add union/except
        check(WORDS);
        opt.sw = new Set();
        boolean union = false;
        boolean except = false;
        while(with) {
          if(consumeWS2(PAR1)) {
            do {
              final byte[] sl = stringLiteral();
              if(except) opt.sw.delete(sl);
              else if(!union || opt.sw.id(sl) == 0) opt.sw.add(sl);
            } while(consumeWS2(COMMA));
            check(PAR2);
          } else if(consumeWS2(AT)) {
            IO fl = new IO(string(stringLiteral()));
            if(!fl.exists() && ctx.file != null) {
              fl = file.merge(fl);
              if(!fl.exists()) Err.or(NOSTOPFILE, fl);
            }
            try {
              for(final byte[] sl : split(norm(fl.content()), ' ')) {
                if(except) opt.sw.delete(sl);
                else if(!union || opt.sw.id(sl) == 0) opt.sw.add(sl);
              }
            } catch(final IOException ex) {
              Err.or(NOSTOPFILE, fl);
            }
          } else if(!union && !except) {
            Err.or(FTSTOP);
          }
          union = consumeWS2(UNION);
          except = !union && consumeWS2(EXCEPT);
          if(!union && !except) break;
        }
      } else if(consumeWS(DEFAULT)) {
        check(STOP);
        check(WORDS);
      } else if(consumeWS2(WILDCARDS)) {
        if(opt.fz) Err.or(FTFZWC);
        opt.wc = with;
      } else if(consumeWS2(FUZZY)) {
        if(opt.wc) Err.or(FTFZWC);
        opt.fz = with;
      } else {
        qp = p;
        return false;
      }
    }
    return true;
  }

  /**
   * [FT171] Parses an FTThesaurusID.
   * @throws XQException xquery exception
   */
  private void ftThesaurusID() throws XQException {
    stringLiteral();
    if(consumeWS2(RELATIONSHIP)) stringLiteral();
    if(ftRange() != null) check(LEVELS);
  }

  /**
   * Parses an NCName.
   * @param err optional error message
   * @return string
   * @throws XQException xquery exception
   */
  private byte[] ncName(final Object[] err) throws XQException {
    skipWS();
    tok.reset();
    if(ncName(true)) return tok.finish();
    if(err != null) Err.or(err);
    return Token.EMPTY;
  }

  /**
   * Parses a QName.
   * @param err optional error message
   * @return string
   * @throws XQException xquery exception
   */
  private byte[] qName(final Object[] err) throws XQException {
    skipWS();
    tok.reset();
    final boolean ok = ncName(true);
    if(ok && consume(':')) ncName(false);
    if(!ok && err != null) Err.or(err);
    return tok.finish();
  }

  /**
   * Helper method for parsing NCNames.
   * @param first flag for first call
   * @return true for success
   */
  private boolean ncName(final boolean first) {
    char c = curr();
    if(!XMLToken.isXMLLetter(c)) {
      if(!first) qp--;
      return false;
    }
    if(!first) tok.add(':');
    do {
      tok.add(consume());
      c = curr();
      if(!XMLToken.isXMLLetterOrDigit(c) && c != '-' && c != '_' && c != '.')
        break;
    } while(c != 0);
    return true;
  }

  /**
   * Parse and convert entities.
   * @param tb token builder
   * @throws XQException xquery exception
   */
  void entity(final TokenBuilder tb) throws XQException {
    final String ent = ent(tb);
    if(ent != null)  Err.or(ENTINVALID, ent);
  }

  /**
   * Throws an exception if the specified expression is empty.
   * @param expr expression
   * @param err error message
   * @return expression
   * @throws XQException xquery exception
   */
  private Expr check(final Expr expr, final Object[] err) throws XQException {
    if(expr == null) Err.or(err);
    return expr;
  }

  /**
   * Checks for the specified character and throws an exception if
   * something else is found.
   * @param ch character to be found.
   * @throws XQException xquery exception
   */
  private void check(final int ch) throws XQException {
    if(!consume(ch)) Err.or(list(Character.toString((char) ch)),
        WRONGCHAR, (char) ch, found());
  }

  /**
   * Checks for the specified string and throws an exception if
   * something else is found.
   * @param s string to be found.
   * @throws XQException xquery exception
   */
  private void check(final String s) throws XQException {
    if(!consumeWS2(s)) Err.or(list(s), WRONGCHAR, s, found());
  }

  /**
   * Checks if the specified character is not found; if the input is
   * exhausted, throws an exception.
   * @param ch character to be found.
   * @return result of check
   * @throws XQException xquery exception
   */
  private boolean not(final int ch) throws XQException {
    final char c = curr();
    if(c == 0) Err.or(WRONGEND, ch);
    return c != ch;
  }

  /**
   * Consumes the specified expression or resets the query position.
   * @param expr expression
   * @param p query position
   * @return expression or null
   */
  private Expr consume(final Expr expr, final int p) {
    if(expr != null) return expr;
    qp = p;
    return expr;
  }

  /**
   * Consumes the specified token and surrounding whitespaces.
   * @param t token to consume
   * @return true if token was found
   * @throws XQException xquery exception
   */
  private boolean consumeWS(final String t) throws XQException {
    final int p = qp;
    final boolean ok = consumeWS2(t);
    final int q = qp;
    final int c = curr();
    final boolean ok2 = skipWS() || (!XMLToken.isFirstLetter(t.charAt(0)) ||
        !XMLToken.isXMLLetterOrDigit(c) && c != '-');
    qp = ok2 ? q : p;
    return ok && ok2;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position.
   * @param s1 string to be consumed
   * @param s2 second string
   * @param expr alternative error message
   * @return result of check
   * @throws XQException xquery exception
   */
  private boolean consumeWS(final String s1, final String s2,
      final Object[] expr) throws XQException {
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
   * @throws XQException xquery exception
   */
  private boolean consumeWS2(final String str) throws XQException {
    skipWS();
    return consume(str);
  }

  /**
   * Consumes all whitespace characters from the remaining query.
   * @return true if whitespaces were found
   * @throws XQException xquery exception
   */
  private boolean skipWS() throws XQException {
    final int p = qp;
    while(more()) {
      final int c = curr();
      if(c == '(' && next() == ':') {
        comment();
      } else {
        if(c <= 0 || c > ' ') return p != qp;
        qp++;
      }
    }
    return true;
  }

  /**
   * Consumes a comment.
   * @throws XQException xquery exception
   */
  private void comment() throws XQException {
    qp++;
    while(++qp < ql) {
      if(curr('(') && next() == ':') comment();
      if(curr(':') && next() == ')') {
        qp += 2;
        return;
      }
    }
    Err.or(COMCLOSE);
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
      qp++;
    }
    return true;
  }

  /**
   * Throws the alternative error message.
   * @throws XQException xquery exception
   */
  void error() throws XQException {
    qp = ap;
    if(alter.length != 1) Err.or(alter);
    ctx.fun.funError((QNm) alter[0]);
    Err.or(FUNCUNKNOWN, ((QNm) alter[0]).str());
  }

  /**
   * Finds and returns the specified node type.
   * @param type type as string
   * @return type or null
   */
  public static Type node(final QNm type) {
    final byte[] ln = type.ln();
    final byte[] uri = type.uri.str();
    for(final Type t : Type.values()) {
      if(t.node() && eq(ln, t.name) && eq(uri, t.uri)) return t;
    }
    return null;
  }

  /**
   * Adds an expression to the specified array.
   * @param ar input array
   * @param e new expression
   * @return new array
   * @throws XQException xquery exception
   */
  private static Expr[] add(final Expr[] ar, final Expr e) throws XQException {
    if(e == null) Err.or(INCOMPLETE);
    final int size = ar.length;
    final Expr[] t = new Expr[size + 1];
    System.arraycopy(ar, 0, t, 0, size);
    t[size] = e;
    return t;
  }
}
