# This example shows how new databases can be created.
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

require 'BaseXClient.rb'

begin

  # create session
  session = BaseXClient::Session.new("localhost", 1984, "admin", "admin")
  
  # create new database
  session.create("database", "<x>Hello World!</x>")
  print "\n" + session.info()
  
  # run query on database
  print "\n" + session.execute("xquery doc('database')") + "\n"
  
  # drop database
  session.execute("drop db database")

  # close session
  session.close

rescue Exception => e
  # print exception
  puts e
end
