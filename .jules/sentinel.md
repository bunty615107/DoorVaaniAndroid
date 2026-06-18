## 2023-10-27 - [URI Injection Prevention in Intents]
**Vulnerability:** Unsanitized user inputs appended to `Uri.parse("tel:...")`.
**Learning:** Raw input strings containing special characters like `#` can cause URI truncation or injection, as `#` is a fragment separator. This is particularly problematic for phone numbers that may include USSD codes or extensions.
**Prevention:** Always use `Uri.encode()` when constructing URIs from untrusted or user-provided input, especially for intents like `ACTION_CALL`.