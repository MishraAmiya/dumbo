# Dumbo
Dumbo is an international search engine :)

Our first phase is to crawl sites and build our database


Checkout our WIKI for more detail info

# Requirements:
Servers with hdfs, hbase, kafka, elastic search which their config should also put in our resources folder


# How to use:
Run search module, then "?l" command shows all possible commands.
0) ?l: Shows commands and their abbreviations.
1) search: Simple search, contains the whole phrase.
2) advanced-search: Has 3 options:

(a) must: Search result must contain the whole phrase 

(b) mustnot: Search result must not contain the whole phrase

(c) should: Search results containing the whole phrase will be ranked higher
 
 You may add more than one phrase to each of advanced-search's options.