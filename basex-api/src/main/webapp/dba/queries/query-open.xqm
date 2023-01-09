(:~
 : Open query.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/queries';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Returns the contents of a query file.
 : @param  $name  name of query file
 : @return query string
 :)
declare
  %rest:path('/dba/query-open')
  %rest:query-param('name', '{$name}')
  %output:method('text')
function dba:query-open(
  $name  as xs:string
) as xs:string {
  config:query($name),
  file:read-text(config:directory() || $name)
};
