'
' * -----------------------------------------------------------------------------
' *
' * This example shows how BaseX commands can be performed via the VB.Net API.
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
	Public Class Example
		Public Shared Sub Main(args As String())
			' initialize timer
			Dim watch As New Stopwatch()
			watch.Start()

			' command to be performed
			Dim cmd As String = "xquery 1 to 10"

			Try
				' create session
				Dim session As New Session("localhost", 1984, "admin", "admin")

				' Version 1: perform command and show result or error output
				If session.Execute(cmd) Then
					Console.WriteLine(session.Result)
				Else
					Console.WriteLine(session.Info)
				End If

				' Version 2 (faster): send result to the specified output stream
				Dim stream As Stream = Console.OpenStandardOutput()
				If Not session.Execute(cmd, stream) Then
					Console.WriteLine(session.Info)
				End If

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
