require 'BaseX.rb'

begin
sess = Session.new("localhost", 1984, "admin", "admin")
sess.execute("xquery 1 to 10")
puts sess.result
sess.close
rescue Exception => e
puts e
end

