package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.ann.*;
import org.basex.query.ann.Annotation;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr;
import org.basex.query.expr.List;
import org.basex.query.expr.Lookup;
import org.basex.query.expr.Pragma;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.ft.FTAnd;
import org.basex.query.expr.ft.FTContent;
import org.basex.query.expr.ft.FTDistance;
import org.basex.query.expr.ft.FTMildNot;
import org.basex.query.expr.ft.FTOr;
import org.basex.query.expr.ft.FTScope;
import org.basex.query.expr.ft.FTWeight;
import org.basex.query.expr.ft.FTWindow;
import org.basex.query.expr.ft.FTWords;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Axis;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.parse.gen.*;
import org.basex.query.parse.gen.Nodes.*;
import org.basex.query.parse.gen.XQuerySyntaxParser.*;
import org.basex.query.parse.syntax.*;
import org.basex.query.parse.syntax.Terminal;
import org.basex.query.scope.*;
import org.basex.query.scope.LibraryModule;
import org.basex.query.scope.MainModule;
import org.basex.query.up.expr.*;
import org.basex.query.up.expr.Insert.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.format.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.ArrayType;
import org.basex.query.value.type.ChoiceItemType;
import org.basex.query.value.type.MapType;
import org.basex.query.var.*;
import org.basex.query.var.VarRef;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Parser for XQuery expressions, which is driven by a syntax tree. The syntax tree is created
 * by the REx generated parser: its nodes are records, whose components are the children of the
 * nonterminals. The tree is evaluated without scanning the input again, except for the values of
 * string literals and entities. Error positions are the start offsets of nodes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RExQueryParser extends AQueryParser {
  /**
   * Constructor.
   * @param query query string
   * @param uri base URI (can be {@code null}; only passed on if not bound to static context yet)
   * @param qctx query context
   * @param sctx static context (can be {@code null})
   */
  RExQueryParser(final String query, final String uri, final QueryContext qctx,
      final StaticContext sctx) {
    super(query, uri, qctx, sctx);
  }

  /** Kinds of literals. */
  private static final BitSet LITERALS = Sym.set(Sym.INTEGER_LITERAL,
      Sym.HEX_INTEGER_LITERAL, Sym.BINARY_INTEGER_LITERAL, Sym.DECIMAL_LITERAL,
      Sym.DOUBLE_LITERAL, Sym.STRING_LITERAL, Sym.QNameLiteral);

  /** Maximum number of expected tokens that are listed in a syntax error. */
  private static final int MAX_EXPECTED = 3;

  /** Declarations of the second part of the prolog, which can refer to imported modules. */
  private static final BitSet DECLARATIONS = Sym.set(Sym.ContextValueDecl, Sym.VarDecl,
      Sym.FunctionDecl, Sym.ItemTypeDecl, Sym.LabeledRecordTypeDecl, Sym.OptionDecl);

  /**
   * Declarations after which the comments are processed before the documentation is cleared.
   * Their last token consumes the whitespace and comments that follow it.
   */
  private static final BitSet TRAILING = Sym.set(Sym.BoundarySpaceDecl,
      Sym.ConstructionDecl, Sym.OrderingModeDecl, Sym.RevalidationDecl,
      Sym.CopyNamespacesDecl, Sym.EmptyOrderDecl, Sym.DecimalFormatDecl,
      Sym.ContextValueDecl);

  /** No nodes. */
  private static final Node[] NONE = { };

  /** QName check: skip namespace check. */
  private static final byte[] SKIPCHECK = {};

  /** Absolute value of the minimum integer (2^63). */
  private static final BigInteger MIN_INTEGER = BigInteger.ONE.shiftLeft(63);

  /** Axes. */
  private static final Axis[] AXES = Axis.values();

  /** Namespaces. */
  private final TokenObjectMap<byte[]> namespaces = new TokenObjectMap<>();

  /** Parsed variables. */
  private final ArrayList<StaticVar> vars = new ArrayList<>();

  /** Type names referenced by the item type that is currently parsed; {@code null} otherwise. */
  private QNmSet currentTypeDeps;

  /** Options. */
  private final QNmMap<String> options = new QNmMap<>();

  /** Declared flags. */
  private final HashSet<String> decl = new HashSet<>();

  /** Output declarations. */
  private final HashMap<String, Object> sparams = new HashMap<>();

  /** Namespaces of parsed element constructors, to be enriched by enclosing constructors. */
  private final ArrayList<ConstrNS> constrNS = new ArrayList<>();

  /** Temporary token cache. */
  private final TokenBuilder token = new TokenBuilder();

  /** Range error for literal 2^63, which is valid if negated (can be {@code null}). */
  private Expr minInteger;

  /** Gaps of the syntax tree: pairs of start and end offsets. */
  private int[] gaps;

  /** Index of the next gap that has not been scanned for comments. */
  private int gap;

  /**
   * Namespaces of an element constructor.
   * @param nspaces declared namespaces
   * @param inherited inherited namespaces
   */
  private record ConstrNS(Atts nspaces, Atts inherited) { }

  /**
   * Parses the "MainModule" rule.
   * @return module
   * @throws QueryException query exception
   */
  @Override
  MainModule parseMain() throws QueryException {
    init();
    try {
      final Nodes.Module module = syntaxTree();
      versionDecl(module.versionDecl());

      final Nodes.LibraryModule library = module.libraryModule();
      if(library != null) {
        pos = after(library.moduleDecl().begin() + MODULE.length());
        throw error(MAINMOD);
      }
      final Nodes.MainModule main = module.mainModule();
      prolog(main.prolog());

      localVars.pushContext(false);
      final Expr expr = single(main.queryBody());
      final VarScope vs = localVars.popContext();
      comments(Integer.MAX_VALUE);
      pos = input.length;
      final MainModule mm = new MainModule(expr, vs, sc);
      mm.set(funcs, vars, sc.imports, namespaces, options, moduleDoc);
      finish();
      check(mm);
      return mm;
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * Parses the "LibraryModule" rule.
   * @param root indicates if this library is or is not imported by another module
   * @return module
   * @throws QueryException query exception
   */
  @Override
  LibraryModule parseLibrary(final boolean root) throws QueryException {
    init();
    try {
      final Nodes.Module module = syntaxTree();
      versionDecl(module.versionDecl());

      final Nodes.LibraryModule library = module.libraryModule();
      if(library == null) throw error(WRONGCHAR_X_X, MODULE, found());
      final ModuleDecl mdecl = library.moduleDecl();
      final byte[] prefix = token(text(mdecl.ncName()));
      final byte[] uri = uriLiteral(mdecl.uriLiteral());
      if(uri.length == 0) throw error(NSMODURI);

      sc.module = new QNm(prefix, uri);
      sc.ns.add(prefix, uri, info(mdecl));
      namespaces.put(prefix, uri);
      comments(mdecl.end());

      final byte[] pth = token(qc.modulePath(sc.baseIO()));
      qc.modParsed.put(pth, uri);
      // records of modules with a location are identified by their path
      if(!(root && qc.inlineModule)) recordIdentity = string(pth);

      prolog(library.prolog());
      comments(Integer.MAX_VALUE);
      pos = input.length;
      finish();
      if(root) check(null);

      final LibraryModule lm = new LibraryModule(sc);
      lm.set(funcs, vars, sc.imports, namespaces, options, moduleDoc);
      return lm;
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * Parses a sequence type.
   * @return sequence type
   * @throws QueryException query exception
   */
  @Override
  SeqType parseSeqType() throws QueryException {
    try {
      final SyntaxTree tree;
      try {
        tree = SyntaxParser.parseSequenceType(input);
      } catch(final ParseException ex) {
        throw syntaxError(ex);
      }
      gaps = tree.gaps();
      gap = 0;
      return sequenceType((Nodes.SequenceType) tree.root());
    } catch(final QueryException ex) {
      throw error(CASTTYPE_X, null, ex.getLocalizedMessage()).cause(ex);
    }
  }

  /**
   * Builds the syntax tree of the query with the generated parser. The input is not copied:
   * {@link InputParser#input} is never modified.
   * @return root of the syntax tree
   * @throws QueryException query exception, if the input is not accepted by the grammar
   */
  private Nodes.Module syntaxTree() throws QueryException {
    try {
      final SyntaxTree tree = SyntaxParser.parse(input);
      gaps = tree.gaps();
      gap = 0;
      return (Nodes.Module) tree.root();
    } catch(final ParseException ex) {
      throw syntaxError(ex);
    }
  }

  /**
   * Maps a syntax error of the generated parser to a query error, which is positioned at the
   * start of the offending token.
   * @param ex exception of the generated parser
   * @return query error
   */
  private QueryException syntaxError(final ParseException ex) {
    pos = ex.getBegin();

    // Elvis operator: a colon follows a question mark, where a key specifier is expected
    int p = pos;
    while(p > 0 && input[p - 1] <= ' ') p--;
    if(current() == ':' && p > 0 && input[p - 1] == '?') {
      return error(KEYSPEC_X, QueryError.similar(found(), "otherwise"));
    }

    // a few expected tokens are reported as in hand-written checks, if all of them are literals
    // (whitespace and comments are allowed wherever whitespace is)
    final ArrayList<String> tokens = new ArrayList<>();
    boolean literals = true;
    for(final String t : XQuerySyntaxParser.getExpectedTokenSet(ex)) {
      if(t.equals("S") || t.equals("'(:'")) continue;
      literals = t.length() > 2 && t.charAt(0) == '\'' && tokens.size() < MAX_EXPECTED;
      if(!literals) break;
      tokens.add(t.substring(1, t.length() - 1).replace("''", "'"));
    }
    if(literals && !tokens.isEmpty()) {
      return error(WRONGCHAR_X_X, String.join("' or '", tokens), found());
    }
    // the input ends before the expression is complete
    final byte[] t = found();
    return t == EMPTY ? error(INCOMPLETE) : error(SYNTAX_X, t);
  }

  /**
   * Finishes the parsing step.
   * @throws QueryException query exception
   */
  private void finish() throws QueryException {
    // completes the parsing step
    if(sc.elemNS != null) sc.ns.add(EMPTY, sc.elemNS, null);
    declareExtensions(extensions, true);
    // reject item type declarations of this module that depend on themselves
    checkTypes(sc.types.values());
    resolveTypeRefs();
  }

  /**
   * Scans the comments of the gaps that start before or at a position. XQDoc comments are
   * collected for the documentation of declarations and modules, as in {@link QueryParser}.
   * @param end position
   * @throws QueryException query exception
   */
  private void comments(final int end) throws QueryException {
    comments(end, true);
  }

  /**
   * Scans the comments of the gaps that end before a position.
   * @param end position
   * @param trailing include the gap that starts at the position
   * @throws QueryException query exception
   */
  private void comments(final int end, final boolean trailing) throws QueryException {
    while(gap < gaps.length && (trailing ? gaps[gap] <= end : gaps[gap + 1] <= end)) {
      final int begin = gaps[gap], close = gaps[gap + 1];
      gap += 2;
      // gaps without comments do not need to be scanned
      for(int p = begin; p + 1 < close; p++) {
        if(input[p] == '(' && input[p + 1] == ':') {
          pos = begin;
          skipWs();
          break;
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers

  /**
   * Returns the text of a terminal.
   * @param node terminal
   * @return text
   */
  private String text(final Node node) {
    return ((Terminal) node).text(input);
  }

  /**
   * Creates input information for a node.
   * @param node node
   * @return input information
   */
  private InputInfo info(final Node node) {
    pos = node.begin();
    return info();
  }

  /**
   * Returns the start of the token that follows an offset: whitespace and comments are skipped.
   * @param end offset
   * @return offset of the next token
   */
  private int after(final int end) {
    int lo = 0, hi = gaps.length / 2 - 1;
    while(lo <= hi) {
      final int mid = lo + hi >>> 1, begin = gaps[mid << 1];
      if(begin < end) lo = mid + 1;
      else if(begin > end) hi = mid - 1;
      else return gaps[(mid << 1) + 1];
    }
    return end;
  }

  /**
   * Returns the end of the first words of a declaration, or the start of the token behind them.
   * @param from start offset
   * @param count number of words (keywords)
   * @param space skip whitespace and comments behind the last word
   * @return offset
   */
  private int words(final int from, final int count, final boolean space) {
    int p = from;
    for(int w = 0; w < count; w++) {
      while(p < input.length && (Character.isLetterOrDigit(input[p]) || input[p] == '-' ||
          input[p] == '_' || input[p] == '.')) p++;
      if(w < count - 1 || space) p = after(p);
    }
    return p;
  }

  /**
   * Returns the end of the previous token: the start of the whitespace that precedes an offset.
   * @param begin offset
   * @return offset
   */
  private int before(final int begin) {
    int lo = 0, hi = gaps.length / 2 - 1;
    while(lo <= hi) {
      final int mid = lo + hi >>> 1, end = gaps[(mid << 1) + 1];
      if(end < begin) lo = mid + 1;
      else if(end > begin) hi = mid - 1;
      else return gaps[mid << 1];
    }
    return begin;
  }

  /**
   * Creates input information for an offset.
   * @param offset offset
   * @return input information
   */
  private InputInfo infoAt(final int offset) {
    pos = offset;
    return info();
  }

  /**
   * Creates input information for the token that follows a node, as the position of the
   * hand-written parser after it has parsed the node.
   * @param node node
   * @return input information
   */
  private InputInfo infoAfter(final Node node) {
    return infoAt(after(node.end()));
  }

  /**
   * Creates the specified error at the start of a node.
   * @param node node
   * @param error error to be thrown
   * @param arg error arguments
   * @return error
   */
  private QueryException error(final Node node, final QueryError error, final Object... arg) {
    pos = node.begin();
    return error(error, arg);
  }

  /**
   * Creates the specified error at the end of a node, as the position of the hand-written parser
   * after it has parsed the node.
   * @param node node
   * @param error error to be thrown
   * @param arg error arguments
   * @return error
   */
  private QueryException errorAtEnd(final Node node, final QueryError error,
      final Object... arg) {
    pos = node.end();
    return error(error, arg);
  }

  /**
   * Returns the first node that is not {@code null}.
   * @param nodes nodes
   * @return node, or {@code null} if there is none
   */
  private static Node first(final Node... nodes) {
    for(final Node node : nodes) {
      if(node != null) return node;
    }
    return null;
  }

  /**
   * Returns the value of a string literal.
   * @param node string literal (terminal)
   * @return value
   * @throws QueryException query exception
   */
  private byte[] stringLiteral(final Node node) throws QueryException {
    final Terminal literal = (Terminal) node;
    final int quote = input[literal.begin], end = literal.end() - 1;
    // fast path: no entities, no escaped quotes
    boolean plain = true;
    for(int p = literal.begin + 1; p < end; p++) {
      final int cp = input[p];
      if(cp == '&' || cp == quote) {
        plain = false;
        break;
      }
    }
    if(plain) return token(new String(input, literal.begin + 1, end - literal.begin - 1));

    pos = literal.begin;
    token.reset();
    for(pos++; pos < end;) {
      if(current() == quote) pos++;
      entity(token);
    }
    return token.toArray();
  }

  /**
   * Returns the value of a URI literal.
   * @param node URI literal (terminal)
   * @param special tokens that are allowed though not valid URI literals
   * @return value
   * @throws QueryException query exception
   */
  private byte[] uriLiteral(final Node node, final byte[]... special) throws QueryException {
    final byte[] uri = normalize(stringLiteral(node));
    for(final byte[] sp : special) if(eq(uri, sp)) return uri;
    if(!Uri.get(uri).isValid()) {
      pos = node.end();
      throw error(INVURI_X, uri);
    }
    return uri;
  }

  /**
   * Parses an "EQName".
   * @param node name (terminal)
   * @param def default namespace URI (can be {@code null}), or {@link #SKIPCHECK} to skip checks
   * @return name
   * @throws QueryException query exception
   */
  private QNm eQName(final Node node, final byte[] def) throws QueryException {
    final Terminal name = (Terminal) node;
    final String text = name.text(input);

    // URIQualifiedName
    if(name.sym == Sym.URI_QUALIFIED_NAME) {
      pos = name.begin + 2;
      final byte[] uri = bracedURILiteral();
      final String[] local = text.substring(text.indexOf('}') + 1).split(":");
      if(local.length == 1) return new QNm(token(local[0]), uri);
      if(uri.length == 0) {
        pos = name.end();
        throw error(PREFIXNOURI_X, local[1]);
      }
      return new QNm(token(local[0]), token(local[1]), uri);
    }
    // QName: create EQName and set namespace
    final byte[] nm = token(text);
    if(def == SKIPCHECK) return new QNm(nm);
    final QNm qnm = new QNm(nm, qc, sc);
    if(!qnm.hasURI()) {
      if(qnm.hasPrefix()) throw error(node, NOURI_X, undeclared(qnm.prefix()));
      qnm.uri(def);
    }
    return qnm;
  }

  /**
   * Parses the "BracedURILiteral" rule without the "Q{" prefix.
   * @return URI
   * @throws QueryException query exception
   */
  private byte[] bracedURILiteral() throws QueryException {
    final int p = pos;
    token.reset();
    while(!consume('}')) entity(token);
    final byte[] ns = normalize(token.toArray());
    if(!Uri.get(ns).isValid()) throw error(INVURI_X, ns);
    if(eq(ns, XMLNS_URI)) {
      pos = p;
      throw error(ILLEGALEQNAME_X, ns);
    }
    return ns;
  }

  /**
   * Parses an "EnclosedExpr".
   * @param node enclosed expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr enclosedExpr(final Node node) throws QueryException {
    final Node expr = ((EnclosedExpr) node).expr();
    return expr == null ? Empty.VALUE : single(expr);
  }

  // ---------------------------------------------------------------------------------------------
  // types

  /**
   * Parses the "SequenceType" rule.
   * @param node sequence type
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType sequenceType(final Nodes.SequenceType node) throws QueryException {
    if(node.isEmptySequence()) return Types.EMPTY_SEQUENCE_Z;
    final SeqType st = itemType(node.itemType());
    final Node occ = node.occurrenceIndicator();
    if(occ == null) return st;
    final int indicator = input[occ.begin()];
    return st.with(indicator == '?' ? Occ.ZERO_OR_ONE : indicator == '+' ? Occ.ONE_OR_MORE :
      Occ.ZERO_OR_MORE);
  }

  /**
   * Parses the "ItemType" rule.
   * @param node item type
   * @return item type
   * @throws QueryException query exception
   */
  private SeqType itemType(final Node node) throws QueryException {
    final int sym = node.sym();
    if(sym != Sym.Q_NAME && sym != Sym.URI_QUALIFIED_NAME) return regularType(node);

    // named type
    final QNm name = eQName(node, null);
    final byte[] local = name.hasURI() ? null : name.local();
    if(eq(local, token(ENUM))) throw error(node, TYPEUNKNOWN_X, BasicType.similar(name));
    // attach default element namespace, or schema namespace if default element namespace is ##any
    if(!name.hasURI()) name.uri(sc.elemNsAny ? XS_URI : sc.elemNS);
    final Type basic = BasicType.get(name, false);
    if(basic != null) return basic.seqType();
    // declared type: record dependency on a named type (for detecting cyclic type declarations)
    if(currentTypeDeps != null) currentTypeDeps.add(name);
    final SeqType st = declaredType(name);
    if(st != null) return st;
    return typeRef(name, infoAfter(node)).seqType();
  }

  /**
   * Parses the types that are not named: the "RegularItemType", "FunctionType" and
   * "ChoiceItemType" rules.
   * @param node type
   * @return item type
   * @throws QueryException query exception
   */
  private SeqType regularType(final Node node) throws QueryException {
    return switch(node) {
      case final Nodes.ChoiceItemType choice -> {
        final ChoiceItemType.Builder builder = new ChoiceItemType.Builder();
        for(final Node item : choice.children()) builder.add(itemType(item).type);
        yield builder.build().seqType();
      }
      case final Nodes.FunctionType function -> functionType(function);
      case final Nodes.AnyItemType any -> BasicType.ITEM.seqType();
      case final Nodes.AnyMapType any -> shapeType(any);
      case final Nodes.TypedMapType map -> shapeType(map);
      case final Nodes.AnyArrayType any -> shapeType(any);
      case final Nodes.TypedArrayType array -> shapeType(array);
      case final Nodes.AnyRecordType any -> shapeType(any);
      case final Nodes.TypedRecordType record -> shapeType(record);
      case final Nodes.EnumerationType enumeration -> {
        final TokenSet values = new TokenSet();
        for(final Terminal value : enumeration.children()) values.add(stringLiteral(value));
        yield new EnumType(values).seqType();
      }
      default -> nodeType(node);
    };
  }

  /**
   * Parses the "FunctionType" rule.
   * @param node function type
   * @return function type
   * @throws QueryException query exception
   */
  private SeqType functionType(final Nodes.FunctionType node) throws QueryException {
    // only %updating is relevant for function tests; other non-reserved ones are ignored
    final AnnList anns = annotations(node.children()).check(false, false);
    final boolean updating = anns.contains(Annotation.UPDATING);
    for(final Ann ann : anns) {
      if(ann.definition == Annotation.PUBLIC || ann.definition == Annotation.PRIVATE) {
        throw ANNRESERVED_X.get(ann.info, ann);
      }
    }
    if(node.anyFunctionType() != null) {
      return (updating ? Types.UPDATING_FUNCTION : Types.FUNCTION).seqType();
    }

    final TypedFunctionType function = node.typedFunctionType();
    SeqType[] args = { };
    final QNmSet names = new QNmSet();
    for(final TypedFunctionParam param : function.children()) {
      if(param.eqName() != null) {
        final QNm qnm = eQName(param.eqName(), null);
        if(!names.add(qnm)) throw FUNCDUPL_X.get(infoAt(param.eqName().end()), qnm.varString());
      }
      args = Array.add(args, sequenceType(param.sequenceType()));
    }
    return FuncType.get(updating ? Types.UPDATING_FUNCTION.anns : AnnList.EMPTY,
        sequenceType(function.sequenceType()), args).seqType();
  }

  /**
   * Parses the "MapType", "ArrayType" and "RecordType" rules.
   * @param node map, array or record type
   * @return type
   * @throws QueryException query exception
   */
  private SeqType shapeType(final Node node) throws QueryException {
    return switch(node) {
      case final Nodes.AnyMapType any -> FuncType.get(new QNm(token(MAP))).seqType();
      case final Nodes.AnyArrayType any -> FuncType.get(new QNm(token(ARRAY))).seqType();
      case final Nodes.AnyRecordType any -> FuncType.get(new QNm(token(RECORD))).seqType();
      case final Nodes.TypedArrayType array ->
        ArrayType.get(sequenceType(array.sequenceType())).seqType();
      case final Nodes.TypedMapType map -> {
        final Type key = itemType(map.itemType()).type;
        if(key instanceof final TypeRef ref && !ref.resolved()) {
          qc.deferredMapKeys.add(new DeferredRef(ref, infoAfter(map.itemType())));
        } else if(!key.instanceOf(BasicType.ANY_ATOMIC_TYPE)) {
          throw error(node, MAPTAAT_X, key);
        }
        yield MapType.get(key, sequenceType(map.sequenceType())).seqType();
      }
      case final Nodes.TypedRecordType record -> {
        final TokenObjectMap<ShapeField> fields = new TokenObjectMap<>();
        for(final FieldDecl field : record.children()) {
          final byte[] fn = fieldName(field.fieldName());
          if(fields.contains(fn)) {
            pos = after(field.end());
            throw error(DUPFIELD_X, fn);
          }
          final Nodes.SequenceType st = field.sequenceType();
          fields.put(fn, new ShapeField(st == null ? null : sequenceType(st)));
        }
        yield qc.shared.shape(new ShapeType(fields)).seqType();
      }
      default -> throw new IllegalStateException("Unexpected node: " + node.sym());
    };
  }

  /**
   * Returns the name of a record field.
   * @param node field name (string literal or name)
   * @return name
   * @throws QueryException query exception
   */
  private byte[] fieldName(final Node node) throws QueryException {
    return node.sym() == Sym.STRING_LITERAL ? stringLiteral(node) : token(text(node));
  }

  /**
   * Returns the kind of a node type.
   * @param sym symbol of the node type
   * @return node kind
   */
  private static Kind kind(final int sym) {
    return switch(sym) {
      case Sym.DocumentNodeType -> Kind.DOCUMENT;
      case Sym.ElementNodeType -> Kind.ELEMENT;
      case Sym.AttributeNodeType -> Kind.ATTRIBUTE;
      case Sym.SchemaElementNodeType -> Kind.SCHEMA_ELEMENT;
      case Sym.SchemaAttributeNodeType -> Kind.SCHEMA_ATTRIBUTE;
      case Sym.ProcessingInstructionNodeType -> Kind.PROCESSING_INSTRUCTION;
      case Sym.CommentNodeType -> Kind.COMMENT;
      case Sym.TextNodeType -> Kind.TEXT;
      case Sym.NamespaceNodeType -> Kind.NAMESPACE;
      case Sym.AnyXNodeType -> Kind.XNODE;
      case Sym.JNodeType -> Kind.JNODE;
      default -> Kind.NODE;
    };
  }

  /**
   * Parses the "XNodeType", "GNodeType" and "JNodeType" rules.
   * @param node node type
   * @return node type
   * @throws QueryException query exception
   */
  private SeqType nodeType(final Node node) throws QueryException {
    final Kind kind = kind(node.sym());
    final Test test = rawKindTest(node, kind);
    return (test == null ? NodeType.get(kind) : NodeType.get(test)).seqType();
  }

  /**
   * Parses a kind test.
   * @param node node type
   * @return test
   * @throws QueryException query exception
   */
  private Test kindTest(final Node node) throws QueryException {
    final Kind kind = kind(node.sym());
    final Test test = rawKindTest(node, kind);
    return test != null ? test : NodeTest.get(kind);
  }

  /**
   * Parses the content of a node type.
   * @param node node type
   * @param kind node kind
   * @return test, or {@code null} if the node type has no content
   * @throws QueryException query exception
   */
  private Test rawKindTest(final Node node, final Kind kind) throws QueryException {
    return switch(kind) {
      case JNODE -> jnodeTest((Nodes.JNodeType) node);
      case DOCUMENT -> {
        final Nodes.DocumentNodeType doc = (Nodes.DocumentNodeType) node;
        final Node content = first(doc.elementNodeType(), doc.schemaElementNodeType(),
            doc.nameTestUnion());
        if(content == null) yield null;
        final Test inner = content instanceof final Nodes.NameTestUnion union ?
          Test.get(nameTestUnion(union, Kind.ELEMENT)) : kindTest(content);
        yield new DocTest(inner != null ? inner : NodeTest.ELEMENT);
      }
      case ELEMENT, ATTRIBUTE -> {
        final Nodes.NameTestUnion union;
        final Node typeName;
        if(kind == Kind.ELEMENT) {
          final ElementNodeType element = (ElementNodeType) node;
          union = element.nameTestUnion();
          typeName = element.typeName();
        } else {
          final AttributeNodeType attribute = (AttributeNodeType) node;
          union = attribute.nameTestUnion();
          typeName = attribute.typeName();
        }
        if(union == null) yield null;
        final ArrayList<Test> tests = nameTestUnion(union, kind);
        if(typeName == null) yield Test.get(tests);
        final QNm name = eQName(typeName, sc.elemNS);
        Type ann = ListType.get(name);
        if(ann == null) ann = BasicType.get(name, true);
        if(ann == null) {
          pos = typeName.end();
          throw error(TYPEUNDEF_X, BasicType.similar(name));
        }
        // elements are annotated with xs:untyped, attributes with xs:untypedAtomic
        final boolean untyped = kind == Kind.ELEMENT ?
          ann.oneOf(BasicType.ANY_TYPE, BasicType.UNTYPED) :
          ann.oneOf(BasicType.ANY_TYPE, BasicType.ANY_SIMPLE_TYPE, BasicType.ANY_ATOMIC_TYPE,
          BasicType.UNTYPED_ATOMIC);
        yield untyped ? Test.get(tests) : new AnnotationTest(Test.get(tests), ann);
      }
      case PROCESSING_INSTRUCTION -> {
        final ProcessingInstructionNodeType pi = (ProcessingInstructionNodeType) node;
        final Node content = first(pi.ncName(), pi.stringLiteral());
        if(content == null) yield null;
        final byte[] name = content.sym() == Sym.STRING_LITERAL ? trim(stringLiteral(content)) :
          token(text(content));
        if(!XMLToken.isNCName(name)) {
          pos = content.end();
          throw error(INVNCNAME_X, name);
        }
        yield NameTest.get(new QNm(name), Kind.PROCESSING_INSTRUCTION);
      }
      case SCHEMA_ELEMENT, SCHEMA_ATTRIBUTE -> {
        final Node name = kind == Kind.SCHEMA_ELEMENT ?
          ((SchemaElementNodeType) node).elementName() :
          ((SchemaAttributeNodeType) node).attributeName();
        final QNm qn = eQName(name, sc.elemNS);
        pos = name.end();
        throw error(SCHEMAINV_X, qn);
      }
      default -> null;
    };
  }

  /**
   * Parses the "JNodeType" rule.
   * @param node node type
   * @return test
   * @throws QueryException query exception
   */
  private Test jnodeTest(final Nodes.JNodeType node) throws QueryException {
    Item key = null;
    if(node.jRootSelector() != null) key = Empty.VALUE;
    else if(node.ncName() != null) key = Str.get(token(text(node.ncName())));
    else if(node.constant() != null) key = constant(node.constant());
    final Nodes.SequenceType st = node.sequenceType();
    return JNodeTest.get(key, st == null ? null : sequenceType(st));
  }

  /**
   * Parses the "NameTestUnion" rule.
   * @param node name tests
   * @param kind node kind ({@link Kind#ELEMENT}, {@link Kind#ATTRIBUTE}, or {@code null})
   * @return name tests
   * @throws QueryException query exception
   */
  private ArrayList<Test> nameTestUnion(final Nodes.NameTestUnion node, final Kind kind)
      throws QueryException {

    final ArrayList<Test> tests = new ArrayList<>();
    for(final Node test : node.children()) tests.add(nameTest(test, kind, false));
    return tests;
  }

  /**
   * Parses the "SimpleNodeTest" rule.
   * @param node simple node test: name test or kind test
   * @param kind node kind ({@link Kind#ELEMENT}, {@link Kind#ATTRIBUTE}, or {@code null})
   * @param all check all tests, or only names
   * @return node test
   * @throws QueryException query exception
   */
  private Test simpleNodeTest(final Node node, final Kind kind, final boolean all)
      throws QueryException {
    return node instanceof Terminal ? nameTest(node, kind, all) : kindTest(node);
  }

  /**
   * Parses the "NameTest" and "Selector" rules.
   * @param test name test (terminal)
   * @param kind node kind ({@link Kind#ELEMENT}, {@link Kind#ATTRIBUTE}, or {@code null})
   * @param all check all tests, or only names
   * @return name test
   * @throws QueryException query exception
   */
  private Test nameTest(final Node test, final Kind kind, final boolean all)
      throws QueryException {

    final Kind k = all && kind == Kind.ELEMENT ? null : kind;
    final InputInfo ii = info(test);
    if(test.sym() == Sym.WILDCARD) {
      final String text = text(test);
      if(text.equals("*")) return Test.get(k, null, NameTest.Scope.ALL, sc.elemNS);
      if(text.startsWith("*")) {
        return Test.get(k, new QNm(token(text.substring(2))), NameTest.Scope.LOCAL, sc.elemNS);
      }
      // name test: Q{uri}* or prefix:*
      QNm name;
      if(text.startsWith("Q{")) {
        pos = test.begin() + 2;
        final byte[] uri = bracedURILiteral();
        name = new QNm(cpToken(':'), uri);
      } else {
        name = new QNm(token(text.substring(0, text.length() - 1)));
        resolveQNm(name, kind == Kind.ELEMENT ? sc.elemNS : null, ii);
      }
      return Test.get(k, name, NameTest.Scope.URI, sc.elemNS);
    }

    // name test: prefix:name, name, Q{uri}name
    final QNm name = eQName(test, SKIPCHECK);
    NameTest.Scope scope = NameTest.Scope.FULL;
    if(!name.hasPrefix() && test.sym() != Sym.URI_QUALIFIED_NAME) {
      scope = kind == Kind.ELEMENT && sc.elemNsAny ? NameTest.Scope.LOCAL :
        NameTest.Scope.FLEXIBLE;
    }
    resolveQNm(name, kind == Kind.ELEMENT ? sc.elemNS : null, ii);
    return Test.get(k, name, scope, sc.elemNS);
  }

  /**
   * Parses the "CastTarget" rule.
   * @param node cast target
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType castTarget(final CastTarget node) throws QueryException {
    final Node target = first(node.typeName(), node.choiceItemType(), node.enumerationType(),
        node.arrayType(), node.mapType(), node.recordType());
    Type type;
    if(target instanceof Terminal) {
      final QNm name = eQName(target, null);
      final byte[] local = name.hasURI() ? null : name.local();
      if(eq(local, token(ENUM))) {
        pos = after(target.end());
        throw error(TYPEUNKNOWN_X, BasicType.similar(name));
      }
      // attach default element namespace, or schema namespace if default is ##any
      if(!name.hasURI()) name.uri(sc.elemNsAny ? XS_URI : sc.elemNS);
      // schema type, list type, or (forward) reference to a named item type
      type = ListType.get(name);
      if(type == null) {
        type = BasicType.get(name, false);
        if(type == null ? name.eq(BasicType.ANY_SIMPLE_TYPE.qname()) :
          type.oneOf(BasicType.ANY_ATOMIC_TYPE, BasicType.NOTATION))
        {
            pos = after(target.end());
            throw error(INVALIDCAST_X, name.prefixId(XML));
          }
        if(type == null) {
          final SeqType st = declaredType(name);
          if(st != null) {
            type = st.type;
          } else if(BasicType.get(name, true) != null) {
            // known schema type that is not simple (xs:anyType, xs:untyped)
            pos = after(target.end());
            throw error(WHICHCAST_X, name.prefixId(XML));
          } else {
            final TypeRef ref = typeRef(name, infoAfter(target));
            qc.deferredCastTargets.add(new DeferredRef(ref, infoAfter(target)));
            type = ref;
          }
        }
      }
    } else {
      type = regularType(target).type;
    }
    // check cast eligibility (forward references are re-checked after resolution)
    pos = target instanceof Nodes.ChoiceItemType ? target.end() : after(target.end());
    checkCastTarget(type, false, info());
    return type.seqType(node.isQuestion() ? Occ.ZERO_OR_ONE : Occ.EXACTLY_ONE);
  }

  /**
   * Creates a variable from a "VarName" or "VarNameAndType" node.
   * @param node variable name, with an optional type declaration
   * @param type type (can be {@code null}, in which case the type declaration is taken)
   * @return variable
   * @throws QueryException query exception
   */
  private Var newVar(final Node node, final SeqType type) throws QueryException {
    if(node instanceof final VarName name) {
      return new Var(eQName(name.eqName(), null), type, qc, info(node));
    }
    final VarNameAndType var = (VarNameAndType) node;
    return newVar(var, var.typeDeclaration(), type);
  }

  /**
   * Creates a variable.
   * @param node variable name
   * @param typeDecl type declaration (can be {@code null})
   * @param type type (can be {@code null}, in which case the type declaration is taken)
   * @return variable
   * @throws QueryException query exception
   */
  private Var newVar(final Node node, final TypeDeclaration typeDecl, final SeqType type)
      throws QueryException {
    final InputInfo ii = info(node);
    final Node eqName = node instanceof final VarName name ? name.eqName() :
      ((VarNameAndType) node).eqName();
    final QNm name = eQName(eqName, null);
    return new Var(name, type != null || typeDecl == null ? type :
      sequenceType(typeDecl.sequenceType()), qc, ii);
  }

  /**
   * Creates the variable of an optional "VarName" holder.
   * @param holder positional, score, key, value, previous, next or current variable (can be
   *   {@code null})
   * @param name variable name (can be {@code null})
   * @param type type
   * @return variable, or {@code null} if there is no variable
   * @throws QueryException query exception
   */
  private Var optVar(final Node holder, final Node name, final SeqType type)
      throws QueryException {
    return holder == null ? null : newVar(name, type);
  }

  // ---------------------------------------------------------------------------------------------
  // prolog

  /**
   * Parses the "VersionDecl" rule.
   * @param node declaration (can be {@code null})
   * @throws QueryException query exception
   */
  private void versionDecl(final VersionDecl node) throws QueryException {
    if(node == null) return;

    final Node version = node.isVersion() ? node.stringLiteral() : null;
    final Node encoding = node.isVersion() ? node.stringLiteral2() : node.stringLiteral();
    if(version != null) {
      final String value = string(stringLiteral(version)).replaceAll("0+(\\d)", "$1");
      if(!QueryContext.isSupported(value)) throw errorAtEnd(version, XQUERYVER_X, value);
    }
    if(encoding != null) {
      final String error = Strings.checkEncoding(string(stringLiteral(encoding)));
      if(error != null) throw errorAtEnd(encoding, XQUERYENC2_X, error);
    }
    comments(node.end());
  }

  /**
   * Parses the "Prolog" rule. The declarations of the first part are namespace declarations,
   * settings and imports. The modules are imported before the declarations of the second part.
   * @param prolog prolog
   * @throws QueryException query exception
   */
  private void prolog(final Nodes.Prolog prolog) throws QueryException {
    boolean imported = false;
    for(final Node node : prolog.children()) {
      final int sym = node.sym();
      if(!imported && DECLARATIONS.get(sym)) {
        importModules();
        imported = true;
      }
      comments(node.begin());
      switch(sym) {
        case Sym.DefaultNamespaceDecl -> defaultNamespaceDecl((DefaultNamespaceDecl) node);
        case Sym.BoundarySpaceDecl -> {
          pos = words(node.begin(), 2, true);
          if(!decl.add(BOUNDARY_SPACE)) throw error(DUPLBOUND);
          sc.spaces = ((BoundarySpaceDecl) node).isPreserve();
        }
        case Sym.DefaultCollationDecl -> {
          pos = words(node.begin(), 3, true);
          if(!decl.add(COLLATION)) throw error(DUPLCOLL);
          final Node uri = ((DefaultCollationDecl) node).uriLiteral();
          sc.collation = Collation.get(uriLiteral(uri), qc, infoAt(uri.end()), WHICHDEFCOLL_X);
        }
        case Sym.BaseURIDecl -> {
          pos = words(node.begin(), 2, true);
          if(!decl.add(BASE_URI)) throw error(DUPLBASE);
          sc.baseURI(string(uriLiteral(((BaseURIDecl) node).uriLiteral())));
        }
        case Sym.ConstructionDecl -> {
          pos = words(node.begin(), 2, true);
          if(!decl.add(CONSTRUCTION)) throw error(DUPLCONS);
          sc.strip = ((ConstructionDecl) node).isStrip();
        }
        case Sym.OrderingModeDecl -> {
          pos = words(node.begin(), 2, true);
          if(!decl.add(ORDERING)) throw error(DUPLORD);
          sc.ordered = ((OrderingModeDecl) node).isOrdered();
        }
        case Sym.EmptyOrderDecl -> {
          pos = words(node.begin(), 4, false);
          if(!decl.add(EMPTYY)) throw error(DUPLORDEMP);
          sc.orderGreatest = ((EmptyOrderDecl) node).isGreatest();
        }
        case Sym.CopyNamespacesDecl -> {
          pos = words(node.begin(), 2, true);
          if(!decl.add(COPY_NAMESPACES)) throw error(DUPLCOPYNS);
          final CopyNamespacesDecl cn = (CopyNamespacesDecl) node;
          sc.preserveNS = cn.preserveMode().sym() == Sym.PRESERVE;
          sc.inheritNS = cn.inheritMode().sym() == Sym.INHERIT;
        }
        case Sym.RevalidationDecl -> {
          pos = words(node.begin(), 2, true);
          if(!decl.add(REVALIDATION)) throw error(DUPLREVAL);
          pos = after(node.end());
          if(!((RevalidationDecl) node).isSkip()) throw error(NOREVAL);
        }
        case Sym.DecimalFormatDecl -> decimalFormatDecl((DecimalFormatDecl) node);
        case Sym.NamespaceDecl -> {
          final NamespaceDecl nd = (NamespaceDecl) node;
          final byte[] prefix = token(text(nd.ncName()));
          final byte[] uri = uriLiteral(nd.uriLiteral());
          pos = nd.uriLiteral().end();
          if(sc.ns.resolveDeclared(prefix) != null) throw error(DUPLNSDECL_X, prefix);
          sc.ns.add(prefix, uri, info());
          namespaces.put(prefix, uri);
        }
        case Sym.FTOptionDecl -> {
          // subsequent assignment required to enable duplicate checks
          final FTOpt fto = new FTOpt();
          for(final FTMatchOption option : ((FTOptionDecl) node).ftMatchOptions().children()) {
            ftMatchOption(option, fto);
          }
          qc.ftOpt().assign(fto);
        }
        case Sym.SchemaImport -> schemaImport((SchemaImport) node);
        case Sym.ModuleImport -> moduleImport((ModuleImport) node);
        case Sym.ContextValueDecl -> contextValueDecl((ContextValueDecl) node);
        case Sym.OptionDecl -> {
          final OptionDecl od = (OptionDecl) node;
          optionDecl(od.eqName(), od.stringLiteral());
        }
        default -> annotatedDecl(node);
      }
      // the last token of some declarations consumes the whitespace and comments that follow it
      comments(node.end(), TRAILING.get(sym));
      docBuilder.reset();
    }
    if(!imported) importModules();

    // parse serialization parameters
    if(!sparams.isEmpty()) {
      final QueryBiConsumer<String, Object[]> parse = (k, v) ->
        qc.parameters().parse(k, (String) v[0], (InputInfo) v[1]);
      final String key = SerializerOptions.PARAMETER_DOCUMENT.name();
      final Object value = sparams.remove(key);
      if(value != null) parse.accept(key, (Object[]) value);
      for(final Map.Entry<String, Object> entry : sparams.entrySet()) {
        parse.accept(entry.getKey(), (Object[]) entry.getValue());
      }
    }
  }

  /**
   * Parses the declaration of a variable, function or type, which can have annotations.
   * @param node declaration
   * @throws QueryException query exception
   */
  private void annotatedDecl(final Node node) throws QueryException {
    final int sym = node.sym();
    final Node[] children = switch(sym) {
      case Sym.VarDecl -> ((VarDecl) node).children();
      case Sym.FunctionDecl -> ((FunctionDecl) node).children();
      case Sym.ItemTypeDecl -> ((ItemTypeDecl) node).children();
      default -> ((LabeledRecordTypeDecl) node).children();
    };
    final AnnList anns = annotations(children);
    // variables and types cannot be updating
    if(sym != Sym.FunctionDecl && anns.contains(Annotation.UPDATING)) {
      throw error(node, sym == Sym.VarDecl ? UPDATINGVAR : UPDATINGTYPE);
    }
    switch(sym) {
      case Sym.VarDecl -> varDecl((VarDecl) node, anns.check(true, true));
      case Sym.FunctionDecl -> functionDecl((FunctionDecl) node, anns.check(false, true));
      case Sym.ItemTypeDecl -> typeDecl((ItemTypeDecl) node, anns.check(false, true));
      default -> labeledRecordTypeDecl((LabeledRecordTypeDecl) node,
          anns.check(false, true), children);
    }
  }

  /**
   * Parses the "DefaultNamespaceDecl" rule.
   * @param node declaration
   * @throws QueryException query exception
   */
  private void defaultNamespaceDecl(final DefaultNamespaceDecl node) throws QueryException {
    final boolean elem = node.isElement();
    final byte[] uri = elem ? uriLiteral(node.uriLiteral(), ANY_URI) :
      uriLiteral(node.uriLiteral());
    pos = node.uriLiteral().end();
    if(eq(XML_URI, uri)) throw error(BINDXMLURI_X_X, uri, XML);
    if(eq(XMLNS_URI, uri)) throw error(BINDXMLURI_X_X, uri, XMLNS);

    if(elem) {
      if(!decl.add(ELEMENT)) throw error(DUPLNS);
      sc.elemNsFixed = node.isFixed();
      sc.elemNsAny = Token.eq(uri, QueryText.ANY_URI);
      sc.elemNS = sc.elemNsAny || uri.length == 0 ? null : uri;
      sc.dirNS = sc.elemNS;
    } else {
      if(!decl.add(FUNCTION)) throw error(DUPLNS);
      sc.funcNS = uri;
    }
  }

  /**
   * Parses the "DecimalFormatDecl" rule.
   * @param node declaration
   * @throws QueryException query exception
   */
  private void decimalFormatDecl(final DecimalFormatDecl node) throws QueryException {
    // use empty name for default declaration
    final Node eqname = node.eqName();
    final byte[] name = (eqname == null ? QNm.EMPTY : eQName(eqname, null)).unique();

    // check if format has already been declared
    if(sc.decFormats.get(name) != null) {
      pos = eqname != null ? eqname.end() : words(node.begin(), 3, true);
      throw error(DECDUPL);
    }

    // collect all property declarations
    final DecFormatOptions dfo = new DecFormatOptions();
    final Node[] children = node.children();
    for(int c = 0; c < children.length; c += 2) {
      final String prop = text(children[c]);
      if(dfo.get(prop) != null) {
        pos = after(children[c].end()) + 1;
        throw error(DECDUPLPROP_X, prop);
      }
      try {
        dfo.assign(prop, string(stringLiteral(children[c + 1])));
      } catch(final BaseXException ex) {
        pos = children[c + 1].end();
        throw error(FORMPROP_X, ex);
      }
    }
    sc.decFormats.put(name, new DecFormatter(dfo, infoAt(node.end())));
  }

  /**
   * Parses the "SchemaImport" rule.
   * @param node import
   * @throws QueryException query exception
   */
  private void schemaImport(final SchemaImport node) throws QueryException {
    final SchemaPrefix sp = node.schemaPrefix();
    final Node ncname = sp == null ? null : sp.ncName();
    final byte[] prefix = ncname == null ? null : token(text(ncname));
    if(prefix != null && eq(prefix, XML, XMLNS)) {
      pos = ncname.end();
      throw error(BINDXML_X, prefix);
    }
    final Node[] children = node.children();
    final byte[] uri = uriLiteral(children[0]);
    pos = children[0].end();
    if(prefix != null && uri.length == 0) throw error(NSEMPTY);
    locations(children, new TokenList());
    pos = after(children[children.length - 1].end());
    throw error(IMPLSCHEMA);
  }

  /**
   * Parses the "ModuleImport" rule.
   * @param node import
   * @throws QueryException query exception
   */
  private void moduleImport(final ModuleImport node) throws QueryException {
    final Node ncname = node.ncName();
    final byte[] prefix = ncname == null ? EMPTY : token(text(ncname));
    final Node[] children = node.children();
    final byte[] uri = uriLiteral(children[0]);
    pos = children[0].end();
    if(uri.length == 0) throw error(NSMODURI);
    if(sc.imports.contains(token(uri))) throw error(DUPLMODULE_X, uri);
    sc.imports.add(uri);

    // add non-default namespace
    if(prefix != EMPTY) {
      final byte[] su = sc.ns.resolveDeclared(prefix);
      if(su == null) {
        sc.ns.add(prefix, uri, info());
        namespaces.put(prefix, uri);
      } else {
        final byte[] mu = sc.module == null ? null : sc.module.uri();
        if(!Token.eq(su, uri) || !Token.eq(mu, uri)) throw error(DUPLNSDECL_X, prefix);
      }
    }

    // check modules at specified locations
    final ModInfo mi = new ModInfo();
    if(!locations(children, mi.paths)) {
      // check module files that have been pre-declared by a test API
      final TokenList pths = qc.modDeclared.get(uri);
      if(pths != null) {
        pths.forEach(mi.paths::add);
        mi.declared = true;
      }
    }
    mi.uri = uri;
    mi.info = infoAt(after(children[children.length - 1].end()));
    modules.add(mi);
  }

  /**
   * Adds the locations that follow the keyword "at" of an import.
   * @param children URI literals of the import: the first one is the namespace
   * @param list list for the locations
   * @return indicates if locations were specified
   * @throws QueryException query exception
   */
  private boolean locations(final Node[] children, final TokenList list) throws QueryException {
    for(int c = 1; c < children.length; c++) {
      list.add(uriLiteral(children[c]));
    }
    return children.length > 1;
  }

  /**
   * Parses the "ContextValueDecl" rule.
   * @param node declaration
   * @throws QueryException query exception
   */
  private void contextValueDecl(final ContextValueDecl node) throws QueryException {
    final boolean item = node.isItem();
    pos = words(node.begin(), 3, false);
    if(!decl.add(VALUEE)) throw error(DUPLVALUE);

    final SeqType cst = sc.contextType;
    SeqType st = cst != null ? cst : item ? Types.ITEM_O : Types.ITEM_ZM;
    if(node.isAs()) {
      st = item ? itemType(node.itemType()) : sequenceType(node.sequenceType());
      sc.contextType = st;
      if(sc.module == null) qc.contextType = st;
    }

    final Node value = first(node.varValue(), node.varDefaultValue());
    if(value == null) return;
    if(!node.isExternal()) qc.finalContext = true;

    localVars.pushContext(false);
    final Expr expr = single(value);
    final VarScope vs = localVars.popContext();
    comments(node.end());
    qc.contextValue = new ContextScope(expr, st, vs, sc, info(node), docBuilder.toString());

    if(sc.module != null) throw error(node, DECITEM);
  }

  /**
   * Parses the "VarDecl" rule.
   * @param node declaration
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void varDecl(final VarDecl node, final AnnList anns) throws QueryException {
    final Var var = newVar(node.varNameAndType(), null);
    localVars.pushContext(false);
    final Node value = first(node.varValue(), node.varDefaultValue());
    Expr expr = null;
    if(value != null) {
      declaration = string(var.name.varString());
      expr = single(value);
      declaration = null;
    }
    final VarScope vs = localVars.popContext();
    comments(node.end());
    final String doc = docBuilder.toString();
    final StaticVar sv = qc.vars.declare(var, sc.imports, expr, anns, node.isExternal(), vs, doc);
    vars.add(sv);
  }

  /**
   * Parses the "Annotation" rules.
   * @param nodes nodes: annotations and other nodes, which are ignored
   * @return annotations
   * @throws QueryException query exception
   */
  private AnnList annotations(final Node[] nodes) throws QueryException {
    AnnList anns = AnnList.EMPTY;
    for(final Node child : nodes) {
      final Ann ann;
      if(child.sym() == Sym.UPDATING) {
        // compatibility annotation
        ann = new Ann(infoAt(child.end()), Annotation.UPDATING, Empty.VALUE);
      } else if(child instanceof final Nodes.Annotation annotation) {
        final InputInfo ii = info(annotation.eqName());
        final QNm name = eQName(annotation.eqName(), XQ_URI);
        final ItemList items = new ItemList();
        for(final Constant constant : annotation.children()) items.add(constant(constant));

        // check if annotation is a pre-defined one
        final Annotation def = Annotation.get(name);
        final java.util.function.Function<Object, Object> ai = n -> concat(cpToken('%'), n);
        if(def == null) {
          // reject unknown annotations with pre-defined namespaces, ignore others
          final byte[] uri = name.uri();
          if(NSGlobal.prefix(uri).length != 0 && !eq(uri, LOCAL_URI, ERROR_URI)) {
            Object hnt = name.string();
            final String removed = eq(uri, OUTPUT_URI) ? Removed.option(string(name.local())) :
              null;
            if(removed != null) {
              hnt = Util.info("% (%)", hnt, removed);
            } else {
              final Annotation similar = Annotation.similar(name);
              if(similar != null) {
                hnt = QueryError.similar(hnt, ai.apply(name.hasPrefix() ?
                  concat(name.prefix(), cpToken(':'), similar.name.local()) :
                  similar.name.local()));
              }
            }
            throw error(NSGlobal.reserved(uri) ? ANNRESERVED_X : BASEX_ANN1_X, ii, ai.apply(hnt));
          }
          ann = new Ann(ii, name, items.value());
        } else {
          if(def.single && anns.contains(def)) {
            throw error(BASEX_ANN3_X, ii, ai.apply(name.string()));
          }
          final int is = items.size();
          final IntList arities = Functions.checkArity(is, def.minMax[0], def.minMax[1]);
          if(arities != null) {
            throw error(BASEX_ANN2_X_X, ii, ai.apply(def), arity(arguments(is), arities));
          }
          final int al = def.params.length;
          for(int i = 0; i < is; i++) {
            final BasicType type = def.params[Math.min(al - 1, i)];
            final Item item = items.get(i);
            if(!item.type.instanceOf(type)) {
              throw error(BASEX_ANN_X_X_X, ii, ai.apply(def), type, item.seqType());
            }
          }
          ann = new Ann(ii, def, items.value());
        }
      } else {
        continue;
      }
      anns = anns.attach(ann);
      if(ann.definition == Annotation.UPDATING) qc.updating();
    }
    return anns;
  }

  /**
   * Parses the "OptionDecl" and "FTExtensionOption" rules.
   * @param eqname name
   * @param literal value
   * @throws QueryException query exception
   */
  private void optionDecl(final Node eqname, final Node literal) throws QueryException {
    final QNm qname = eQName(eqname, XQ_URI);
    final String name = string(qname.local());
    final String value = string(stringLiteral(literal));
    final byte[] uri = qname.uri();

    pos = literal.end();
    if(eq(uri, OUTPUT_URI)) {
      // output declaration
      if(sc.module != null) throw error(OUTPUTLIB_X, name);
      if(name.equals(SerializerOptions.PARAMETER_DOCUMENT.name())) checkExternal(value, info());
      if(sparams.put(name, new Object[] { value, info() }) != null) throw error(OUTDUPL_X, name);
    } else if(eq(uri, DB_URI)) {
      // project-specific declaration
      if(sc.module != null) throw error(BASEX_OPTIONSLIB_X, name);
      qc.options.add(name, value, this);
    } else if(eq(uri, BASEX_URI)) {
      // query-specific options
      if(!name.equals(LOCK)) throw error(BASEX_OPTIONSINV_X, name);
      for(final String lock : Locking.queryLocks(token(value))) qc.locks.add(lock);
    }
    // ignore unknown options
    options.put(qname, value);
  }

  /**
   * Parses the "FunctionDecl" rule.
   * @param node function declaration
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void functionDecl(final FunctionDecl node, final AnnList anns) throws QueryException {
    final Node eqname = node.eqName();
    final InputInfo ii = info(eqname);
    final QNm name = eQName(eqname, sc.funcNS);
    if(reserved(name, eqname.begin())) throw error(eqname, RESERVED_X, name.local());

    if(!anns.contains(Annotation.PRIVATE) && sc.module != null && !name.eqUri(sc.module)) {
      pos = eqname.end();
      throw error(MODULENS_X, name.prefixString());
    }

    localVars.pushContext(false);
    final ParamListWithDefaults list = node.paramListWithDefaults();
    final Params params = paramList(list == null ? NONE : list.children(), node.typeDeclaration());
    // input info of the body will refer to this, even after inlining
    declaration = Strings.concat(name.prefixString(), '#', params.size());
    final Node body = node.functionBody();
    final Expr expr = body == null ? null : enclosedExpr(body);
    declaration = null;
    comments(node.end());
    final String doc = docBuilder.toString();
    final VarScope vs = localVars.popContext();
    final byte[] uri = name.uri();
    if(NSGlobal.reserved(uri) || Functions.builtIn(name) != null)
      throw FNRESERVED_X.get(ii, name.string());
    final StaticFunc func = qc.functions.declare(sc, name, params, expr, anns, doc, vs, ii);
    funcs.add(func);
  }

  /**
   * Parses the "ItemTypeDecl" rule.
   * @param node type declaration
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void typeDecl(final ItemTypeDecl node, final AnnList anns) throws QueryException {
    final Node eqname = node.eqName();
    final InputInfo ii = info(eqname);
    final QNm qn = eQName(eqname, sc.elemNsAny ? XS_URI : sc.elemNS);
    pos = eqname.end();
    checkTypeName(qn);
    // collect referenced type names to detect cyclic or self-referential declarations
    final QNmSet refs = new QNmSet();
    currentTypeDeps = refs;
    final SeqType st = itemType(node.itemType());
    currentTypeDeps = null;
    pos = after(node.end());
    declareType(qn, st, refs, anns, ii);
    comments(node.end());
    qc.typeCnstrs.add(new TypeCnstr(sc, qn, st, anns, docBuilder.toString(), ii, funcs));
  }

  /**
   * Parses the "LabeledRecordTypeDecl" rule.
   * @param node record declaration
   * @param anns annotations
   * @param children annotations and field declarations
   * @throws QueryException query exception
   */
  private void labeledRecordTypeDecl(final LabeledRecordTypeDecl node, final AnnList anns,
      final Node[] children) throws QueryException {

    final Node eqname = node.eqName();
    final InputInfo ii = info(eqname);
    final QNm qn = eQName(eqname, sc.elemNS);
    pos = eqname.end();
    checkTypeName(qn);
    QNm base = null;
    if(node.isExtends()) {
      base = eQName(node.eqName2(), sc.elemNsAny ? XS_URI : sc.elemNS);
      pos = node.eqName2().end();
    }
    final TokenObjectMap<ShapeField> fields = new TokenObjectMap<>();
    final TokenSet untyped = new TokenSet();
    boolean defaults = false;
    for(final Node child : children) {
      if(!(child instanceof final LabeledFieldDecl field)) continue;
      final FieldNameAndType fd = field.fieldNameAndType();
      final InputInfo fi = info(fd);
      final byte[] name = fieldName(fd.ncName());
      final Nodes.SequenceType type = fd.sequenceType();
      final SeqType seqType = type == null ? null : sequenceType(type);
      if(fields.contains(name)) {
        pos = after(fd.end());
        throw error(DUPFIELD_X, name);
      }
      Expr init = null;
      final Node value = field.exprSingle();
      if(value != null) {
        // the initializing expression is evaluated like the one of a global variable
        localVars.pushContext(false);
        final Expr expr = single(value);
        final VarScope vs = localVars.popContext();
        final QNm vn = new QNm(concat(qn.string(), cpToken('?'), name), qn.uri());
        init = qc.vars.declareField(new Var(vn, seqType, qc, fi), expr, vs);
        // fixed fields are not parameters of the constructor function
        if(!field.isPercentFixed()) defaults = true;
      } else if(defaults) {
        pos = after(field.end());
        throw error(PARAMOPTIONAL_X, name);
      }
      if(seqType == null) untyped.add(name);
      fields.put(name, new ShapeField(seqType, init, field.isPercentFixed()));
    }
    // the integrity constraint is a focus function, bound to a variable like a field initializer
    Expr constraint = null;
    if(node.isWhere()) {
      final InputInfo ci = info(node.exprSingle());
      localVars.pushContext(false);
      final Closure func = focusFunction(AnnList.EMPTY, node.exprSingle(), false, ci);
      final QNm vn = new QNm(concat(qn.string(), token("?where")), qn.uri());
      constraint = qc.vars.declareField(new Var(vn, null, qc, ci), func, localVars.popContext());
    }
    pos = node.end();
    final RecordDecl rd = new RecordDecl(qn, base, fields, untyped, anns, constraint,
        docBuilder.toString(), ii);
    if(base != null) extensions.put(qn, new DerivedRecord(this, rd));
    else declareRecord(rd, fields, null);
  }

  /**
   * Parses the parameters of a function.
   * @param list parameters: variables with types, optionally with default values
   * @param type type declaration of the result (can be {@code null})
   * @return parameters
   * @throws QueryException query exception
   */
  private Params paramList(final Node[] list, final TypeDeclaration type) throws QueryException {
    final Params params = new Params();
    boolean defaults = false;
    for(final Node param : list) {
      final ParamWithDefault pd = param instanceof final ParamWithDefault p ? p : null;
      final VarNameAndType var = pd != null ? pd.varNameAndType() : (VarNameAndType) param;
      final InputInfo ii = info(var);
      final QNm name = eQName(var.eqName(), null);
      final TypeDeclaration td = var.typeDeclaration();
      final SeqType st = td == null ? null : sequenceType(td.sequenceType());
      Expr expr = null;
      if(pd != null && pd.exprSingle() != null) {
        defaults = true;
        expr = single(pd.exprSingle());
      } else if(defaults) {
        pos = after(var.end());
        throw error(PARAMOPTIONAL_X, name.varString());
      }
      params.add(name, st, expr, ii);
    }
    return params.seqType(type == null ? null : sequenceType(type.sequenceType())).
        finish(qc, localVars);
  }

  // ---------------------------------------------------------------------------------------------
  // expressions

  /**
   * Parses an expression: the "Expr" and "ExprSingle" rules, and all expressions that are
   * operands of these.
   * @param node expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr single(final Node node) throws QueryException {
    // switching on the symbol and casting is faster than a switch on the types of the nodes
    // (benchmark: about 26 ns vs. 42 ns per dispatch of the operands of the QT4 queries)
    return switch(node.sym()) {
      case Sym.FLWORExpr -> flwor((FLWORExpr) node);
      case Sym.QuantifiedExpr -> quantified((QuantifiedExpr) node);
      case Sym.SwitchExpr -> switchh((SwitchExpr) node);
      case Sym.TypeswitchExpr -> typeswitch((TypeswitchExpr) node);
      case Sym.IfExpr -> iff((IfExpr) node);
      case Sym.TryCatchExpr -> tryCatch((TryCatchExpr) node);
      case Sym.InsertExpr -> {
        final InsertExpr insert = (InsertExpr) node;
        final Expr src = single(insert.sourceExpr());
        final InsertExprTargetChoice choice = insert.insertExprTargetChoice();
        final Mode mode = choice.isFirst() ? Mode.FIRST : choice.isLast() ? Mode.LAST :
          choice.isAfter() ? Mode.AFTER : choice.isBefore() ? Mode.BEFORE : Mode.INTO;
        final Expr trg = single(insert.targetExpr());
        qc.updating();
        yield new Insert(infoAfter(node), src, mode, trg);
      }
      case Sym.DeleteExpr -> {
        final Node trg = ((DeleteExpr) node).targetExpr();
        qc.updating();
        yield new Delete(info(trg), single(trg));
      }
      case Sym.RenameExpr -> {
        final RenameExpr rename = (RenameExpr) node;
        final Expr trg = single(rename.targetExpr());
        final Expr name = single(rename.newNameExpr());
        qc.updating();
        yield new Rename(infoAfter(node), trg, name);
      }
      case Sym.ReplaceExpr -> {
        final ReplaceExpr replace = (ReplaceExpr) node;
        final Expr trg = single(replace.targetExpr());
        final Expr src = single(replace.exprSingle());
        qc.updating();
        final boolean value = replace.isValue();
        // replace value of: the source is processed like the content of a text constructor
        final InputInfo ii = infoAfter(node);
        yield new Replace(ii, trg, value ? new CTxt(ii, src) : src, value);
      }
      case Sym.UpdatingFunctionCall -> {
        final UpdatingFunctionCall call = (UpdatingFunctionCall) node;
        final Expr func = single(call.primaryExpr());
        final InputInfo ii = infoAt(after(call.primaryExpr().end()) + 1);
        final Expr[] args = operands(call.children());
        qc.updating();
        yield new DynFuncCall(ii, true, false, func, args);
      }
      case Sym.CopyModifyExpr -> copyModify((CopyModifyExpr) node);
      case Sym.Expr -> {
        final Node[] ch = ((Nodes.Expr) node).children();
        yield new List(infoAfter(ch[ch.length - 1]), operands(ch));
      }
      case Sym.OrExpr -> {
        final Node[] ch = ((OrExpr) node).children();
        yield new Or(info(ch[1]), operands(ch));
      }
      case Sym.AndExpr -> {
        final Node[] ch = ((AndExpr) node).children();
        yield new And(info(ch[1]), operands(ch));
      }
      case Sym.ComparisonExpr -> compare((ComparisonExpr) node);
      case Sym.FTContainsExpr -> {
        final FTContainsExpr contains = (FTContainsExpr) node;
        final Expr expr = single(contains.otherwiseExpr());
        final FTExpr select = ftSelection(contains.ftSelection());
        if(contains.ftIgnoreOption() != null) {
          pos = after(node.end());
          throw error(FTIGNORE);
        }
        yield new FTContains(expr, select, infoAfter(contains.ftSelection()));
      }
      case Sym.OtherwiseExpr ->
        new Otherwise(infoAfter(node), operands(((OtherwiseExpr) node).children()));
      case Sym.StringConcatExpr ->
        new Concat(infoAfter(node), operands(((StringConcatExpr) node).children()));
      case Sym.RangeExpr -> {
        final RangeExpr range = (RangeExpr) node;
        yield new Range(info(range.additiveExpr2()), single(range.additiveExpr()),
            single(range.additiveExpr2()));
      }
      case Sym.AdditiveExpr -> arith(((AdditiveExpr) node).children());
      case Sym.MultiplicativeExpr -> arith(((MultiplicativeExpr) node).children());
      case Sym.UnionExpr ->
        new Union(infoAfter(node), operands(((UnionExpr) node).children(), 2));
      case Sym.IntersectExceptExpr -> intersectExcept(((IntersectExceptExpr) node).children());
      case Sym.ButWithExpr -> {
        final Node[] ch = ((ButWithExpr) node).children();
        Expr expr = single(ch[0]);
        for(int c = 1; c < ch.length; c++) {
          expr = new ButWith(infoAt(before(ch[c].begin())), expr, single(ch[c]));
        }
        yield expr;
      }
      case Sym.InstanceofExpr -> {
        final InstanceofExpr instance = (InstanceofExpr) node;
        yield new Instance(infoAt(before(instance.sequenceType().begin())),
            single(instance.treatExpr()),
            sequenceType(instance.sequenceType()));
      }
      case Sym.TreatExpr -> {
        final TreatExpr treat = (TreatExpr) node;
        yield new Treat(infoAt(before(treat.sequenceType().begin())), single(treat.coerceExpr()),
            sequenceType(treat.sequenceType()));
      }
      case Sym.CoerceExpr -> {
        final CoerceExpr coerce = (CoerceExpr) node;
        yield new TypeCheck(infoAt(before(coerce.sequenceType().begin())),
            single(coerce.castableExpr()),
            sequenceType(coerce.sequenceType()));
      }
      case Sym.CastableExpr -> {
        final CastableExpr castable = (CastableExpr) node;
        final Expr expr = single(castable.castExpr());
        final InputInfo ii = infoAt(before(castable.castTarget().begin()));
        yield new Castable(ii, expr, castTarget(castable.castTarget()));
      }
      case Sym.CastExpr -> {
        final CastExpr cast = (CastExpr) node;
        final Expr expr = single(cast.transformWithExpr());
        final InputInfo ii = infoAt(before(cast.castTarget().begin()));
        yield new Cast(ii, expr, castTarget(cast.castTarget()));
      }
      case Sym.TransformWithExpr -> transformWith((TransformWithExpr) node);
      case Sym.PipelineExpr ->
        new Pipeline(infoAfter(node), operands(((PipelineExpr) node).children()));
      case Sym.ArrowExpr -> arrow((ArrowExpr) node);
      case Sym.UnaryExpr -> {
        final UnaryExpr unary = (UnaryExpr) node;
        boolean minus = false;
        for(final Node sign : unary.children()) minus ^= sign.sym() == Sym.MINUS;
        final Expr expr = single(unary.valueExpr());
        yield minus && expr == minInteger ? Itr.MIN : new Unary(infoAfter(node), expr, minus);
      }
      case Sym.ValidateExpr -> {
        final ValidateExpr validate = (ValidateExpr) node;
        if(validate.isType()) eQName(validate.typeName(), sc.elemNS);
        single(validate.expr());
        pos = node.end();
        throw error(IMPLVAL);
      }
      case Sym.ExtensionExpr -> {
        final ExtensionExpr extension = (ExtensionExpr) node;
        final Pragma[] pragmas = pragmas(extension.children());
        if(extension.expr() == null) {
          pos = node.end() - 1;
          throw error(NOPRAGMA);
        }
        Expr expr = single(extension.expr());
        final InputInfo ii = infoAfter(node);
        for(int p = pragmas.length - 1; p >= 0; p--) expr = new Extension(ii, pragmas[p], expr);
        yield expr;
      }
      case Sym.SimpleMapExpr ->
        new CachedMap(infoAfter(node), operands(((SimpleMapExpr) node).children()));
      case Sym.AbsolutePathExpr, Sym.RelativePathExpr, Sym.AxisStep -> path(node);
      case Sym.FilterExpr, Sym.DynamicFunctionCall, Sym.LookupExpr, Sym.MethodCall ->
        postfix(node);
      default -> primary(node);
    };
  }

  /**
   * Parses the operands of an expression.
   * @param nodes operands
   * @return expressions
   * @throws QueryException query exception
   */
  private Expr[] operands(final Node[] nodes) throws QueryException {
    return operands(nodes, 1);
  }

  /**
   * Parses the operands of an expression.
   * @param nodes operands, which can be interleaved with operators
   * @param step distance of the operands
   * @return expressions
   * @throws QueryException query exception
   */
  private Expr[] operands(final Node[] nodes, final int step) throws QueryException {
    final Expr[] exprs = new Expr[(nodes.length + step - 1) / step];
    for(int n = 0; n < exprs.length; n++) exprs[n] = single(nodes[n * step]);
    return exprs;
  }

  /**
   * Parses the "FLWORExpr" rule.
   * @param node FLWOR expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr flwor(final FLWORExpr node) throws QueryException {
    final int s = localVars.openScope();
    final LinkedList<Clause> clauses = new LinkedList<>();
    final TokenObjectMap<Var> curr = new TokenObjectMap<>();
    clause(node.initialClause(), clauses, curr);
    for(final Node clause : node.children()) clause(clause, clauses, curr);
    final Expr rtrn = single(node.returnClause().exprSingle());
    localVars.closeScope(s);
    return new GFLWOR(clauses.peek().info(), clauses, rtrn);
  }

  /**
   * Parses a clause of a FLWOR expression.
   * @param clause clause
   * @param clauses clauses
   * @param curr variables that are currently in scope
   * @throws QueryException query exception
   */
  private void clause(final Node clause, final LinkedList<Clause> clauses,
      final TokenObjectMap<Var> curr) throws QueryException {

    final int size = clauses.size();
    switch(clause.sym()) {
      case Sym.ForClause -> forClause((ForClause) clause, clauses);
      case Sym.LetClause -> letClause((LetClause) clause, clauses);
      case Sym.WindowClause -> clauses.add(windowClause((WindowClause) clause));
      case Sym.WhereClause ->
        clauses.add(new Where(single(((WhereClause) clause).exprSingle()), infoAfter(clause)));
      case Sym.WhileClause ->
        clauses.add(new While(single(((WhileClause) clause).exprSingle()), infoAfter(clause)));
      case Sym.TraceClause ->
        clauses.add(new Trace(single(((TraceClause) clause).exprSingle()), infoAfter(clause)));
      case Sym.CountClause -> clauses.add(new Count(localVars.add(
          newVar(((CountClause) clause).varName(), Types.INTEGER_O))));
      case Sym.GroupByClause -> groupBy((GroupByClause) clause, clauses, curr);
      default -> orderBy((OrderByClause) clause, clauses, curr);
    }
    for(final Clause cl : clauses.subList(size, clauses.size())) {
      for(final Var var : cl.vars()) curr.put(var.name.unique(), var);
    }
  }

  /**
   * Parses the "ForClause" rule.
   * @param clause clause
   * @param clauses clauses
   * @throws QueryException query exception
   */
  private void forClause(final ForClause clause, final LinkedList<Clause> clauses)
      throws QueryException {

    for(final ForBinding binding : clause.children()) {
      final VarNameAndType name = binding.varNameAndType();
      final ForEntryKeyBinding key = binding.forEntryKeyBinding();
      final ForEntryValueBinding value = binding.forEntryValueBinding();
      final PositionalVar positional = binding.positionalVar();
      final FTScoreVar scoreVar = binding.ftScoreVar();
      final Node allowing = binding.allowingEmpty(), in = binding.exprSingle();
      final boolean item = binding.sym() == Sym.ForItemBinding, empty = allowing != null;
      final Var memberVar = name != null && !item ? newVar(name, null) : null;
      final Var keyVar = key != null ? newVar(key.varNameAndType(), null) : null;
      final Var valueVar = value != null ? newVar(value.varNameAndType(), null) : null;
      final Var frVar = name != null && item ? newVar(name, null) : null;
      final Var at = positional != null ? newVar(positional.varName(), Types.INTEGER_O) : null;
      final Var score = scoreVar != null ? newVar(scoreVar.varName(), Types.DOUBLE_O) : null;
      final InputInfo ii = info(binding.in());
      final Expr expr = single(in);

      // declare variables after the expression, check for duplicates
      final QNmSet names = new QNmSet();
      for(final Var var : new Var[] { memberVar, keyVar, valueVar, frVar, at, score }) {
        if(var != null && !names.add(var.name)) {
          pos = after(in.end());
          throw error(DUPLVAR_X, var);
        }
      }

      localVars.add(frVar, at, score);
      if(frVar != null) {
        clauses.add(new For(frVar, at, score, expr, empty));
      } else if(memberVar == null && valueVar == null) {
        localVars.add(keyVar);
        clauses.add(new For(keyVar, at, score, forEach(Function._MAP_KEYS, expr, ii), false));
      } else {
        final boolean array = memberVar != null;
        final Var bound = array ? memberVar : valueVar;
        final Var split = localVars.add(new Var(bound.name, null, qc, ii));
        clauses.add(new For(split, at, score, forEach(
            array ? Function._ARRAY_SPLIT : Function._MAP_ENTRIES, expr, ii), false));
        final VarRef ref = new VarRef(ii, split);
        if(keyVar != null) {
          clauses.add(new Let(localVars.add(keyVar), Function._MAP_KEYS.get(ii, ref)));
        }
        clauses.add(new Let(localVars.add(bound),
            (array ? Function._ARRAY_ITEMS : Function._MAP_ITEMS).get(ii, ref)));
      }
    }
  }

  /**
   * Parses the "LetClause" rule.
   * @param clause clause
   * @param clauses clauses
   * @throws QueryException query exception
   */
  private void letClause(final LetClause clause, final LinkedList<Clause> clauses)
      throws QueryException {

    for(final LetBinding binding : clause.children()) {
      if(binding instanceof final LetValueBinding let) {
        final boolean score = let.ftScoreVar() != null;
        final Var var = score ? newVar(let.ftScoreVar().varName(), Types.DOUBLE_O) :
          newVar(let.varNameAndType(), null);
        final Expr expr = single(let.exprSingle());
        clauses.add(new Let(localVars.add(var), expr, score));
      } else {
        letStructBinding(binding, clauses);
      }
    }
  }

  /**
   * Parses the "LetSequenceBinding", "LetArrayBinding" and "LetMapBinding" rules.
   * @param binding binding
   * @param clauses clauses
   * @throws QueryException query exception
   */
  private void letStructBinding(final LetBinding binding, final LinkedList<Clause> clauses)
      throws QueryException {

    final LetStructBinding lsb = switch(binding.sym()) {
      case Sym.LetArrayBinding -> new LetStructBinding('[', ']', Types.ARRAY_O);
      case Sym.LetMapBinding -> new LetStructBinding('{', '}', Types.MAP_O);
      default -> new LetStructBinding('(', ')', Types.ITEM_ZM);
    };
    final InputInfo ii = info(binding);
    final LinkedList<Var> vrs = new LinkedList<>();
    for(final VarNameAndType child : binding.children()) vrs.add(newVar(child, null));
    final TypeDeclaration typeDecl = binding.typeDeclaration();
    final SeqType asType = typeDecl == null ? Types.ITEM_ZM : sequenceType(typeDecl.sequenceType());
    final SeqType st = asType.intersect(lsb.type);
    if(st == null) {
      pos = after(typeDecl.end());
      throw error(NOSUB_X_X, lsb.type, asType);
    }
    // name the bound value after the pattern: $( $a, $b )
    final TokenBuilder name = new TokenBuilder().add(lsb.startCp);
    for(final Var var : vrs) name.add(name.size() == 1 ? " " : ", ").add(var.name.varString());
    final Var struct = new Var(new QNm(name.add(' ').add(lsb.endCp).finish()), st, qc, ii);

    clauses.add(new Let(localVars.add(struct), single(binding.exprSingle())));
    final VarRef seqRef = new VarRef(ii, struct);
    int i = 0;
    for(final Var var : vrs) {
      final Expr value = switch(lsb.endCp) {
        case ']' -> Function._ARRAY_GET.get(var.info, seqRef, Itr.get(++i));
        case '}' -> new Lookup(var.info, seqRef, Str.get(var.name.local()));
        default -> (++i < vrs.size() ? Function.ITEMS_AT : Function.SUBSEQUENCE).
          get(var.info, seqRef, Itr.get(i));
      };
      clauses.add(new Let(localVars.add(var), value));
    }
  }

  /**
   * Parses the "TumblingWindowClause" and "SlidingWindowClause" rules.
   * @param clause window clause
   * @return window clause
   * @throws QueryException query exception
   */
  private Window windowClause(final WindowClause clause) throws QueryException {
    final boolean sliding = clause.slidingWindowClause() != null;
    final VarNameAndType name;
    final Node in;
    final WindowStartCondition s;
    final WindowEndCondition e;
    if(sliding) {
      final SlidingWindowClause window = clause.slidingWindowClause();
      name = window.varNameAndType();
      in = window.exprSingle();
      s = window.windowStartCondition();
      e = window.windowEndCondition();
    } else {
      final TumblingWindowClause window = clause.tumblingWindowClause();
      name = window.varNameAndType();
      in = window.exprSingle();
      s = window.windowStartCondition();
      e = window.windowEndCondition();
    }
    final Var var = newVar(name, null);
    final Expr expr = single(in);
    final Condition start = s != null ? windowCond(s, s.windowVars(), s.exprSingle(), true) :
      new Condition(true, null, null, null, null, Bln.TRUE, info(clause));
    final Condition end = e != null ? windowCond(e, e.windowVars(), e.exprSingle(), false) : null;
    return new Window(sliding, localVars.add(var), expr, start, e != null && e.isOnly(), end);
  }

  /**
   * Checks if a window condition is an end condition that starts with {@code only}.
   * @param cond window condition
   * @return result of check
   */
  private static boolean isOnly(final Node cond) {
    return cond instanceof final WindowEndCondition end && end.isOnly();
  }

  /**
   * Parses the "WindowStartCondition" and "WindowEndCondition" rules.
   * @param cond condition
   * @param windowVars window variables
   * @param when condition expression (can be {@code null})
   * @param start start condition
   * @return condition
   * @throws QueryException query exception
   */
  private Condition windowCond(final Node cond, final WindowVars windowVars, final Node when,
      final boolean start) throws QueryException {

    // the position of the hand-written parser: behind the keywords
    final InputInfo ii = infoAt(words(cond.begin(), start || !isOnly(cond) ? 1 : 2, true));
    Var var = null, at = null, prv = null, nxt = null;
    if(windowVars != null) {
      var = windowVar(windowVars.currentVar(), windowVars.currentVar(), Types.ITEM_O);
      at = windowVar(windowVars.positionalVar(),
          windowVars.positionalVar() != null ? windowVars.positionalVar().varName() : null,
          Types.INTEGER_O);
      prv = windowVar(windowVars.previousVar(),
          windowVars.previousVar() != null ? windowVars.previousVar().varName() : null,
          Types.ITEM_ZO);
      nxt = windowVar(windowVars.nextVar(),
          windowVars.nextVar() != null ? windowVars.nextVar().varName() : null, Types.ITEM_ZO);
    }
    return new Condition(start, var, at, prv, nxt, when != null ? single(when) : Bln.TRUE, ii);
  }

  /**
   * Creates and declares a variable of a window condition.
   * @param holder variable node (can be {@code null})
   * @param name variable name node
   * @param type type
   * @return variable, or {@code null} if there is no such variable
   * @throws QueryException query exception
   */
  private Var windowVar(final Node holder, final Node name, final SeqType type)
      throws QueryException {
    return localVars.add(optVar(holder, name, type));
  }

  /**
   * Parses the specifications of a "GroupByClause" rule, and adds the clause.
   * @param clause clause
   * @param clauses clauses
   * @param curr variables that are currently in scope
   * @throws QueryException query exception
   */
  private void groupBy(final GroupByClause clause, final LinkedList<Clause> clauses,
      final TokenObjectMap<Var> curr) throws QueryException {

    final GroupSpec[] specs = groupSpecs(clause, clauses);

    // find all non-grouping variables that aren't shadowed
    final ArrayList<VarRef> ng = new ArrayList<>();
    for(final GroupSpec spec : specs) curr.put(spec.var.name.unique(), spec.var);
    VARS:
    for(final Var var : curr.values()) {
      for(final GroupSpec spec : specs) {
        if(spec.var == var) continue VARS;
      }
      ng.add(new VarRef(specs[0].info(), var));
    }

    // add new copies for all non-grouping variables
    final int ns = ng.size();
    final Var[] ngrp = new Var[ns];
    for(int i = ns; --i >= 0;) {
      final VarRef ref = ng.get(i);
      // if one groups variables such as $x as xs:integer, then the resulting
      // sequence isn't compatible with the type and can't be assigned
      final Var nv = localVars.add(new Var(ref.var.name, null, qc, ref.var.info));
      ngrp[i] = nv;
      curr.put(nv.name.unique(), nv);
    }
    clauses.add(new GroupBy(specs, ng.toArray(VarRef[]::new), ngrp, specs[0].info()));
  }

  /**
   * Parses the "GroupingSpecList" rule.
   * @param clause clause
   * @param cl preceding clauses
   * @return group specifications
   * @throws QueryException query exception
   */
  private GroupSpec[] groupSpecs(final GroupByClause clause, final LinkedList<Clause> cl)
      throws QueryException {

    final ArrayList<GroupSpec> specs = new ArrayList<>();
    for(final GroupingSpec node : clause.children()) {
      final Var var = newVar(node.varName(), node.typeDeclaration(), null);
      final Expr expr;
      if(node.exprSingle() != null) {
        expr = single(node.exprSingle());
      } else {
        final VarRef ref = localVars.resolveLocal(var.name, var.info);
        // the grouping variable has to be declared by the same FLWOR expression
        boolean dec = false;
        if(ref != null) {
          // check preceding clauses
          for(final Clause c : cl) {
            if(c.declares(ref.var)) {
              dec = true;
              break;
            }
          }

          // check other grouping variables
          if(!dec) {
            for(final GroupSpec spec : specs) {
              if(spec.var == ref.var) {
                dec = true;
                break;
              }
            }
          }
        }
        if(!dec) {
          pos = after(node.end());
          throw error(GVARNOTDEFINED_X, var);
        }
        expr = ref;
      }

      final Node collation = node.uriLiteral();
      final Collation coll = collation == null ? sc.collation :
        Collation.get(uriLiteral(collation), qc, infoAt(collation.end()), FLWORCOLL_X);
      final GroupSpec spec = new GroupSpec(var.info, localVars.add(var), expr, coll);
      for(final GroupSpec previous : specs) {
        if(previous.var.name.eq(spec.var.name)) previous.occluded = true;
      }
      specs.add(spec);
    }
    return specs.toArray(GroupSpec[]::new);
  }

  /**
   * Parses the "OrderByClause" rule, and adds the clause.
   * @param clause clause
   * @param clauses clauses
   * @param curr variables that are currently in scope
   * @throws QueryException query exception
   */
  private void orderBy(final OrderByClause clause, final LinkedList<Clause> clauses,
      final TokenObjectMap<Var> curr) throws QueryException {

    final OrderSpec[] specs = clause.children();
    final OrderKey[] keys = new OrderKey[specs.length];
    for(int s = 0; s < keys.length; s++) keys[s] = orderSpec(specs[s]);
    final VarRef[] vs = new VarRef[curr.size()];
    int i = 0;
    for(final Var var : curr.values()) vs[i++] = new VarRef(keys[0].info(), var);
    clauses.add(new OrderBy(vs, keys, keys[0].info()));
  }

  /**
   * Parses the "OrderSpec" rule.
   * @param spec specification
   * @return order key
   * @throws QueryException query exception
   */
  private OrderKey orderSpec(final OrderSpec spec) throws QueryException {
    final Expr expr = single(spec.exprSingle());
    final OrderModifier modifier = spec.orderModifier();
    final boolean desc = modifier != null && modifier.isDescending();
    final boolean least = modifier == null || !modifier.isEmpty() ? !sc.orderGreatest :
      !modifier.isGreatest();
    final Node collation = modifier != null ? modifier.uriLiteral() : null;
    final Collation coll = collation == null ? sc.collation :
      Collation.get(uriLiteral(collation), qc, infoAt(collation.end()), FLWORCOLL_X);
    return new OrderKey(infoAfter(spec), expr, desc, least, coll);
  }

  /**
   * Represents the parameters of a structure binding in a let clause.
   * @param startCp start character of binding ('(', '[', '{')
   * @param endCp end character of binding (')', ']', '}')
   * @param type type of binding structure (array(*), map(*), item()*)
   */
  private record LetStructBinding(char startCp, char endCp, SeqType type) { }

  /**
   * Parses the "QuantifiedExpr" rule.
   * @param quant quantified expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr quantified(final QuantifiedExpr quant) throws QueryException {
    final int s = localVars.openScope();
    final LinkedList<Clause> clauses = new LinkedList<>();
    for(final QuantifierBinding binding : quant.children()) {
      final Var var = newVar(binding.varNameAndType(), null);
      final Expr expr = single(binding.exprSingle());
      clauses.add(new For(localVars.add(var), expr));
    }
    final Expr rtrn = Function.BOOLEAN.get(infoAt(before(quant.exprSingle().begin())),
        single(quant.exprSingle()));
    localVars.closeScope(s);

    final boolean some = quant.isSome();
    final InputInfo ii = clauses.peek().info();
    final CmpG cmp = new CmpG(ii, new GFLWOR(ii, clauses, rtrn), Bln.get(some), CmpOp.EQ);
    return some ? cmp : Function.NOT.get(ii, cmp);
  }

  /**
   * Parses the "SwitchExpr" rule.
   * @param node switch expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr switchh(final SwitchExpr node) throws QueryException {
    final SwitchComparand comparand = node.switchComparand();
    final InputInfo ii = info(comparand);
    final Expr cond = comparand.expr() == null ? Bln.TRUE : single(comparand.expr());
    final SwitchCases cases = node.switchCases() != null ? node.switchCases() :
      node.bracedSwitchCases().switchCases();
    final SwitchCaseClause[] clauses = cases.children();
    final SwitchGroup[] groups = new SwitchGroup[clauses.length + 1];
    for(int c = 0; c < clauses.length; c++) {
      groups[c] = switchGroup(clauses[c].children(), clauses[c].exprSingle());
    }
    groups[clauses.length] = switchGroup(NONE, cases.exprSingle());
    return new Switch(ii, cond, groups);
  }

  /**
   * Parses a "SwitchCaseClause", or the default clause of the "SwitchCases" rule.
   * @param operands operands
   * @param rtrn return expression
   * @return group
   * @throws QueryException query exception
   */
  private SwitchGroup switchGroup(final Node[] operands, final Node rtrn)
      throws QueryException {

    // the return expression is first, but processed last
    final Expr[] exprs = new Expr[operands.length + 1];
    for(int o = 0; o < operands.length; o++) exprs[o + 1] = single(operands[o]);
    exprs[0] = single(rtrn);
    return new SwitchGroup(infoAfter(rtrn), exprs);
  }

  /**
   * Parses the "TypeswitchExpr" rule.
   * @param node typeswitch expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr typeswitch(final TypeswitchExpr node) throws QueryException {
    final InputInfo ii = infoAt(after(node.begin() + TYPESWITCH.length()));
    final Expr cond = single(node.expr());
    final TypeswitchCases cases = node.typeswitchCases() != null ? node.typeswitchCases() :
      node.bracedTypeswitchCases().typeswitchCases();
    final int s = localVars.openScope();
    final CaseClause[] clauses = cases.children();
    final TypeswitchGroup[] groups = new TypeswitchGroup[clauses.length + 1];
    for(int c = 0; c < clauses.length; c++) {
      groups[c] = typeswitchGroup(clauses[c].varName(), clauses[c].sequenceTypeUnion().children(),
          clauses[c].exprSingle(), s);
    }
    groups[clauses.length] = typeswitchGroup(cases.varName(), new Nodes.SequenceType[0],
        cases.exprSingle(), s);
    return new Typeswitch(ii, cond, groups);
  }

  /**
   * Parses a "CaseClause", or the default clause of the "TypeswitchCases" rule.
   * @param name variable name (can be {@code null})
   * @param types sequence types
   * @param rtrn return expression
   * @param scope variable scope that is closed
   * @return group
   * @throws QueryException query exception
   */
  private TypeswitchGroup typeswitchGroup(final VarName name,
      final Nodes.SequenceType[] types, final Node rtrn, final int scope)
      throws QueryException {

    final Var var = name == null ? null : localVars.add(newVar(name, Types.ITEM_ZM));
    final SeqType[] seqTypes = new SeqType[types.length];
    for(int t = 0; t < seqTypes.length; t++) {
      seqTypes[t] = sequenceType(types[t]);
    }
    final Expr expr = single(rtrn);
    final TypeswitchGroup group = new TypeswitchGroup(infoAfter(rtrn), var, seqTypes, expr);
    localVars.closeScope(scope);
    return group;
  }

  /**
   * Parses the "IfExpr" rule, including the chain of "else if" branches of a braced conditional.
   * @param root conditional expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr iff(final IfExpr root) throws QueryException {
    final LinkedList<InputInfo> infos = new LinkedList<>();
    final ExprList list = new ExprList();
    Expr els = Empty.VALUE;
    Node cond = root.expr(), at = root;
    boolean nested = false;
    final UnbracedActions unbraced = root.unbracedActions();
    BracedActions braced = root.bracedActions();
    while(true) {
      infos.add(infoAt(nested ? at.begin() + IF.length() : after(at.begin() + IF.length())));
      list.add(single(cond));
      if(unbraced != null) {
        list.add(single(unbraced.exprSingle()));
        if(unbraced.isElse()) els = single(unbraced.exprSingle2());
        break;
      }
      list.add(enclosedExpr(braced.enclosedExpr()));
      if(!braced.isElse()) break;
      final BracedElseAction action = braced.bracedElseAction();
      if(!action.isIf()) {
        els = enclosedExpr(action.enclosedExpr());
        break;
      }
      at = action;
      nested = true;
      cond = action.expr();
      braced = action.bracedActions();
    }
    while(!list.isEmpty()) {
      final Expr thn = list.pop(), cnd = list.pop();
      els = new If(infos.removeLast(), cnd, thn, els);
    }
    return els;
  }

  /**
   * Creates an comparison.
   * @param node comparison
   * @return comparison
   * @throws QueryException query exception
   */
  private Expr compare(final ComparisonExpr node) throws QueryException {
    final Expr expr1 = single(node.ftContainsExpr()), expr2 = single(node.ftContainsExpr2());
    final InputInfo ii = info(node.ftContainsExpr2());
    final CmpOp op = cmpOp(first(node.valueComp(), node.generalComp(), node.nodeComp()).sym());
    return node.valueComp() != null ? new CmpV(ii, expr1, expr2, op) :
      node.nodeComp() != null ? new CmpN(ii, expr1, expr2, op) :
      new CmpG(ii, expr1, expr2, op);
  }

  /**
   * Returns the comparison operator of a comparison token.
   * @param sym value, general or node comparison token
   * @return operator
   */
  private static CmpOp cmpOp(final int sym) {
    return switch(sym) {
      case Sym.EQ, Sym.EQUALS, Sym.IS -> CmpOp.EQ;
      case Sym.NE, Sym.NOT_EQUALS, Sym.IS_NOT -> CmpOp.NE;
      case Sym.LT, Sym.LESS, Sym.LESS2, Sym.PRECEDES -> CmpOp.LT;
      case Sym.LE, Sym.LESS_EQUAL, Sym.PRECEDES_OR_IS -> CmpOp.LE;
      case Sym.GT, Sym.GREATER, Sym.GREATER2, Sym.FOLLOWS -> CmpOp.GT;
      default -> CmpOp.GE;
    };
  }

  /**
   * Creates an additive or multiplicative expression.
   * @param ch operands and operators
   * @return expression
   * @throws QueryException query exception
   */
  private Expr arith(final Node[] ch) throws QueryException {
    Expr expr = single(ch[0]);
    for(int e = 1; e < ch.length; e += 2) {
      final Calc calc = switch(ch[e].sym()) {
        case Sym.PLUS -> Calc.ADD;
        case Sym.MINUS -> Calc.SUBTRACT;
        case Sym.DIV, Sym.DIVIDE_SIGN -> Calc.DIVIDE;
        case Sym.IDIV -> Calc.DIVIDEINT;
        case Sym.MOD -> Calc.MODULO;
        default -> Calc.MULTIPLY;
      };
      final Expr operand = single(ch[e + 1]);
      // the position of keywords follows the whitespace
      final InputInfo ii = Character.isLetter(input[ch[e].begin()]) ? info(ch[e + 1]) :
        infoAt(ch[e].end());
      expr = new Arith(ii, expr, operand, calc);
    }
    return expr;
  }

  /**
   * Creates intersect and except expressions. Operands of the same operator are combined.
   * @param ch operands and operators
   * @return expression
   * @throws QueryException query exception
   */
  private Expr intersectExcept(final Node[] ch) throws QueryException {
    ExprList list = new ExprList(single(ch[0]));
    boolean last = ch[1].sym() == Sym.INTERSECT;
    for(int e = 1; e < ch.length; e += 2) {
      final boolean is = ch[e].sym() == Sym.INTERSECT;
      if(is != last) {
        list = new ExprList(intersectExcept(last, list, info(ch[e + 1])));
        last = is;
      }
      list.add(single(ch[e + 1]));
    }
    return intersectExcept(last, list, infoAfter(ch[ch.length - 1]));
  }

  /**
   * Creates an intersect or except expression.
   * @param intersect intersect flag
   * @param el operands
   * @param ii input information
   * @return expression
   */
  private static Expr intersectExcept(final boolean intersect, final ExprList el,
      final InputInfo ii) {
    return intersect ? new Intersect(ii, el.finish()) : new Except(ii, el.finish());
  }

  /**
   * Parses the "TransformWithExpr" rule.
   * @param node expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr transformWith(final TransformWithExpr node) throws QueryException {
    Expr expr = single(node.pipelineExpr());
    final Node[] ch = node.children();
    for(int c = 0; c < ch.length;) {
      // "transform with {...}" or "update {...}"
      final Node keyword = ch[c];
      c += keyword.sym() == Sym.TRANSFORM ? 2 : 1;
      qc.updating();
      final InputInfo ii = infoAt(ch[c - 1].end());
      final Node body = c < ch.length && !(ch[c] instanceof Terminal t && isKeyword(t)) ?
        ch[c++] : null;
      expr = new TransformWith(ii, expr, body != null ? single(body) : Empty.VALUE);
    }
    return expr;
  }

  /**
   * Checks if a terminal is a keyword of a "TransformWithExpr".
   * @param terminal terminal
   * @return result of check
   */
  private static boolean isKeyword(final Terminal terminal) {
    return terminal.sym == Sym.TRANSFORM || terminal.sym == Sym.UPDATE;
  }

  /**
   * Parses the "ArrowExpr" rule.
   * @param node arrow expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr arrow(final ArrowExpr node) throws QueryException {
    Expr expr = single(node.unaryExpr());
    for(final Node target : node.children()) {
      final boolean mapping = target.sym() == Sym.MappingArrowTarget;
      final Node call = mapping ? ((MappingArrowTarget) target).arrowTarget() :
        ((SequenceArrowTarget) target).arrowTarget();

      QNm name = null;
      Expr ex = null;
      final Node arguments;
      final boolean positional;
      if(call instanceof final FunctionCall function) {
        final Node eqname = function.eqName();
        name = eQName(eqname, null);
        if(reserved(name, eqname.begin())) throw error(eqname, RESERVED_X, name.local());
        arguments = function.argumentList();
        positional = false;
      } else {
        final RestrictedDynamicCall dynamic = (RestrictedDynamicCall) call;
        ex = primary(first(dynamic.varRef(), dynamic.parenthesizedExpr(),
            dynamic.functionItemExpr(), dynamic.mapConstructor(), dynamic.arrayConstructor()));
        arguments = dynamic.positionalArgumentList();
        positional = true;
      }
      final InputInfo ii = info(call);
      final Expr arg;
      For fr = null;
      if(mapping) {
        // register the variable without making it visible to the arguments
        final int s = localVars.openScope();
        fr = new For(localVars.add(new Var(new QNm("item"), null, qc, ii)), expr);
        localVars.closeScope(s);
        arg = new VarRef(ii, fr.var);
      } else {
        arg = expr;
      }
      final FuncBuilder fb = positional ? argumentList((PositionalArgumentList) arguments, arg) :
        argumentList((ArgumentList) arguments, arg);
      if(ex != null) {
        expr = Functions.dynamic(ex, fb);
      } else  {
        final QNm funcName = name;
        expr = qc.functions.newRef(() -> Functions.get(funcName, fb, qc));
      }
      if(mapping) expr = new GFLWOR(ii, fr, expr);
    }
    return expr;
  }

  /**
   * Parses the "Pragma" rules of an extension expression.
   * @param nodes pragmas
   * @return pragmas
   * @throws QueryException query exception
   */
  private Pragma[] pragmas(final Nodes.Pragma[] nodes) throws QueryException {
    final ArrayList<Pragma> el = new ArrayList<>();
    for(final Nodes.Pragma pragma : nodes) {
      final QNm name = eQName(pragma.eqName(), null);
      final Node contents = pragma.pragmaContents();
      final byte[] value = contents == null ? EMPTY : trim(token(text(contents)));
      pos = pragma.end() - 2;
      if(name.eqPrefix(DB_PREFIX)) {
        // project-specific declaration
        final String key = string(uc(name.local()));
        final MainOptions mopts = qc.context.options;
        final Option<?> option = mopts.option(key);
        if(option == null) throw error(BASEX_OPTIONS_X, mopts.similar(key));
        el.add(new DBPragma(name, option, value));
      } else if(name.eqPrefix(BASEX_PREFIX)) {
        // project-specific declaration
        el.add(new BaseXPragma(name, value));
      }
    }
    return el.toArray(Pragma[]::new);
  }

  /**
   * Parses the "CopyModifyExpr" rule.
   * @param node expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr copyModify(final CopyModifyExpr node) throws QueryException {
    final int s = localVars.openScope();
    final Node[] ch = node.children();
    final int bindings = ch.length - 2;
    Let[] fl = { };
    for(int c = 0; c < bindings; c += 2) {
      final Var var = newVar(ch[c], Types.XNODE_O);
      final Expr expr = single(ch[c + 1]);
      fl = Array.add(fl, new Let(localVars.add(var), expr));
    }
    final InputInfo ii = infoAt(before(ch[bindings].begin()));
    final Expr m = single(ch[bindings]);
    final Expr r = single(ch[bindings + 1]);

    localVars.closeScope(s);
    qc.updating();
    return new Transform(ii, fl, m, r);
  }

  // ---------------------------------------------------------------------------------------------
  // paths and primary expressions

  /**
   * Parses a path expression: the "AbsolutePathExpr", "RelativePathExpr" and "AxisStep" rules.
   * @param node path expression or step
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr path(final Node node) throws QueryException {
    final ExprList el = new ExprList();
    Expr root = null;
    Node operand = node;
    final boolean abs = node instanceof AbsolutePathExpr;
    if(abs) {
      // absolute path: root, and an optional descendant-or-self step
      final AbsolutePathExpr absolute = (AbsolutePathExpr) node;
      final InputInfo ii = infoAt(node.begin() + 1);
      root = Function._UTIL_ROOT.get(ii, new ContextValue(ii));
      if(absolute.relativePathExpr() == null) return root;
      if(absolute.isSlash2()) {
        el.add(new CachedStep(infoAt(node.begin() + 2), Axis.DESCENDANT_OR_SELF, NodeTest.NODE));
      }
      operand = absolute.relativePathExpr();
    }
    // steps and slashes alternate
    final Node[] items = operand instanceof final RelativePathExpr relative ? relative.children() :
      new Node[] { operand };
    for(int i = 0; i < items.length; i++) {
      final Node item = items[i];
      if(i % 2 == 1) {
        if(item.sym() == Sym.SLASH2) {
          el.add(new CachedStep(infoAt(item.end()), Axis.DESCENDANT_OR_SELF, NodeTest.NODE));
        }
      } else {
        final Expr expr = item instanceof final Nodes.AxisStep step ? axisStep(step) : single(item);
        if(i == 0 && !abs && !(expr instanceof Step)) root = expr;
        else el.add(expr);
      }
    }
    return Path.get(infoAfter(node), root, dynamicSteps(root, el.finish()));
  }

  /**
   * Parses the "AxisStep" rule.
   * @param node axis step
   * @return step
   * @throws QueryException query exception
   */
  private Step axisStep(final Nodes.AxisStep node) throws QueryException {
    final AbbreviatedStep abbreviated = node.abbreviatedStep();
    final FullStep full = node.fullStep();
    Axis axis;
    final ExprInfo test;
    if(full != null) {
      axis = axis(full.axis());
      test = nodeTest(full.nodeTest(), axis);
    } else if(abbreviated.isDot2()) {
      axis = Axis.PARENT;
      test = NodeTest.NODE;
    } else if(abbreviated.isAtSign()) {
      axis = Axis.ATTRIBUTE;
      test = nodeTest(abbreviated.nodeTest(), axis);
    } else {
      axis = Axis.CHILD;
      test = simpleNodeTest(abbreviated.simpleNodeTest(), Kind.ELEMENT, true);
      final Kind kind = ((Test) test).kind;
      if(kind == Kind.ATTRIBUTE) axis = Axis.ATTRIBUTE;
      else if(kind == Kind.NAMESPACE) axis = Axis.NAMESPACE;
    }

    final Predicate[] predicates = node.children();
    final Expr[] preds = new Expr[predicates.length];
    for(int p = 0; p < preds.length; p++) preds[p] = single(predicates[p].expr());

    // step with node test, or step with selector
    final InputInfo ii = infoAfter(node);
    if(test instanceof final Test t) return new CachedStep(ii, axis, t, preds);
    return new SelectorStep(ii, axis, (Expr) test, preds);
  }

  /**
   * Returns the axis of a step.
   * @param node axis
   * @return axis
   */
  private Axis axis(final Nodes.Axis node) {
    // the name is followed by "::"
    int e = node.begin();
    while(input[e] != ':' && input[e] > ' ') e++;
    final String name = new String(input, node.begin(), e - node.begin());
    for(final Axis axis : AXES) {
      if(axis.name.equals(name)) return axis;
    }
    return Axis.CHILD;
  }

  /**
   * Parses the "NodeTest" rule.
   * @param node node test
   * @param axis axis
   * @return test or selector
   * @throws QueryException query exception
   */
  private ExprInfo nodeTest(final Node node, final Axis axis) throws QueryException {
    // dynamic node test: enclosed expression
    if(node.sym() == Sym.EnclosedExpr) return enclosedExpr(node);

    final Kind kind = axis.principal();
    if(!(node instanceof final UnionNodeTest union)) return simpleNodeTest(node, kind, true);

    final ArrayList<Test> list = new ArrayList<>();
    for(final Node simple : union.children()) {
      final Test t = simpleNodeTest(simple, kind, true);
      if(!list.contains(t)) list.add(t);
    }
    return Test.get(list);
  }

  /**
   * Parses a postfix expression: the "FilterExpr", "DynamicFunctionCall", "LookupExpr" and
   * "MethodCall" rules.
   * @param node postfix expression
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr postfix(final Node node) throws QueryException {
    return switch(node.sym()) {
      case Sym.FilterExpr -> {
        // combine consecutive predicates
        int count = 0;
        Node inner = node;
        while(inner instanceof final FilterExpr filter) {
          count++;
          inner = filter.postfixExpr();
        }
        final Node[] conditions = new Node[count];
        Node filter = node;
        for(int p = count; p-- > 0;) {
          final FilterExpr f = (FilterExpr) filter;
          conditions[p] = f.predicate().expr();
          filter = f.postfixExpr();
        }
        final Expr expr = single(inner);
        final Expr[] preds = new Expr[count];
        for(int p = 0; p < count; p++) preds[p] = single(conditions[p]);
        yield new CachedFilter(infoAfter(node), expr, preds);
      }
      case Sym.MethodCall -> methodCall((MethodCall) node);
      case Sym.DynamicFunctionCall -> {
        final DynamicFunctionCall call = (DynamicFunctionCall) node;
        final Expr expr = single(call.postfixExpr());
        yield Functions.dynamic(expr, argumentList(call.positionalArgumentList(), null));
      }
      default -> {
        final LookupExpr lookup = (LookupExpr) node;
        yield lookup(single(lookup.postfixExpr()), lookup.lookup());
      }
    };
  }

  /**
   * Parses the "Lookup" and "UnaryLookup" rules.
   * @param expr expression (can be {@code null})
   * @param node lookup
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr lookup(final Expr expr, final Nodes.Lookup node) throws QueryException {
    final InputInfo info = info(node.keySpecifier());
    return new Lookup(info, expr != null ? expr : new ContextValue(info),
        keySpecifier(node.keySpecifier()));
  }

  /**
   * Parses the "MethodCall" rule.
   * @param node method call
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr methodCall(final MethodCall node) throws QueryException {
    final Expr expr = single(node.postfixExpr());
    final InputInfo info = info(node.ncName());
    final Str key = Str.get(token(text(node.ncName())));
    final int s = localVars.openScope();
    final For fr = new For(localVars.add(new Var(new QNm("method"), null, qc, info)), expr);
    final VarRef arg = new VarRef(info, fr.var);
    final FuncBuilder fb = argumentList(node.positionalArgumentList(), arg);
    if(fb.placeholders != 0) throw error(node, INVPLACEHOLDER_X, key);
    final Lookup func = new Lookup(info, arg, key);
    final Expr call = Functions.dynamic(func, fb);
    final GFLWOR gflwor = new GFLWOR(info, fr, call);
    localVars.closeScope(s);
    return gflwor;
  }

  /**
   * Parses the "PrimaryExpr" rule.
   * @param node primary expression
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr primary(final Node node) throws QueryException {
    // as in single(): the symbol is switched on, as that is faster than a switch on the types
    return switch(node.sym()) {
      case Sym.ContextValueRef -> new ContextValue(infoAt(node.end()));
      case Sym.MapConstructor -> mapConstructor((MapConstructor) node);
      case Sym.SquareArrayConstructor, Sym.CurlyArrayConstructor ->
        arrayConstructor((ArrayConstructor) node);
      case Sym.Lookup -> lookup(null, (Nodes.Lookup) node);
      case Sym.ParenthesizedExpr -> {
        final Node expr = ((ParenthesizedExpr) node).expr();
        yield expr == null ? Empty.VALUE : single(expr);
      }
      case Sym.VarRef ->
        localVars.resolve(eQName(((Nodes.VarRef) node).eqName(), null), info(node));
      case Sym.FunctionCall -> {
        final FunctionCall call = (FunctionCall) node;
        final QNm name = eQName(call.eqName(), null);
        final FuncBuilder fb = argumentList(call.argumentList(), null);
        yield qc.functions.newRef(() -> Functions.get(name, fb, qc));
      }
      case Sym.NamedFunctionRef -> functionRef((NamedFunctionRef) node);
      case Sym.InlineFunctionExpr -> inlineFunction((InlineFunctionExpr) node);
      case Sym.OrderedExpr -> enclosedExpr(((OrderedExpr) node).enclosedExpr());
      case Sym.UnorderedExpr -> enclosedExpr(((UnorderedExpr) node).enclosedExpr());
      case Sym.DirElemConstructor, Sym.DirCommentConstructor, Sym.DirPIConstructor ->
        dirConstructor((DirectConstructor) node, true);
      case Sym.CompDocConstructor, Sym.CompElemConstructor, Sym.CompAttrConstructor,
        Sym.CompNamespaceConstructor, Sym.CompTextConstructor, Sym.CompCommentConstructor,
        Sym.CompPIConstructor ->
        compConstructor((ComputedConstructor) node);
      case Sym.StringTemplate, Sym.StringConstructor -> stringConstructor(node);
      default -> {
        final Item item = literal(node, true);
        if(item == Itr.MIN) yield minInteger = FnError.get(RANGE_X.get(info(node), token));
        yield item == Dbl.NEGATIVE_ZERO ? FnError.get(RANGE_X.get(info(node), token)) : item;
      }
    };
  }

  /**
   * Parses the "KeySpecifier" rule.
   * @param node key of the specifier
   * @return specifier expression ({@link Str#WILDCARD} means wildcard)
   * @throws QueryException query exception
   */
  private Expr keySpecifier(final Node node) throws QueryException {
    final int kind = node.sym();
    if(kind == Sym.NC_NAME) return Str.get(token(text(node)));
    if(kind == Sym.STAR) return Str.WILDCARD;
    return LITERALS.get(kind) ? literal(node, false) : primary(node);
  }

  /**
   * Parses the "MapConstructor" rule.
   * @param node map constructor
   * @return map constructor
   * @throws QueryException query exception
   */
  private CMap mapConstructor(final MapConstructor node) throws QueryException {
    // the position follows the opening brace
    int brace = node.begin();
    while(input[brace] != '{') brace++;
    final InputInfo info = infoAt(brace + 1);
    final ExprList el = new ExprList();
    final ItemSet set = new HashItemSet(ItemSet.Mode.ATOMIC, info);
    for(final MapConstructorEntry entry : node.children()) {
      final Expr first = single(entry.exprSingle());
      el.add(first);
      if(entry.exprSingle2() == null) {
        el.add(Empty.UNDEFINED);
      } else {
        if(first instanceof final Item item && !item.isEmpty() && !set.add(item)) {
          pos = before(entry.exprSingle2().begin());
          throw error(MAPDUPLKEY_X, first);
        }
        el.add(single(entry.exprSingle2()));
      }
    }
    return new CMap(info, el.finish());
  }

  /**
   * Parses the "ArrayConstructor" rule.
   * @param node array constructor
   * @return array constructor
   * @throws QueryException query exception
   */
  private Expr arrayConstructor(final ArrayConstructor node) throws QueryException {
    return switch(node) {
      case final CurlyArrayConstructor curly -> {
        final Node enclosed = curly.enclosedExpr().expr();
        if(enclosed == null) yield XQArray.empty();
        final Expr expr = single(enclosed);
        yield new CItemArray(infoAt(node.end()), expr);
      }
      case final SquareArrayConstructor square -> {
        final Node[] members = square.children();
        yield members.length == 0 ? XQArray.empty() :
          new CArray(infoAt(node.begin() + 1), operands(members));
      }
    };
  }

  /**
   * Parses the "InlineFunctionExpr" rule.
   * @param node inline function
   * @return function item
   * @throws QueryException query exception
   */
  private Expr inlineFunction(final InlineFunctionExpr node) throws QueryException {
    final AnnList anns = annotations(node.children()).check(false, true);
    final FunctionSignature sig = node.functionSignature();
    final Closure closure;
    if(sig != null) {
      final HashMap<Var, Expr> global = localVars.pushContext(true);
      final Params params = paramList(sig.paramList().children(), sig.typeDeclaration());
      final Expr expr = enclosedExpr(node.functionBody());
      closure = new Closure(infoAt(node.end()), expr, params, anns, localVars.popContext(),
          global);
    } else {
      closure = focusFunction(anns, node.functionBody(), true, infoAt(node.end()));
    }
    if(anns.contains(Annotation.PRIVATE) || anns.contains(Annotation.PUBLIC)) {
      pos = node.end();
      throw error(NOVISALLOWED);
    }
    return closure;
  }

  /**
   * Parses the body of a focus function.
   * @param anns annotations
   * @param body body
   * @param enclosed parse an enclosed expression
   * @param ci input information of the function
   * @return function
   * @throws QueryException query exception
   */
  private Closure focusFunction(final AnnList anns, final Node body, final boolean enclosed,
      final InputInfo ci) throws QueryException {
    final HashMap<Var, Expr> global = localVars.pushContext(true);
    // register the parameter without making it visible to the body
    final InputInfo ii = info(body);
    final int s = localVars.openScope();
    final Params params = new Params().add(new QNm("arg"), Types.ITEM_ZM, null, ii).
        finish(qc, localVars);
    localVars.closeScope(s);
    final Expr expr = new Pipeline(ii, new VarRef(ii, params.vars()[0]),
        enclosed ? enclosedExpr(body) : single(body));
    return new Closure(ci, expr, params, anns, localVars.popContext(), global);
  }

  /**
   * Parses the "NamedFunctionRef" rule.
   * @param node function reference
   * @return function item
   * @throws QueryException query exception
   */
  private Expr functionRef(final NamedFunctionRef node) throws QueryException {
    final Node eqname = node.eqName(), arity = node.integerLiteral();
    final QNm name = eQName(eqname, null);
    if(reserved(name, eqname.begin())) throw error(eqname, RESERVED_X, name.local());
    final Item item = literal(arity, true);
    final long num = item instanceof final Itr itr ? itr.itr() : Long.MAX_VALUE;
    if(item == Itr.MIN || item == Dbl.NEGATIVE_ZERO || num > Integer.MAX_VALUE) {
      throw RANGE_X.get(infoAt(arity.end()), text(arity).replace("_", ""));
    }
    final InputInfo info = infoAt(node.end());
    return qc.functions.newRef(() -> Functions.item(name, (int) num, false, info, qc));
  }

  /**
   * Parses the "Constant" rule.
   * @param node constant
   * @return item
   * @throws QueryException query exception
   */
  private Item constant(final Constant node) throws QueryException {
    if(node.isTrue()) return Bln.TRUE;
    if(node.isFalse()) return Bln.FALSE;
    if(node.stringLiteral() != null) return Str.get(stringLiteral(node.stringLiteral()));
    if(node.qNameLiteral() != null) return literal(node.qNameLiteral(), false);
    if(!node.isMinus()) return literal(node.numericLiteral(), false);

    // negated number: 2^63 is the minimum integer
    final Item item = literal(node.numericLiteral(), true);
    if(item == Dbl.NEGATIVE_ZERO) throw RANGE_X.get(info(node), token);
    if(item instanceof final Itr itr) return item == Itr.MIN ? item : Itr.get(-itr.itr());
    if(item instanceof final Dec dec) return Dec.get(dec.dec(info(node)).negate());
    return Dbl.get(-item.dbl(info(node)));
  }

  /**
   * Parses the "Literal" rule.
   * @param node literal
   * @param dummy return {@link Dbl#NEGATIVE_ZERO} for an invalid range, minimum integer for 2^63
   * @return literal
   * @throws QueryException query exception
   */
  private Item literal(final Node node, final boolean dummy) throws QueryException {
    if(node instanceof final QNameLiteral qname) return eQName(qname.eqName(), null);

    final int kind = node.sym();
    if(kind == Sym.STRING_LITERAL) return Str.get(stringLiteral(node));

    final String text = text(node).replace("_", "");
    if(kind == Sym.DECIMAL_LITERAL) return Dec.get(new BigDecimal(text));
    if(kind == Sym.DOUBLE_LITERAL) {
      return Dbl.get(Dbl.parse(token(text.replace('E', 'e')), info(node)));
    }
    if(kind == Sym.INTEGER_LITERAL && text.length() < 19) return Itr.get(Long.parseLong(text));

    final int base = kind == Sym.HEX_INTEGER_LITERAL ? 16 :
      kind == Sym.BINARY_INTEGER_LITERAL ? 2 : 10;
    final String digits = base == 10 ? text : text.substring(2);
    final BigInteger value = new BigInteger(digits, base);
    if(value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) return Itr.get(value.longValue());
    // 2^63: minimum integer, which is only valid if negated
    token.reset().add(digits);
    if(dummy) return value.equals(MIN_INTEGER) ? Itr.MIN : Dbl.NEGATIVE_ZERO;
    throw RANGE_X.get(info(node), token);
  }

  /**
   * Parses the "ArgumentList" rule.
   * @param node argument list
   * @param expr first argument (can be {@code null})
   * @return function builder
   * @throws QueryException query exception
   */
  private FuncBuilder argumentList(final ArgumentList node, final Expr expr)
      throws QueryException {
    return argList(node, node.positionalArguments(), node.keywordArguments(), expr);
  }

  /**
   * Parses the "PositionalArgumentList" rule.
   * @param node argument list
   * @param expr first argument (can be {@code null})
   * @return function builder
   * @throws QueryException query exception
   */
  private FuncBuilder argumentList(final PositionalArgumentList node, final Expr expr)
      throws QueryException {
    return argList(node, node.positionalArguments(), null, expr);
  }

  /**
   * Parses the arguments of a function call.
   * @param node argument list
   * @param positional positional arguments (can be {@code null})
   * @param keywords keyword arguments (can be {@code null})
   * @param expr first argument (can be {@code null})
   * @return function builder
   * @throws QueryException query exception
   */
  private FuncBuilder argList(final Node node, final PositionalArguments positional,
      final KeywordArguments keywords, final Expr expr) throws QueryException {

    final FuncBuilder fb = new FuncBuilder(info(node));
    if(expr != null) fb.add(expr, null);
    if(positional != null) {
      for(final Node argument : positional.children()) fb.add(argument(argument), null);
    }
    if(keywords != null) {
      for(final KeywordArgument keyword : keywords.children()) {
        final QNm name = eQName(keyword.eqName(), null);
        if(fb.add(argument(keyword.argument()), name)) {
          pos = keyword.end();
          throw error(PARAMTWICE_X, name.prefixString());
        }
      }
    }
    return fb;
  }

  /**
   * Parses an argument.
   * @param node argument or placeholder
   * @return expression
   * @throws QueryException query exception
   */
  private Expr argument(final Node node) throws QueryException {
    return node.sym() == Sym.ArgumentPlaceholder ? Empty.UNDEFINED : single(node);
  }

  /**
   * Parses the "StringConstructor" and "StringTemplate" rules.
   * @param node string constructor or template
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr stringConstructor(final Node node) throws QueryException {
    final Node[] parts = node instanceof final StringConstructor constr ?
      constr.stringConstructorContent().children() : ((Nodes.StringTemplate) node).children();
    final ExprList el = new ExprList();
    final TokenBuilder tb = new TokenBuilder();
    for(final Node part : parts) {
      if(part.sym() == Sym.STRING_CONSTRUCTOR_CHARS) {
        tb.add(text(part));
      } else if(part.sym() == Sym.STRING_TEMPLATE_FIXED_PART) {
        // braces and backticks are doubled
        tb.add(text(part).replace("{{", "{").replace("}}", "}").replace("``", "`"));
      } else {
        if(!tb.isEmpty()) el.add(Str.get(tb.next()));
        final EnclosedExpr enclosed = part instanceof final StringInterpolation interpolation ?
          interpolation.enclosedExpr() : (EnclosedExpr) part;
        final Expr expr = enclosed.expr() == null ? null : single(enclosed.expr());
        if(expr != null) el.add(Function.STRING_JOIN.get(info(part), expr, Str.get(' ')));
      }
    }
    if(!tb.isEmpty()) el.add(Str.get(tb.next()));
    return el.size() == 1 ? el.get(0) : new Concat(infoAt(node.end()), el.finish());
  }

  // ---------------------------------------------------------------------------------------------
  // constructors

  /**
   * Adds code points of the input to a token builder.
   * @param tb token builder
   * @param begin first position
   * @param end end position
   */
  private void add(final TokenBuilder tb, final int begin, final int end) {
    for(int p = begin; p < end; p++) tb.add(input[p]);
  }

  /**
   * Parses the "DirectConstructor" rule.
   * @param node direct constructor
   * @param root root call
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirConstructor(final DirectConstructor node, final boolean root)
      throws QueryException {

    final Expr expr = switch(node) {
      case final DirCommentConstructor comment -> {
        final Node text = comment.dirCommentContents();
        yield new CComm(infoAt(node.end()), false,
            Str.get(text == null ? EMPTY : token(text(text))));
      }
      case final DirPIConstructor pi -> dirPI(pi);
      case final DirElemConstructor element -> dirElement(element);
    };
    return root ? expr : ((CNode) expr).nested();
  }

  /**
   * Parses the "DirElemConstructor" and "DirAttributeList" rules.
   * @param node element constructor
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElement(final DirElemConstructor node) throws QueryException {
    final InputInfo ii = info(node.qName());
    final byte[] qnm = token(text(node.qName()));

    // cache namespace information
    final int size = qc.ns.size(), cpos = constrNS.size();
    final byte[] nse = sc.elemNS, nsd = sc.dirNS;
    final Atts ns = new Atts();
    final ExprList cont = new ExprList();

    // parse attributes: namespace declarations first, as they apply to the other attributes
    final DirAttributeList attributeList = node.dirAttributeList();
    final Node[] attrs = attributeList == null ? NONE : attributeList.children();
    namespaceDecls(attrs, ns);
    final ArrayList<QNm> atts = attributes(attrs, cont);

    // resolve element name
    final QNm name = new QNm(qnm);
    resolveQNm(name, sc.dirNS, ii);

    // parse content: consecutive text parts are joined
    final TokenBuilder tb = new TokenBuilder();
    boolean strip = true;
    for(final Node child : node.children()) {
      final int kind = child.sym();
      switch(kind) {
        case Sym.ELEMENT_CONTENT_CHAR -> add(tb, child.begin(), child.end());
        case Sym.LBRACE2 -> tb.add('{');
        case Sym.RBRACE2 -> tb.add('}');
        case Sym.CDataSection -> {
          final Node contents = ((CDataSection) child).cDataSectionContents();
          if(contents != null) add(tb, contents.begin(), contents.end());
          strip = false;
        }
        case Sym.PREDEFINED_ENTITY_REF, Sym.CHAR_REF -> {
          pos = child.begin();
          strip &= !entity(tb);
        }
        default -> {
          final Str txt = text(tb, strip);
          if(txt != null) cont.add(txt);
          tb.reset();
          strip = true;
          cont.add(kind == Sym.EnclosedExpr ? enclosedExpr(child) :
            dirConstructor((DirectConstructor) child, false));
        }
      }
    }
    final Str txt = text(tb, strip);
    if(txt != null) cont.add(txt);

    // closing tag
    final Node end = node.qName2();
    if(end != null) {
      final byte[] close = token(text(end));
      if(!eq(name.string(), close)) {
        pos = node.end();
        throw error(TAGWRONG_X_X, name.string(), close);
      }
    }

    // check for duplicate attribute names
    if(atts != null) {
      final int as = atts.size();
      for(int a = 0; a < as - 1; a++) {
        for(int b = a + 1; b < as; b++) {
          if(atts.get(a).eq(atts.get(b))) {
            pos = node.end();
            throw error(ATTDUPL_X, atts.get(a).prefixString());
          }
        }
      }
    }

    qc.ns.size(size);
    sc.elemNS = nse;
    sc.dirNS = nsd;

    // propagate namespace declarations to enclosed constructors, register own namespaces
    final int cs = constrNS.size(), nl = ns.size();
    for(int c = cpos; c < cs; c++) {
      final ConstrNS cns = constrNS.get(c);
      for(int n = 0; n < nl; n++) {
        final byte[] prefix = ns.name(n);
        if(!cns.nspaces.contains(prefix) && !cns.inherited.contains(prefix)) {
          cns.inherited.add(prefix, ns.value(n));
        }
      }
    }
    final Atts inherited = new Atts();
    constrNS.add(new ConstrNS(ns, inherited));
    return new CElem(infoAt(node.end()), false, name, ns, inherited, cont.finish());
  }

  /**
   * Checks if the name of a direct attribute is a namespace declaration.
   * @param name attribute name
   * @return result of check
   */
  private static boolean isNamespace(final byte[] name) {
    return startsWith(name, XMLNS_COLON) || eq(name, XMLNS);
  }

  /**
   * Processes the namespace declarations among the attributes of a direct element constructor.
   * @param attrs names and values of the attributes
   * @param ns declared namespaces
   * @throws QueryException query exception
   */
  private void namespaceDecls(final Node[] attrs, final Atts ns) throws QueryException {
    boolean xmlDecl = false;
    for(int a = 0; a < attrs.length; a += 2) {
      final byte[] atn = token(text(attrs[a]));
      if(!isNamespace(atn)) continue;

      final boolean pr = startsWith(atn, XMLNS_COLON);
      final ExprList attv = attributeValue((DirAttributeValue) attrs[a + 1], true);
      final byte[] prefix = pr ? local(atn) : EMPTY;
      final byte[] uri = attv.isEmpty() ? EMPTY : ((Str) attv.get(0)).string();
      pos = attrs[a + 1].end();
      if(eq(prefix, XML) && eq(uri, XML_URI)) {
        if(xmlDecl) throw error(DUPLNSDEF_X, XML);
        xmlDecl = true;
        continue;
      }
      if(!Uri.get(uri).isValid()) throw error(INVURI_X, uri);
      if(pr) {
        if(uri.length == 0) throw error(NSEMPTYURI);
        if(eq(prefix, XML, XMLNS)) throw error(BINDXML_X, prefix);
        if(eq(uri, XML_URI)) throw error(BINDXMLURI_X_X, uri, XML);
        if(eq(uri, XMLNS_URI)) throw error(BINDXMLURI_X_X, uri, XMLNS);
        qc.ns.add(prefix, uri);
      } else {
        if(eq(uri, XML_URI)) throw error(XMLNSDEF_X, uri);
        sc.dirNS = uri;
        if(!sc.elemNsFixed) sc.elemNS = sc.dirNS;
      }
      if(ns.contains(prefix)) throw error(DUPLNSDEF_X, prefix);
      ns.add(prefix, uri);
    }
  }

  /**
   * Creates the attribute constructors of a direct element constructor.
   * @param attrs names and values of the attributes
   * @param cont content of the element, to which the constructors are added
   * @return names of the attributes (can be {@code null})
   * @throws QueryException query exception
   */
  private ArrayList<QNm> attributes(final Node[] attrs, final ExprList cont)
      throws QueryException {

    ArrayList<QNm> atts = null;
    for(int a = 0; a < attrs.length; a += 2) {
      final byte[] atn = token(text(attrs[a]));
      if(isNamespace(atn)) continue;

      final ExprList attv = attributeValue((DirAttributeValue) attrs[a + 1], false);
      final QNm attn = new QNm(atn);
      if(atts == null) atts = new ArrayList<>(1);
      atts.add(attn);
      final InputInfo ai = infoAt(attrs[a + 1].end());
      resolveQNm(attn, null, ai);
      cont.add(new CAttr(ai, false, attn, attv.finish()));
    }
    return atts;
  }

  /**
   * Parses a direct attribute value into its literal string and enclosed-expression parts.
   * @param node attribute value
   * @param literal reject enclosed expressions (namespace declaration value)
   * @return value parts
   * @throws QueryException query exception
   */
  private ExprList attributeValue(final DirAttributeValue node, final boolean literal)
      throws QueryException {

    final ExprList attv = new ExprList();
    final TokenBuilder tb = new TokenBuilder();
    for(final Node part : node.children()) {
      switch(part.sym()) {
        case Sym.EnclosedExpr -> {
          if(literal) throw error(part, NSCONS);
          if(!tb.isEmpty()) attv.add(Str.get(tb.next()));
          attv.add(enclosedExpr(part));
        }
        case Sym.PREDEFINED_ENTITY_REF, Sym.CHAR_REF -> {
          pos = part.begin();
          entity(tb);
        }
        case Sym.LBRACE2 -> tb.add('{');
        case Sym.RBRACE2 -> tb.add('}');
        case Sym.ESCAPE_QUOT -> tb.add('"');
        case Sym.ESCAPE_APOS -> tb.add('\'');
        default -> {
          // whitespace is normalized
          for(int p = part.begin(), end = part.end(); p < end; p++) {
            final int cp = input[p];
            if(cp == '\r' && p + 1 < end && input[p + 1] == '\n') p++;
            tb.add(cp == '\n' || cp == '\r' || cp == '\t' ? ' ' : cp);
          }
        }
      }
    }
    if(!tb.isEmpty()) attv.add(Str.get(tb.finish()));
    return attv;
  }

  /**
   * Parses the "DirPIConstructor" and "DirPIContents" rules.
   * @param node processing instruction constructor
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirPI(final DirPIConstructor node) throws QueryException {
    final byte[] name = token(text(node.piTarget()));
    if(eq(lc(name), XML)) {
      pos = node.piTarget().end();
      throw error(PIXML_X, name);
    }
    final Node contents = node.dirPiContents();
    byte[] text = EMPTY;
    if(contents != null) {
      int p = contents.begin();
      while(p < contents.end() && (input[p] == ' ' || input[p] == '\t' || input[p] == '\r' ||
          input[p] == '\n')) p++;
      text = token(new String(input, p, contents.end() - p));
    }
    return new CPI(infoAt(node.end()), false, Str.get(name), Str.get(text));
  }

  /**
   * Parses the "ComputedConstructor" rule.
   * @param node computed constructor
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compConstructor(final ComputedConstructor node) throws QueryException {
    return switch(node) {
      // records with a single component: the offsets are not needed
      case CompDocConstructor(final int begin, final int end, final EnclosedExpr body) ->
        new CDoc(info(body), false, enclosedExpr(body));
      case CompTextConstructor(final int begin, final int end, final EnclosedExpr body) ->
        new CTxt(info(body), enclosedExpr(body));
      case CompCommentConstructor(final int begin, final int end, final EnclosedExpr body) ->
        new CComm(info(body), true, enclosedExpr(body));
      case final CompElemConstructor elem -> {
        final Expr name = compName(elem.compNodeName(), sc.elemNS);
        final InputInfo ii = info(elem.enclosedContentExpr());
        // register namespaces, to be enriched by enclosing direct constructors
        final Atts ns = new Atts(), inherited = new Atts();
        constrNS.add(new ConstrNS(ns, inherited));
        yield new CElem(ii, true, name, ns, inherited, enclosedExpr(elem.enclosedContentExpr()));
      }
      case final CompAttrConstructor attr -> {
        final Expr name = compName(attr.compNodeName(), null);
        yield new CAttr(info(attr.enclosedExpr()), true, name, enclosedExpr(attr.enclosedExpr()));
      }
      case final CompNamespaceConstructor ns -> {
        final Expr name = compName(ns.compNodeNCName());
        yield new CNSpace(info(ns.enclosedExpr()), true, name, enclosedExpr(ns.enclosedExpr()));
      }
      case final CompPIConstructor pi -> {
        final Expr name = compName(pi.compNodeNCName());
        yield new CPI(info(pi.enclosedExpr()), true, name, enclosedExpr(pi.enclosedExpr()));
      }
    };
  }

  /**
   * Parses the "CompNodeName" rule.
   * @param node name
   * @param ns default namespace (can be {@code null})
   * @return name
   * @throws QueryException query exception
   */
  private Expr compName(final CompNodeName node, final byte[] ns) throws QueryException {
    // name enclosed in curly braces
    if(node.expr() != null) return single(node.expr());
    // literal name
    return eQName(node.qNameLiteral() != null ? node.qNameLiteral().eqName() : node.eqName(), ns);
  }

  /**
   * Parses the "CompNodeNCName" rule.
   * @param node name
   * @return name
   * @throws QueryException query exception
   */
  private Expr compName(final CompNodeNCName node) throws QueryException {
    // name enclosed in curly braces
    if(node.expr() != null) return single(node.expr());
    // literal name
    return Str.get(token(text(node.markedNCName() != null ? node.markedNCName().ncName() :
      node.ncName())));
  }

  /**
   * Parses the "TryCatchExpr" rule.
   * @param node try/catch expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr tryCatch(final TryCatchExpr node) throws QueryException {
    final Expr expr = enclosedExpr(node.tryClause().enclosedExpr());
    Catch[] catches = { };
    for(final CatchClause clause : node.children()) {
      final ArrayList<Test> tests = nameTestUnion(clause.nameTestUnion(), null);
      final int s = localVars.openScope();
      final InputInfo ii = infoAt(after(clause.nameTestUnion().end()));
      final Var[] vrs = QueryException.variables(qc, ii);
      for(final Var var : vrs) localVars.add(var);
      final Expr rtrn = enclosedExpr(clause.enclosedExpr());
      catches = Array.add(catches, new Catch(ii, rtrn, vrs, tests));
      localVars.closeScope(s);
    }
    final FinallyClause fin = node.finallyClause();
    final Expr fnlly = fin == null ? Empty.VALUE : enclosedExpr(fin.enclosedExpr());
    return new Try(infoAfter(node), expr, fnlly, catches);
  }

  // ---------------------------------------------------------------------------------------------
  // full-text

  /**
   * Parses the "FTSelection", "FTOr", "FTAnd", "FTMildNot", "FTUnaryNot" and "FTPrimary" rules.
   * @param node full-text selection
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftSelection(final Node node) throws QueryException {
    switch(node) {
      case final Nodes.FTSelection selection -> {
        FTExpr expr = ftSelection(selection.ftOr()), first = null;
        boolean ordered = false;
        for(final Node filter : selection.children()) {
          switch(filter) {
            case final Terminal ordering -> ordered = true;
            case final Nodes.FTWindow window -> {
              final InputInfo ii = info(window.additiveExpr());
              expr = new FTWindow(ii, expr, single(window.additiveExpr()),
                  ftUnit(window.ftUnit()));
            }
            case final Nodes.FTDistance distance -> {
              final FTRange range = distance.ftRange();
              final InputInfo ii = info(range);
              final Expr[] occ = ftRange(range.isExactly(), range.additiveExpr(),
                  range.additiveExpr2(), range.isLeast(), range.isFrom(), false, Itr.MIN);
              expr = new FTDistance(ii, expr, occ[0], occ[1], ftUnit(distance.ftUnit()));
            }
            case final Nodes.FTContent content ->
              expr = new FTContent(info(filter), expr, content.isStart() ? FTContents.START :
                content.isEnd() ? FTContents.END : FTContents.ENTIRE);
            case final Nodes.FTScope scope ->
              expr = new FTScope(info(filter), expr, scope.isSame(),
                  scope.ftBigUnit().sym() == Sym.SENTENCE ? FTUnit.SENTENCES : FTUnit.PARAGRAPHS);
            default -> throw new IllegalStateException("Unexpected node: " + filter.sym());
          }
          // the "ordered" option is applied to the operand of the first other filter
          if(first == null && filter.sym() != Sym.ORDERED) first = expr;
        }
        if(!ordered) return expr;
        if(first == null) return new FTOrder(info(node), expr);
        first.exprs[0] = new FTOrder(info(node), first.exprs[0]);
        return expr;
      }
      case final Nodes.FTOr or -> {
        return new FTOr(info(node), ftList(or.children()));
      }
      case final Nodes.FTAnd and -> {
        return new FTAnd(info(node), ftList(and.children()));
      }
      case final Nodes.FTMildNot mild -> {
        final Node[] children = mild.children();
        final FTExpr expr = ftSelection(children[0]);
        final FTExpr[] list = new FTExpr[children.length - 1];
        for(int c = 0; c < list.length; c++) list[c] = ftSelection(children[c + 1]);
        // convert "A not in B not in ..." to "A not in (B or ...)"
        final InputInfo ii = infoAfter(children[children.length - 1]);
        final FTExpr not = list.length == 1 ? list[0] : new FTOr(ii, list);
        if(expr.usesExclude() || not.usesExclude()) throw error(FTMILD, ii);
        return new FTMildNot(ii, expr, not);
      }
      case final Nodes.FTUnaryNot not -> {
        final FTExpr expr = ftSelection(not.ftPrimaryWithOptions());
        return new FTNot(info(node), expr);
      }
      case final Nodes.FTPrimaryWithOptions primary -> {
        FTExpr expr = ftSelection(primary.ftPrimary());
        final FTMatchOptions matchOptions = primary.ftMatchOptions();
        final FTOpt fto = new FTOpt();
        if(matchOptions != null) {
          for(final FTMatchOption option : matchOptions.children()) ftMatchOption(option, fto);
          // check if specified language is not available
          if(fto.ln == null) fto.ln = Language.def();
          pos = after(matchOptions.end());
          if(!Tokenizer.supportFor(fto.ln)) throw error(FTNOTOK_X, fto.ln);
          if(fto.is(ST) && fto.sd == null && !Stemmer.supportFor(fto.ln))
            throw error(FTNOSTEM_X, fto.ln);
        }
        // consume weight option
        final Nodes.FTWeight weight = primary.ftWeight();
        if(weight != null) {
          final InputInfo ii = info(weight);
          expr = new FTWeight(ii, expr, single(weight.expr()));
        }
        return matchOptions != null ? new FTOptions(info(node), expr, fto) : expr;
      }
      case final Nodes.FTPrimary primary -> {
        return ftPrimary(primary);
      }
      default -> throw new IllegalStateException("Unexpected node: " + node.sym());
    }
  }

  /**
   * Parses the operands of a full-text disjunction or conjunction.
   * @param children operands
   * @return query expressions
   * @throws QueryException query exception
   */
  private FTExpr[] ftList(final Node[] children) throws QueryException {
    final FTExpr[] list = new FTExpr[children.length];
    for(int c = 0; c < list.length; c++) list[c] = ftSelection(children[c]);
    return list;
  }

  /**
   * Parses the "FTPrimary" rule.
   * @param node primary full-text selection
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftPrimary(final FTPrimary node) throws QueryException {
    final FTExtensionSelection extension = node.ftExtensionSelection();
    if(extension != null) {
      final Pragma[] pragmas = pragmas(extension.children());
      if(extension.ftSelection() == null) {
        pos = extension.end() - 1;
        throw error(NOPRAGMA, found());
      }
      FTExpr expr = ftSelection(extension.ftSelection());
      final InputInfo ii = info(extension);
      for(int p = pragmas.length - 1; p >= 0; p--) expr = new FTExtension(ii, pragmas[p], expr);
      return expr;
    }
    final Nodes.FTWords words = node.ftWords();
    if(words == null) return ftSelection(node.ftSelection());

    final FTWordsValue value = words.ftWordsValue();
    final Expr e = value.stringLiteral() != null ? Str.get(stringLiteral(value.stringLiteral())) :
      single(value.expr());
    // FTAnyAllOption
    FTMode mode = FTMode.ANY;
    final FTAnyallOption any = words.ftAnyallOption();
    if(any != null) {
      mode = any.isPhrase() ? FTMode.PHRASE : any.isAll() ?
        any.isWords() ? FTMode.ALL_WORDS : FTMode.ALL :
        any.isWord() ? FTMode.ANY_WORD : FTMode.ANY;
    }
    // FTTimes
    final FTTimes times = node.ftTimes();
    final Expr[] occ = times == null ? null : ftRange(times.ftRange().isExactly(),
        times.ftRange().additiveExpr(), times.ftRange().additiveExpr2(),
        times.ftRange().isLeast(), times.ftRange().isFrom(), false, Itr.ZERO);
    return new FTWords(info(node), e, mode, occ);
  }

  /**
   * Parses the "FTRange" and "FTLiteralRange" rules.
   * @param exactly "exactly" flag
   * @param first first value
   * @param second second value (only for "from")
   * @param least "at least" flag (otherwise "at most")
   * @param from "from" flag
   * @param literal accept only integers ("FTLiteralRange")
   * @param min default minimum
   * @return query expressions
   * @throws QueryException query exception
   */
  private Expr[] ftRange(final boolean exactly, final Node first, final Node second,
      final boolean least, final boolean from, final boolean literal, final Itr min)
      throws QueryException {

    final Expr[] occ = { min, Itr.MAX };
    if(exactly) {
      occ[0] = occ[1] = ftAdditive(first, literal);
    } else if(from) {
      occ[0] = ftAdditive(first, literal);
      occ[1] = ftAdditive(second, literal);
    } else {
      occ[least ? 0 : 1] = ftAdditive(first, literal);
    }
    return occ;
  }

  /**
   * Returns an argument of the "FTRange" rule.
   * @param node argument
   * @param literal accept only integers
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr ftAdditive(final Node node, final boolean literal) throws QueryException {
    return literal ? literal(node, false) : single(node);
  }

  /**
   * Parses the "FTUnit" rule.
   * @param node unit
   * @return unit
   */
  private static FTUnit ftUnit(final Node node) {
    final int unit = node.sym();
    return unit == Sym.WORDS ? FTUnit.WORDS : unit == Sym.SENTENCES ? FTUnit.SENTENCES :
      FTUnit.PARAGRAPHS;
  }

  /**
   * Parses the "FTMatchOption" rule.
   * @param option option
   * @param opt options instance
   * @throws QueryException query exception
   */
  private void ftMatchOption(final FTMatchOption option, final FTOpt opt) throws QueryException {
    // errors refer to the position behind the keywords
    final int words = switch(option) {
      case final Nodes.FTStemOption stem -> stem.isNo() ? 2 : 1;
      case final Nodes.FTWildCardOption wildcard -> wildcard.isNo() ? 2 : 1;
      case final Nodes.FTThesaurusOption thesaurus -> thesaurus.isNo() ? 2 : 1;
      case final Nodes.FTStopWordOption stop -> stop.isNo() ? 3 : 2;
      default -> 1;
    };
    pos = words(option.begin(), words, true);
    switch(option) {
      case final Nodes.FTCaseOption cs -> {
        if(opt.cs != null) throw error(FTDUP_X, CASE);
        opt.cs = cs.isLowercase() ? FTCase.LOWER : cs.isUppercase() ? FTCase.UPPER :
          cs.isSensitive() ? FTCase.SENSITIVE : FTCase.INSENSITIVE;
      }
      case final Nodes.FTDiacriticsOption diacritics -> {
        if(opt.isSet(DC)) throw error(FTDUP_X, DIACRITICS);
        opt.set(DC, diacritics.isSensitive());
      }
      case final Nodes.FTLanguageOption language -> {
        if(opt.ln != null) throw error(FTDUP_X, LANGUAGE);
        final byte[] lan = stringLiteral(language.stringLiteral());
        opt.ln = Language.get(string(lan));
        if(opt.ln == null) {
          pos = after(language.stringLiteral().end());
          throw error(FTNOTOK_X, lan);
        }
      }
      case final Nodes.FTExtensionOption extension ->
        optionDecl(extension.eqName(), extension.stringLiteral());
      case final Nodes.FTStemOption stem -> {
        if(opt.isSet(ST)) throw error(FTDUP_X, STEMMING);
        opt.set(ST, !stem.isNo());
      }
      case final Nodes.FTThesaurusOption thesaurus -> {
        if(opt.th != null) throw error(FTDUP_X, THESAURUS);
        opt.th = new ThesList(thesaurus.isNo());
        for(final FTThesaurusID id : thesaurus.children()) ftThesaurusID(id, opt.th);
      }
      case final Nodes.FTStopWordOption stop -> {
        final boolean using = !stop.isNo();
        // add union/except
        if(opt.sw != null) throw error(FTDUP_X, STOP + ' ' + WORDS);
        final StopWords sw = new StopWords();
        opt.sw = sw;
        if(stop.isDefault()) {
          if(!using) throw error(FTSTOP);
          sw.requestDefaults();
        } else if(using) {
          ftStopWords(stop.ftStopWords(), sw, false);
        }
        for(final FTStopWordsInclExcl inclExcl : stop.children()) {
          ftStopWords(inclExcl.ftStopWords(), sw, inclExcl.isExcept());
        }
      }
      case final Nodes.FTWildCardOption wildcard -> {
        if(opt.isSet(WC)) throw error(FTDUP_X, WILDCARDS);
        if(opt.is(FZ)) throw error(FT_OPTIONS);
        opt.set(WC, !wildcard.isNo());
      }
      case final Nodes.FTFuzzyOption fuzzy -> {
        // extension to the official extension: "using fuzzy"
        if(opt.isSet(FZ)) throw error(FTDUP_X, FUZZY);
        if(opt.is(WC)) throw error(FT_OPTIONS);
        opt.set(FZ, true);
        final Node errors = fuzzy.integerLiteral();
        if(errors != null) {
          opt.errors = Math.clamp(((ANum) ftAdditive(errors, true)).itr(), 0, Integer.MAX_VALUE);
        }
      }
    }
  }

  /**
   * Parses the "FTStopWords" rule.
   * @param node stop words
   * @param sw stop words
   * @param except remove stop words
   * @throws QueryException query exception
   */
  private void ftStopWords(final Nodes.FTStopWords node, final StopWords sw, final boolean except)
      throws QueryException {

    if(!node.isAt()) {
      for(final Node word : node.children()) {
        final byte[] sl = stringLiteral(word);
        if(except) sw.remove(sl);
        else sw.add(sl);
      }
    } else {
      // optional: resolve URI reference
      final String location = string(stringLiteral(node.uriLiteral()));
      pos = node.uriLiteral().end();
      checkExternal(location, info());
      final IO fl = qc.resources.stopWords(location, sc);
      try {
        sw.read(fl, except);
      } catch(final IOException ex) {
        throw error(NOSTOPFILE_X, fl).cause(ex);
      }
    }
  }

  /**
   * Parses the "FTThesaurusID" rule.
   * @param node thesaurus
   * @param queries thesaurus queries
   * @throws QueryException query exception
   */
  private void ftThesaurusID(final Nodes.FTThesaurusID node, final ThesList queries)
      throws QueryException {

    // optional: resolve URI reference
    final String location = string(uriLiteral(node.uriLiteral()));
    // favor database with the same name; files require ADMIN permission
    final String db = Databases.validName(location) &&
        qc.context.soptions.dbExists(location) ? location : null;
    pos = node.uriLiteral().end();
    if(db == null) checkExternal(location, info());
    final IO fl = qc.resources.thesaurus(location, sc);
    final byte[] rel = node.stringLiteral() == null ? EMPTY : stringLiteral(node.stringLiteral());
    final FTLiteralRange range = node.ftLiteralRange();
    long min = 0, max = ThesAccessor.LEVELS;
    if(range != null) {
      // values will always be integer instances
      final Expr[] occ = ftRange(range.isExactly(), range.integerLiteral(),
          range.integerLiteral2(), range.isLeast(), range.isFrom(), true, Itr.ZERO);
      min = ((ANum) occ[0]).itr();
      max = ((ANum) occ[1]).itr();
    }
    queries.add(new ThesAccessor(fl, db, rel, min, max, info(node)));
  }
}
