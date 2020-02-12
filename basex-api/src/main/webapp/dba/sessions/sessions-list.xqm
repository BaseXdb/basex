(:~
 : Sessions page.
 :
 : @author Christian Grün, BaseX Team 2005-20, BSD License
 :)
module namespace dba = 'dba/sessions';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'sessions';

(:~
 : Sessions page.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/sessions')
  %rest:query-param('sort',  '{$sort}', 'access')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:sessions(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action='{ $dba:CAT }' method='post' class='update'>
        <h2>Web Sessions</h2>
        {
          let $headers := (
            map { 'key': 'id', 'label': 'ID', 'type': 'id' },
            map { 'key': 'name', 'label': 'Name' },
            map { 'key': 'value', 'label': 'Value' },
            map { 'key': 'access', 'label': 'Last Access', 'type': 'dateTime', 'order': 'desc' },
            map { 'key': 'you', 'label': 'You' }
          )
          let $entries :=
            for $id in sessions:ids()
            let $access := sessions:accessed($id)
            let $you := if(session:id() = $id) then '✓' else '–'
            (: supported session ids (application-specific, can be extended) :)
            for $name in sessions:names($id)[. = ($config:SESSION-KEY, 'id')]
            let $value := try {
              sessions:get($id, $name)
            } catch sessions:get {
              '–' (: non-XQuery session value :)
            }
            let $string := util:chop(serialize($value, map { 'method': 'basex' }), 20)
            order by $access descending
            return map {
              'id': $id || '|' || $name,
              'name': $name,
              'value': $string,
              'access': $access,
              'you': $you
            }
          let $buttons := (
            html:button('session-kill', 'Kill', true())
          )
          let $options := map { 'sort': $sort, 'presort': 'access' }
          return html:table($headers, $entries, $buttons, map { }, $options)
        }
        </form>
      </td>
      <td class='vertical'/>
      <td>
        <h2>Database Sessions</h2>
        {
          let $headers := (
            map { 'key': 'address', 'label': 'Address' },
            map { 'key': 'user', 'label': 'User' }
          )
          let $entries := admin:sessions() ! map {
            'address': @address,
            'user': @user
          }
          return html:table($headers, $entries, (), map { }, map { })
        }
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of users
 : @param  $ids     ids
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/sessions')
  %rest:query-param('action', '{$action}')
  %rest:query-param('name',   '{$names}')
  %rest:query-param('id',     '{$ids}')
function dba:users-redirect(
  $action  as xs:string,
  $names   as xs:string*,
  $ids     as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'id': $ids })
};
