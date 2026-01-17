
# WheelShift Pro — Feature Suggestions (Quick Wins & Minor Enhancements)

This document focuses on **practical, low-risk additions** that deliver high user value with minimal architectural change.

---
## 1) Vehicle Warranty / AMC (Annual Maintenance Contract)
**Goal:** Track warranties and service coverages per vehicle (car or motorcycle) and streamline claims.

### 1.1 Key Capabilities
- Create **Warranty Policies** and attach one or more to a vehicle (e.g., *Dealer Warranty*, *Third‑party Warranty*, *Extended Warranty*, *AMC*).
- Coverage model: component groups (engine/powertrain/electrical/consumables), **claim limits**, **deductibles**, **kilometre** or **time** caps.
- Status lifecycle: *Active → Expiring Soon → Expired → Claimed → Closed*.
- Automated **reminders** before expiry (email/SMS/WhatsApp/in‑app).
- **Claims**: log issue, attach photos/invoices, track costs, mark outcomes, and link to **Financial Transactions**.
- **Transferability** on sale (optional) and record of the new client.

### 1.2 Minimal Schema (illustrative)
- `warranty_policies(id, name, provider, coverage_json, max_claims, deductible_amount, duration_months, km_limit, terms_url)`
- `vehicle_warranties(id, vehicle_type, car_id, motorcycle_id, policy_id, start_date, end_date, kms_start, kms_end, status)`
- `warranty_claims(id, vehicle_warranty_id, reported_at, description, status, amount_claimed, amount_approved, documents_url)`

### 1.3 Acceptance Criteria
- A vehicle can have **0..n** active warranties/AMCs.
- Dashboards surface **expiring in ≤30 days**.
- On sale completion, system prompts whether to **transfer** warranty to the buyer and updates client ownership history.

---
## 2) Multi‑Image & Media Gallery per Vehicle
**Goal:** Rich, trustworthy listings with fast load times.

### 2.1 Image Management
- Upload **multiple images** per vehicle with **drag‑and‑drop**, **re-ordering**, **cover image** flag, and **captions/tags** (exterior/interior/odometer/tyres/documents).
- Generate **thumbnails** and **WebP** derivatives; strip EXIF; optional **watermark** with dealership logo.
- Store on cloud (e.g., S3/Azure Blob) with **pre‑signed URLs**, CDN, and **lazy‑loading** in UI.
- Validation: max file size, allowed formats (JPEG/PNG/WebP), count limits per vehicle, image dimension checks.

### 2.2 Video Support (optional, small)
- Per‑vehicle **walkaround video** or short clips; store URL + duration + thumbnail.
- Transcode to web‑friendly formats; show **play** overlay in gallery.

### 2.3 Minimal Schema (illustrative)
- `vehicle_media(id, vehicle_type, car_id, motorcycle_id, kind='IMAGE|VIDEO', url, thumb_url, sort_order, caption, tag, is_cover)`

### 2.4 Acceptance Criteria
- Reorder persists; first image defaults to cover unless explicitly set.
- API returns optimized URLs & metadata; UI consumes **thumb → full** progressive loading.

---
## 3) Attachments & Paperwork
- Upload and categorize **RC**, **Insurance**, **PUC**, **Loan NOC**, **Warranty/AMC documents**, **Service bills**.
- Extract expiry dates (simple OCR optional) → **auto‑reminders**.
- Link expenses from bills to **Financial Transactions**.

---
## 4) Test‑Drive Scheduling (Lightweight)
- Create a **test‑drive appointment** from an inquiry: capture **DL number**, **OTP consent**, **in/out odometer**, **fuel level**, and **vehicle condition** notes.
- Record damage, photos, and **responsible employee**.

---
## 5) Quotes & Proforma Invoices
- Generate **quotation/P.I.** with dealership branding, **GST** breakdown, validity date, terms.
- Share via **Email/WhatsApp**; track whether the client viewed/downloaded.

---
## 6) RTO / Ownership Transfer Tracker (Lean)
- Checklist of forms (Form 29/30 etc.), fee receipts, and status updates.
- Target dates and blockers surfaced on **Sales** and **Tasks** dashboards.

---
## 7) Reconditioning (Recon) Mini‑Workflow
- Track **jobs** (wash, denting/painting, tyres, battery), **parts** & **labour** costs; mark ready‑for‑sale.
- Show **recon cost** roll‑up on vehicle P&L.

---
## 8) Price Negotiation & Approvals
- Record **offer history** with timestamps, employee involved, and reason codes.
- Optional **approval rules** (e.g., discount > ₹25,000 requires Admin approval).

---
## 9) Lead Quality & WhatsApp Enhancements
- Capture **UTM/source** on inquiries; auto‑tag **Walk‑in/Phone/Whatsapp/Website**.
- One‑click **WhatsApp deep link** with pre‑filled template (pulls vehicle title, price, cover image link).

---
## 10) Custom Fields, Tags, and Saved Views
- Admin‑defined **custom fields** on Vehicles, Clients, Sales.
- **Tags/labels** (e.g., *Urgent*, *Premium*, *Trade‑in*).
- **Saved filters** (e.g., “High‑margin SUVs”, “Bikes < 15k km”).

---
## 11) Inventory Convenience Additions
- Track **number of keys**, **tyre condition**, **battery age**, **color**, **variant options**.
- **EV specifics**: SoC at intake, **battery health %, warranty remainder**.
- **QR code** for each vehicle → quick open in mobile app for photos/notes.

---
## 12) Bulk Operations & Import/Export
- **Bulk image upload** per vehicle with drag‑select.
- **CSV import/export** for vehicles/clients with field mapping and validation report.

---
## 13) Mobile UX Tweaks (Next.js + Tailwind + shadcn)
- Camera capture with **image compression** client‑side.
- **Document scanner** mode (auto‑crop/contrast) for paperwork photos.
- Sticky action bar: *Call, WhatsApp, Quote, Reserve*.

---
## 14) Notifications (Quick Additions)
- Templates for **warranty expiring**, **PUC expiring**, **RTO step pending**, **test‑drive reminder**.
- Per‑channel quiet hours and digest mode (already supported by your notification framework).

---
## 15) Minimal API Additions (illustrative)
```http
POST   /api/v1/vehicles/{type}/{id}/warranties
GET    /api/v1/vehicles/{type}/{id}/warranties
POST   /api/v1/warranties/{id}/claims
POST   /api/v1/vehicles/{type}/{id}/media
PATCH  /api/v1/vehicles/{type}/{id}/media/{mediaId}   # reorder, set cover, caption
DELETE /api/v1/vehicles/{type}/{id}/media/{mediaId}
```

---
## Rollout Order (2–3 sprints)
1. **Multi‑image gallery** (backend + UI) → visible impact on listings.
2. **Warranty/AMC** basics + reminders.
3. **Attachments** + expiry extraction → reminders.
4. **Quotes/P.I.** and WhatsApp share.
5. **Test‑drive** + recon mini‑workflow.
