/*
 * -----------------------------------------------------------------------------
 * 
 * This module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the Execute() method with the
 * database command as argument. The method returns the result or throws
 * an exception with the received error message.
 * For the execution of the iterative version of a query you need to call
 * the Query() method. The results will then be returned via the More() and
 * the Next() methods. If an error occurs an exception will be thrown.
 *
 * An even faster approach is to call Execute() with the database command and
 * an output stream. The result will directly be printed and does not have to
 * be cached.
 * 
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */


namespace BaseXClient

import System
import System.Net.Sockets
import System.Security.Cryptography
import System.Text
import System.IO

internal class Session:

	private cache as (byte) = array(byte, 4096)
	public stream as NetworkStream
	private socket as TcpClient
	private info = ''
	private bpos as int
	private bsize as int
	
	/** see readme.txt */
	public def Constructor(host as string, port as int, username as string, pw as string):
		socket = TcpClient(host, port)
		stream = socket.GetStream()
		ts as string = Receive()
		Send(username)
		Send(MD5((MD5(pw) + ts)))
		if stream.ReadByte() != 0:
			raise IOException('Access denied.')

	/** see readme.txt */
	public def Execute(com as string, ms as Stream) as bool:
		Send(com)
		Init()
		Receive(ms)
		info = Receive()
		return Ok()

	/** see readme.txt */
	public def Execute(com as string) as String:
		Send(com)
		Init()
		result as String = Receive()
		info = Receive()
		if not Ok():
			raise IOException(info)
		return result
	
	/** see readme.txt */
	public def Query(q as string) as Query:
		return Query(self, q)

	/** see readme.txt */
	public Info as string:
		get:
			return info
	
	/** see readme.txt */
	public def Close():
		Send('exit')
		socket.Close()

	/** Initializes the byte transfer. */
	private def Init():
		bpos = 0
		bsize = 0
	
	/** Returns a single byte from the socket. */
	private def Read() as byte:
		if bpos == bsize:
			bsize = stream.Read(cache, 0, 4096)
			bpos = 0
		return cache[(bpos++)]

	/** Receives a string from the socket. */
	private def Receive(ms as Stream):
		while true:
			b as byte = Read()
			if b == 0:
				break 
			ms.WriteByte(b)

	/** Receives a string from the socket. */
	public def Receive() as string:
		ms = MemoryStream()
		Receive(ms)
		return System.Text.Encoding.UTF8.GetString(ms.ToArray())

	/** Sends string to server. */
	public def Send(message as string):
		msg as (byte) = System.Text.Encoding.UTF8.GetBytes(message)
		stream.Write(msg, 0, msg.Length)
		stream.WriteByte(0)

	/** Returns success check. */
	public def Ok() as bool:
		return (Read() == 0)
	
	/** Returns the md5 hash of a string. */
	private def MD5(input as string) as string:
		MD5 as MD5CryptoServiceProvider = MD5CryptoServiceProvider()
		hash as (byte) = MD5.ComputeHash(Encoding.UTF8.GetBytes(input))
		
		sb = StringBuilder()
		for h as byte in hash:
			sb.Append(h.ToString('x2'))
		return sb.ToString()


internal class Query:
	private session as Session
	private id as string
	private nextItem as string

	/** see readme.txt */
	public def Constructor(s as Session, query as string):
		session = s
		session.stream.WriteByte(0)
		session.Send(query)
		id = session.Receive()
		if not session.Ok():
			raise IOException(session.Receive())

	/** see readme.txt */
	public def More() as bool:
		session.stream.WriteByte(1)
		session.Send(id)
		nextItem = session.Receive()
		if not session.Ok():
			raise IOException(session.Receive())
		return (nextItem.Length != 0)

	/** see readme.txt */
	public def Next() as string:
		return nextItem

	/** see readme.txt */
	public def Close():
		session.stream.WriteByte(2)
		session.Send(id)