package org.basex.api.jaxrx;

import static org.jaxrx.core.URLConstants.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Run;
import org.basex.core.cmd.Set;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.io.IO;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
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
  /** Configuration: User. */
  public static final String USER = "org.basex.user";
  /** Configuration: Password. */
  public static final String PASSWORD = "org.basex.password";
  /** Configuration: Server port. */
  public static final String SERVERPORT = "org.basex.serverport";
  /** Configuration: REST path. */
  public static final String RESTPATH = "org.basex.restpath";
  /** Configuration: serializer options. */
  public static final String SERIALIZER = "org.jaxrx.parameter.output";

  @Override
  public HashSet<QueryParameter> getParameters() {
    final HashSet<QueryParameter> p = new HashSet<QueryParameter>();
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
    if(rp.getDepth() != 0) return query(null, rp);

    return new BXOutput(null) {
      @Override
      String code() throws IOException {
        final XMLSerializer xml = new XMLSerializer(out,
            new SerializerProp(params(rp)));

        // retrieve list of databases
        final Table table = new Table(exec(new List(), null));
        for(final TokenList l : table.contents) {
          xml.emptyElement(Token.token(JAXRX + ":" + "resource"),
              Token.token("name"), l.get(0),
              Token.token("documents"), l.get(1),
              Token.token("size"), l.get(2));
        }
        xml.close();
        return null;
      }
    };
  }

  @Override
  public StreamingOutput query(final String query, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      String code() {
        // wrap start and counter around query expression
        final String xq = query != null ? query : ".";
        // evaluate first result and number of results
        final int s = num(rp, QueryParameter.START, 1);
        final int m = num(rp, QueryParameter.COUNT, Integer.MAX_VALUE - s);

        try {
          cs.execute(new Set(Prop.SERIALIZER, params(path)));
          cs.setOutputStream(out);

          // create query instance
          final ClientQuery cq = cs.query(xq);
          final String var = path.getValue(QueryParameter.VAR);
          if(var != null) {
            final Scanner sc = new Scanner(var);
            sc.useDelimiter("\1");
            while(sc.hasNext()) {
              final String v = sc.next();
              String[] sp = v.split("\2", 3);
              if(sp.length < 2) sp = v.split("=", 3);
              cq.bind(sp[0], sp.length > 1 ? sp[1] : "",
                  sp.length > 2 ? sp[2] : "");
            }
          }
          // loop through all results
          int c = 0;
          cq.init();
          while(++c < s + m && cq.more()) if(c >= s) cq.next();
          cq.close();
          return null;
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
      String code() {
        // get root directory for files
        final String root = System.getProperty("org.basex.restpath") + "/";
        final IO io = IO.get(root + file);
        exec(new Run(io.path()), out);
        return cs.info();
      }
    };
  }

  @Override
  public StreamingOutput command(final String cmd, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      String code() throws IOException {
        // perform command
        final ArrayOutput ao = new ArrayOutput();
        exec(cmd, ao);

        // serialize output and remove carriage returns
        final XMLSerializer xml =
          new XMLSerializer(out, new SerializerProp(params(path)));
        xml.text(Token.delete(ao.toArray(), '\r'));
        xml.close();
        return cs.info();
      }
    };
  }

  @Override
  public String add(final InputStream input, final ResourcePath rp) {
    return new BXCode() {
      @Override
      String code() {
        // open database
        try {
          cs.execute(new Open(db(rp)));
        } catch(final BaseXException ex) {
          throw new JaxRxException(404, ex.getMessage());
        }
        add(input, rp, cs);
        return cs.info();
      }
    }.run();
  }

  /**
   * Adds a document to the database.
   * @param input input stream
   * @param rp resource path
   * @param cs client session
   */
  void add(final InputStream input, final ResourcePath rp,
      final ClientSession cs) {

    final int d = rp.getDepth();
    if(d < 2) throw new JaxRxException(400, "Document name is missing.");

    final StringBuilder target = new StringBuilder();
    for(int i = 1; i < d - 1; i++) {
      target.append('/').append(rp.getResource(i));
    }
    try {
      cs.add(rp.getResource(d - 1), target.toString(), input);
    } catch(final BaseXException ex) {
      throw new JaxRxException(400, ex.getMessage());
    }
  }

  @Override
  public String update(final InputStream input, final ResourcePath rp) {
    return new BXCode() {
      @Override
      String code() {
        try {
          final int d = rp.getDepth();
          // create new database
          if(d == 1) {
            cs.create(db(rp), input);
          } else {
            // add document to database
            cs.execute(new Open(db(rp)));
            final StringBuilder target = new StringBuilder();
            for(int i = 1; i < d; i++) {
              target.append('/').append(rp.getResource(i));
            }
            cs.execute(new Delete(target.toString()));
            add(input, rp, cs);
          }
          return cs.info();
        } catch(final BaseXException ex) {
          // return exception if process failed
          throw new JaxRxException(400, ex.getMessage());
        }
      }
    }.run();
  }

  @Override
  public String delete(final ResourcePath rp) {
    return new BXCode() {
      @Override
      String code() {
        try {
          if(rp.getDepth() == 1) {
            cs.execute(new DropDB(db(rp)));
          } else {
            cs.execute(new Open(db(rp)));
            cs.execute(new Delete(path(rp)));
          }
          return cs.info();
        } catch(final BaseXException ex) {
          // return exception if process failed
          throw new JaxRxException(404, ex.getMessage());
        }
      }
    }.run();
  }
}
