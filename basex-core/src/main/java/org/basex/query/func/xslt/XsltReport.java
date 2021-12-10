package org.basex.query.func.xslt;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import javax.xml.transform.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * XSLT report builder, with focus on the XSLT Saxon processor.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class XsltReport {
  /** Saxon TransformerImpl class. */
  private static final Class<?> TI = Reflect.find("net.sf.saxon.jaxp.TransformerImpl");
  /** Saxon XsltController class. */
  private static final Class<?> XC = Reflect.find("net.sf.saxon.trans.XsltController");
  /** Saxon MessageWarner class. */
  private static final Class<?> MW = Reflect.find("net.sf.saxon.serialize.MessageWarner");

  /** TransformerImpl.getUnderlyingController method. */
  private static final Method TI_GUC = Reflect.method(TI, "getUnderlyingController");
  /** MessageWarner.getWriter method. */
  private static final Method MW_GW = Reflect.method(MW, "getWriter");
  /** XsltController.setMessageFactory method. */
  private static final Method MW_SMW = Reflect.method(XC, "setMessageFactory", Supplier.class);

  /** Report map builder. */
  private final MapBuilder rb = new MapBuilder();
  /** Saxon-specific: Message collector. */
  final Stack<Object> messages = new Stack<>();

  /**
   * Constructor.
   */
  XsltReport() {

  }

  /**
   * Registers a message factory to collect messages.
   * @param tr transformer
   */
  void register(final Transformer tr) {
    if(tr.getClass() == TI) {
      try {
        final Supplier<Object> supplier = () -> {
          final Object mw = Reflect.get(MW);
          messages.add(mw);
          return mw;
        };
        MW_SMW.invoke(TI_GUC.invoke(tr), supplier);
      } catch(final Exception ex) {
        Util.stack(ex);
      }
    }
  }

  /**
   * Adds a result.
   * @param result transformation result
   * @throws QueryException query exception
   */
  void addResult(final byte[] result) throws QueryException {
    rb.put("result", convert(new IOContent(result)));
  }

  /**
   * Adds report messages.
   * @param qc query context
   * @throws QueryException query exception
   */
  void addMessage(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    try {
      for(final Object message : messages) {
        vb.add(convert(new IOContent(MW_GW.invoke(message).toString())));
      }
    } catch(final Exception ex) {
      Util.debug(ex);
    }
    rb.put("messages", vb.value());
  }

  /**
   * Adds a transformation error.
   * @param error error message
   * @throws QueryException query exception
   */
  void addError(final Str error) throws QueryException {
    rb.put("error", error);
  }

  /**
   * Returns the finalized report.
   * @return report
   */
  XQMap finish() {
    return rb.finish();
  }

  /**
   * Converts a transformation result to an item (node or untyped atomic).
   * @param io result container
   * @return item or empty sequence
   */
  private Item convert(final IOContent io) {
    ANode node = null;
    try {
      node = new DBNode(io);
    } catch(final IOException ex) {
      Util.debug(ex);
      try {
        node = new DBNode(new XMLParser(io, MainOptions.get(), true));
      } catch(final IOException ex2) {
        Util.debug(ex2);
        return Str.get(io.read());
      }
    }
    node = node.childIter().next();
    return node == null ? Empty.VALUE : node.type == NodeType.TEXT ? Atm.get(node.string()) :
      node.finish();
  }
}
