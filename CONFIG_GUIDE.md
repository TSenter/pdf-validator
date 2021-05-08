# PDF Validator Configuration Guide

## Fields

The `fields` property is an array of field objects, described in-depth below. If a field is found in a given PDF but does not have an entry in the `fields` property, a warning will be thrown if the `preferences.warnOnUnknownField` property is set. Regardless of the `preferences.warnOnUnknownField` setting, unknown fields will be skipped while validation occurs. See the [Preferences](#preferences) section for more information.  

Each field specified in the `fields` property must have the following properties defined:  
| Property |    Type    |          Accepted Values          |                   Description                   |
| :------: | :--------: | :-------------------------------: | :---------------------------------------------: |
| `name`   | String     | `*`                               | The name corresponding to the field in the file |
| `type`   | String     | `button\|text\|choice\|signature` | The type of field                               |

The following properties are optional and allow more control over the content allowed in a particular field:  
| Property      |    Type    |          Accepted Values          |                   Description                     |
| :-----------: | :--------: | :-------------------------------: | :-----------------------------------------------: |
| `validations` | Object     | `*`                               | See the [validations](#field-validations) section |

### Field Validations

In order to control the input allowed inside a particular field, multiple validations can be created. Each validation can have any combination of the following properties. The simplest way to define a validation is simply the property name as the key, and the value of the validation as the value. If additional information needs to be passed, use the property name as the key and create an object as the value.

If the validation value is an object, the object must contain the field `value` for the validation value. It may also contain a field `message` holding the custom message to be logged if the validation fails.

#### Property - `required`

|                |                                 |
| -------------- | ------------------------------- |
| Name           | `required`                      |
| Default Value  | `no`                            |
| Allowed Values | `yes\|true\|no\|false\|warning` |

This property specifies whether or not this field is required. A value of `yes` (or `true`) will mark the field as required, meaning an error will be thrown if the field is "empty". A value of `no` (or `false`) will mark the field as not required, meaning that this portion of the validation will always pass. A value of `warning` will throw a warning if the field is "empty", but the validation will otherwise pass.

#### Property - `format`

|                |                           |
| -------------- | ------------------------- |
| Name           | `format`                  |
| Default Value  | `null`                    |
| Allowed Values | `email\|integer\|decimal` |

This property requires the field to fit a particular format. The `decimal` format will allow decimal or integer values, whereas the `integer` format restricts the input to only whole numbers. The `email` format requires that the field holds a valid email address. Please note this does not validate that the email address exists, merely that the data provided matches the format required.

Both numeric types can also specify `minimum`, `maximum`, and `equals` options to enforce restrictions on the values of integers that can be entered.

#### Property - `allowList`

|                |             |
| -------------- | ----------- |
| Name           | `allowList` |
| Default Value  | `null`      |
| Allowed Values | Array       |

This property requires the field to match an item in the given list. The `value` property must be either an array or a string of comma-separated values. If the value is not matched to a value in the allow list, the validation fails.

The `caseSensitive` option (defaults to true) can be turned off to allow strings that match when ignoring case, eg. an allow list of `"A,B,C,D"` would match `a` if the `caseSensitive` option is `false` and thus the validation would pass.

#### Property - `disallowList`

|                |                |
| -------------- | -------------- |
| Name           | `disallowList` |
| Default Value  | `null`         |
| Allowed Values | String, Array  |

This property requires the field to *not* match an item in the given list. The `value` property must be either an array or a string of comma-separated values. If the value is matched to a value in the disallow list, the validation fails.

The `caseSensitive` option (defaults to true) can be turned off to allow strings that match when ignoring case, eg. a disallow list of `"A,B,C,D"` would match `a` if the `caseSensitive` option is `false` and thus the validation would fail.

#### Property - `regex`

|                |         |
| -------------- | ------- |
| Name           | `regex` |
| Default Value  | `null`  |
| Allowed Values | `*`     |

This property requires the field to match a given regular expression. An invalid expression will throw an error before the form begins processing, since the configuration files are loaded before any processing occurs.

#### Property - `custom`

|                |          |
| -------------- | -------- |
| Name           | `custom` |
| Default Value  | `null`   |
| Allowed Values | `*`      |

The bulk of the power with the validator is the ability to create custom validation rules. To create a custom rule, create a class that extends `com.tylersenter.pdf.validations.FieldValidation`. This will require the following function to be overriden:  

`boolean validate(FormField, Map<String, FormField>, Report, Preferences)`

The first argument, `FormField`, contains information relevant to the particular field being validated.

The second argument is a mapping of all form fields, where the key is the field's name. This can be useful for creating fields dependent on other fields, which is currently not supported natively.

The third argument is a report object that tracks the status of the form as a whole. All information related to validation success, warnings and errors should be logged in the report, regardless of the reporting settings - that is handled later.

The fourth and final argument is a preferences object that can be used to access default settings.

The return value of this function is whether or not further validations should be applied to this particular field. If `true`, the next validation (if it exists) will be applied. Returning `false` prevents any futher actions being taken against a particular field, and can be used to prevent multiple error or warning messages being generated for the same field.

To apply your custom rule to a field, add the `custom` field to the validations object on a field with the value of the full classname, eg. `com.tylersenter.pdf.validations.FormatValidation`.

## Preferences

The `preferences` property is a map of optional configuration options that affect the entire validation system. The following sections list and document these properties, all of which are optional.

### Default Valid Message

|                |                                     |
| -------------- | ------------------------------------|
| Name           | `validMessage`                      |
| Default Value  | `The field {{fieldName}} is valid.` |
| Allowed Values | `*`                                 |

If a field passes its validation, a message will be generated based on that fields individual properties. If no message is set, this message will be printed. A few variables are supported to allow for meaningful, specific messages. The supported variables are listed below.

| Name             | Value                                                 |
| :--------------: | :---------------------------------------------------: |
| `{{fieldName}}`  | The fully qualified name of the field in question     |
| `{{fieldValue}}` | The current value of the field that failed validation |

### Default Invalid Message

|                |                                                          |
| -------------- | -------------------------------------------------------- |
| Name           | `invalidMessage`                                         |
| Default Value  | `The value {{fieldValue}} for {{fieldName}} is invalid.` |
| Allowed Values | `*`                                                      |

If a field fails its validation, a message will be generated based on that field's individual properties. If no message is set, this message will be printed. A few variables are supported to allow for meaningful, specific messages. The supported variables are listed above in the [Default Valid Message](#default-valid-message) section.

### Reporting Type

|                |                                           |
| -------------- | ----------------------------------------- |
| Name           | `reportingType`                           |
| Default Value  | `detailed`                                |
| Allowed Values | `none\|exit_code\|compact\|detailed\|all` |  

Regardless of the reporting type defined, internal errors such as configuration errors, form errors or other runtime errors will always be reported to `stderr`. The only exception is if the [silent](#suppress-all-output) option is set to `true`.

#### Type - `none`

No output will be reported. The program will complete with an exit code of `0` unless an error occurs.

#### Type - `exit_code`

A valid form will print nothing, and will exit with code `0`. An invalid form will exit with a non-zero value. Nothing will be written to `stdout`.

#### Type - `compact`

For every invalid form field, the name (or ID, if specified) of the field will be printed on its own line to `stdout`. A valid form will print nothing, and will exit with code `0`. An invalid form will exit with a non-zero value.

#### Type - `detailed`

For every invalid form field, a descriptive message will be printed on a new line. This message will either be the default message supplied in the [preferences](#default-message), a custom message defined for that particular field, or a message generated by the program. A valid form will print nothing, and will exit with code `0`.

#### Type - `all`

For every invalid form field follows the same rules as the [detailed](#type---detailed) level of reporting. Every valid for field will print a report of the [valid message](#valid-message), or the custom message (if defined).

### Suppress All Output

|                |               |
| -------------- | ------------- |
| Name           | `silent`      |
| Default Value  | `false`       |
| Allowed Values | `true\|false` |

A value of `true` for the Silent flag will suppress all output to `stdout` and `stderr`, including internal errors, regardless of the value of [Reporting Type](#reporting-type). Any fatal errors encountered will simply kill the program with a non-zero exit code.

### Warn On Unknown Field

|                |                      |
| -------------- | -------------------- |
| Name           | `warnOnUnknownField` |
| Default Value  | `true`               |
| Allowed Values | `true\|false`        |

When a PDF is being validated, all fields must be explicitly defined in the configuration file. If a field is found that is not registered, a warning will be thrown if this property is `true`.
