# Custom Record Parsing

This module contains a single project that uses a custom `LineTokenizer` and `FieldSetMapper` to read CSV files.  This project contains two profiles for running CSV files with two separate scenarios &ndash; a single record CSV and a multiple record CSV formats.

The implementation of each tokenizer and mapper is straight and just exercises the use of each interface.  The multiple-record CSV mapper `EmployeeBuilderFieldSetMapper` returns a builder class so the custom file reader can append work items to each employee for an accurate count of employee objects w/ work items when processing items within the configured chunk size.

###  Executions

`java -Dspring.profiles.active=single inputFile=./src/main/resources/employees.csv`

Executes the project against the single record file format.

`java -Dspring.profiles.active=multi inputFile=./src/main/resources/employees-w-workitems.csv`

Executes the project against the multi-record file format.