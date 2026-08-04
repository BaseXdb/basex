package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.inspect.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class defines syntax highlighting for XQuery files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SyntaxXQuery extends SyntaxMarkup {
  /** Reserved words and type names. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();
  /** Names of built-in functions. */
  private static final HashSet<String> FUNCTIONS = new HashSet<>();
  /** Maximum length of a keyword. */
  private static final int MAXKEY = 64;

  /** Code completion snippets. */
  private static final ArrayList<Completion> SNIPPETS = new ArrayList<>();
  /** Code completions (types, functions), ordered by relevance. */
  private static final ArrayList<ArrayList<Completion>> COMPLETIONS = new ArrayList<>();
  /** Code completions (types). */
  private static final ArrayList<Completion> TYPES = new ArrayList<>();
  /** Code completions (annotations), ordered by relevance. */
  private static final ArrayList<ArrayList<Completion>> ANNOTATIONS = new ArrayList<>();
  /** Code completions (declarations that follow the {@code declare} keyword). */
  private static final ArrayList<Completion> PROLOG = new ArrayList<>();
  /** Code completions (declarations that follow the {@code declare default} keywords). */
  private static final ArrayList<Completion> DEFAULTS = new ArrayList<>();
  /** Code completions (start and snippets of prolog declarations). */
  private static final ArrayList<Completion> DECLARATIONS = new ArrayList<>();
  /** Signatures of the built-in functions, with and without namespace prefix. */
  private static final HashMap<String, Signature> SIGNATURES = new HashMap<>();
  /** Code completions (documentation tags of a comment). */
  private static final ArrayList<Completion> TAGS = new ArrayList<>();
  /** Placeholder in a snippet for the namespace prefix of a library module. */
  private static final String PLACEHOLDER = "~";
  /** Pattern for abbreviating names: hyphens and colons separate the words of a name. */
  private static final Pattern ABBR = Pattern.compile("(:?.)[^-:]*-?");

  /** Prolog declaration for boundary whitespace. */
  private static final byte[] BOUNDARY = token("boundary-space");
  /** Clauses of a FLWOR expression (without {@code return}). */
  private static final HashSet<String> CLAUSES = new HashSet<>(Arrays.asList(
    COUNT, FOR, GROUP, LET, ORDER, STABLE, WHERE, WINDOW));

  /** Line type: no clause. */
  private static final int NONE = 0;
  /** Line type: clause that is followed by further clauses. */
  private static final int CLAUSE = 1;
  /** Line type: last clause of a FLWOR expression ({@code return}). */
  private static final int FINAL = 2;
  /** Operators that are followed by an expression (no asterisk: '/*', 'xs:string*'). */
  private static final String OPERATORS = "=+-<>|!/";
  /** Operator keywords that follow an operand. */
  private static final HashSet<String> INFIX = new HashSet<>(Arrays.asList(
    AND, CAST, CASTABLE, DIV, EXCEPT, IDIV, INSTANCE, INTERSECT, MOD, OR, OTHERWISE, TO, TREAT,
    UNION));
  /** Keywords that are followed by an expression. */
  private static final HashSet<String> DANGLING = new HashSet<>(Arrays.asList(
    AS, CASE, DEFAULT, ELSE, IN, OF, RETURN, SATISFIES, THEN, WHERE));
  static {
    // comparison operators are infix operators as well; every infix operator dangles
    for(final CmpOp op : CmpOp.values()) INFIX.add(op.toValueString());
    INFIX.add(CmpOp.EQ.toNodeString());
    DANGLING.addAll(INFIX);
  }

  /** Mode: code. */
  private static final int CODE = MODES;
  /** Mode: double-quoted string. */
  private static final int STRING_D = MODES + 1;
  /** Mode: single-quoted string. */
  private static final int STRING_S = MODES + 2;
  /** Mode: string template ({@code `...`}). */
  private static final int TEMPLATE = MODES + 3;
  /** Mode: string constructor ({@code ``[...]``}). */
  private static final int CONSTRUCTOR = MODES + 4;
  /** Mode: comment (nesting is tracked by the mode stack). */
  private static final int COMMENT = MODES + 5;
  /** Mode: pragma. */
  private static final int PRAGMA = MODES + 6;
  /** Mode: URI of an EQName ({@code Q{...}}). */
  private static final int EQNAME = MODES + 7;

  /** Declaration scan: outside a declaration. */
  private static final int OUTSIDE = 0;
  /** Declaration scan: after the {@code declare} keyword. */
  private static final int DECLARED = 1;
  /** Declaration scan: after the {@code function} keyword. */
  private static final int FUNCTION_NAME = 2;
  /** Declaration scan: after the {@code variable} keyword. */
  private static final int VARIABLE_NAME = 3;
  /** Declaration scan: before the first name of a declaration or the query body. */
  private static final int INITIAL = 4;
  /** Declaration scan: after the {@code default} keyword of a declaration. */
  private static final int DEFAULTED = 5;
  /** Declaration scan: in the parameter list of a function. */
  private static final int FUNCTION_PARAMS = 6;

  /** Module scan: before the module declaration. */
  private static final int MODULE_KEYWORD = 0;
  /** Module scan: after the {@code module} keyword. */
  private static final int MODULE_NAMESPACE = 1;
  /** Module scan: after the {@code namespace} keyword. */
  private static final int MODULE_PREFIX = 2;
  /** Module scan: prefix found, or main module. */
  private static final int MODULE_DONE = 3;

  // initialize keywords
  static {
    try {
      for(final Field f : QueryText.class.getFields()) {
        if("IGNORE".equals(f.getName())) break;
        KEYWORDS.add((String) f.get(null));
      }
      for(final BasicType type : BasicType.values()) {
        final QNm name = type.qname();
        final byte[] prefix = NSGlobal.prefix(name.uri());
        KEYWORDS.add((prefix.length != 0 ? string(prefix) + ':' : "") + string(name.local()));
      }
      for(final Axis axis : Axis.values()) KEYWORDS.add(axis.name);
      for(final CmpOp op : CmpOp.values()) {
        KEYWORDS.add(op.toValueString());
        Collections.addAll(KEYWORDS, op.nodes);
      }
      for(final QNm name : Functions.BUILT_IN) {
        final String local = string(name.local());
        final byte[] prefix = NSGlobal.prefix(name.uri());
        if(prefix.length != 0) FUNCTIONS.add(string(prefix) + ':' + local);
        // functions of the default function namespace can be addressed without prefix
        if(eq(name.uri(), QueryText.FN_URI)) FUNCTIONS.add(local);
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  // initialize code completions
  static {
    final ArrayList<Completion> abbrs = new ArrayList<>(), names = new ArrayList<>(),
        prefixedAbbrs = new ArrayList<>(), prefixedNames = new ArrayList<>(),
        annAbbrs = new ArrayList<>(), annNames = new ArrayList<>();

    DECLARATIONS.add(new Completion(DECLARE, DECLARE, DECLARE + ' ', false));
    final TokenObjectMap<byte[]> map = Util.properties("completions.properties");
    for(final byte[] key : map) {
      final String value = string(map.get(key));
      final Completion snippet = new Completion(string(key), value, value, false);
      // declarations are no expressions: they are only proposed where a new one may start
      (value.startsWith(DECLARE + ' ') || value.startsWith(MODULE + ' ') ||
        value.startsWith(IMPORT + ' ') ? DECLARATIONS : SNIPPETS).add(snippet);
    }
    // add node kinds and atomic types
    for(final Kind kind : Kind.values()) TYPES.add(Completion.get(kind.toString(), false));
    for(final BasicType type : BasicType.values()) {
      TYPES.add(Completion.get(type.toString(), false));
    }
    // add types with a mandatory argument: the cursor is placed inside the parentheses
    for(final String name : new String[] { ARRAY, ENUM, FUNCTION, MAP, RECORD }) {
      TYPES.add(new Completion(name, name + "()", name + "(_)", false));
    }
    // add functions (functions of the default namespace can also be called without prefix)
    for(final FuncDefinition fd : Functions.BUILT_IN.values()) {
      final String args = '(' + fd.paramString() + ')', value = fd.params.length > 0 ? "(_)" : "()";
      final boolean deflt = eq(fd.name.uri(), FN_URI);
      final String prefixed = string(fd.name.prefixId());
      final Signature signature = Signature.get(args);
      SIGNATURES.put(prefixed, signature);
      if(deflt) {
        final String local = string(fd.name.local());
        SIGNATURES.put(local, signature);
        add(local, args, value, abbrs, names, false);
      }
      add(prefixed, args, value, prefixedAbbrs, prefixedNames, deflt);
    }
    // add annotations (annotations of the XQuery namespace are specified without prefix)
    for(final Annotation ann : Annotation.values()) {
      final String params = ann.paramString, args = params.isEmpty() ? "" : '(' + params + ')';
      add(string(ann.name.prefixId(XQ_URI)), args, params.isEmpty() ? "" : "(_)",
        annAbbrs, annNames, false);
    }
    // add the declarations of a prolog, which continue the keywords that introduce them
    continuations(PROLOG, DECLARE + ' ');
    continuations(DEFAULTS, DECLARE + ' ' + DEFAULT + ' ');
    // add the documentation tags, which are followed by their description
    for(final byte[] tag : Inspect.DOC_TAGS) {
      final String name = string(tag);
      TAGS.add(new Completion(name, name, name + ' ', false));
    }
    TAGS.sort(Comparator.comparing(Completion::match));

    Collections.addAll(COMPLETIONS, TYPES, abbrs, names, prefixedAbbrs, prefixedNames);
    Collections.addAll(ANNOTATIONS, annAbbrs, annNames);
  }

  /** Indicates if the last resolved name is a keyword. */
  private boolean nameKeyword;

  @Override
  int initialMode() {
    return CODE;
  }

  @Override
  boolean code(final int mode) {
    // tags are code as well (see SyntaxMarkup)
    return mode == CODE || super.code(mode);
  }

  @Override
  boolean quoteEscape() {
    return true;
  }

  @Override
  boolean completable() {
    // names are completed in the tags of element constructors, tags in comments
    final int after = modeAfter();
    return modeBefore() == CODE && after == CODE || after == TAG || after == ETAG ||
      after == COMMENT;
  }

  @Override
  boolean completeStart(final int ch) {
    // variables, annotations and the lookups of maps and arrays
    return ch == '$' || ch == '%' || ch == '?' || super.completeStart(ch);
  }

  @Override
  Color color(final int mode) {
    return switch(mode) {
      case STRING_D, STRING_S, TEMPLATE, CONSTRUCTOR, EQNAME -> brown;
      case COMMENT, PRAGMA -> cyan;
      default -> super.color(mode);
    };
  }

  @Override
  Color mode(final byte[] text, final int pos, final int end, final int ch, final int mode) {
    return switch(mode) {
      case CODE -> code(text, pos, ch);
      case COMMENT -> {
        // comments nest: an inner comment pushes the outer one onto the mode stack
        if(ch == '(' && cp(text, pos + 1) == ':') enter(COMMENT, 1);
        else if(ch == ':' && cp(text, pos + 1) == ')') close(1);
        yield cyan;
      }
      case PRAGMA -> {
        if(ch == '#' && cp(text, pos + 1) == ')') close(1);
        yield cyan;
      }
      case EQNAME -> {
        if(reference(text, pos)) yield purple;
        if(ch == '}') close(0);
        yield brown;
      }
      case STRING_D, STRING_S -> {
        if(reference(text, pos)) yield purple;
        final int quote = mode == STRING_D ? '"' : '\'';
        if(ch == quote) {
          // doubled quotes are escaped
          if(cp(text, pos + 1) == quote) state[SKIP] = 1;
          else close(0);
        }
        yield brown;
      }
      case TEMPLATE -> {
        if(ch == '`') {
          if(cp(text, pos + 1) == '`') state[SKIP] = 1;
          else close(0);
        } else if(enclosed(text, pos, TEMPLATE)) {
          yield plain;
        }
        yield brown;
      }
      case CONSTRUCTOR -> {
        if(ch == ']' && startsWith(text, pos, "]``")) close(2);
        else if(ch == '`' && cp(text, pos + 1) == '{') enter(CODE, 1);
        yield brown;
      }
      default -> super.mode(text, pos, end, ch, mode);
    };
  }

  /**
   * Determines the color of a character in code.
   * @param text text
   * @param pos position
   * @param ch current character
   * @return color
   */
  private Color code(final byte[] text, final int pos, final int ch) {
    if(ch == '(') {
      final int next = cp(text, pos + 1);
      if(next == ':' || next == '#') {
        enter(next == ':' ? COMMENT : PRAGMA, 1);
        return cyan;
      }
      return plain;
    }
    if(ch == '"' || ch == '\'') {
      enter(ch == '"' ? STRING_D : STRING_S, 0);
      return brown;
    }
    if(ch == '`') {
      final boolean constr = startsWith(text, pos, "``[");
      enter(constr ? CONSTRUCTOR : TEMPLATE, constr ? 2 : 0);
      return brown;
    }
    // URI of an EQName: must not be parsed as code
    if(ch == 'Q' && cp(text, pos + 1) == '{') {
      enter(EQNAME, 1);
      return brown;
    }
    if(ch == '<') return open(text, pos);
    if(ch == '{') {
      enter(CODE, 0);
      return plain;
    }
    if(ch == '}') {
      close(0);
      return plain;
    }
    // a variable name may be separated from its '$' by whitespace ('$ x'); comments are ignored
    if(ch == '$' && XMLToken.isNCStartChar(cp(text, skipWs(text, pos + 1)))) return green;
    if(ch == '%' && XMLToken.isNCStartChar(cp(text, pos + 1))) return blue;

    if(name(text, pos)) {
      final int prev = nameStart > 0 ? text[nameStart - 1] : 0;
      if(cp(text, skipWsBack(text, nameStart)) == '$') return green;
      // a name glued to a preceding digit is the tail of a numeric literal: '10_000', '1e5', '0xF'
      if(digit(prev)) return purple;
      return prev == '%' || nameKeyword ? blue : plain;
    }
    // numeric literals (a dot is only a decimal point if it is not part of a name)
    return digit(ch) || ch == '.' && (digit(cp(text, pos + 1)) || digit(prev(text, pos))) ?
      purple : plain;
  }

  @Override
  boolean element(final byte[] text, final int pos) {
    return state[MODE] == CONTENT || !operand(text, pos);
  }

  @Override
  boolean enclosed(final byte[] text, final int pos, final int mode) {
    final int ch = cp(text, pos);
    if(ch != '{' && ch != '}') return false;
    // doubled curly braces are escaped
    if(cp(text, pos + 1) == ch) {
      state[SKIP] = 1;
      return false;
    }
    if(ch == '}') return false;
    enter(CODE, 0);
    return true;
  }

  @Override
  void classify(final byte[] text, final int start, final int end) {
    nameKeyword = keyword(text, start, end);
  }

  @Override
  boolean operandName(final byte[] text, final int pos) {
    // numbers, variables and user-defined names end an operand; keywords do not
    if(!name(text, pos)) return true;
    if(nameStart > 0 && text[nameStart - 1] == '$') return true;
    return !nameKeyword;
  }

  /**
   * Checks if the specified name is highlighted as a keyword.
   * @param text text
   * @param start start of the name
   * @param end end of the name
   * @return result of check
   */
  private static boolean keyword(final byte[] text, final int start, final int end) {
    if(end - start > MAXKEY) return false;
    String name = string(text, start, end - start);
    int first = start;

    // an EQName is resolved via its braced URI; a lexical prefix is ignored (XQuery 4.0, 'EQName')
    final int brace = braced(text, start);
    if(brace != -1) {
      final byte[] prefix = NSGlobal.prefix(substring(text, brace + 1, start - 1));
      if(prefix.length == 0) return false;
      final int colon = name.indexOf(':');
      name = string(prefix) + ':' + (colon == -1 ? name : name.substring(colon + 1));
      first = brace - 1;
    }

    // built-in functions are only highlighted if they are called: 'count(1)', 'count#1', 'a::b'
    final int next = skipWs(text, end);
    final int nc = cp(text, next);
    if(nc == '(' || nc == '#' || nc == ':' && cp(text, next + 1) == ':')
      return KEYWORDS.contains(name) || FUNCTIONS.contains(name);

    // reserved words are no keywords in name tests: '//name', '@id', 'child::text', '$map?key'
    final int prev = skipWsBack(text, first), pc = cp(text, prev);
    final boolean step = pc == '/' || pc == '@' || pc == '?' ||
      pc == ':' && cp(text, back(text, prev)) == ':';
    return !step && KEYWORDS.contains(name);
  }

  /**
   * Returns the opening brace of the braced URI that precedes an EQName.
   * @param text text
   * @param start start of the name
   * @return position, or {@code -1} if the name has no braced URI
   */
  private static int braced(final byte[] text, final int start) {
    if(start < 3 || text[start - 1] != '}') return -1;
    // a braced URI contains no braces
    for(int p = start - 2; p > 0; p--) {
      if(text[p] == '}') return -1;
      if(text[p] == '{') return text[p - 1] == 'Q' ? p : -1;
    }
    return -1;
  }

  @Override
  boolean hasDeclarations() {
    return true;
  }

  @Override
  ArrayList<Declaration> declarations(final byte[] text) {
    return scan(text, new Context(-1));
  }

  @Override
  ArrayList<ArrayList<Completion>> completions(final byte[] text, final int pos) {
    final Context context = new Context(pos);
    final ArrayList<Declaration> declarations = scan(text, context);
    // snippets adopt the namespace prefix of a library module
    final String prefix = context.prefix.isEmpty() ? "" : context.prefix + ":";

    final int before = prev(text, pos);
    // an at sign introduces a documentation tag; a comment has no other candidates
    if(context.mode == COMMENT) return before == '@' ? single(TAGS) : new ArrayList<>();
    // a percent sign introduces an annotation
    if(before == '%') return ANNOTATIONS;
    // in a start tag, the element name follows the angle bracket, all other names are attributes
    if(context.mode == TAG) {
      final boolean element = cp(text, pos) == '<';
      return single(candidates(element ? context.elements : context.attributes,
        element ? "<" : ""));
    }
    // an end tag is closed by the name of the innermost open element
    if(context.mode == ETAG) return context.element == null ? new ArrayList<>() :
      single(candidates(new TokenSet(context.element), ""));
    // a question mark introduces the lookup of a map or array
    if(before == '?') return single(candidates(context.lookups, ""));
    // a sequence type follows the 'as' and 'instance of' keywords
    if(Strings.eq(endName(text, pos), AS, OF)) return single(TYPES);
    // a declaration is continued by the keywords of its syntax
    if(context.scan == DECLARED) return single(resolve(PROLOG, prefix));
    if(context.scan == DEFAULTED) return single(resolve(DEFAULTS, prefix));
    // the name of a new declaration has no candidates
    if(context.scan == FUNCTION_NAME || context.scan == VARIABLE_NAME) return new ArrayList<>();

    final ArrayList<Completion> local = new ArrayList<>();
    for(final Declaration declaration : declarations) {
      // functions are completed with their parameters; variables are in scope in the whole module
      final String name = declaration.name();
      if(name.indexOf('$') != -1) {
        local.add(Completion.get(name, false));
      } else {
        // the cursor is placed inside the parentheses if the function has parameters
        final String args = declaration.args();
        local.add(new Completion(name.toLowerCase(Locale.ENGLISH), name + args,
          name + (args.length() > 2 ? "(_)" : "()"), false));
      }
    }
    local.addAll(candidates(context.variables, ""));

    final ArrayList<ArrayList<Completion>> lists = single(local);
    // a new declaration may be started
    if(context.scan == INITIAL) lists.add(resolve(DECLARATIONS, prefix));
    lists.add(resolve(SNIPPETS, prefix));
    lists.addAll(COMPLETIONS);
    return lists;
  }

  @Override
  Signature signature(final String name) {
    return SIGNATURES.get(name);
  }

  /**
   * Adds the snippets that continue a declaration, without the keywords that introduce it.
   * @param list list to be filled
   * @param keywords keywords of the declaration, followed by a space
   */
  private static void continuations(final ArrayList<Completion> list, final String keywords) {
    for(final Completion snippet : DECLARATIONS) {
      final String value = snippet.value();
      // the keywords themselves continue no declaration
      if(value.length() > keywords.length() && value.startsWith(keywords)) {
        // the candidate is matched by the keyword that follows
        final String string = value.substring(keywords.length());
        final int s = string.indexOf(' ');
        list.add(new Completion(s == -1 ? string : string.substring(0, s), string, string, false));
      }
    }
  }

  /**
   * Returns the name that ends before the specified position.
   * @param text text
   * @param pos position
   * @return name (empty string if the position is preceded by no name)
   */
  private static String endName(final byte[] text, final int pos) {
    final int p = skipWsBack(text, pos);
    if(p < 0) return "";
    final int end = p + cl(text, p), start = nameStart(text, end);
    // a name that is preceded by a dollar sign is a variable
    return start == end || prev(text, start) == '$' ? "" : string(text, start, end - start);
  }

  /**
   * Replaces the placeholders of the specified candidates with a namespace prefix.
   * @param candidates candidates
   * @param prefix namespace prefix
   * @return candidates
   */
  private static ArrayList<Completion> resolve(final ArrayList<Completion> candidates,
      final String prefix) {
    final ArrayList<Completion> list = new ArrayList<>(candidates.size());
    for(final Completion candidate : candidates) {
      final String label = candidate.label();
      list.add(label.contains(PLACEHOLDER) ? new Completion(candidate.match(),
        label.replace(PLACEHOLDER, prefix), candidate.value().replace(PLACEHOLDER, prefix), false) :
        candidate);
    }
    return list;
  }

  /**
   * Returns the argument string of a function declaration and resets the specified lists.
   * Optional parameters are enclosed in nested square brackets, as with built-in functions.
   * @param params parameter names
   * @param optional optionality of the parameters
   * @return argument string, enclosed in parentheses
   */
  private static String args(final StringList params, final BoolList optional) {
    final StringBuilder args = new StringBuilder().append('(');
    int brackets = 0;
    final int ps = params.size();
    for(int p = 0; p < ps; p++) {
      if(optional.get(p)) {
        args.append('[');
        brackets++;
      }
      if(p > 0) args.append(',');
      args.append(params.get(p));
    }
    while(brackets-- > 0) args.append(']');
    params.reset();
    optional.reset();
    return args.append(')').toString();
  }

  /**
   * Adds the completions for a built-in function or annotation.
   * @param name function or annotation name
   * @param args argument string to be appended to the label
   * @param value string to be appended to the inserted name
   * @param abbrs completions for the abbreviated name
   * @param names completions for the full name
   * @param alias prefixed name of a function that can also be called without prefix
   */
  private static void add(final String name, final String args, final String value,
      final ArrayList<Completion> abbrs, final ArrayList<Completion> names, final boolean alias) {
    final String label = name + args, insert = name + value;
    abbrs.add(new Completion(ABBR.matcher(name).replaceAll("$1").toLowerCase(Locale.ENGLISH),
      label, insert, alias));
    names.add(new Completion(name.toLowerCase(Locale.ENGLISH), label, insert, alias));
  }

  /**
   * Returns the function and variable declarations of the specified text.
   * @param text text
   * @param context context of the code completion
   * @return declarations
   */
  private ArrayList<Declaration> scan(final byte[] text, final Context context) {
    final int pos = context.pos;
    final ArrayList<Declaration> declarations = new ArrayList<>();
    reset();

    // start and line of the current name, nesting depth of the arguments of an annotation
    int begin = -1, beginLine = 1, line = 1, scan = INITIAL, depth = 0, module = MODULE_KEYWORD;
    boolean annotation = false;
    // names of the open elements, name of the last start tag
    final TokenList stack = new TokenList();
    byte[] element = null;
    // parameter names of the current function, their optionality, and flag for the next parameter
    final StringList params = new StringList();
    final BoolList optional = new BoolList();
    boolean parameter = false;

    final int tl = text.length;
    for(int p = 0; p < tl;) {
      final int cl = cl(text, p), ch = cp(text, p);
      color(text, p, p + cl);

      if(p == pos) {
        context.scan = scan;
        // the mode of a tag is entered by its angle bracket
        context.mode = ch == '<' ? state[MODE] : modeBefore();
        context.element = stack.isEmpty() ? null : stack.peek();
      }

      // a colon continues a name if it separates prefix and local name
      final boolean code = code(), nc = code && (XMLToken.isNCChar(ch) ||
        ch == ':' && begin != -1 && XMLToken.isNCStartChar(cp(text, p + cl)));
      if(nc) {
        if(begin == -1) {
          begin = p;
          beginLine = line;
        }
      } else {
        if(begin != -1) {
          final String word = string(text, begin, p - begin);
          if(begin > 0) {
            final int prev = text[begin - 1];
            // a dollar sign indicates the declaration or reference of a variable
            if(prev == '$') {
              if(begin < pos) context.variables.add(token('$' + word));
              // the first variable of a parameter is its name; the others occur in default values
              if(scan == FUNCTION_PARAMS && parameter) {
                params.add(word);
                optional.add(false);
                parameter = false;
              }
            }
            // a question mark indicates the lookup of a map or array
            if(prev == '?') context.lookups.add(token(word));
            // in a start tag, the element name follows the angle bracket
            if(tag()) {
              if(prev == '<') {
                element = token(word);
                context.elements.add(element);
              } else {
                context.attributes.add(token(word));
              }
            }
          }
          // the module declaration is the first one of a library module
          if(module != MODULE_DONE) {
            switch(module) {
              case MODULE_KEYWORD -> {
                // the words of a version declaration are skipped
                if(MODULE.equals(word)) {
                  module = MODULE_NAMESPACE;
                } else if(!Strings.eq(word, XQUERY, VERSION, ENCODING)) {
                  module = MODULE_DONE;
                }
              }
              case MODULE_NAMESPACE -> module = NAMESPACE.equals(word) ? MODULE_PREFIX :
                MODULE_DONE;
              default -> {
                context.prefix = word;
                module = MODULE_DONE;
              }
            }
          }
          switch(scan) {
            case OUTSIDE, INITIAL -> {
              if(DECLARE.equals(word)) {
                scan = DECLARED;
                depth = 0;
              } else {
                scan = OUTSIDE;
              }
            }
            case DECLARED -> {
              // the name and the arguments of an annotation are no prolog keywords
              if(annotation || depth > 0) {
                annotation = false;
              } else if(FUNCTION.equals(word)) {
                scan = FUNCTION_NAME;
              } else if(VARIABLE.equals(word)) {
                scan = VARIABLE_NAME;
              } else if(DEFAULT.equals(word)) {
                scan = DEFAULTED;
              } else if(!UPDATING.equals(word)) {
                scan = OUTSIDE;
              }
            }
            case DEFAULTED -> scan = OUTSIDE;
            case FUNCTION_PARAMS -> {
              // the words of a parameter list are names, types and keywords
            }
            default -> {
              if(scan == FUNCTION_NAME) {
                // the parameter names are collected until the list is closed
                declarations.add(new Declaration(word, "()", begin, beginLine));
                scan = FUNCTION_PARAMS;
                params.reset();
                optional.reset();
                depth = 0;
              } else {
                declarations.add(new Declaration('$' + word, "", begin, beginLine));
                scan = OUTSIDE;
              }
            }
          }
          begin = -1;
        }
        if(code && scan == DECLARED) {
          if(ch == '%') annotation = true;
          else if(ch == '(') depth++;
          else if(ch == ')' && depth > 0) depth--;
        } else if(code && scan == FUNCTION_PARAMS) {
          // types and default values may be parenthesized: only the outermost list is relevant
          if(ch == '(') {
            parameter = ++depth == 1;
          } else if(ch == ',') {
            parameter = depth == 1;
          } else if(ch == ':' && p + cl < tl && text[p + cl] == '=' && !params.isEmpty()) {
            // a default value makes the last parameter optional
            optional.set(params.size() - 1, true);
          } else if(ch == ')' && depth > 0 && --depth == 0) {
            // the declaration adopts the collected parameter names
            final Declaration decl = declarations.getLast();
            declarations.set(declarations.size() - 1, new Declaration(decl.name(),
              args(params, optional), decl.pos(), decl.line()));
            parameter = false;
            scan = OUTSIDE;
          }
        }
        if(code && ch == ';') {
          // a semicolon ends a declaration: a new one may follow, its variables are out of scope
          scan = INITIAL;
          if(p < pos) context.variables.clear();
        } else if(code && scan == INITIAL && !ws(ch)) {
          // an expression was started: no declaration can follow
          scan = OUTSIDE;
        }
      }
      // the name of a start tag is pushed when the element is opened, and popped by its end tag
      if(elementOpen(text, p)) stack.add(element);
      else if(modeBefore() == ETAG && modeAfter() != ETAG && !stack.isEmpty()) stack.pop();

      if(ch == '\n') line++;
      p += cl;
    }
    if(pos >= tl) {
      context.scan = scan;
      context.mode = state[MODE];
      context.element = stack.isEmpty() ? null : stack.peek();
    }
    return declarations;
  }

  @Override
  public byte[] commentOpen() {
    return XMLToken.XQCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.XQCOMM_C;
  }

  @Override
  Indent indent(final byte[] text, final int pos, final int last, final int mode,
      final int newlines, final Indent previous) {
    // the attributes of a tag are indented, as in XML
    if(tag()) return super.indent(text, pos, last, mode, newlines, previous);

    final boolean code = mode == CODE;
    final int type = clause(text, pos), ref = previous.reference();
    // the commas of a clause separate its own operands, not the operands of an enclosing list
    final boolean separates = type != CLAUSE;
    // a clause continuation opens a new baseline; a non-clause continuation is transparent and
    // preserves the clause context, so that a following clause stays aligned with the continued one
    if(continued(text, pos, last, code, newlines)) return type != NONE ?
      new Indent(1, 1, type, separates) : new Indent(1, ref, previous.type(), separates);
    // consecutive clauses are indented alike
    if(type != NONE && previous.type() == CLAUSE) return new Indent(ref, ref, type, separates);
    // further operands of a clause are indented; the clause remains the reference
    if(code && prev(text, last) == ',' && previous.type() == CLAUSE)
      return new Indent(ref + 1, ref, CLAUSE, false);
    return new Indent(0, 0, type, separates);
  }

  /**
   * Checks if a line continues the expression of the previous one.
   * @param text text
   * @param pos start of the line
   * @param last position after the last character of the previous line
   * @param code indicates if the previous line ends with code
   * @param newlines number of line breaks between the two lines
   * @return result of check
   */
  private boolean continued(final byte[] text, final int pos, final int last, final boolean code,
      final int newlines) {
    // annotations continue a declaration
    if(cp(text, pos) == '%') return true;
    if(newlines != 1 || !code) return false;
    // an operator keyword at the start of the line continues the preceding expression
    if(INFIX.contains(startName(text, pos))) return true;
    final int p = skipWsBack(text, last), ch = cp(text, p);
    if(OPERATORS.indexOf(ch) != -1) return true;
    return XMLToken.isNCChar(ch) && name(text, p) &&
      DANGLING.contains(string(text, nameStart, nameEnd - nameStart));
  }

  /**
   * Returns the type of the FLWOR clause that starts at the specified position.
   * @param text text
   * @param pos start of the line
   * @return {@link #NONE}, {@link #CLAUSE} or {@link #FINAL}
   */
  private int clause(final byte[] text, final int pos) {
    final String name = startName(text, pos);
    return RETURN.equals(name) ? FINAL : CLAUSES.contains(name) ? CLAUSE : NONE;
  }

  /**
   * Returns the name that starts at the specified position.
   * @param text text
   * @param pos position
   * @return name, or {@code null} if no name starts at the position
   */
  private String startName(final byte[] text, final int pos) {
    return name(text, pos) && nameStart == pos ? string(text, nameStart, nameEnd - nameStart) :
      null;
  }

  @Override
  String separators() {
    // colons are no separators: they occur in QNames, axes, map entries and ':='
    return ",";
  }

  @Override
  String lists() {
    // curly braces enclose no lists: their commas may separate let clauses or map entries
    return "([";
  }

  @Override
  boolean boundarySpace(final byte[] text) {
    // all occurrences are checked: the first one may be part of a comment or a string
    for(int p = indexOf(text, BOUNDARY); p != -1; p = indexOf(text, BOUNDARY, p + 1)) {
      if(startsWith(text, skipWs(text, p + BOUNDARY.length), "preserve")) return false;
    }
    return true;
  }

  /** Names and states that are collected by a text scan for a code completion. */
  private static final class Context {
    /** Start of the completed string ({@code -1} if no completion is requested). */
    private final int pos;
    /** Names of the variables in scope. */
    private final TokenSet variables = new TokenSet();
    /** Names of all lookups. */
    private final TokenSet lookups = new TokenSet();
    /** Names of all element constructors. */
    private final TokenSet elements = new TokenSet();
    /** Names of all attribute constructors. */
    private final TokenSet attributes = new TokenSet();
    /** Namespace prefix of a library module. */
    private String prefix = "";
    /** Declaration scan state at the completion position. */
    private int scan = OUTSIDE;
    /** Highlighting mode at the completion position. */
    private int mode = CODE;
    /** Name of the innermost element that is open at the completion position (can be
     * {@code null}). */
    private byte[] element;

    /**
     * Constructor.
     * @param pos start of the completed string
     */
    private Context(final int pos) {
      this.pos = pos;
    }
  }
}
