(:~
 : This XQuery module is evaluated by some JUnit tests.
 : @author BaseX Team, BSD License
 : @version 1.0
 :)
module namespace hello = "world";

(:~ External function. :)
declare function hello:ext() external;