# newsfeels

Basic sentiment analysis for news headlines.

## Usage

```
$lein run                                                             

Most-Viewed Articles in the Last 24 Hours:

|   Source |                                                                         Headline | Headline Valence | Abstract Valence | Total Valence |
|----------+----------------------------------------------------------------------------------+------------------+------------------+---------------|
| :nytimes |                 Modi’s Party Loses a Key Election, Held Under the Cloud of Covid |                0 |               -8 |            -8 |
| :nytimes |       Oregon Lawmaker Who Let Protesters Into State Capitol Is Charged in Breach |               -5 |               -2 |            -7 |
| :nytimes |          Boxer Charged in Killing of Pregnant Woman Found in Puerto Rican Lagoon |               -6 |                0 |            -6 |
| :nytimes |                          India’s Covid-19 Crisis Shakes Modi’s Image of Strength |               -1 |               -4 |            -5 |
| :nytimes |            A third of Basecamp’s workers resign after a ban on talking politics. |               -3 |               -2 |            -5 |
| :nytimes |              There’s a Name for the Blah You’re Feeling: It’s Called Languishing |               -1 |               -3 |            -4 |
| :nytimes |                              How a Miami School Became a Beacon for Anti-Vaxxers |                0 |               -2 |            -2 |
| :nytimes |     The World Knows Her as ‘Disaster Girl.’ She Just Made $500,000 Off the Meme. |                0 |               -1 |            -1 |
| :nytimes |                  Bill and Melinda Gates Are Divorcing After 27 Years of Marriage |                0 |                0 |             0 |
| :nytimes | The F.D.A. is set to authorize the Pfizer-BioNTech vaccine for those 12-15 ye... |                0 |                0 |             0 |
| :nytimes |                                               How Y’all, Youse and You Guys Talk |                0 |                0 |             0 |
| :nytimes |            Reaching ‘Herd Immunity’ Is Unlikely in the U.S., Experts Now Believe |                1 |                0 |             1 |
| :nytimes |               SpaceX Makes First Nighttime Splashdown With Astronauts Since 1968 |                0 |                1 |             1 |
| :nytimes |                   Maskless and Sweaty: Clubbing Returns to Britain for a Weekend |                0 |                1 |             1 |
| :nytimes |                                            From Best Friends to Platonic Spouses |                3 |                0 |             3 |
| :nytimes |               Andrew Yang Promised to Create 100,000 Jobs. He Ended Up With 150. |                1 |                2 |             3 |
| :nytimes |                                                         Girl, Wash Your Timeline |                0 |                3 |             3 |
| :nytimes |                                      The Best (and Worst) States for Remote Work |                3 |                0 |             3 |
| :nytimes |                       Olympia Dukakis, Oscar Winner for ‘Moonstruck,’ Dies at 89 |                4 |                3 |             7 |
| :nytimes |   Help! If There’s a Chance That I Could Endanger Others, Should I Still Travel? |                4 |                4 |             8 |

```

## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
