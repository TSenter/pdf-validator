# Table Example

Imagine a table listing purchase orders with 10 rows and 3 fields in each row: 'Order Number', 'Date', and 'Total Cost'. An example configuration for this table is shown below:

```json
{
  "tables": [
    {
      "name": "purchase_orders",
      "range": {
        "end": 10
      },
      "structure": [
        {
          "name": "OrderNumber{{row}}",
          "type": "text",
          "validations": {
            "regex": "[0-9]{8}"
          }
        },
        {
          "name": "OrderDate{{row}}",
          "type": "text",
          "validations": {
            "regex": "^(?:1[0-2]|0?[1-9])\\/(?:3[0-1]|[1-2][0-9]|0?[0-9])\\/(?:20[0-9]{2})$"
          }
        },
        {
          "name": "OrderCost{{row}}",
          "type": "text",
          "validations": {
            "regex": "^\$?[0-9]*(?:\.[0-9]{2})?$"
          }
        }
      ]
    }
  ]
}
```

In the above example, a table called `purchase_orders` was created that has 10 rows (default start of 1 through end of 10). This table has three fields, all with differing validations. All of these fields have a `{{row}}` in the name - this variable will be replaced with the number of the field being validated, and allows for 10 instances of each field to be created while only needing to write one field defintion.

The `OrderNumber` field must match a basic regular expression requiring an 8-digit number. The `OrderDate` field must match a more complicated regular expression that represents a date in `MM/DD/YYYY` format. Finally, the `OrderCost` field must match a regular expression requiring a monetary amount in the form `$###.##`.

This definition may work fine if the entire table (i.e. all rows) should be filled out, but what if the user can fill out only as many rows as they need? This is where the power of dependent keys comes into play. The following configuration adds the ability to only check fields if the first field in the table is complete.

```json
{
  "tables": [
    {
      ...
      "structure": [
        {
          "name": "OrderNumber{{row}}",
          "type": "text",
          "validations": {
            "regex": "[0-9]{8}"
          }
        },
        {
          "name": "OrderDate{{row}}",
          "type": "text",
          "validations": {
            "required": {
              "value": "yes",
              "dependentKeys": "OrderNumber{{row}}"
            },
            "regex": "^(?:1[0-2]|0?[1-9])\\/(?:3[0-1]|[1-2][0-9]|0?[0-9])\\/(?:20[0-9]{2})$"
          }
        },
        {
          "name": "OrderCost{{row}}",
          "type": "text",
          "validations": {
            "required": {
              "value": "yes",
              "dependentKeys": "OrderNumber{{row}}"
            },
            "regex": "^\$?[0-9]*(?:\.[0-9]{2})?$"
          }
        }
      ]
    }
  ]
}
```

By adding a dependency on the first field in the row, the other fields will not be validated if the `OrderNumber` field is empty. While this is closer to what we want, each `OrderNumber` field is still required, even if some of them should be left empty. That can be fixed by adding the `required: no` validation to the field:

```json
{
  "tables": [
    {
      ...
      "structure": [
        {
          "name": "OrderNumber{{row}}",
          "type": "text",
          "validations": {
            "required": "no",
            "regex": "[0-9]{8}"
          }
        },
        ...
      ]
    }
  ]
}
```

What this means now is that the first field in each row is not required, but will still have to pass the regular expression test if it is filled out. This way the user can complete as many rows as they would like, and only the rows they complete will be checked.
