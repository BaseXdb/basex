/*
 * -----------------------------------------------------------------------------
 *
 * This example shows how results from a query can be received in an iterative
 * mode via the Boo API.
 * The execution time will be printed along with the result of the command.
 *
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */

namespace BaseXClient

import System
import System.Diagnostics
import System.IO

[module]
public class QueryIteratorExample:

	public static def Main(args as (string)):
		// initialize timer
		watch = Stopwatch()
		watch.Start()
		// command to be performed
		cmd = '1 to 10'
		try:
			// create session
			session = Session('localhost', 1984, 'admin', 'admin')
			try:
				query as Query = session.query(cmd)
				// run query iterator
				while query.more():
					Console.WriteLine(query.next())
				query.close()
			except e as IOException:
				// print exception
				Console.WriteLine(e.Message)
				
			// close session
			session.Close()
			// print time needed
			Console.WriteLine((('\n' + watch.ElapsedMilliseconds) + ' ms.'))
			
		except e as IOException:
			// print exception
			Console.WriteLine(e.Message)

