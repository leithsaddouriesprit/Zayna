# AI Pipeline – Issues Found and Fixes Applied

## A) Review – Issues Found

| Location | Issue | Fix |
|----------|--------|-----|
| **JavaFX** `ChatAIController.java` | Request sent only `message`, `enfantId`, `parentId`; **no `busId`** so API often had null bus and incomplete context. | Payload now includes `userMessage`, `enfantId`, `busId` (resolved from enfant’s trajet via `TrajetService.getTrajetByEnfant`), and `selectedChild` (childId, nom, prenom, onBoard). |
| **JavaFX** `ChatAIController.java` | Response handling only read `reply`; no handling of `MISSING_CONTEXT`, `missing_fields`, or `suggested_actions`. | `displayStructuredResponse()` now parses full JSON, shows `missing_fields` and `suggested_actions` when `intent == "MISSING_CONTEXT"`, and displays `facts_used` (e.g. lastUpdateTime, busLat/busLng). |
| **API** `AiController.java` | Used only `req.busId` and `req.enfantId`; no resolution of bus from enfant. | Controller now uses `TrackingContextService.buildContract(busId, enfantId)`, which resolves `busId` from enfant’s trajet when `busId` is null. |
| **API** `AiController.java` | Single prompt + single LLM call; no validation, no intent, no deterministic answers. | Pipeline: validate context → detect intent → try deterministic answer → else build strict-JSON prompt → call Ollama → parse/repair/fallback. |
| **API** `ChatRequest.java` | Only `message`, `busId`, `enfantId`; no tracking contract. | Replaced with full contract: `userMessage`, `language`, `userRole`, `sessionId`, `selectedChild`, `selectedBus`, `trackingSnapshot`, `dataQuality`; kept `busId`/`enfantId` for backward compatibility. |
| **API** `ChatResponse.java` | Only `reply` (free-form). | New `StructuredChatResponse` with `intent`, `answer_fr`/`answer_en`/`answer_ar`, `facts_used`, `missing_fields`, `confidence`, `suggested_actions`, and `reply` for compatibility. |
| **API** `ContextBuilderService.java` | Built a single JSON string for the LLM; no structured DTO or data-quality flags. | New `TrackingContextService` builds a `TrackingContextContract` (selectedChild, selectedBus, trackingSnapshot, dataQuality) and resolves bus from enfant when needed. |
| **API** `OllamaClient.java` | No temperature/top_p/num_predict; default Ollama params could increase hallucinations. | Added `ai.temperature` (0.2), `ai.top_p` (0.9), `ai.max_tokens` (512) in `application.properties` and pass them in `options` to Ollama. |
| **API** | No validation of context vs question (e.g. “position exacte” without busLat/busLng). | New `ContextValidator`: returns `MISSING_CONTEXT` with `missing_fields` and `suggested_actions` when required data is missing or test mode (e.g. busId=1). |
| **API** | No intent detection or rule-based answers. | New `IntentDetectionService` (regex-based) and `DeterministicAnswerService`: answer ON_BOARD_STATUS, CHILD_LOCATION, BUS_LOCATION, ETA, SAFETY_STATUS from snapshot when possible. |
| **API** | LLM output was free-form; no guaranteed JSON. | `PromptBuilderService` enforces strict JSON output format; `ResponseParserService` parses it, strips markdown, runs repair prompt on invalid JSON, and returns deterministic fallback if still invalid. |

---

## B) Tracking Context Contract (DTO + validation)

- **New DTOs:** `SelectedChildDto`, `SelectedBusDto`, `TrackingSnapshotDto`, `DataQualityDto` (see `dto` package).
- **ChatRequest** extended with full contract fields; API still accepts legacy `message`/`busId`/`enfantId` and fills context via `TrackingContextService.buildContract()`.
- **Validation rules (ContextValidator):**
  - “Où est mon enfant / position exacte” without `busLat`/`busLng` → `MISSING_CONTEXT` + suggest enabling GPS/tracking or selecting child/bus.
  - “Quand il arrive / ETA” without child → `MISSING_CONTEXT`; without snapshot → same; if only ETA missing, backend can still answer with distance/next stop (no hard fail).
  - `busId == 1` or test mode → do not answer with fake data; return test-mode message and suggested actions.
  - No values are invented; missing data is reported in `missing_fields` and `suggested_actions`.

