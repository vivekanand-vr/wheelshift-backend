## Summary of changes in Entities

| Entity | Single Image | Gallery/Multiple Images | Documents | Report/Attachments |
| --- | --- | --- | --- | --- |
| **CarModel** | ✅ modelImageId | - | - | - |
| **MotorcycleModel** | ✅ modelImageId | - | - | - |
| **Car** | ✅ primaryImageId | ✅ galleryImageIds | ✅ documentFileIds | - |
| **Motorcycle** | ✅ primaryImageId | ✅ galleryImageIds | ✅ documentFileIds | - |
| **CarInspection** | - | ✅ inspectionImageIds | - | ✅ inspectionReportFileId |
| **MotorcycleInspection** | - | ✅ inspectionImageIds | - | ✅ inspectionReportFileId |
| **Employee** | ✅ profileImageId | - | - | - |
| **Client** | ✅ profileImageId | - | ✅ documentFileIds | - |
| **StorageLocation** | ✅ locationImageId | - | - | - |
| **Sale** | - | - | ✅ saleDocumentIds | - |
| **FinancialTransaction** | - | - | ✅ transactionFileIds | - |
| **Event** | - | - | - | ✅ attachmentFileIds |
| **Inquiry** | - | - | - | ✅ attachmentFileIds |
| **Reservation** | - | - | ✅ reservationDocumentIds | - |
| **Task** | - | - | - | ✅ attachmentFileIds |