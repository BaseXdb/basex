package org.basex.query.util.inspect;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains simple functions for inspecting XQuery modules.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Plain extends Inspect {
  /**
   * Constructor.
   * @param qc query context
   * @param ii input info
   */
  public Plain(final QueryContext qc, final InputInfo ii) {
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
    final FElem module = elem("module", null);
    if(mod != null) {
      module.add("prefix", mod.string());
      module.add("uri", mod.uri());
    }
    if(main != null) {
      final TokenMap doc = main.doc();
      if(doc != null) {
        for(final byte[] key : doc) {
          final FElem elem = eq(key, DOC_TAGS) ? elem(string(key), module) :
            elem("tag", module).add("name", key);
          add(elem, doc.get(key), ctx);
        }
      }
    }

    for(final StaticVar sv : qp.vars) {
      variable(sv, module);
    }
    for(final StaticFunc sf : qp.funcs) {
      final SeqType[] types = new SeqType[sf.args.length];
      for(int t = 0; t < types.length; t++) types[t] = sf.args[t].declType;
      function(sf.name, types, sf.declType, sf, module);
    }

    return module;
  }

  /**
   * Creates a description for the specified variable.
   * @param sv static variable
   * @param parent node
   * @return resulting value
   * @throws QueryException query exception
   */
  public FElem variable(final StaticVar sv, final FElem parent) throws QueryException {
    final FElem variable = elem("variable", parent);
    variable.add("name", sv.name.string());
    if(sv.name.uri().length != 0) variable.add("uri", sv.name.uri());
    type(sv.declType, variable);
    comment(sv, variable);
    annotations(sv.ann, variable);
    return variable;
  }

  /**
   * Creates a description for the specified function.
   * @param fname name of function
   * @param types types of arguments
   * @param ret return type
   * @param sf static function
   * @param parent node
   * @return resulting value
   */
  public FElem function(final QNm fname, final SeqType[] types, final SeqType ret,
      final StaticFunc sf, final FElem parent) {

    final FElem function = elem("function", parent);
    if(fname != null) {
      function.add("name", fname.string());
      if(fname.uri().length != 0) function.add("uri", fname.uri());
    }

    final TokenMap doc = sf != null ? sf.doc() : null;
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
            final byte[] val = doc.get(key);
            final int vl = val.length;
            for(int v = 0; v < vl; v++) {
              if(!ws(val[v])) continue;
              if(eq(replaceAll(substring(val, 0, v), "^\\$", ""), name)) {
                add(parameter, trim(substring(val, v + 1, vl)), ctx);
              }
              break;
            }
          }
        }
      }
      type(types[a], parameter);
    }

    if(sf != null) {
      for(int a = 0; a < sf.ann.size(); a++) {
        final FElem annotation = elem("annotation", function);
        annotation.add("name", sf.ann.names[a].string());
        annotation.add("uri", sf.ann.names[a].uri());
      }
    }

    if(doc != null) {
      for(final byte[] key : doc) {
        if(eq(key, DOC_PARAM, DOC_RETURN)) continue;
        final FElem elem = eq(key, DOC_TAGS) ? elem(string(key), function) :
          elem("tag", function).add("name", key);
        add(elem, doc.get(key), ctx);
      }
    }

    final FElem returnn = type(ret, elem("return", function));
    if(doc != null) {
      for(final byte[] key : doc) {
        if(!eq(key, DOC_RETURN)) continue;
        returnn.add(doc.get(key));
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
   * @param elem element
   * @param val string to parse
   * @param ctx query context
   */
  private static void add(final FElem elem, final byte[] val, final QueryContext ctx) {
    try {
      final ANode node = FNGen.parseXml(new IOContent(val), ctx, true);
      for(final ANode n : node.children()) elem.add(n.copy());
    } catch(final IOException ex) {
      elem.add(val);
    }
  }

  /**
   * Creates a comment element.
   * @param scope scope
   * @param parent parent element
   */
  private void comment(final StaticScope scope, final FElem parent) {
    final TokenMap map = scope.doc();
    if(map == null) return;
    for(final byte[] entry : map) comment(parent, entry, map.get(entry));
  }

  /**
   * Creates a comment sub element.
   * @param parent parent element
   * @param key key
   * @param val value
   */
  private void comment(final FElem parent, final byte[] key, final byte[] val) {
    try {
      final FElem elem = eq(key, QueryText.DOC_TAGS) ? elem(string(key), parent) :
        elem("custom", parent).add("tag", key);
      final IOContent io = new IOContent(trim(val));
      final ANode node = FNGen.parseXml(io, ctx, true);
      for(final ANode n : node.children()) elem.add(n.copy());
    } catch(final IOException ex) {
      // fallback: add string representation
      elem(string(key), parent).add(trim(val));
    }
  }

  /**
   * Creates annotation elements.
   * @param ann annotations
   * @param parent parent node
   * @throws QueryException query exception
   */
  private void annotations(final Ann ann, final FElem parent) throws QueryException {
    final int as = ann.size();
    for(int a = 0; a < as; a++) {
      final FElem annotation = elem("annotation", parent);
      annotation.add("name", ann.names[a].string());
      annotation.add("uri", ann.names[a].uri());
      for(final Item it : ann.values[a]) {
        final FElem literal = elem("literal", annotation);
        literal.add("type", it.type.toString()).add(it.string(null));
      }
    }
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
