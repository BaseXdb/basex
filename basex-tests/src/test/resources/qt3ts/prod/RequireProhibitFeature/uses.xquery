module namespace my = "http://www.w3.org/XQueryTest/RequireProhibitFeature";

declare function my:one() as function(*)
{
  function() { 1 }
};
