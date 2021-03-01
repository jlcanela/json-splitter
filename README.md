# JSON Splitter

This project allow to split a json file in multiple files selecting the output using a regex.

Usage:
```
sbt "run split-json src/test/resources/sample.json out"
```

To verify: 
```
cat out/a.json 
{ "nodeType": "a"}
```

Install awslocal
```
pip install awscli-local
```

or use 
```
aws --endpoint-url=http://localhost:4566 
```

```
awslocal s3 mb s3://mybucket
awslocal s3 cp src/test/resources/sample.json s3://mybucket/in/sample.json
```
