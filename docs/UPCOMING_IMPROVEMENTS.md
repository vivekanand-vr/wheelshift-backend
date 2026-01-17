
# WheelShift Pro — Backend Improvements & Industry Standards (Robustness & Fault Tolerance)

This document lists pragmatic steps to harden the current Spring Boot stack, improve reliability, and align with best practices.

---
## 1) Reliability Patterns (Resilience4j)
- **Circuit Breakers & Timeouts** around external calls (Email/SMS/WhatsApp, push, file storage). Configure sensible `slowCallDurationThreshold` and sliding windows.
- **Retries with backoff + jitter** only for **idempotent** operations.
- **Bulkheads** (thread‑pool isolation) to prevent noisy‑neighbor failures.
- **Rate Limiter** per integration to respect vendor quotas.

### Implementation Notes
- Central `RestClient`/`WebClient` with Resilience4j decorators.
- Expose **health indicators** for each downstream (`/actuator/health` with custom contributors) and show them on an Ops dashboard.

---
## 2) Idempotency & Exactly‑Once Effects
- Add **Idempotency‑Key** header for critical POSTs (reservations, sales, payments). Store seen keys in Redis with TTL to avoid duplicates.
- Use **Outbox Pattern** for notifications & side‑effects: write event to `outbox_events` in the same DB transaction; a background worker publishes to the broker and marks it delivered.
- Consider **Sagas** (orchestration) for multi‑step flows like *Reserve → Collect Deposit → Mark Vehicle Reserved → Notify* with compensations on failure.

---
## 3) Messaging & Async Processing
- Introduce **Kafka or RabbitMQ** (notifications, audit fan‑out, cache invalidations, recon jobs). Keep the main request path fast; push work to async consumers.
- Define **DLQ** (dead‑letter queues) with retention and alerting.

---
## 4) Concurrency & Data Integrity
- Apply **Optimistic Locking** (`@Version`) on core entities (Car, Motorcycle, Reservation, Sale) to prevent lost updates.
- For reservation & sale creation, prefer **pessimistic locks** or a **Redis-based distributed lock** to avoid double‑sell.
- Strengthen **DB constraints**: check constraints on `vehicle_type`, non‑overlapping reservations, positive monetary amounts, and additional unique keys where relevant.

---
## 5) Cache Hardening (Redis)
- Add **cache stampede** protection (randomized TTL/jitter, `@Cacheable` with `sync = true` for hot keys).
- Namespace keys (e.g., `ws:v1:cars:{id}`) and implement **versioned cache keys** to support rolling deployments.
- Guard against **thundering herds** on invalidation with background refresh.

---
## 6) Sessions & Auth
- Move to **Spring Session (Redis)** for horizontal scaling and single‑sign‑out. Set **SameSite=Lax**, **Secure**, **HttpOnly**.
- Add **2FA** (TOTP/SMS) for privileged roles; **device/session listing** with remote logout.
- Introduce **per‑endpoint rate limiting** (Bucket4j) and **captcha** on repeated login failures.

---
## 7) Security Hardening
- **PII encryption** at field level (e.g., client phone/email) with a **KMS** (AWS KMS/Azure Key Vault/HashiCorp Vault) and envelope keys.
- Rotate secrets; adopt **Secret Manager** and remove plaintext secrets from env files.
- Add **Content Security Policy**, **HSTS**, **X‑Frame‑Options**, **X‑Content‑Type‑Options**, **Referrer‑Policy** headers.
- **Input validation**/sanitization libraries for any file upload paths; **antivirus scan** (ClamAV) for uploaded attachments.
- Make audit logs **tamper‑evident** (hash chain or write‑once storage/S3 object lock).

---
## 8) Observability (Logs, Metrics, Traces)
- **Structured JSON logs** with a `traceId`/`spanId` correlation; propagate W3C `traceparent` across services.
- **Micrometer** → **Prometheus** for metrics; define SLIs (availability, error rate, p95 latency) and SLOs per domain.
- **OpenTelemetry** tracing (Jaeger/Tempo). Add spans around repository calls, external I/O, and cache access.
- Create Grafana dashboards: **API latency**, **DB health** (connections, slow queries), **cache hit rate**, **queue lag**, **notification success**.

---
## 9) Database Performance & Lifecycle
- Tune **HikariCP** (max pool size, timeouts) per node.
- Guard against **N+1** with fetch joins and DTO projections; paginate everywhere.
- Add **covering indexes** for common filters (status, make/model, location, created_at).
- Partition/archival for **high‑volume** tables (notifications, audit, deliveries) and implement **data retention** (e.g., 180–365 days) with export before purge.
- Use **read replicas** for heavy reads/reporting.

---
## 10) File/Media Storage
- Offload to **object storage** (S3/Azure Blob) with **pre‑signed uploads**, **lifecycle** (infrequent access, archival), **checksums**, and optional **server‑side encryption**.
- Generate derivatives (thumbnails/WebP) in an **async job**; store metadata in DB.

---
## 11) Deployment & Scalability
- **Blue/Green** or **Canary** deployments; health‑check new pods via **readiness**/**liveness** probes before shifting traffic.
- Graceful shutdown with **`spring.lifecycle.timeout-per-shutdown-phase`** and **`server.shutdown=graceful`**.
- Kubernetes basics: HPA, PodDisruptionBudgets, resource requests/limits, node autoscaling.
- Use **feature flags** (Unleash/FF4J) for safe rollouts.

---
## 12) Testing Strategy
- **Unit + Integration** tests to >80% for critical modules.
- **Contract tests** (Spring Cloud Contract) for APIs; **consumer‑driven** contracts for integrations.
- **Load testing** (k6/JMeter) with success criteria tied to SLOs.
- **Chaos testing** (Chaos Monkey for Spring Boot) in staging to validate resilience.

---
## 13) DR, Backups, and Runbooks
- Define **RPO/RTO** targets; enable **PITR** with MySQL binlogs and verify **restore drills** quarterly.
- Geo‑redundant offsite backups; automated **backup integrity checks** (checksum + test restore).
- Maintain **runbooks** for incident response (DB failover, cache outage, queue unavailability, file store errors).

---
## 14) Compliance & Data Governance
- **Data classification** (PII, financial, operational); mask/redact in logs and non‑prod dumps.
- **Right‑to‑erasure/export** processes (client data); purge policies for expired warranties/PUC/insurance docs.
- India‑specific: **GST** calculations on invoices, **RTO** document retention timelines.

---
## 15) 30/60/90‑Day Plan
**30 days**: Resilience4j on integrations, Redis Session, idempotency keys, JSON logs + traceId; image media service + S3.

**60 days**: Outbox + broker (Kafka/Rabbit), DLQs, OpenTelemetry traces, per‑endpoint rate limiting, optimistic locking, improved indexes.

**90 days**: Blue/Green deploys, chaos tests in staging, PITR drills, archival jobs, feature flags for risky changes, SLO‑based alerting.
