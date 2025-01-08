(:~
 : Sessions.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/sessions';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'sessions';

(:~
 : Sessions.
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
  %output:html-version('5')
function dba:sessions(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  <tr>
    <td>
      <form method='post' autocomplete='off'>
      <h2>Web Sessions</h2>
      {
        let $headers := (
          { 'key': 'id', 'label': 'ID', 'type': 'id' },
          { 'key': 'name', 'label': 'Name' },
          { 'key': 'value', 'label': 'Value' },
          { 'key': 'access', 'label': 'Last Access', 'type': 'time', 'order': 'desc' },
          { 'key': 'you', 'label': 'You' }
        )
        let $entries :=
          for $id in sessions:ids()
          let $access := sessions:accessed($id)
          let $you := if (session:id() = $id) then '✓' else '–'
          (: supported session ids (application-specific, can be extended) :)
          for $name in sessions:names($id)[. = ($config:SESSION-KEY, 'id')]
          let $value := try {
            sessions:get($id, $name)
          } catch sessions:get {
            '–' (: non-XQuery session value :)
          }
          let $string := utils:chop(serialize($value, { 'method': 'basex' }), 20)
          order by $access descending
          return {
            'id': $id || '|' || $name,
            'name': $name,
            'value': $string,
            'access': $access,
            'you': $you
          }
        let $buttons := (
          html:button('session-kill', 'Kill', ('CHECK', 'CONFIRM'))
        )
        let $options := { 'sort': $sort, 'presort': 'access' }
        return html:table($headers, $entries, $buttons, {}, $options)
      }
      </form>
    </td>
    <td class='vertical'/>
    <td>
      <h2>Database Sessions</h2>
      {
        let $headers := (
          { 'key': 'address', 'label': 'Address' },
          { 'key': 'user', 'label': 'User' }
        )
        let $entries := admin:sessions() ! {
          'address': @address,
          'user': @user
        }
        return html:table($headers, $entries)
      }
    </td>
  </tr>
  => html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error })
};
