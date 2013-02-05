# Tarql: SPARQL for Tables

Tarql is a command-line tool for converting CSV files to RDF using SPARQL 1.1 syntax.

# Introduction

In Tarql, the following SPARQL query:

    SELECT *
    FROM <file.csv>
    WHERE {
      ...
    }

is equivalent to executing the following over an empty graph:

    SELECT *
    WHERE {
      VALUES (...) { ... }
      ...
    }

In other words, the CSV file's contents are input into the query as a table of bindings.

# Details

Column headings are ?a, ?b, ?c and so on. If the CSV file already contains column headings, then they will show up in the data as a binding, and probably should be skipped by appending OFFSET 1 to the query.

The input CSV file can be specified using FROM or on the command line.

# Building

* Maven
* `mvn package appassembler:assembly` creates an executable script in `/target/appassembler/bin/tarql`.

# Design patterns

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
    FROM <companies.csv>
    WHERE {
      BIND (URI(CONCAT('companies/', ?d)) AS ?uri)
      BIND (STRLANG(?a, "en") AS ?name)
    }
    OFFSET 1

# TODO

* Better command line tool
* Use first row as column names if OFFSET 1 is specified
* Allow specification of column name behaviour on command line
* Allow multiple CONSTRUCT/FROM/WHERE blocks in one file
* Package a proper distribution
* Experiment with input files in TSV, different quoting styles, etc.
* Allow overriding of encoding on command line
* Web service?
* Get this into ARQ!?
