# PDF Validator

This project has started as part of a school assignment. With this software, the content of forms embedded inside a PDF can be validated through a variety of predefined and custom validations.

## Getting Started

1. Clone the repository
1. Run the command `mvn clean compile assembly:single` to generate a runnable JAR file with all of the necessary dependencies included
1. Create a [configuration file](./CONFIG_GUIDE) that all files will be matched against. This file must be named `config.json`
1. To validate one or more files, run the following command:

```bash
$ java -jar target/pdf-validator-{installed.version}-jar-with-dependencies.jar [file1] (file2...)
{
  "reports": [ ... ],
  "warnings": [ ... ],
  "errors": [ ... ]
}
```
