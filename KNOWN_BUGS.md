# Known Bugs

This document serves to catalog all bugs known to the developers, as well as the status towards a solution.

## v1.0-SNAPSHOT

### Multiple reports on the same field for different validations

**Status:** None

If a field has multiple validations that pass, the same report (often the default message) will be added to the Report object. This is especially noticable when a field both is required and has a secondary validation.