---

## C) Structured JSON output + repair + fallback

- **StructuredChatResponse** schema: `intent`, `answer_fr` (and optional `answer_en`/`answer_ar`), `facts_used`, `missing_fields`, `confidence`, `suggested_actions`, `reply`.
- **PromptBuilderService:** system prompt instructs the model to output only this JSON (no markdown, no extra text).
- **ResponseParserService:** parses raw output, strips ``` blocks; on parse failure calls Ollama again with `buildRepairPrompt()`; if still invalid returns `fallbackResponse()` (deterministic “Désolé, je n'ai pas pu traiter…”).

---

## D) Intent detection + deterministic answers

- **IntentDetectionService:** regex patterns for ON_BOARD_STATUS, CHILD_LOCATION, BUS_LOCATION, ETA, SAFETY_STATUS; else GENERAL_HELP.
- **DeterministicAnswerService:** for each intent, if required fields exist (e.g. onBoard for ON_BOARD, busLat/busLng for CHILD_LOCATION), returns a rule-based `StructuredChatResponse`; otherwise returns null and controller proceeds to LLM.
- Deterministic answers use only data from the contract (e.g. “L’arrivée au prochain arrêt est estimée dans environ X minutes (Nom arrêt).” when ETA is present; safe message when ETA is missing but distance/next stop exist).

---

## E) JavaFX integration

- **Payload:** `buildRequestJson()` builds `userMessage`, `message`, `language`, `userRole`, `sessionId`, `enfantId`, `parentId`, `selectedChild` (from `EnfantService.getEnfantById`), and `busId`/`selectedBus` (from `TrajetService.getTrajetByEnfant`).
- **Response:** `displayStructuredResponse()` reads `reply`, `intent`, `missing_fields`, `suggested_actions`, `facts_used`; when `intent == "MISSING_CONTEXT"` it shows missing fields and suggested actions; when facts contain `lastUpdateTime` or `busLat`/`busLng` it displays them.

---

## F) Model parameters

- **application.properties:** `ai.temperature=0.2`, `ai.top_p=0.9`, `ai.max_tokens=512`.
- **OllamaClient:** sends these in the `options` object to `/api/generate`.

---

## G) End-to-end tests (8 cases)

In `AiChatIntegrationTest.java` (with `TrackingContextService` mocked):

1. **“Mon enfant est dans le bus ?”** → ON_BOARD_STATUS, reply indicates yes (child on board).
2. **“Où est mon enfant ? position exacte”** → CHILD_LOCATION, reply includes coordinates or approximation note.
3. **“Est-il on board ?”** → ON_BOARD_STATUS.
4. **“Quand il arrive ?”** → ETA, reply mentions minutes or next stop.
5. **“Mon fils est cv ?”** → SAFETY_STATUS.
6. **ETA question with missing eta** → ETA intent, safe reply (no invented ETA); mentions next stop/distance or asks to refresh.
7. **Position exacte but missing busLat/busLng** → MISSING_CONTEXT, `missing_fields` and `suggested_actions` present.
8. **Test mode (busId=1)** → MISSING_CONTEXT, reply mentions test mode, `suggested_actions` present.

---

## Files created/updated

**zayna-ai-api**

- **New:** `dto/SelectedChildDto.java`, `SelectedBusDto.java`, `TrackingSnapshotDto.java`, `DataQualityDto.java`, `Intent.java`, `StructuredChatResponse.java`
- **Updated:** `dto/ChatRequest.java`
- **New:** `service/TrackingContextService.java`, `ContextValidator.java`, `IntentDetectionService.java`, `DeterministicAnswerService.java`, `PromptBuilderService.java`, `ResponseParserService.java`
- **Updated:** `service/OllamaClient.java` (options: temperature, top_p, num_predict)
- **Updated:** `controller/AiController.java` (full pipeline)
- **Updated:** `application.properties` (ai.temperature, ai.top_p, ai.max_tokens)
- **New:** `AiChatIntegrationTest.java` (8 tests)

**Zayna (JavaFX)**

- **Updated:** `controlleurs/leith/AI/ChatAIController.java` (payload with selectedChild + busId, structured response parsing and display)

**Note:** `ContextBuilderService` is still used for the legacy JSON context string if needed; the main flow uses `TrackingContextService` and the new DTOs.
