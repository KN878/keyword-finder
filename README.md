# Desription
This is a keyword searcher among the provided web pages. Urls are 
read from file <code>urls.txt</code>, keywords - <code>keywords.txt</code>.
The results are writen in file <code>result.csv</code> in the format

<code>URL,keyword1,keyword2,...\
http://url1.com,1,1,...\
http://url2.com,12,34,...</code>

Each line corresponds to the url, and each column corresponds to the word you are trying to search.
If the url is not reachable - putting zeros.

Finder is build with purely functional style in mind, meaninig handling 
all side-effects properly. Also, tagless final is used.

Each keyword is searched concurrently in the provided URL with a limit
in number of workers set to 100.

Also, the algorithm is benchmarked using simple time difference from
starting it to it's end.  
# Libraries
<ol>
   <li>Cats-core</li>
   <li>Cats-effect</li>
   <li>Monix</li>
   <li>Tofu</li>
   <li>Http4s</li>
   <li>Fs2</li>
   <li>Pureconfig</li>
</ol>