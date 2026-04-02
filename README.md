# Stock Entry Android App

Simple Android app to manage stock entries and upload them to Google Spreadsheet.

## Features

1. **Create new entry** with product type (`wood` or `nariyal`), rate, and quantity.
2. **Delete previous entry** from local storage.
3. **Show last 10 entries** in the app.
4. **Sync/upload** unsynced entries to Google Spreadsheet via Google Apps Script webhook.

## Tech stack

- Kotlin + Jetpack Compose UI
- Room database (local storage)
- OkHttp for HTTP upload

## Google Spreadsheet setup

Create a Google Apps Script attached to your spreadsheet and deploy as Web App.

```javascript
function doPost(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName('Sheet1');
  var body = JSON.parse(e.postData.contents);
  var entries = body.entries || [];

  entries.forEach(function(item) {
    sheet.appendRow([
      new Date(item.createdAt),
      item.productType,
      item.rate,
      item.quantity,
      item.id
    ]);
  });

  return ContentService
    .createTextOutput(JSON.stringify({ ok: true, count: entries.length }))
    .setMimeType(ContentService.MimeType.JSON);
}
```

Then set your webhook URL in `~/.gradle/gradle.properties` (or project `gradle.properties`):

```properties
SHEETS_WEBHOOK_URL=https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec
```

## Build

Open in Android Studio and run on emulator/device.
