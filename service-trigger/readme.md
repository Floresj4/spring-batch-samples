# Service Triggered

Launch a Spring Batch application from a HTTP `post` request.

### Curl

Using curl to request the application launch

`curl -H 'content-type: application/json' -X POST -d '{"name": "serviceTriggerJob", "jobParameters": {"inputFile": "../input-files/employees-w-workitems.json"}}'`

#### Curl -d (--data)

POST data requires the `name` of the batch job to trigger and a collection of `jobParameters`.

##### JobParameters

|Parameter      |   Description         |
|---------------|-----------------------|
| InputFile     |  The remote resource URI to process.        |

##### Name
The name used to find and launch a job.  This name must match the method name used to define the `@Bean Job`.