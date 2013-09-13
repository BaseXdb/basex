(: Written By: Frans Englich(maintainer, not original author) :)
(: Purpose: Return the name of the person with ID `person0'. :)
(: Date: 2007-03-09 :)

declare namespace local = "http://www.example.com/";


declare function local:convert($v as xs:decimal?) as xs:decimal?
{
  2.20371 * $v (: convert Dfl to Euro :)
};

<XMark-result-All>

  <XMark-result-Q1>
    {
      let $auction := (/) return
      for $b in $auction/site/people/person[@id = "person0"] return $b/name/text()
    }
  </XMark-result-Q1>

  <XMark-result-Q2>
    {
      let $auction := (/) return
      for $b in $auction/site/open_auctions/open_auction
      return <increase>{$b/bidder[1]/increase/text()}</increase>
    }
  </XMark-result-Q2>
  
  <XMark-result-Q3>
    {
      let $auction := (/) return
      for $b in $auction/site/open_auctions/open_auction
      where zero-or-one($b/bidder[1]/increase/text()) * 2.0 <= $b/bidder[last()]/increase/text()
      return <increase first="{$b/bidder[1]/increase/text()}"
                       last="{$b/bidder[last()]/increase/text()}"/>
    }
  </XMark-result-Q3>
  
  <XMark-result-Q4>
    {
      let $auction := (/) return
      for $b in $auction/site/open_auctions/open_auction
      where
        some $pr1 in $b/bidder/personref[@person = "person20"],
             $pr2 in $b/bidder/personref[@person = "person51"]
        satisfies $pr1 << $pr2
      return <history>{$b/reserve/text()}</history>
    }
  </XMark-result-Q4>
  
  <XMark-result-Q5>
    {
      let $auction := (/) return
      count(for $i in $auction/site/closed_auctions/closed_auction
            where $i/price/text() >= 40.0
            return $i/price)
    }
  </XMark-result-Q5>
  
  <XMark-result-Q6>
    {
      let $auction := (/) return
      for $b in $auction//site/regions return count($b//item)
    }
  </XMark-result-Q6>
  
  <XMark-result-Q7>
    {
      let $auction := (/) return
      for $p in $auction/site
      return count($p//description) + count($p//annotation) + count($p//emailaddress)
    }
  </XMark-result-Q7>
  
  <XMark-result-Q8>
    {
      let $auction := (/) return
      for $p in $auction/site/people/person
      let $a := for $t in $auction/site/closed_auctions/closed_auction
                where $t/buyer/@person = $p/@id
                return $t
      return <item person="{$p/name/text()}">{count($a)}</item>
    }
  </XMark-result-Q8>
  
  <XMark-result-Q9>
    {
      let $auction := (/) return
      let $ca := $auction/site/closed_auctions/closed_auction return
      let $ei := $auction/site/regions/europe/item
      for $p in $auction/site/people/person
        let $a := for $t in $ca
                  where $p/@id = $t/buyer/@person
                  return let $n := for $t2 in $ei where $t/itemref/@item = $t2/@id return $t2
                         return <item>{$n/name/text()}</item>
      return <person name="{$p/name/text()}">{$a}</person>
    }
  </XMark-result-Q9>
  
  <XMark-result-Q10>
    {
    let $auction := (/)
    return
      for $i in
        distinct-values($auction/site/people/person/profile/interest/@category)
      let $p :=
        for $t in $auction/site/people/person
        where $t/profile/interest/@category = $i
        return
          <personne>
            <statistiques>
              <sexe>{$t/profile/gender/text()}</sexe>
              <age>{$t/profile/age/text()}</age>
              <education>{$t/profile/education/text()}</education>
              <revenu>{fn:data($t/profile/@income)}</revenu>
            </statistiques>
            <coordonnees>
              <nom>{$t/name/text()}</nom>
              <rue>{$t/address/street/text()}</rue>
              <ville>{$t/address/city/text()}</ville>
              <pays>{$t/address/country/text()}</pays>
              <reseau>
                <courrier>{$t/emailaddress/text()}</courrier>
                <pagePerso>{$t/homepage/text()}</pagePerso>
              </reseau>
            </coordonnees>
            <cartePaiement>{$t/creditcard/text()}</cartePaiement>
          </personne>
      return <categorie>{<id>{$i}</id>, $p}</categorie>
    }
  </XMark-result-Q10>
  
  <XMark-result-Q11>
    {
      let $auction := (/) return
      for $p in $auction/site/people/person
      let $l := for $i in $auction/site/open_auctions/open_auction/initial
                where $p/profile/@income > 5000 * exactly-one($i/text())
                return $i
      return <items name="{$p/name/text()}">{count($l)}</items>
    }
  </XMark-result-Q11>
  
  <XMark-result-Q12>
    {
      let $auction := (/) return
      for $p in $auction/site/people/person
      let $l :=
        for $i in $auction/site/open_auctions/open_auction/initial
        where $p/profile/@income > 5000.0 * exactly-one($i/text())
        return $i
      where $p/profile/@income > 50000.0
      return <items person="{$p/profile/@income}">{count($l)}</items>
    }
  </XMark-result-Q12>
  
  <XMark-result-Q13>
    {
      let $auction := (/) return
      for $i in $auction/site/regions/australia/item
      return <item name="{$i/name/text()}">{$i/description}</item>
    }
  </XMark-result-Q13>
  
  <XMark-result-Q14>
    {
      let $auction := (/) return
      for $i in $auction/site//item
      where contains(string(exactly-one($i/description)), "gold")
      return $i/name/text()
    }
  </XMark-result-Q14>
  
  <XMark-result-Q15>
    {
      let $auction := (/) return
      for $a in
        $auction/site/closed_auctions/closed_auction/annotation/description/parlist/
         listitem/
         parlist/
         listitem/
         text/
         emph/
         keyword/
         text()
      return <text>{$a}</text>
    }
  </XMark-result-Q15>
  
  <XMark-result-Q16>
    {
      let $auction := (/) return
      for $a in $auction/site/closed_auctions/closed_auction
      where
        not(
          empty(
            $a/annotation/description/parlist/listitem/parlist/listitem/text/emph/
             keyword/
             text()
          )
        )
      return <person id="{$a/seller/@person}"/>
    }
  </XMark-result-Q16>
  
  <XMark-result-Q17>
    {
      let $auction := (/) return
      for $p in $auction/site/people/person
      where empty($p/homepage/text())
      return <person name="{$p/name/text()}"/>
    }
  </XMark-result-Q17>
  
  <XMark-result-Q18>
    {
      let $auction := (/) return
      for $i in $auction/site/open_auctions/open_auction
      return local:convert(zero-or-one($i/reserve))
    }
  </XMark-result-Q18>
  
  <XMark-result-Q19>
    {
      let $auction := (/) return
      for $b in $auction/site/regions//item
      let $k := $b/name/text()
      stable order by zero-or-one($b/location) ascending empty greatest
      return <item name="{$k}">{$b/location/text()}</item>
    }
  </XMark-result-Q19>
  
  <XMark-result-Q20>
    {
      let $auction := (/) return
      <result>
        <preferred>
          {count($auction/site/people/person/profile[@income >= 100000.0])}
        </preferred>
        <standard>
          {
            count(
              $auction/site/people/person/
               profile[@income < 100000.0 and @income >= 30000.0]
            )
          }
        </standard>
        <challenge>
          {count($auction/site/people/person/profile[@income < 30000.0])}
        </challenge>
        <na>
          {
            count(
              for $p in $auction/site/people/person
              where empty($p/profile/@income)
              return $p
            )
          }
        </na>
      </result>
    }
  </XMark-result-Q20>

</XMark-result-All>
