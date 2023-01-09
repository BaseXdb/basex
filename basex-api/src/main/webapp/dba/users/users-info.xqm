(:~
 : Updates users information.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Updates users information.
 : @param  $info  users information
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/users-info')
  %rest:form-param('info', '{$info}')
function dba:users-info(
  $info  as xs:string
) as empty-sequence() {
  try {
    (: change user info :)
    let $xml := if($info) then (
      parse-xml($info)/*[self::info or error(xs:QName(err:FORC0006))]
    ) else (
      <info/>
    )
    where not(deep-equal(user:info(), $xml))
    return user:update-info($xml),

    util:redirect($dba:CAT, map { 'info': 'User information was updated.' })
  } catch err:FODC0006 {
    util:redirect($dba:CAT, map { 'error': 'XML with "info" root element expected.' })
  } catch * {
    util:redirect($dba:CAT, map { 'error': $err:description })
  }
};
