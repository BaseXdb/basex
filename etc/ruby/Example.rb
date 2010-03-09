# -----------------------------------------------------------------------------
#
# This example shows how BaseX commands can be performed via the Ruby API.
# The execution time will be printed along with the result of the command.
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

require 'BaseX.rb'

# initialize timer
start_time = Time.now

# command to be performed
com = "xquery 1 to 10"

begin
  # create session
  session = Session.new("localhost", 1984, "admin", "admin")
  
  # perform command and show result or error output
  if session.execute(com) == "\0"
    puts session.result
  else
   puts session.info
  end
  
  # close session
  session.close
  
  # print time needed
  time = (Time.now - start_time) * 1000
  puts "#{time} ms." 
  
rescue Exception => e
  # print exception
  puts e
end

