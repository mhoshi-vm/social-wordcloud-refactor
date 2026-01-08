```
query GetMessageById {
  socialMessage(id: "202847a4-edae-31ff-86b7-278fea2e782d") {
    id
    text
    lang
    createDateTime
  }
}
```

```
mutation delete{
  deleteCustomBatch(createDateTimes: ["2025-12-17T08:05:53"],ids: ["202847a4-edae-31ff-86b7-278fea2e782d"])
}
```