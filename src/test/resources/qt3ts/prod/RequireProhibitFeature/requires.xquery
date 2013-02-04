module namespace my = "http://www.w3.org/XQueryTest/RequireProhibitFeature";

declare option require-feature "higher-order-function";

declare function my:one() as function(*)
{
  function() { 1 }
};
