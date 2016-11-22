# ve-challenge

## Problem
Setup a system that is capable of ingesting Twitter data with the “challenge” hashtag. Save the data as you receive it and perform some analysis on it. Select the top 5 countries based on the received tweets that have been geotagged.

Please detail the technologies you used in the system. You are also required to send the code/ templates or just print-screens showing the full system integration, together with the time window that you used for the input data and the list of the resulting 5 countries.

## Solution

### Real-time solution with spark streaming

**Code:** `io.github.adrianulbona.ve.VeChallengeRealTime`

**Time window:** 2 minutes (it can be easily changed - see the code)

**Output:** for each 2 minutes microbatch - top 5 countries based from geotagged tweets 

```
.....
2016-11-22  07:58:00
(US,4)

2016-11-22  08:00:00
(AR,1)
(US,1)

2016-11-22  08:02:00
(US,5)
(AU,1)

2016-11-22  08:04:00
(US,6)
(AU,1)
(AR,1)
(ID,1)
.....
```


### Ingestion with spark streaming and parquet files 

**Code:** `io.github.adrianulbona.ve.VeChallengeIngest`

**Time window:** 2 minutes (it can be easily changed - see the code)

**Output:** for each 2 minutes microbatch
  - one parquet file with relevant information extracted from each tweet
  - top 5 countries based from geotagged tweets


### Batch solution with spark dataframes and parquet files 

**Code:** `io.github.adrianulbona.ve.VeChallengeBatch`

**Time window:** depends on how much time you've run the injestion part

**Output:** top 5 countries based from geotagged

## Run

Rename the `ve-challenge/src/main/resources/twitter4j.properties.template` to `twitter4j.properties` and add your twitter credentials inside.
