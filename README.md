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

In other words, the CSV file's contents are input into the query as a table of bindings. This allows manipulation of CSV data using the full power of SPARQL 1.1 syntax, and in particular the generation of RDF using `CONSTRUCT` queries. See below for more examples.


## Building

Tarql uses Maven. To create executable scripts for Windows and Unix in `/target/appassembler/bin/tarql`:

    mvn package appassembler:assemble


## Command line

* `tarql --help`
* `tarql my_mapping.sparql input1.csv input2.csv`
* `tarql my_mapping.sparql` (if CSV file is defined in `FROM` clause in query)


## Details

The input CSV file can be specified using `FROM` or on the command line. Use `FROM <file:filename.csv>` to load a file from the current directory.

Tarql auto-detects the input CSV file's encoding. This is a guess and may fail sometimes.

By default, variable `?a` will contain values from the first column, `?b` from the second, and so on.

If the CSV file already has **column headings** in the first row, Tarql can be instructed to use the column headings as variable names by appending `OFFSET 1` to the SPARQL query. In standard SPARQL, this has the effect of skipping the first row. Tarql will take it as a sign to use the first row as variable names. In the variable names, spaces will be replaced with underscores. If any column doesn't contain a valid variable name, then the default name (`?a`, `?b`, etc.) will be used for the column.

All-empty rows are skipped automatically.


## Design patterns

### Use column headings in first row as variable names

    SELECT ?First_name ?Last_name ?Phone_number
    WHERE { ... }
    OFFSET 1

The `OFFSET 1` indicates that the first row is to be used to provide variable names.

### Skip bad rows

    SELECT ...
    WHERE { FILTER (BOUND(?d)) }

### Compute additional columns

    SELECT ...
    WHERE {
      BIND (URI(CONCAT('http://example.com/ns#', ?b)) AS ?uri)
      BIND (STRLANG(?a, 'en') AS ?with_language_tag)
    }

### CONSTRUCT an RDF graph

    CONSTRUCT {
      ?URI a ex:Organization;
          ex:name ?NameWithLang;
          ex:CIK ?CIK;
          ex:LEI ?LEI;
          ex:ticker ?Stock_ticker;
    }
    FROM <file:companies.csv>
    WHERE {
      BIND (URI(CONCAT('companies/', ?Stock_ticker)) AS ?URI)
      BIND (STRLANG(?Name, "en") AS ?NameWithLang)
    }
    OFFSET 1


## TODO

* Allow specification of column name behaviour on command line
* Allow multiple CONSTRUCT/FROM/WHERE blocks in one file
* Choice of output format, writing to file, etc.
* Package a proper distribution
* Experiment with input files in TSV, different quoting styles, etc.
* Allow overriding of encoding on command line
* Find a way of streaming the CSV parser output into ARQ
* Handle tables where repeated values within a column are omitted, maybe tarql:valueFromPreviousRow(?a))
* Support Excel files?
* Read CSV from stdin?
* Web service?
* Get this into ARQ!?
