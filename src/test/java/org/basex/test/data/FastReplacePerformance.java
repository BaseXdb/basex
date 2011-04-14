package org.basex.test.data;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;

public class FastReplacePerformance {
  
  final String INPUTPATH;
  static final String DBNAME = "Xmark11MBfastreplace";
  static final String QUERY1 = "let $regionscopy := " +
			"copy $s := /site/regions modify for $i in $s//item return " +
			"replace node $i/location with <location>NEWLOC</location> return " +
			"$s return replace node /site/regions with $regionscopy";
	static final String QUERY2 = "for $i in //item return " +
			"replace node $i with $i";
	static final String QUERY3 = "for $i in //item return " +
			"replace node $i with copy $item := $i modify replace node " +
			"$item/location with <loc>NEW</loc> return $i";

	public FastReplacePerformance(final String path) {
	  INPUTPATH = path;
	}
	
	public void fastReplace() {
		System.out.println("fast replace:");
		final Context ctx = new Context();
		try {
			new CreateDB(DBNAME, INPUTPATH).execute(ctx);
			new Set("runs", "10").execute(ctx);
			final XQuery xq = new XQuery(QUERY3);
			xq.execute(ctx);
			System.out.println(xq.info());
		} catch (BaseXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n\n");
	}
	
	public void normalReplace() {
		System.out.println("simple replace:");
		final Context ctx = new Context();
		try {
			new CreateDB(DBNAME, INPUTPATH).execute(ctx);
			new Set("runs", "10").execute(ctx);
			final XQuery xq = new XQuery(QUERY2);
			System.out.println(xq.execute(ctx));
			System.out.println(xq.info());
		} catch (BaseXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n\n");
	}
	
	public static void main(final String[] args) {
		new FastReplacePerformance("/Users/lukas/Dropbox/basex/xml/11MB.xml")
		.fastReplace();
	}
}
