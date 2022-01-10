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
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * XSLT report builder, with focus on the XSLT Saxon processor.
 *
 * @author BaseX Team 2005-22, BSD License
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
  private final Stack<Object> messages = new Stack<>();
  /** Query context. */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param qc query context
   */
  XsltReport(final QueryContext qc) {
    this.qc = qc;
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
    rb.put("result", convert(new IOContent(result), true));
  }

  /**
   * Adds report messages.
   * @throws QueryException query exception
   */
  void addMessage() throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    try {
      for(final Object message : messages) {
        ArrayBuilder ab = new ArrayBuilder();
        for(final Item item : convert(new IOContent(MW_GW.invoke(message).toString()), false)) {
          ab.append(item);
        }
        vb.add(ab.array());
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
   * @param content result
   * @param result result or message flag
   * @return item or empty sequence
   */
  private Value convert(final IOContent content, final boolean result) {
    ANode node = null;
    try {
      node = new DBNode(content);
    } catch(final IOException ex) {
      Util.debug(ex);
      try {
        node = new DBNode(new XMLParser(content, MainOptions.get(), true));
      } catch(final IOException ex2) {
        Util.debug(ex2);
        return Str.get(content.read());
      }
    }
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final ANode child : node.childIter()) {
      vb.add(child.type == NodeType.TEXT ? Atm.get(child.string()) :
        result ? new FDoc().add(child.finish()) : child.finish());
    }
    return vb.value();
  }
}
