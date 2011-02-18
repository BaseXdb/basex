# This example shows how new documents can be added.
# Documentation: http://basex.org/api
#
# (C) BaseX Team 2005-11, ISC License

require 'BaseXClient.rb'

begin

  # create session
  session = Session.new("localhost", 1984, "admin", "admin")
  
  # create empty database
  session.execute("create db database")
  print "\n" + session.info()
  
  # add document
  session.add("World.xml", "/world", "<x>Hello World!</x>")
  print "\n" + session.info()
  
  # add document
  session.add("Universe.xml", "", "<x>Hello Universe!</x>")
  print "\n" + session.info() + "\n"
  
  # run query on database
  print "\n" + session.execute("xquery collection('database')") + "\n"
  
  # drop database
  session.execute("drop db database")

  # close session
  session.close

rescue Exception => e
  # print exception
  puts e
end
