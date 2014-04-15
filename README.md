PageRank Implementation
-------------------------

Author: Manuel Medina Gonzalez


# Description
This program implements the pagerank calculation described in the technical report "Searching the Web" (http://ilpubs.stanford.edu:8090/457/).

# Classes

## MMPageRank: 
Main class used as entrance to the program. It takes the main command arguments and makes the necessary calls to execute the program as directed.

## PRMatrix: 
The main structure used in this implementation. It contains a concurrent map of String (the URL of a site) to PRMatrixEntry (described below) in which each 
URL information is stored.

The map is not supposed to be exposed outside the class, and for that reason utility methods were implemented; however, in some methods of other
classes it is necessary to access the entry set of the map.

As the map is concurrent, it can be safely accessed by multiple threads.

## PRMatrixEntry: 
Class that holds information about a URL. It contains the following attributes:

- String url -> A reference to the URL this entry holds information for.
- HashSet<LinkInfo> linksOut; -> A set of the outgoing links from this URL and their transition probabilities (not calculated in this implementation).
- HashSet<String> linksIn -> A set of the incoming links to this URL.
- boolean visited -> See lowindex below
- int index -> See lowindex below
- int lowindex -> index, lowindex and visited are used by the Tarjan algorithm to find strong connected components (SCC). SCC are used to find rank sinks (see below).
- double pageRank -> The URL's pagerank value
- boolean dangling; -> A flag indicating whether this URL is a dangling node (rank leak).

## LinkInfo: 
A class that holds a URL and the transition probability from another URL to it. The transition probability is not used in this implementation.


## PRSettings: 
Holds the settings that will be used when creating the PRMatrix or calculating the pagerank.

## PRMatrixFactory: 
A factory that creates a PRMatrix. In fact, this is the only way a PRMatrix can be created.

The factory receives a filename and reads the URLs from there; then, when creating the PRMatrix, it handles self links and dangling nodes 
as specified in the settings. Once all the URLs have been read, it checks for rank sinks in the PRMatrix. Last, it sets the initial 
pagerank values to 1/N.

## PRMatrixPolicy: 
An enum defining policies to follow when handling dangling nodes and self links.

Possible values:
* KEEP
* IGNORE

The meaning of each value varies with the handled cases. See the properties file definition before.

## MalformedEntryException: 
An exception raised when a line in the file containing the links is not in the correct format (2 strings per line).

## Pair: 
A class implementing a 2-tuple. It is used when creating the PRMatrix.

## Tarjan: 
A simple implementation of the Tarjan algorithm in order to find strong connected components. See below for information about rank sinks.

 
The calculation of the pagerank is delegated to classes implementing the PageRankCalculator interface. Here, I created one, but others can be created and added
as necessary:

## DecayFactorPageRank: 
As it name implies, it calculates the pagerank of the URLs in the PRMatrix by using a decay factor in order to deal with rank sinks.
It defines an ExecutorServices with a default number of threads and delegates the main calculation to workers of TemporaryPageRankCalculator,
and then checks the error by using an instance of ErrorComputer combined with a Future<Double>.
It loops until the pagerank values converge or the maximum number of iterations has been reached.

## TemporaryPageRankCalculator: 
Performs the calculation of a URL's pagerank by consulting the values in the PRMatrix using the formula:

**r(i) = decay_factor * Sum(r(j)/N(j)) + (1 - decay_factor) / m**

Where:

- r(i): This URL's pagerank
- r(j): Pagerank of a URL j that contains a link to the page i.
- N(j): Number of outgoing links from the page j
  The Sum adds each of the previous 2 values for each page that contains an outgoing link to the page i.
- decay_factor: A value between 0 and 1 used to handle rank sinks.
- m = Total number of URL in the PRMatrix

## ErrorComputer: 
Once all the pageranks of a PRMatrix are calculated, it compares them with the previous pagerank and calculates an error rate (the L1 Norm of the
difference between the previous and the current values).

# Rank leaks
Rank leaks (here called "dangling nodes") will always be added to the PRMatrix, but they are handled as explained below in the "Properties file" section.

# Rank sinks
Rank sinks are detected as follows:

1. The tarjan algorithm is applied to the PRMatrix to find strong connected components (SCC).
2. For each SCC:

    - For each url in the SCC, check if it contains a link to any URL not included in the SCC. 
        If found, this is not a rank sink.

    - If no outgoing links to URLs outside the SCC are found, this SCC is a rank sink.

**Rank leaks are special cases of rank sinks, and thus, they will be detected as such.**

**For more information about the Tarjan algorithm, see the Wikipedia entry:**
  http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm


# Properties file
The behavior of the program regarding some concepts can be changed with a properties file, defined as "prMatrix.props" for this implementation.
The properties file must contain the following entries:

1. self.links.policy
2. dangling.nodes.policy
3. error.rate

Possible values are as follows:

1) self.links.policy ->

     * keep: The link will be added as outgoing from, and incoming to, the URL.
     * ignore: The link won't be added as outgoing from, nor incoming to, the URL.


2) dangling.nodes.policy -> 

     * keep: A link to each other URL will be added to the outgoing links of dangling nodes.
           When calculating pagerank, all URLs will be considered to have an incoming link from dangling nodes, thus
           affecting the result.

           The policy for self links is also applied here.

     * ignore: No outgoing links will be added to dangling nodes.
             They won't conttribute to the pagerank calculation of other URLs.

    Example: Consider the following network with 3 URLs:

    1 2
    2 1
    2 3

    Here, 3 is a dangling node. Depending on the settings, the final pagerank will vary:

    Case 1:
    self.links.policy = ignore
    dangling.nodes.policy = keep
    
    Final pageranks:
    1 => 0.33321368565784804
    2 => 0.428416335394704
    3 => 0.2380128307091765

    Case 2:
    self.links.policy = keep
    dangling.nodes.policy = keep
     
    Final pageranks:
    1 => 0.3042573303760206
    2 => 0.39117579750850107
    3 => 0.3042573303760206

    Case 3:
    dangling.nodes.policy = ignore
    (self.links.policy does not matter)

    Final pageranks:
    1 => 0.1372618008572723
    2 => 0.17647610735248448
    3 => 0.1372618008572723
    
    All calculations were done with the current program.

3) error.rate ->

     The error rate to check when calculating pagerank values. If not specified, a default value of 0.8 will be used.


# Duplicate links
Ignored. The outgoing links are contained in a set, which effectively prevents adding the same element more than once.


# Running the program
As indicated, the program runs either in "check" mode or in "run" mode.

The suggested way to execute the program is with maven, using the following command from the directory containing the pom.xml:

     Check mode: mvn exec:java -Dexec:mainClass="org.mmg.pagerank.MMPageRank" -Dexec:args="check <filename>"

     Run mode: mvn exec:java -Dexec:mainClass="org.mmg.pagerank.MMPageRank" -Dexec:args="run <filename> <max number of iterations> <decay factor>"

The program outputs information at the start and during each iteration. The last series of values are the final pagerank values.

The information is outputted at the same time to the screen and to the file logs/pagerank.logs

# Test files
Some files are provided to test the program (links.txt ~ links6.txt). They have been formatted as follows:

- URLA URLB

2 URLs per line, separated by a space. In the example above, this means that URLA has a link to URLB.

Note that you can't put more than an outgoing link in a single line. Thus, if you want to add a link from URLA to URLC, you need to add another line:

       URLA URLB
       URLA URLC

The program will detect if a line is not correctly formatted and will throw a MalformedEntryException. 