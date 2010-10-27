package org.basex.api.jaxrx;

import static org.jaxrx.core.URLConstants.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Run;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.io.IO;
import org.basex.server.ClientQuery;
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
    p.add(QueryParameter.VAR);
    return p;
  }

  @Override
  public StreamingOutput get(final ResourcePath rp) {
    if(rp.getDepth() != 0) return query(".", rp);

    return new BXOutput(null) {
      @Override
      void code() throws IOException {
        // retrieve list of databases
        final ArrayOutput ao = new ArrayOutput();
        exec(new List(), ao);
        final Table table = new Table(ao.toString());

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
      void code() {
        // wrap start and counter around query expression
        final String xq = query != null ? query : ".";
        // evaluate first result and number of results
        final int s = num(rp, QueryParameter.START, 1);
        final int m = num(rp, QueryParameter.COUNT, Integer.MAX_VALUE - s);

        // [CG] REST: ensure that results will be correctly wrapped
        try {
          // create query instance
          final ClientQuery cq = cs.query(xq);
          final String var = path.getValue(QueryParameter.VAR);
          if(var != null) {
            final Scanner sc = new Scanner(var);
            sc.useDelimiter("\t");
            while(sc.hasNext()) {
              final String v = sc.next();
              final String[] sp = v.split(":", 2);
              cq.bind(sp[0], sp.length == 1 ? "" : sp[1]);
            }
          }
          // loop through all results
          int c = 0;
          while(++c < s + m && cq.more()) if(c >= s) cq.next(out);
          cq.close();
        } catch(final BaseXException ex) {
          throw new JaxRxException(400, ex.getMessage());
        }
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
      /* exception follows for both branches. */
    }
    throw new JaxRxException(400, "Parameter '" + qp +
        "' is no valid integer: " + val);
  }

  @Override
  public StreamingOutput run(final String file, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      void code() {
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
        final ArrayOutput ao = new ArrayOutput();
        exec(cmd, ao);

        final XMLSerializer xml =
          new XMLSerializer(out, new SerializerProp(params(path)));
        xml.text(Token.delete(ao.toArray(), '\r'));
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
        try {
          cs.execute(new Open(root(rp)));
        } catch(final BaseXException ex) {
          throw new JaxRxException(404, ex.getMessage());
        }

        // add cached file to the database
        final File file = cache(input);
        try {
          cs.execute(new Add(file.toString()));
        } catch(final BaseXException ex) {
          throw new JaxRxException(404, ex.getMessage());
        } finally {
          file.delete();
        }
      }
    }.run();
  }

  @Override
  public void update(final InputStream input, final ResourcePath rp) {
    new BXCode() {
      @Override
      void code() {
        final String name = root(rp);
        try {
          cs.create(name, input);
        } catch(final BaseXException ex) {
          // return exception if process failed
          throw new JaxRxException(400, ex.getMessage());
        }
      }
    }.run();
  }

  @Override
  public void delete(final ResourcePath rp) {
    new BXCode() {
      @Override
      void code() {
        try {
          cs.execute(new DropDB(root(rp)));
        } catch(final BaseXException ex) {
          // return exception if process failed
          throw new JaxRxException(404, ex.getMessage());
        }
      }
    }.run();
  }
}
