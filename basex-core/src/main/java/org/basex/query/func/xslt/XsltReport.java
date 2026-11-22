package org.basex.query.func.xslt;

import java.io.*;
import java.lang.reflect.*;
import java.util.function.*;

import javax.xml.transform.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * XSLT report builder, with focus on the XSLT Saxon processor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class XsltReport {
  /** Saxon TransformerImpl class. */
  private static final Class<?> TI = Reflect.find("net.sf.saxon.jaxp.TransformerImpl");
  /** Saxon XsltController class. */
  private static final Class<?> XC = Reflect.find("net.sf.saxon.trans.XsltController");
  /** Saxon Message class. */
  private static final Class<?> MSG = Reflect.find("net.sf.saxon.s9api.Message");

  /** TransformerImpl.getUnderlyingController method. */
  private static final Method TI_GUC = Reflect.method(TI, "getUnderlyingController");
  /** XsltController.setMessageHandler method. */
  private static final Method XC_SMH = Reflect.method(XC, "setMessageHandler", Consumer.class);
  /** Message.getContent method. */
  private static final Method MSG_GC = Reflect.method(MSG, "getContent");

  /** Transformation result. */
  private Value result = Empty.VALUE;
  /** Messages. */
  private Value messages = Empty.VALUE;
  /** Transformation error. */
  private Value error = Empty.VALUE;
  /** Saxon-specific: serialized messages. */
  private final StringList strings = new StringList();
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
   * Registers a message handler to collect messages.
   * @param tr transformer
   */
  void register(final Transformer tr) {
    if(tr.getClass() == TI && TI_GUC != null && XC_SMH != null && MSG_GC != null) {
      final Consumer<Object> handler = msg -> strings.add(Reflect.invoke(MSG_GC, msg).toString());
      Reflect.invoke(XC_SMH, Reflect.invoke(TI_GUC, tr), handler);
    }
  }

  /**
   * Adds a result.
   * @param output transformation result
   */
  void addResult(final byte[] output) {
    result = convert(new IOContent(output), true);
  }

  /**
   * Adds report messages.
   */
  void addMessage() {
    final ValueBuilder vb = new ValueBuilder(qc, strings.size());
    for(final String message : strings) {
      final Value value = convert(new IOContent(message), false);
      final ArrayBuilder ab = new ArrayBuilder(qc, value.size());
      for(final Item item : value) ab.add(item);
      vb.add(ab.array());
    }
    messages = vb.value();
  }

  /**
   * Adds a transformation error.
   * @param message error message
   */
  void addError(final Str message) {
    error = message;
  }

  /**
   * Returns the finalized report.
   * @return report
   */
  XQMap finish() {
    return XQMap.get(Records.XSLT_TRANSFORM_REPORT.get(), result, messages, error);
  }

  /**
   * Converts a transformation result to an item (node or untyped atomic).
   * @param content result
   * @param res result or message flag
   * @return item or empty sequence
   */
  private Value convert(final IOContent content, final boolean res) {
    XNode node;
    try {
      node = new DBNode(content);
    } catch(final IOException ex) {
      Util.debug(ex);
      try {
        node = new DBNode(new XMLParser(content, new MainOptions(), true));
      } catch(final IOException ex2) {
        Util.debug(ex2);
        return Str.get(content.read());
      }
    }
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final GNode child : node.childIter()) {
      vb.add(child.kind() == Kind.TEXT ? Atm.get(child.string()) :
        res ? FDoc.build().node(child).finish() : child);
    }
    return vb.value();
  }
}
