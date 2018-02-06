(:~
 : Settings page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/settings';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace options = 'dba/options' at '../modules/options.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~
 : Settings page.
 : @param  $error  error string
 : @param  $info   info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/settings")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function dba:settings(
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  let $system := html:properties(db:system())
  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='32%'>
        <form action="settings" method="post">
          <h2>Settings » <button>Save</button></h2>
          <h3>Querying</h3>
          <table>
            {
              dba:input($options:TIMEOUT, 'Timeout, in seconds (0 = disabled)'),
              dba:input($options:MEMORY, 'Memory limit, in MB (0 = disabled)'),
              dba:input($options:MAXCHARS, 'Maximum output size')
            }
            <tr>
              <td colspan='2'>Permission:</td>
            </tr>
            <tr>
              <td>
                <select name="permission">{
                  let $pm := options:get($options:PERMISSION)
                  for $p in $options:PERMISSIONS
                  return element option { attribute selected { }[$p = $pm], $p }
                }</select>
              </td>
            </tr>
          </table>
          <h3>Tables</h3>
          <table>
            { dba:input($options:MAXROWS,  'Displayed table rows') }
          </table>
        </form>
      </td>
      <td class='vertical'/>
      <td width='32%'>
        <h2>Global Options</h2>
        <table>{
          $system/tr[th][3]/preceding-sibling::tr[not(th)]
        }</table>
      </td>
      <td class='vertical'/>
      <td width='32%'>
        <h2>Local Options</h2>
        <table>{
          $system/tr[th][3]/following-sibling::tr
        }</table>
      </td>
    </tr>
  )
};

(:~
 : Returns a text input component.
 : @param  $key    key
 : @param  $label  label
 : @return table row
 :)
declare %private function dba:input(
  $key    as xs:string,
  $value  as xs:string
) as element(tr)* {
  <tr>
    <td>{ $value }:<br/>
      <input name="{ $key }" type="number" value="{ options:get($key) }"/>
    </td>
  </tr>
};

(:~
 : Saves the settings.
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/settings")
function dba:settings-save(
) as element(rest:response) {
  options:save(map:merge(Request:parameter-names() ! map:entry(., Request:parameter(.)))),
  web:redirect($dba:CAT, map { 'info': 'Settings were saved.' })
};
