package org.basex.api.jaxrx;

import static org.jaxrx.core.URLConstants.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Run;
import org.basex.core.cmd.XQuery;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.TokenList;
import org.jaxrx.JaxRx;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;

/**
 * This class offers an implementation of the JAX-RX interface.
 * It contains all methods which are necessary to answer GET, POST, PUT
 * and DELETE requests via REST.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 * @author Lukas Lewandowski
 */
public final class BXJaxRx implements JaxRx {
  @Override
  public Set<QueryParameter> getParameters() {
    final Set<QueryParameter> p = new HashSet<QueryParameter>();
    p.add(QueryParameter.COMMAND);
    p.add(QueryParameter.QUERY);
    p.add(QueryParameter.RUN);
    p.add(QueryParameter.COUNT);
    p.add(QueryParameter.OUTPUT);
    p.add(QueryParameter.START);
    p.add(QueryParameter.WRAP);
    return p;
  }

  @Override
  public StreamingOutput get(final ResourcePath rp) {
    if(rp.getDepth() != 0) return query(".", rp);

    return new BXOutput(null) {
      @Override
      void code() throws IOException {
        // retrieve list of databases
        final CachedOutput co = new CachedOutput();
        exec(new List(), co);
        final Table table = new Table(co.toString());

        final XMLSerializer xml = new XMLSerializer(out,
            new SerializerProp(params(rp)));

        for(final TokenList l : table.contents) {
          xml.emptyElement(Token.token(JAXRX + ":" + "resource"),
              Token.token("name"), l.get(0),
              Token.token("documents"), l.get(1),
              Token.token("size"), l.get(2));
        }
        xml.close();
      }
    };
  }

  @Override
  public StreamingOutput query(final String query, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      void code() throws IOException {
        // wrap start and counter around query expression
        final int s = num(rp, QueryParameter.START, 1);
        final int m = num(rp, QueryParameter.COUNT, Integer.MAX_VALUE - s);
        String xq = query != null ? query : ".";

        if(s != 1 || m != Integer.MAX_VALUE - s)
          xq = "(" + query + ")[position() = " + s + " to " + (s + m - 1) + "]";
        exec(new XQuery(xq), out);
      }
    };
  }

  /**
   * Converts the specified query parameter to a positive integer.
   * Throws an exception if the string is smaller than 1 or cannot be converted.
   * @param rp resource path
   * @param qp query parameter
   * @param def default value
   * @return integer
   */
  int num(final ResourcePath rp, final QueryParameter qp, final int def) {
    final String val = rp.getValue(qp);
    if(val == null) return def;

    try {
      final int i = Integer.parseInt(val);
      if(i > 0) return i;
    } catch(final NumberFormatException ex) {
    }
    throw new JaxRxException(400, "Parameter '" + qp +
        "' is no valid integer: " + val);
  }

  @Override
  public StreamingOutput run(final String file, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      void code() throws IOException {
        // get root directory for files
        final String root = System.getProperty("org.basex.jaxrxpath") + "/";
        final IO io = IO.get(root + file);
        exec(new Run(io.path()), out);
      }
    };
  }

  @Override
  public StreamingOutput command(final String cmd, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      void code() throws IOException {
        // perform command and serialize output
        final CachedOutput co = new CachedOutput();
        exec(cmd, co);

        final XMLSerializer xml =
          new XMLSerializer(out, new SerializerProp(params(path)));
        xml.text(Token.delete(co.finish(), '\r'));
        xml.close();
      }
    };
  }

  @Override
  public void add(final InputStream input, final ResourcePath rp) {
    new BXCode() {
      @Override
      void code() throws IOException {
        // open database
        if(!cs.execute(new Open(root(rp))))
          throw new JaxRxException(404, cs.info());

        // add cached file to the database
        final File file = cache(input);
        final boolean ok = cs.execute(new Add(file.toString()));
        file.delete();

        // return exception if process failed
        if(!ok) throw new JaxRxException(400, cs.info());
      }
    }.run();
  }

  @Override
  public void update(final InputStream input, final ResourcePath rp) {
    new BXCode() {
      @Override
      void code() throws IOException {
        // create database from cached file
        final File file = cache(input);
        final boolean ok = cs.execute(new CreateDB(root(rp), file.toString()));
        file.delete();
        // return exception if process failed
        if(!ok) throw new JaxRxException(400, cs.info());
      }
    }.run();
  }

  @Override
  public void delete(final ResourcePath rp) {
    new BXCode() {
      @Override
      void code() throws IOException {
        if(!cs.execute(new DropDB(root(rp))))
          throw new JaxRxException(404, cs.info());
      }
    }.run();
  }
}
