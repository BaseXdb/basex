(:~
 : Drop patterns.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/users';

import module namespace web = 'dba/web' at '../modules/web.xqm';

(:~
 : Drops patterns.
 : @param  $names    names of users
 : @param  $pattern  database pattern
 :)
declare
  %updating
  %rest:GET
  %rest:path("dba/drop-pattern")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("pattern", "{$pattern}")
  %output:method("html")
function _:drop-pattern(
  $name     as xs:string,
  $pattern  as xs:string
) {
  web:check(),
  try {
    web:update("user:drop($n, $p)", map { 'n': $name, 'p': $pattern }),
    web:redirect("user", map { 'name': $name, 'info': 'Pattern dropped: ' || $pattern })
  } catch * {
    web:redirect("user", map { 'error': $err:description })
  }
};
