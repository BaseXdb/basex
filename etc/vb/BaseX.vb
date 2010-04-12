'
' -----------------------------------------------------------------------------
'  
'  This VB.Net module provides methods to connect to and communicate with the
'  BaseX Server.
' 
'  The Constructor of the class expects a hostname, port, username and password
'  for the connection. The socket connection will then be established via the
'  hostname and the port.
' 
'  For the execution of commands you need to call the Execute() method with the
'  database command as argument. The method returns a boolean, indicating if
'  the command was successful. The result is stored in the Result property,
'  and the Info property returns additional processing information or error
'  output.
' 
'  An even faster approach is to call Execute() with the database command and
'  an output stream. The result will directly be printed and does not have to
'  be cached.
'  
'  -----------------------------------------------------------------------------
'  Example:
'  
' Imports System
'
' Namespace BaseX
'	Public Class Example
'		Public Shared Sub Main(args As String())
'			Try
'				Dim session As New Session("localhost", 1984, "admin", "admin")
'
'				' Version 1: perform command and show result or error output
'				If session.Execute("xquery 1 to 10") Then
'					Console.WriteLine(session.Result)
'				Else
'					Console.WriteLine(session.Info)
'				End If
'
'				' Version 2 (faster): send result to the specified output stream
'				Dim stream As Stream = Console.OpenStandardOutput()
'				If Not session.Execute("xquery 1 to 10", stream) Then
'					Console.WriteLine(session.Info)
'				End If
'
'				session.Close()
'			Catch e As Exception
'				Console.WriteLine(e.Message)
'			End Try
'		End Sub
'	End Class
' End Namespace
'  
'  -----------------------------------------------------------------------------
'  (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
'  -----------------------------------------------------------------------------
' 

Imports System
Imports System.Net.Sockets
Imports System.Security.Cryptography
Imports System.Text
Imports System.Collections.Generic
Imports System.IO

Namespace BaseX
	Class BaseX
		Private m_result As New MemoryStream()
		Private cache As Byte() = New Byte(4095) {}
		Private stream As NetworkStream
		Private socket As TcpClient
		Private m_info As String = ""
		Private bpos As Integer
		Private bsize As Integer

		' Constructor, creating a new socket connection. 
		Public Sub New(host As String, port As Integer, username As String, pw As String)
			socket = New TcpClient(host, port)
			stream = socket.GetStream()
			Dim ts As String = ReadString()
			Send(username)
			Send(MD5(MD5(pw) & ts))
			If stream.ReadByte() <> 0 Then
				Throw New IOException("Access denied.")
			End If
		End Sub

		' Executes the specified command. 
		Public Function Execute(com As String, ms As Stream) As Boolean
			Send(com)
			Init()
			ReadString(ms)
			m_info = ReadString()
			Return Read() = 0
		End Function

		' Executes the specified command. 
		Public Function Execute(com As String) As Boolean
			m_result = New MemoryStream()
			Return Execute(com, m_result)
		End Function

		' Returns the result. 
		Public ReadOnly Property Result() As String
			Get
				Return System.Text.Encoding.UTF8.GetString(m_result.ToArray())
			End Get
		End Property

		' Returns the processing information. 
		Public ReadOnly Property Info() As String
			Get
				Return m_info
			End Get
		End Property

		' Closes the connection. 
		Public Sub Close()
			Send("exit")
			socket.Close()
		End Sub

		' Initializes the byte transfer. 
		Private Sub Init()
			bpos = 0
			bsize = 0
		End Sub

		' Returns a single byte from the socket. 
		Private Function Read() As Byte
			If bpos = bsize Then
				bsize = stream.Read(cache, 0, 4096)
				bpos = 0
			End If
			Return cache(bpos++)
		End Function

		' Receives a string from the socket. 
		Private Sub ReadString(ms As Stream)
			While True
				Dim b As Byte = Read()
				If b = 0 Then
					Exit While
				End If
				ms.WriteByte(b)
			End While
		End Sub

		' Receives a string from the socket. 
		Private Function ReadString() As String
			Dim ms As New MemoryStream()
			ReadString(ms)
			Return System.Text.Encoding.UTF8.GetString(ms.ToArray())
		End Function

		' Sends strings to server. 
		Private Sub Send(message As String)
			Dim msg As Byte() = System.Text.Encoding.UTF8.GetBytes(message)
			stream.Write(msg, 0, msg.Length)
			stream.WriteByte(0)
		End Sub

		' Returns the md5 hash of a string. 
		Private Function MD5(input As String) As String
			Dim MD5 As New MD5CryptoServiceProvider()
			Dim hash As Byte() = MD5.ComputeHash(Encoding.UTF8.GetBytes(input))

			Dim sb As New StringBuilder()
			For Each h As Byte In hash
				sb.Append(h.ToString("x2"))
			Next
			Return sb.ToString()
		End Function
	End Class
End Namespace
