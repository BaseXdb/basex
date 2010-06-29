# -----------------------------------------------------------------------------
#
# This example shows how results from a query can be received in an iterative
# mode.
# The execution time will be printed along with the result of the command.
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

require 'BaseXClient.rb'

# initialize timer
start_time = Time.now

# command to be performed
cmd = "1 to 10"

begin
  # create session
  session = Session.new("localhost", 1984, "admin", "admin")
  # create and run query
  begin
  query = session.query(cmd)
  while query.more do
    print " - " + query.next
  end
  # close query object	
  query.close()
  
  rescue Exception => e
    # print exception
    puts e
  end

  # close session
  session.close

  # print time needed
  time = (Time.now - start_time) * 1000
  puts " #{time} ms."

rescue Exception => e
  # print exception
  puts e
end
