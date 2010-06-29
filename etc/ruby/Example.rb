# -----------------------------------------------------------------------------
#
# This example shows how BaseX commands can be performed.
# The execution time will be printed along with the result of the command.
#
# -----------------------------------------------------------------------------
# (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
# -----------------------------------------------------------------------------

require 'BaseXClient.rb'

# initialize timer
start_time = Time.now

# command to be performed
com = "xquery 1 to 10"

begin
  # create session
  session = Session.new("localhost", 1984, "admin", "admin")

  # perform command and show result or error output
  print session.execute(com)

  # close session
  session.close

  # print time needed
  time = (Time.now - start_time) * 1000
  puts " #{time} ms."

rescue Exception => e
  # print exception
  puts e
end

