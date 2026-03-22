# WheelShift Pro — AI Service Overview

**Version:** 1.0.0
**Last Updated:** March 22, 2026
**Status:** Planning

---

## Table of Contents

1. [Why a Separate AI Service](#1-why-a-separate-ai-service)
2. [Proposed AI Features](#2-proposed-ai-features)
3. [High-Level Architecture](#3-high-level-architecture)
4. [Tech Stack](#4-tech-stack)
5. [How Spring Boot Communicates with the AI Service](#5-how-spring-boot-communicates-with-the-ai-service)
6. [API Contract (Draft)](#6-api-contract-draft)
7. [Feature Deep Dives](#7-feature-deep-dives)
8. [Data Sources](#8-data-sources)
9. [Implementation Roadmap](#9-implementation-roadmap)
10. [Failure Handling](#10-failure-handling)

---

## 1. Why a Separate AI Service

AI/ML workloads have different runtime requirements than a standard CRUD backend:

- **Python ecosystem** — nearly all ML libraries (scikit-learn, XGBoost, TensorFlow, Hugging Face, LangChain) are Python-native. Forcing them into the JVM is painful and unnecessary.
- **Independent scaling** — model inference can be CPU/GPU-heavy; you want to scale the AI service independently without scaling the entire Spring Boot app.
- **Deployment flexibility** — AI models can be updated, swapped, or retrained without touching the core business logic.
- **Language model APIs** — LLM integrations (OpenAI, Gemini, Anthropic) are best orchestrated from Python.

The Spring Boot backend remains the **source of truth** for all business data. The AI service is a **read-focused analytics layer** — it reads from the same MySQL database and exposes intelligent endpoints that Spring Boot calls as needed.

---

## 2. Proposed AI Features

### Tier 1 — High Value, Feasible Early

| # | Feature | What It Does | Inputs | Output |
|---|---------|-------------|--------|--------|
| 1 | **Smart Vehicle Pricing** | Suggests an optimal selling price for a vehicle based on its attributes and historical sales patterns | Make, model, year, mileage, condition score, features, current market listings | Suggested price, confidence range |
| 2 | **Lead Scoring** | Ranks incoming inquiries by conversion likelihood so sales staff prioritize the hottest leads | Client history, inquiry type, response time, prior purchases, vehicle category interest | Score 0–100, priority label (Hot / Warm / Cold) |
| 3 | **Inventory Health Score** | Flags slow-moving vehicles before they tie up capital too long | Days in inventory, price vs. market, category, season, views/inquiries count | Health label (Healthy / At Risk / Stale), suggested action |
| 4 | **Similar Vehicle Suggestions** | When a client shows interest in a vehicle, suggest alternatives they may also consider | Vehicle attributes, client interaction history | Ranked list of similar vehicle IDs |

### Tier 2 — Medium Complexity

| # | Feature | What It Does | Inputs | Output |
|---|---------|-------------|--------|--------|
| 5 | **Demand Forecasting** | Predicts which vehicle categories, fuel types, and price bands will be in demand next 30–90 days | Historical sales, seasonality, local trends | Category demand rankings, suggested stocking quantities |
| 6 | **Client Churn Prediction** | Flags clients who are unlikely to return based on engagement decay patterns | Last interaction date, purchase history, inquiry activity, response time | Churn risk score, suggested re-engagement action |
| 7 | **Automated Vehicle Description Generator** | Generates a clean, readable sales listing description from raw vehicle specs | All vehicle fields (make, model, specs, features, condition) | Ready-to-publish description text |
| 8 | **Inspection Risk Summarizer** | Reads free-text inspection notes and structured condition fields to produce a risk summary | Inspection record fields + notes | Risk level, key issues list, estimated repair cost band |

### Tier 3 — Advanced / Longer Term

| # | Feature | What It Does | Inputs | Output |
|---|---------|-------------|--------|--------|
| 9 | **Sales Q&A (Natural Language)** | Let managers ask questions in plain English — "What was the best-selling body type this quarter?" | Natural language query + DB read access | Natural language answer + supporting data |
| 10 | **Transaction Anomaly Detection** | Detect unusual financial patterns (price far below market, duplicate entries, suspicious discounts) | Financial transactions, vehicle data | Anomaly flag, severity, explanation |
| 11 | **Sales Forecasting** | Predict next month's revenue based on reservations, pipeline inquiries, and historical patterns | Pipeline data, historical sales, seasonality | Forecast value, confidence interval |
| 12 | **Dynamic Commission Optimization** | Suggest commission rates that balance employee motivation with business margins | Historical sales, commission history, employee performance metrics | Suggested commission rate per employee/role |

---

## 3. High-Level Architecture

```
┌──────────────────────────────────────────────┐
│              WheelShift Pro                  │
│           (Spring Boot API)                  │
│                                              │
│  AIServiceClient (WebClient / RestTemplate)  │
│    - Calls AI endpoints                      │
│    - Falls back gracefully if AI unavailable │
└───────────────────┬──────────────────────────┘
                    │ HTTP REST
                    │ (internal network / Docker)
                    ▼
┌──────────────────────────────────────────────┐
│            WheelShift AI Service             │
│            (Python · FastAPI)                │
│                                              │
│  ┌──────────────┐  ┌──────────────────────┐  │
│  │  ML Models   │  │   LLM Integration    │  │
│  │ (XGBoost /   │  │  (OpenAI / Gemini /  │  │
│  │  sklearn)    │  │   local LLM)         │  │
│  └──────┬───────┘  └──────────┬───────────┘  │
│         │                     │              │
│  ┌──────▼─────────────────────▼───────────┐  │
│  │         Feature Engineering Layer      │  │
│  │  Reads from DB, computes features      │  │
│  └──────────────────┬────────────────────┘   │
│                     │                        │
│  ┌──────────────────▼────────────────────┐   │
│  │          Background Jobs (Celery)     │   │
│  │   Model retraining, batch scoring     │   │
│  └───────────────────────────────────────┘   │
└──────────┬──────────────────────┬───────────┘
           │                      │
           ▼                      ▼
┌─────────────────┐    ┌────────────────────────┐
│   MySQL DB      │    │  Redis (shared/          │
│ (same DB as     │    │  dedicated)              │
│  Spring Boot,   │    │  Cache AI responses      │
│  read-only      │    │  and job results         │
│  replica ideal) │    └────────────────────────┘
└─────────────────┘
           │
           ▼
┌─────────────────────────────────────────────┐
│         MLflow (optional)                   │
│  Track model versions, experiments,         │
│  performance metrics                        │
└─────────────────────────────────────────────┘
```

### Component Roles

| Component | Role |
|-----------|------|
| **Spring Boot** | Business logic owner; calls AI service for enrichment; never blocked by AI failures |
| **FastAPI (AI Service)** | Serves inference endpoints; manages model lifecycle |
| **MySQL** | Single source of truth; AI service reads data here for training and inference |
| **Redis** | AI responses cached here (pricing suggestions, scores) to avoid redundant inference |
| **Celery + Redis Broker** | Handles async model retraining and batch scoring jobs |
| **MLflow** | Optional but recommended — tracks model experiments and versions over time |
| **LLM API** | External call for text generation features (description generation, Q&A) |

---

## 4. Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| **Language** | Python 3.11+ | ML ecosystem standard |
| **API Framework** | FastAPI | Fast, async, auto-generates OpenAPI docs |
| **ML / Tabular Models** | scikit-learn, XGBoost, LightGBM | pricing, scoring, forecasting |
| **LLM Orchestration** | LangChain / direct OpenAI SDK | description generation, Q&A |
| **LLM Provider** | OpenAI GPT-4o or Google Gemini | interchangeable behind an abstraction |
| **Database Access** | SQLAlchemy + PyMySQL | reads from same MySQL DB |
| **Caching** | Redis (via `redis-py`) | cache inference results, shared with Spring Boot |
| **Task Queue** | Celery + Redis broker | background retraining, batch jobs |
| **Model Tracking** | MLflow | versioned model registry |
| **Containerization** | Docker + Docker Compose | same compose file as Spring Boot |
| **Dependency Management** | Poetry or pip + requirements.txt | standard Python packaging |

---

## 5. How Spring Boot Communicates with the AI Service

### Spring Boot Side

Create a dedicated client bean that wraps all AI service calls:

```java
// AIServiceClient.java
@Component
public class AIServiceClient {

    private final WebClient webClient;

    public AIServiceClient(@Value("${ai.service.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public Optional<PricingSuggestionDto> suggestPrice(Long vehicleId, String vehicleType) {
        return webClient.get()
            .uri("/api/ai/pricing/suggest?vehicleId={id}&type={type}", vehicleId, vehicleType)
            .retrieve()
            .bodyToMono(PricingSuggestionDto.class)
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(e -> Mono.empty()) // graceful fallback
            .blockOptional();
    }

    public Optional<LeadScoreDto> scoreInquiry(Long inquiryId) {
        return webClient.get()
            .uri("/api/ai/leads/score/{inquiryId}", inquiryId)
            .retrieve()
            .bodyToMono(LeadScoreDto.class)
            .timeout(Duration.ofSeconds(3))
            .onErrorResume(e -> Mono.empty())
            .blockOptional();
    }
}
```

### application.properties (new property)

```properties
ai.service.base-url=http://ai-service:8000
ai.service.enabled=true
```

### Calling from a Service

```java
// InquiryService.java (example usage)
public InquiryResponseDto createInquiry(CreateInquiryDto dto) {
    Inquiry inquiry = inquiryRepository.save(mapToEntity(dto));

    // AI enrichment — non-blocking, best-effort
    aiServiceClient.scoreInquiry(inquiry.getId())
        .ifPresent(score -> {
            inquiry.setAiLeadScore(score.getScore());
            inquiry.setAiLeadLabel(score.getLabel());
            inquiryRepository.save(inquiry);
        });

    return mapToDto(inquiry);
}
```

---

## 6. API Contract (Draft)

All AI service endpoints are under `/api/ai/`. The AI service runs on port `8000` internally.

### Vehicle Pricing

```
GET /api/ai/pricing/suggest
Query params: vehicleId (Long), type (car | motorcycle)

Response 200:
{
  "vehicleId": 42,
  "suggestedPrice": 485000.00,
  "priceRange": { "low": 460000.00, "high": 510000.00 },
  "confidence": 0.82,
  "comparablesUsed": 14,
  "factors": ["low_mileage", "recent_model", "high_demand_segment"]
}
```

### Lead Scoring

```
GET /api/ai/leads/score/{inquiryId}

Response 200:
{
  "inquiryId": 99,
  "score": 78,
  "label": "HOT",
  "reasons": ["repeat_client", "specific_model_inquiry", "quick_response"],
  "suggestedAction": "Call within 2 hours"
}
```

### Inventory Health

```
GET /api/ai/inventory/health/{vehicleId}?type=car

Response 200:
{
  "vehicleId": 42,
  "healthLabel": "AT_RISK",
  "daysInInventory": 67,
  "marketPositionDelta": -8.5,
  "suggestedAction": "Consider a 5-8% price reduction",
  "urgency": "MEDIUM"
}
```

### Similar Vehicles

```
GET /api/ai/vehicles/similar?vehicleId=42&type=car&limit=5

Response 200:
{
  "sourceVehicleId": 42,
  "suggestions": [
    { "vehicleId": 18, "score": 0.94, "reason": "Same segment, similar price" },
    { "vehicleId": 55, "score": 0.87, "reason": "Same brand, newer model nearby price" }
  ]
}
```

### Vehicle Description Generator

```
POST /api/ai/vehicles/describe

Request body:
{
  "vehicleId": 42,
  "type": "car",
  "tone": "professional"   // professional | casual | promotional
}

Response 200:
{
  "vehicleId": 42,
  "description": "A well-maintained 2021 Honda City with just 28,000 km on the odometer..."
}
```

### Anomaly Detection

```
GET /api/ai/transactions/anomalies?fromDate=2026-01-01&toDate=2026-03-01

Response 200:
{
  "anomalies": [
    {
      "transactionId": 301,
      "severity": "HIGH",
      "type": "UNDERPRICED_SALE",
      "detail": "Sale price is 34% below market average for this vehicle",
      "flaggedAt": "2026-03-10T14:23:00"
    }
  ]
}
```

---

## 7. Feature Deep Dives

### 7.1 Smart Vehicle Pricing

**How it works:**

1. **Training**: Pull all historical `sales` records joined with vehicle attributes (make, model, year, mileage, fuel type, features, inspection score, city). Train a regression model (XGBoost or LightGBM). Retrain monthly via Celery job.
2. **Inference**: When a new vehicle is added or a price review is requested, Spring Boot calls `/api/ai/pricing/suggest`. The AI service fetches features from DB, runs the model, and returns a price range.
3. **Cold start**: For rare models with insufficient training data, fall back to a rule-based median calculation.

**Model features used:**

- Vehicle: make, model, year, mileage, fuel type, transmission, engine CC, body type
- Condition: inspection score (aggregated from inspection records), accident history flag
- Market: average sold price for same category in last 90 days, days to sell for similar vehicles
- Inventory: current unsold count for same model (supply pressure)

---

### 7.2 Lead Scoring

**How it works:**

1. **Training**: Use historical inquiries where outcome is known (converted to sale vs. closed without sale). Train a binary classification model. Output is a probability score.
2. **Features**: Client's prior purchase count, time since last interaction, inquiry type (specific model vs. general), response time to previous inquiries, vehicle price band alignment with prior purchases.
3. **Inference**: Called when a new inquiry is created or when a sales employee opens their inquiry list (scores refreshed daily in batch).

---

### 7.3 Automated Vehicle Description Generator

**How it works:**

1. Fetch all structured fields for the vehicle (make, model, year, mileage, fuel, features list, inspection summary).
2. Build a structured prompt and call an LLM API (OpenAI GPT-4o / Gemini).
3. The prompt instructs the LLM to write a professional, accurate listing description without embellishing or hallucinating specs not provided.
4. Cache the result in Redis — refresh only when vehicle data changes.

**Prompt template (simplified):**
```
You are a professional vehicle listing writer.
Write a {tone} 3-4 sentence description for this vehicle.
Use only the provided facts. Do not add or invent any details.
Facts: {vehicle_json}
```

---

### 7.4 Inventory Health Score

**How it works:**

1. A Celery scheduled job runs daily and computes a health score for every unsold vehicle.
2. Score factors: days in inventory, price vs. median sold price for same category, active inquiry count, views/inquiry ratio, seasonal demand trend.
3. Results stored in a `vehicle_health_scores` table (or Redis hash) so Spring Boot can read them without calling inference every time.
4. Spring Boot surfaces health labels on the Store Manager dashboard.

---

## 8. Data Sources

The AI service reads directly from the existing WheelShift Pro MySQL database. It should use a **read-only database user** to ensure it can never write to the business tables.

| Table(s) | Used By |
|----------|---------|
| `cars`, `car_models`, `motorcycles`, `motorcycle_models` | Pricing, health score, similarity, description |
| `sales` | Pricing model training, forecasting, anomaly detection |
| `car_inspections`, `motorcycle_inspections` | Pricing features, inspection risk summarizer |
| `inquiries` | Lead scoring training and inference |
| `clients` | Lead scoring features, churn prediction |
| `financial_transactions` | Anomaly detection, forecasting |
| `reservations` | Sales forecasting pipeline |
| `employees`, `tasks` | Commission optimization (future) |

### Read-Only DB User

Create a dedicated MySQL user for the AI service:

```sql
CREATE USER 'wheelshift_ai'@'%' IDENTIFIED BY '<strong_password>';
GRANT SELECT ON wheelshift.* TO 'wheelshift_ai'@'%';
FLUSH PRIVILEGES;
```

---

## 9. Implementation Roadmap

### Phase 1 — Foundation (Week 1–2)

- [ ] Scaffold FastAPI project with Docker and docker-compose integration
- [ ] Set up SQLAlchemy connection to MySQL (read-only user)
- [ ] Set up Redis caching for AI responses
- [ ] Add `AIServiceClient` bean in Spring Boot
- [ ] Add `ai.service.base-url` and `ai.service.enabled` properties
- [ ] Health check endpoint (`GET /health`) so Spring Boot can detect if AI service is up

### Phase 2 — Pricing & Inventory Health (Week 3–4)

- [ ] Build vehicle pricing feature pipeline (extract, transform, train XGBoost)
- [ ] Expose `GET /api/ai/pricing/suggest` endpoint
- [ ] Build inventory health scoring job (Celery daily task)
- [ ] Expose `GET /api/ai/inventory/health/{vehicleId}` endpoint
- [ ] Wire both endpoints into Spring Boot (vehicle detail, store manager dashboard)

### Phase 3 — Lead Scoring & Similarity (Week 5–6)

- [ ] Build lead scoring classifier (train on historical inquiries)
- [ ] Expose `GET /api/ai/leads/score/{inquiryId}` endpoint
- [ ] Build vehicle similarity engine (cosine similarity on feature vectors)
- [ ] Expose `GET /api/ai/vehicles/similar` endpoint
- [ ] Wire lead score into inquiry creation and sales dashboard

### Phase 4 — LLM Features (Week 7–8)

- [ ] Integrate OpenAI / Gemini SDK
- [ ] Build description generator prompt + endpoint
- [ ] Build inspection risk summarizer
- [ ] Add caching for generated content (invalidate on vehicle update)
- [ ] Wire description generator into car/motorcycle edit forms

### Phase 5 — Advanced Features (Future)

- [ ] Sales forecasting model
- [ ] Transaction anomaly detection
- [ ] Natural language Q&A over business data
- [ ] MLflow integration for model version tracking

---

## 10. Failure Handling

The AI service is a **best-effort enrichment layer**. The Spring Boot API must never fail or block because the AI service is slow or unavailable.

**Rules:**

1. All calls from Spring Boot to the AI service must have a **timeout** (max 5 seconds for any endpoint).
2. Every call must have an **`onErrorResume`** fallback that returns an empty result.
3. If `ai.service.enabled=false`, skip all AI calls entirely — critical for local development.
4. AI-enriched fields (lead score, suggested price) are always **supplementary**, never required.
5. Log all AI service errors at `WARN` level with the endpoint and response time — do not alert.

**Graceful degradation example:**

```
AI Service Status     Spring Boot Behavior
──────────────────────────────────────────────
✅ Healthy           Pricing suggestion shown on vehicle detail
⚠️  Slow (>5s)       Timeout fires, fallback: no suggestion shown
❌  Down             `onErrorResume` → empty response, no suggestion shown
🚫 Disabled          ai.service.enabled=false, block skipped entirely
```

---

## Appendix: Suggested docker-compose Addition

Add the AI service to the existing `docker-compose-dev.yml`:

```yaml
  ai-service:
    build:
      context: ./ai-service
      dockerfile: Dockerfile
    container_name: wheelshift-ai
    ports:
      - "8000:8000"
    environment:
      - DB_URL=mysql+pymysql://wheelshift_ai:${AI_DB_PASSWORD}@db:3306/wheelshift
      - REDIS_URL=redis://redis:6379/1
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - ENV=development
    depends_on:
      - db
      - redis
    networks:
      - wheelshift-net
```

The `ai-service/` folder sits alongside the Spring Boot project root.

---

**Document Version:** 1.0.0
**Last Updated:** March 22, 2026
**Maintainer:** Platform Team
