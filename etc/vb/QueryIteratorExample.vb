'
' * -----------------------------------------------------------------------------
' *
' * This example shows how results from a query can be received in an iterative
' * mode.
' * The execution time will be printed along with the result of the command.
' *
' * -----------------------------------------------------------------------------
' * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
' * -----------------------------------------------------------------------------
' 

Imports System
Imports System.Diagnostics
Imports System.IO

Namespace BaseXClient
	Public Class QueryIteratorExample
		Public Shared Sub Main(args As String())
			' initialize timer
			Dim watch As New Stopwatch()
			watch.Start()

			' command to be performed
			Dim cmd As String = "1 to 10"

			Try
				' create session
				Dim session As New Session("localhost", 1984, "admin", "admin")

				Try
					' run query iterator
					Dim query As Query = session.Query(cmd)
					While query.More()
						Console.WriteLine(query.Next())
					End While
					query.Close()
				Catch e As IOException
					' print exception
					Console.WriteLine(e.Message)
				End Try

				' close session
				session.Close()

				' print time needed
				Console.WriteLine(vbLf & watch.ElapsedMilliseconds & " ms.")
			Catch e As IOException
				' print exception
				Console.WriteLine(e.Message)
			End Try
		End Sub
	End Class
End Namespace
