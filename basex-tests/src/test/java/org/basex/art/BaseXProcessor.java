package org.basex.art;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * The reference {@link XQueryProcessor}: runs a module in-process on BaseX.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class BaseXProcessor implements XQueryProcessor {
  @Override public String id() {
    return "basex";
  }

  @Override public String name() {
    return "BaseX";
  }

  @Override public boolean reference() {
    return true;
  }

  @Override public double[] run(final Path module, final String moduleText, final Path modDir,
      final Path result, final Map<String, String> bindings, final OutputStream err)
      throws Exception {
    final PrintStream origErr = System.err;
    final Context ctx = new Context();
    double compile = 0, eval = 0;
    try {
      ctx.options.set(MainOptions.DTD, true);
      ctx.options.set(MainOptions.FNXMLTRUSTED, true);
      // fn:trace / fn:message go to standard error; capture that
      System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
      try(QueryProcessor qp = new QueryProcessor(moduleText, module.toUri().toString(), ctx,
          null)) {
        for(final Map.Entry<String, String> e : bindings.entrySet())
          qp.variable(e.getKey(), e.getValue());
        final long c0 = System.nanoTime();
        qp.optimize();
        compile = (System.nanoTime() - c0) / 1e9;
        final long e0 = System.nanoTime();
        try(OutputStream os = Files.newOutputStream(result); Serializer ser = qp.serializer(os)) {
          for(final Item item : qp.value()) ser.serialize(item);
        }
        eval = (System.nanoTime() - e0) / 1e9;
      }
    } finally {
      System.setErr(origErr);
      ctx.close();
    }
    return new double[] { compile, eval };
  }
}
