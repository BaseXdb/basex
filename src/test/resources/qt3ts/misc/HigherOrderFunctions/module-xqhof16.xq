xquery version "3.0";
module namespace lib="lib";
declare base-uri "lib";

declare function lib:getfun()
{
  fn:static-base-uri#0
};

declare function lib:getfun2()
{
  <lib/>/fn:name#0
};

declare function lib:getfun3()
{
  <lib/>/fn:function-lookup#2
};
