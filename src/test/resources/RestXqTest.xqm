module namespace junit = "http://basex.org/modules/restxq/junit";

declare namespace rest = "http://exquery.org/ns/rest/annotation";

declare
  %rest:GET
  %rest:path("/")
  function junit:root() {
    'root'
};

declare
  %rest:GET
  %rest:path("/one")
  function junit:get() {
    'one'
};

declare
  %rest:GET
  %rest:path("/one/{$x}")
  function junit:var($x) {
    $x
};
