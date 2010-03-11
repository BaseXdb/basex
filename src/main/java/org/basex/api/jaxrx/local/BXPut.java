package org.basex.api.jaxrx.local;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.Parser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.jaxrx.interfaces.IPut;
import org.xml.sax.InputSource;

/**
 * This class offers an implementation of the JAX-RX 'put' operation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXPut implements IPut {
  @Override
  public boolean createResource(final String resource, final InputStream in) {
    final Context ctx = new Context();
    final SAXSource source = new SAXSource(new InputSource(in));
    final Parser parser = new SAXWrapper(source, ctx.prop);
    try {
      CreateDB.xml(ctx, parser, resource);
    } catch(final IOException ex) {
      BXUtil.error(ex);
    } finally {
      ctx.close();
    }
    return true;
  }
}
