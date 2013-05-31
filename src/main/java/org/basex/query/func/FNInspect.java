package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.xqdoc.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Inspect functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class FNInspect extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNInspect(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _INSPECT_FUNCTION: return function(ctx);
      case _INSPECT_XQDOC:    return xqdoc(ctx);
      default:                return super.item(ctx, ii);
    }
  }

  /**
   * Performs the function function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item function(final QueryContext ctx) throws QueryException {
    final FItem f = checkFunc(expr[0], ctx);
    final FuncType ftype = f.funcType();
    final StaticFunc sf = f instanceof FuncItem ? ((FuncItem) f).func : null;
    return function(f.fName(), ftype.args, ftype.ret, sf, ctx);
  }

  /**
   * Creates a description for the specified function.
   * @param fname name of function
   * @param types types of arguments
   * @param ret return type
   * @param sf static function
   * @param ctx query context
   * @return resulting value
   */
  private FElem function(final QNm fname, final SeqType[] types,
      final SeqType ret, final StaticFunc sf, final QueryContext ctx) {

    final FElem function = new FElem("function");
    function.add("name", fname.string());
    function.add("uri", fname.uri());

    final TokenMap doc = sf != null ? sf.doc() : null;
    QNm[] names = null;
    if(sf != null) {
      names = new QNm[sf.args.length];
      for(int n = 0; n < names.length; n++) names[n] = sf.args[n].name;
    }

    for(int a = 0; a < types.length; a++) {
      final FElem parameter = type("parameter", types[a]);
      function.add(parameter);
      if(names == null) continue;
      final byte[] name = names[a].string();
      final byte[] uri = names[a].uri();
      parameter.add("name", name);
      if(uri.length != 0) parameter.add("uri", uri);

      if(doc != null) {
        for(final byte[] key : doc) {
          if(!eq(key, DOC_PARAM)) continue;
          byte[] val = doc.get(key);
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

    for(int a = 0; a < sf.ann.size(); a++) {
      final FElem annotation = new FElem("annotation");
      annotation.add("name", sf.ann.names[a].string());
      annotation.add("uri", sf.ann.names[a].uri());
      function.add(annotation);
    }

    if(doc != null) {
      for(final byte[] key : doc) {
        if(eq(key, DOC_PARAM, DOC_RETURN)) continue;
        final FElem elem = eq(key, DOC_TAGS) ? new FElem(key) :
          new FElem("tag").add("name", key);
        add(elem, doc.get(key), ctx);
        function.add(elem);
      }
    }
    final FElem returnn = type("return", ret);
    function.add(returnn);
    for(final byte[] key : doc) {
      if(!eq(key, DOC_RETURN)) continue;
      returnn.add(doc.get(key));
      break;
    }
    return function;
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
   * Returns an element with type information.
   * @param name element name
   * @param st sequence type
   * @return element
   */
  private FElem type(final String name, final SeqType st) {
    final FElem elem = new FElem(name);
    elem.add("type", st.type.toString());
    final String occ = st.occ.toString();
    if(!occ.isEmpty()) elem.add("occurrence", occ);
    return elem;
  }

  /**
   * Performs the xqdoc function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item xqdoc(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final String path = string(checkStr(expr[0], ctx));
    final IO io = IO.get(path);
    if(!io.exists()) WHICHRES.thrw(info, path);
    return new XQDoc(ctx, info).parse(io);
  }
}
