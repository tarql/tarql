\# sparql\_with\_hobbits external tests



These test files were taken from the sparql\_with\_hobbits project by Erika Duan:



https://github.com/erikaduan/sparql\_with\_hobbits



They are included here as external TARQL test cases for regression testing.



\## Structure



\- `tarql/` - TARQL query files from the original project's `scripts` directory

\- `csv/` - CSV input files from the original project's `data/raw\_data` directory

\- `ttl-expected/` - existing RDF/Turtle output files from the original project's `data/clean\_data` directory



The original file names and contents are preserved as closely as possible.



The expected RDF files use the `.rdf` extension but contain Turtle RDF output. They are stored as UTF-16 encoded files in the original project.



Both the structured and unstructured farming transformations are used as regression tests and their generated RDF graphs are compared semantically with the original expected RDF graphs.

