# Service triggered batch application

`curl -H 'content-type: application/json' -X POST -d '{"name": "serviceTriggerJob", "jobParameters": {"inputFile": "../input-files/employees-w-workitems.json"}}'`

The name property use to find an launch the job must match the method name used to `@Bean Job`.