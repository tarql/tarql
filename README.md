# Tarql: SPARQL for Tables

Tarql is a command-line tool for converting CSV files to RDF using SPARQL 1.1 syntax. It's written in Java and based on Apache ARQ.


## Introduction

In Tarql, the following SPARQL query:

    SELECT *
    FROM <file:table.csv>
    WHERE {
      ...
    }

is equivalent to executing the following over an empty graph:

    SELECT *
    WHERE {
      VALUES (...) { ... }
      ...
    }

In other words, the CSV file's contents are input into the query as a table of bindings. This allows manipulation of CSV data using SPARQL syntax, and in particular the generation of RDF using `CONSTRUCT` queries. See below for more examples.


## Command line

* `tarql --help`
* `tarql my_mapping.sparql input1.csv input2.csv
* `tarql my_mapping.sparql` (if CSV file is defined in `FROM` clause in query)


## Details

Column headings are ?a, ?b, ?c and so on. If the CSV file already contains column headings, then they will show up in the data as a binding, and probably should be skipped by appending OFFSET 1 to the query.

The input CSV file can be specified using FROM or on the command line.


## Building

* Maven
* `mvn package appassembler:assembly` creates executable scripts for Windows and Unix in `/target/appassembler/bin/tarql`.


## Design patterns

Delete header row:

    SELECT ...
    WHERE { ... }
    OFFSET 1

Skip bad rows:

    SELECT ...
    WHERE { FILTER (BOUND(?d)) }

Compute additional columns:

    SELECT ...
    WHERE {
      BIND (URI(CONCAT('http://example.com/ns#', ?b)) AS ?uri)
      BIND (STRLANG(?a, 'en') AS ?with_language_tag)
    }

CONSTRUCT an RDF graph:

    CONSTRUCT {
      ?uri a ex:Organization;
          ex:name ?name;
          ex:CIK ?b;
          ex:LEI ?c;
          ex:ticker ?d;
    }
    FROM <file:companies.csv>
    WHERE {
      BIND (URI(CONCAT('companies/', ?d)) AS ?uri)
      BIND (STRLANG(?a, "en") AS ?name)
    }
    OFFSET 1


## TODO

* Use first row as column names if OFFSET 1 is specified
* Allow specification of column name behaviour on command line
* Allow multiple CONSTRUCT/FROM/WHERE blocks in one file
* Choice of output format, writing to file, etc.
* Package a proper distribution
* Experiment with input files in TSV, different quoting styles, etc.
* Allow overriding of encoding on command line
* Read CSV from stdin?
* Web service?
* Get this into ARQ!?
