## Summary of changes in Entities

| Entity | Single Image | Gallery/Multiple Images | Documents | Report/Attachments | Status |
| --- | --- | --- | --- | --- | --- |
| **CarModel** | ✅ modelImageId | - | - | - | ✅ **UPDATED** (Entity + DTOs + Mapper) |
| **MotorcycleModel** | ✅ modelImageId | - | - | - | ✅ **UPDATED** (Entity + DTOs + Mapper) |
| **Car** | ✅ primaryImageId | ✅ galleryImageIds | ✅ documentFileIds | - | ✅ **UPDATED** (Entity + DTOs + Mapper) |
| **Motorcycle** | ✅ primaryImageId | ✅ galleryImageIds | ✅ documentFileIds | - | ✅ **UPDATED** (Entity + DTOs + Mapper) |
| **CarInspection** | - | ✅ inspectionImageIds | - | ✅ inspectionReportFileId | ✅ **COMPLETE** (Full Stack: Entity + DTOs + Mapper + Service + Controller) |
| **MotorcycleInspection** | - | ✅ inspectionImageIds | - | ✅ inspectionReportFileId | ✅ **COMPLETE** (Full Stack: Entity + DTOs + Mapper + Service + Controller) |
| **Employee** | ✅ profileImageId | - | - | - | ⏳ Pending |
| **Client** | ✅ profileImageId | - | ✅ documentFileIds | - | ⏳ Pending |
| **StorageLocation** | ✅ locationImageId | - | - | - | ⏳ Pending |
| **Sale** | - | - | ✅ saleDocumentIds | - | ⏳ Pending |
| **FinancialTransaction** | - | - | ✅ transactionFileIds | - | ⏳ Pending |
| **Event** | - | - | - | ✅ attachmentFileIds | ✅ **UPDATED** (Entity + DTOs + Mapper) |
| **Inquiry** | - | - | - | ✅ attachmentFileIds | ✅ **UPDATED** (Entity + DTOs + Mapper) |
| **Reservation** | - | - | ✅ reservationDocumentIds | - | ⏳ Pending |
| **Task** | - | - | - | ✅ attachmentFileIds | ⏳ Pending |