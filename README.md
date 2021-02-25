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
