#!/usr/bin/python
# Fabrice Issac
# LDI 2009

import socket
import sys

class BaseXClient(object):
	def __init__(self,host,port):
		self.host = host
		self.port = port
		self.sesid = ""
		self.verb = True
		self.data = ' '
		
	def connect(self):
		self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.s.connect((self.host,self.port))

	def close(self):
		self.s.close()
		
	def formatCommand(self,com):
		# integer to hex e.g. 4 -> '\x00\x04'
		i2h = chr(len(com)>>8)+chr(len(com)%16384)
		fcom = i2h+com
		return fcom
		
	def sendCommand(self,com):
		fcom = self.formatCommand(com)
		if self.verb:
			print "C=>"+repr(fcom)
		self.connect()
		self.s.send(fcom)
		data = self.s.recv(1024)
		if self.verb:
			print "R=>"+repr(data)
		if len(data)==4:
			self.sesid = str(ord(data[2])*256+ord(data[3]))
		else:
			sys.stdout.write("Error\n")
		self.close()
		
	def getResult(self,com):
		fcom = self.formatCommand(com+" "+self.sesid)
		if self.verb:
			print "c=>"+repr(fcom)
		self.connect()
		self.s.send(fcom)
		self.data = self.s.recv(10000000)
		if self.verb:
			print "R=>"+repr(self.data)
		try:
			self.sesid = str(ord(self.data[2])*256+ord(self.data[3]))
		except:
			self.data = self.data + ""
		self.close()
	
	def write(self):
		sys.stdout.write(self.data)
		
	def console(self):
		print "Python BaseX client"
		print "v0.1"
		self.sendCommand("SET INFO ON")
		self.getResult("INTOUTPUT")
		self.write()	
		self.data = ' '
		while self.data != '':
			sys.stdout.write('>')
			com = sys.stdin.readline().rstrip()
			if com != '':
				self.sendCommand(com)
				self.getResult("INTOUTPUT")
				self.write()
				self.getResult("INTINFO")
		print "Profitez de la vie !!!"
		
		
if __name__ == '__main__':
	bxc = BaseXClient("localhost",1984)
	bxc.console()
	
