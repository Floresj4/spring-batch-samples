# BeanWrapper JDBC Template

Using the Spring provided `BeanWrapperFieldSetMapper` and `JdbcBatchItemWriterBuilder` to map record fields to a domain model and insert into database.  `JdbcTemplate` is used post-processing to verify records written to the database.

### Execution

`java -jar bean-wrapper-jdbc-template.jar inputFile=../input-files/simple-names.csv`

