package org.basex.query.util.inspect;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains functions for generating a plain XQuery documentation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class PlainDoc extends Inspect {
  /**
   * Constructor.
   * @param qc query context
   * @param ii input info
   */
  public PlainDoc(final QueryContext qc, final InputInfo ii) {
    super(qc, ii);
  }

  /**
   * Parses a module and returns an xqdoc element.
   * @param io input reference
   * @return xqdoc element
   * @throws QueryException query exception
   */
  public FElem parse(final IO io) throws QueryException {
    final QueryParser qp = parseQuery(io);
    final FElem mod = elem("module", null);
    if(module instanceof LibraryModule) {
      final QNm name = ((LibraryModule) module).name;
      mod.add("prefix", name.string());
      mod.add("uri", name.uri());
    }

    final TokenObjMap<TokenList> doc = module.doc();
    if(doc != null) {
      for(final byte[] key : doc) {
        for(final byte[] value : doc.get(key)) {
          final FElem elem = eq(key, DOC_TAGS) ? elem(string(key), mod) :
            elem("tag", mod).add("name", key);
          add(value, ctx, elem);
        }
      }
    }

    for(final StaticVar sv : qp.vars) {
      variable(sv, mod);
    }
    for(final StaticFunc sf : qp.funcs) {
      final SeqType[] types = new SeqType[sf.args.length];
      for(int t = 0; t < types.length; t++) types[t] = sf.args[t].declType;
      function(sf.name, types, sf.declType, sf, mod);
    }

    return mod;
  }

  /**
   * Creates a description for the specified variable.
   * @param sv static variable
   * @param parent node
   * @return resulting value
   * @throws QueryException query exception
   */
  private FElem variable(final StaticVar sv, final FElem parent) throws QueryException {
    final FElem variable = elem("variable", parent);
    variable.add("name", sv.name.string());
    if(sv.name.uri().length != 0) variable.add("uri", sv.name.uri());
    type(sv.declType, variable);
    comment(sv, variable);
    annotation(sv.ann, variable);
    return variable;
  }

  /**
   * Creates a description for the specified function.
   * @param fname name of function
   * @param types types of arguments
   * @param type return type
   * @param sf static function
   * @param parent node
   * @return resulting value
   * @throws QueryException query exception
   */
  public FElem function(final QNm fname, final SeqType[] types, final SeqType type,
      final StaticFunc sf, final FElem parent) throws QueryException {

    final FElem function = elem("function", parent);
    if(fname != null) {
      function.add("name", fname.string());
      if(fname.uri().length != 0) function.add("uri", fname.uri());
    }

    final TokenObjMap<TokenList> doc = sf != null ? sf.doc() : null;
    QNm[] names = null;
    if(sf != null) {
      names = new QNm[sf.args.length];
      for(int n = 0; n < names.length; n++) names[n] = sf.args[n].name;
    }

    for(int a = 0; a < types.length; a++) {
      final FElem parameter = elem("parameter", function);
      if(names != null) {
        final byte[] name = names[a].string();
        final byte[] uri = names[a].uri();
        parameter.add("name", name);
        if(uri.length != 0) parameter.add("uri", uri);

        if(doc != null) {
          for(final byte[] key : doc) {
            if(!eq(key, DOC_PARAM)) continue;
            for(final byte[] val : doc.get(key)) {
              final int vl = val.length;
              for(int v = 0; v < vl; v++) {
                if(!ws(val[v])) continue;
                if(eq(replaceAll(substring(val, 0, v), "^\\$", ""), name)) {
                  add(trim(substring(val, v + 1, vl)), ctx, parameter);
                }
                break;
              }
            }
          }
        }
      }
      type(types[a], parameter);
    }

    if(sf != null) annotation(sf.ann, function);

    if(doc != null) {
      for(final byte[] key : doc) {
        if(eq(key, DOC_PARAM, DOC_RETURN)) continue;
        for(final byte[] value : doc.get(key)) {
          final FElem elem = eq(key, DOC_TAGS) ? elem(string(key), function) :
            elem("tag", function).add("name", key);
          add(value, ctx, elem);
        }
      }
    }

    final FElem ret = type(type, elem("return", function));
    if(doc != null) {
      for(final byte[] key : doc) {
        if(!eq(key, DOC_RETURN)) continue;
        for(final byte[] value : doc.get(key)) ret.add(value);
        break;
      }
    }
    return function;
  }

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent node
   * @return element node
   */
  @Override
  protected FElem elem(final String name, final FElem parent) {
    final FElem elem = new FElem(name);
    if(parent != null) parent.add(elem);
    return elem;
  }

  /**
   * Parses a string as XML and adds the resulting nodes to the specified parent.
   * @param value string to parse
   * @param ctx query context
   * @param elem element
   */
  private static void add(final byte[] value, final QueryContext ctx, final FElem elem) {
    try {
      final ANode node = FNGen.parseXml(new IOContent(value), ctx, true);
      for(final ANode n : node.children()) elem.add(n.copy());
    } catch(final IOException ex) {
      elem.add(value);
    }
  }

  /**
   * Creates a comment element.
   * @param scope scope
   * @param parent parent element
   */
  private void comment(final StaticScope scope, final FElem parent) {
    final TokenObjMap<TokenList> tags = scope.doc();
    if(tags != null) comment(tags, parent);
  }

  @Override
  protected FElem tag(final byte[] tag, final FElem parent) {
    final String t = string(tag);
    return elem(eq(tag, DOC_TAGS) ? t : t + "_tag", parent);
  }

  /**
   * Returns an element with type information.
   * @param st sequence type
   * @param elem element
   * @return element
   */
  private FElem type(final SeqType st, final FElem elem) {
    if(st != null) {
      elem.add("type", st.type.toString());
      final String occ = st.occ.toString();
      if(!occ.isEmpty()) elem.add("occurrence", occ);
    }
    return elem;
  }
}
