(:~
 : Evaluate query.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/queries';

import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~
 : Evaluates a query and returns the result.
 : @param  $query  query
 : @return result of query
 :)
declare
  %rest:POST('{$query}')
  %rest:path('/dba/query-eval')
  %rest:single
  %output:method('text')
function dba:query-eval(
  $query  as xs:string?
) as xs:string {
  util:query($query, ())
};

(:~
 : Runs an updating query.
 : @param  $query  query
 : @return result of query
 :)
declare
  %updating
  %rest:POST('{$query}')
  %rest:path('/dba/query-update')
  %rest:single
  %output:method('text')
function dba:query-update(
  $query  as xs:string?
) as empty-sequence() {
  util:update-query($query)
};
