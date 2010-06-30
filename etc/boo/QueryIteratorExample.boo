/*
 * -----------------------------------------------------------------------------
 *
 * This example shows how results from a query can be received in an iterative
 * mode.
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
				query as Query = session.Query(cmd)
				// run query iterator
				while query.More():
					Console.WriteLine(query.Next())
				query.Close()
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

