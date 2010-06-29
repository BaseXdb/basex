/*
 * -----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed.
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

public class Example:
	[STAThread]
	public static def Main(args as (string)):
		// initialize timer
		watch = Stopwatch()
		watch.Start()
		// command to be performed
		cmd = 'xquery 1 to 10'
		try:
			// create session
			session = Session('localhost', 1984, 'admin', 'admin')
			
			// Version 1: perform command and show result or error output
			Console.WriteLine(session.Execute(cmd))
			
			// Version 2 (faster): send result to the specified output stream
			stream as Stream = Console.OpenStandardOutput()
			if not session.Execute(cmd, stream):
				Console.WriteLine(session.Info)
				
			// close session
			session.Close()
			
			// print time needed
			Console.WriteLine((('\n' + watch.ElapsedMilliseconds) + ' ms.'))
		except e as IOException:
			// print exception
			Console.WriteLine(e.Message)

