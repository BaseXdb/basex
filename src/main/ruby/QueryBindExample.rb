# This example shows how external variables can be bound to XQuery expressions.
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) BaseX Team 2005-12, BSD License

require 'BaseXClient.rb'

begin
  # create session
  session = BaseXClient::Session.new("localhost", 1984, "admin", "admin")

  begin
    # create query instance
    input = "declare variable $name external; for $i in 1 to 10 return element { $name } { $i }"
    query = session.query(input)
    
    # bind variable
    query.bind("$name", "number")
    
    # print result
    print query.execute

    # close query instance
    print query.close()
  
  rescue Exception => e
    # print exception
    puts e
  end

  # close session
  session.close

rescue Exception => e
  # print exception
  puts e
end
