# Sequo Vendor

Sequo Vendor is the Kotlin Multiplatform merchant app for Sequo sellers and merchant staff. Its purpose is to let an authenticated merchant safely operate the seller side of Sequo commerce: receive paid merchant sub-orders, accept or reject them, prepare packages, mark packages ready, hand packages off to Sequo logistics, review seller catalog state, and manage store availability once the backend exposes a seller-ready settings contract.

This app must follow the customer app product rules from `../Sequo`: operational clarity over marketing UI, French-first copy with English support, CFA integer money, backend-confirmed order/payment state, safe loading/error states, no raw debug errors, and no mobile-side authority for financial or lifecycle truth.

## Product Purpose

- Give merchant owners and staff a focused operational console for paid Sequo orders.
- Reduce seller response and packing delays by showing SLA deadlines and clear next actions.
- Preserve Sequo logistics boundaries: the seller owns only its sub-order/package, while Sequo operations handle consolidation, courier dispatch, relay workflows, settlement, and support escalations.
- Provide catalog visibility and draft/editor surfaces without inventing seller write contracts that the backend has not safely exposed.
- Prepare for store status, hours, notification, and settlement workflows behind repository interfaces once the API is mobile-ready.

## Current Implementation Status

Implemented in the app:

- Production-style Kotlin Multiplatform app shell inspired by the Sequo customer app: splash, onboarding, drawer/settings language and theme controls, operational bottom navigation, dense seller dashboard, safe error panels, and EN/FR localized strings.
- Auth flow with Google social sign-in, email sign-in/sign-up surfaces, secure token/session storage, refresh-session restore, logout, `/api/auth/me`, merchant-role gating, and a clear local/demo fallback when backend credentials or merchant scope are unavailable.
- Network layer using `/api`, JSON content type, bearer auth, one refresh-and-retry attempt on `401`, `Retry-After` parsing for `429`, typed safe failures, and no raw backend debug messages in UI.
- Dashboard that loads merchant sub-orders from `GET /api/merchant/sub-orders?merchantId=...` when authenticated, derives pending/accepted/preparing/ready counts, shows SLA warnings, recent orders, quick actions, store state, and API connection state.
- Orders list with localized tabs/filters for new, accepted, preparing, ready, completed, and rejected/cancelled orders. Cards show code, status, CFA subtotal/net/commission, timestamps, SLA deadlines, package count, and safe next actions.
- Order detail with lifecycle timeline, financial summary, SLA panel from `GET /api/merchant/sub-orders/{id}/sla`, package count entry, rejection reason handling, duplicate-submit protection, and action calls to the active merchant fulfillment endpoints.
- Store availability and weekly opening-hours UI behind `VendorStoreRepository`, including open/closed toggle, temporary closure reason, customer-facing preview, weekday rows, copy-Monday-to-weekdays, temporary exceptions, and validation.
- Catalog browse/editor UI for seller products using public catalog reads by `merchantId`, with search, category/status filters, camera-photo indicator, CFA price, stock/availability summary, last-updated labels, and local draft saves only.
- Notifications inbox/preferences using `SEQUO_MERCHANT` through `/api/notifications/...`, including read/archive/unarchive and push/in-app/SMS preference controls where the API supports them.
- Account/settings with authenticated user display, merchant context, language EN/FR, theme system/light/dark, logout, API health via `/actuator/health`, notifications entry, payments accepted, availability entry, and legal/privacy copy.
- Focused Android host tests for status/action mapping, safe error mapping, CFA formatting, opening-hours validation, order action request bodies, and auth refresh retry behavior.

Mocked or local for now:

- Local/demo mode uses `DemoVendorData` and fake repositories for auth fallback, orders, catalog, notifications, and store state when a real authenticated merchant session is unavailable.
- Store open/closed state, temporary closure reason, weekly hours, and temporary exceptions are local-only because no merchant-scoped store availability endpoint exists yet.
- Catalog editor saves local drafts through the repository interface. It intentionally does not call global catalog write routes because seller-scoped product management is not mobile-ready.
- Product media/camera verification is represented in UI/model state only; no mobile camera upload endpoint is called.
- Notification device registration support exists in the API client, but actual Firebase client token provisioning is not wired in the shared UI.

Known backend gaps:

- The mobile guide says merchant auth should use `appSource=SEQUO_MERCHANT`, but the active backend `AppSource` enum currently exposes `SEQUO_APP`, `SEQUO_HUB`, and `SEQUO_RIDER`. SequoVendor currently uses the backend-accepted source for social login so sign-in can work; the API should add a merchant app source or document the intended accepted source for vendor auth.
- Auth returns merchant roles but does not yet expose a dedicated merchant membership/selection endpoint for users with multiple merchants.
- Seller catalog write endpoints are not mobile-ready. Needed: `GET /api/merchant/products`, `POST /api/merchant/products`, `PATCH /api/merchant/products/{productId}`, archive/unarchive, availability, stock, bargaining controls, return eligibility, variant/option updates, and merchant ownership validation.
- Seller product media endpoints are missing. Needed: real-time camera capture upload/evidence, approved generic sealed-item catalog reference attachment, media freshness/verification read state, and server-side rejection of gallery uploads for seller-specific products.
- Merchant store settings endpoints are missing. Needed: `GET/PUT /api/merchant/store-availability` or `GET/PUT /api/merchant/store-settings` with open/closed state, weekly hours, temporary exceptions, closure note, effective availability, and owner/staff scope validation.
- Richer order detail would benefit from merchant line-item snapshots, option/variant labels, safe handoff/rider context, and package-label data.

## Source Of Truth

- Current implemented mobile API: `../sequo-api/MOBILE_API_GUIDE.md` and active Spring controllers.
- Target/future API ideas: `../sequo-api/API_SPEC.md`. Do not treat target endpoints as implemented unless an active controller exists.
- Customer/product rules: `../Sequo/README.md`, `../Sequo/UI_PATTERNS.md`, `../Sequo/CATALOG_AND_ORDERING.md`, and `../Sequo/GUARDRAIL.md`.

Important current API rules:

- Use base path `/api` for implemented routes.
- Send JSON request bodies with `Content-Type: application/json`.
- Protected routes require `Authorization: Bearer <accessToken>`.
- Responses are not wrapped in a universal `{ data, meta }` envelope.
- Money is integer CFA, never floating point.
- Treat `401`, `403`, and `429` distinctly; honor `Retry-After` on rate limits.
- Do not call webhook, admin-only, provider-secret, or settlement/admin finance routes from normal merchant navigation.

## API Endpoints Currently Available

### Authentication

Implemented in `AuthController.kt`; vendor mobile uses `/api/auth` to match the current mobile guide.

| Method | Path | Use In Vendor App |
| --- | --- | --- |
| `POST` | `/api/auth/whatsapp/request` | Request WhatsApp OTP for login. |
| `POST` | `/api/auth/whatsapp/verify` | Verify OTP and create session. Merchant `appSource` is intended to be `SEQUO_MERCHANT`, but the active backend enum still needs that value. |
| `POST` | `/api/auth/social/{provider}` | Social login where configured. |
| `POST` | `/api/auth/passkey/register/challenge` | Start passkey registration for authenticated user. |
| `POST` | `/api/auth/passkey/register/finish` | Finish passkey registration. |
| `POST` | `/api/auth/passkey/authenticate/challenge` | Start passkey auth. |
| `POST` | `/api/auth/passkey/authenticate/finish` | Finish passkey auth and receive tokens. |
| `POST` | `/api/auth/cross-device/initiate` | Start cross-device login. |
| `POST` | `/api/auth/cross-device/resolve` | Resolve cross-device login. |
| `POST` | `/api/auth/refresh` | Refresh rotated token pair with `refreshToken` and `deviceId`. |
| `POST` | `/api/auth/logout` | Revoke supplied refresh token. |
| `POST` | `/api/auth/logout-all` | Revoke all sessions for authenticated user. |
| `GET` | `/api/auth/me` | Read current safe profile and roles. |
| `GET` | `/api/auth/sessions` | List refresh sessions without exposing token material. |
| `DELETE` | `/api/auth/sessions/{sessionId}` | Revoke one session. |

### Merchant Fulfillment

Implemented in `MerchantFulfillmentController.kt` at `/api/merchant/sub-orders`. A merchant user must be in a merchant owner/staff role and scoped to the `merchantId` it sends.

| Method | Path | Body/Query | Use In Vendor App |
| --- | --- | --- | --- |
| `GET` | `/api/merchant/sub-orders` | query `merchantId`, optional `status` | List seller sub-orders. Defaults to active merchant statuses. |
| `GET` | `/api/merchant/sub-orders/{subOrderId}` | optional query `merchantId` | Read one sub-order. |
| `GET` | `/api/merchant/sub-orders/{subOrderId}/sla` | optional query `merchantId` | Read seller response and packing deadlines. |
| `POST` | `/api/merchant/sub-orders/{subOrderId}/accept` | `{ "merchantId": "..." }` | Accept a pending sub-order. |
| `POST` | `/api/merchant/sub-orders/{subOrderId}/start-preparation` | `{ "merchantId": "..." }` | Mark accepted order as being prepared. |
| `POST` | `/api/merchant/sub-orders/{subOrderId}/mark-packed` | `{ "merchantId": "...", "packageCount": 1 }` | Mark packages ready for pickup. |
| `POST` | `/api/merchant/sub-orders/{subOrderId}/handoff` | `{ "merchantId": "..." }` | Confirm courier/Sequo handoff. |
| `POST` | `/api/merchant/sub-orders/{subOrderId}/reject` | `{ "merchantId": "...", "reason": "..." }` | Reject the seller sub-order with a safe reason. |

Merchant sub-order statuses are:

- `MERCHANT_PENDING`
- `ACCEPTED`
- `PREPARING`
- `PACKED_READY`
- `HANDED_TO_COURIER`
- `REJECTED`
- `CANCELLED`

Operator/admin-only fulfillment routes exist but should not be exposed as normal seller actions:

- `POST /api/merchant/sub-orders`
- `POST /api/merchant/sub-orders/sla/publish-overdue`
- `GET /api/merchant/sub-orders/{subOrderId}/escalations`
- `POST /api/merchant/sub-orders/{subOrderId}/escalations`

### Catalog Read

Implemented in `CommerceApi.kt` at `/api/catalog`.

| Method | Path | Query | Use In Vendor App |
| --- | --- | --- | --- |
| `GET` | `/api/catalog/home` | optional `category`, `subcategory` | Read catalog home aggregate. |
| `GET` | `/api/catalog/categories` | none | Read active catalog categories. |
| `GET` | `/api/catalog/merchants` | optional `category`, `subcategory`, `area`, `sort` | Read public merchant storefronts. |
| `GET` | `/api/catalog/products` | optional `category`, `subcategory`, `merchantId`, `includeArchived` | Read products, including seller products by `merchantId`. Archived products require authenticated caller. |
| `GET` | `/api/catalog/products/{productId}/related` | optional `limit`, max 6 | Read dynamic related products. |

Current SequoVendor implementation uses `GET /api/catalog/products?merchantId={merchantId}&includeArchived=true` for authenticated seller catalog browsing. The Catalog screen supports product search, category filtering, active/draft/paused/archived status filtering, camera verification state, CFA price display, local availability summary, and a last-updated label when the API/model exposes one. If the API read fails or the app is in local/demo mode, the screen uses local mock products only.

### Notifications

Available per `MOBILE_API_GUIDE.md`; use `appFamily=SEQUO_MERCHANT`.

| Method | Path | Use In Vendor App |
| --- | --- | --- |
| `POST` | `/api/notifications/devices/fcm` | Register/rotate device token. |
| `DELETE` | `/api/notifications/devices/{appFamily}/{deviceId}` | Revoke the current user's token for a device. |
| `GET` | `/api/notifications/inbox` | Read in-app inbox. |
| `PATCH` | `/api/notifications/inbox/{messageId}/read` | Mark notification read. |
| `POST` | `/api/notifications/inbox/{messageId}/archive` | Archive notification. |
| `DELETE` | `/api/notifications/inbox/{messageId}/archive` | Unarchive notification. |
| `GET` | `/api/notifications/preferences/{appFamily}/effective` | Read effective preference for an event type. |
| `PUT` | `/api/notifications/preferences/{appFamily}` | Save one event preference. |

### Preferences And Account

| Method | Path | Use In Vendor App |
| --- | --- | --- |
| `GET` | `/api/preferences?appFamily=SEQUO_MERCHANT` | Read app preferences. |
| `PATCH` | `/api/preferences?appFamily=SEQUO_MERCHANT` | Update safe preferences such as theme/language. |
| `POST` | `/api/account/deletion-requests` | Request audited account deletion. |

### Settlement Read

Available but should be treated carefully and scoped to merchant finance UI.

| Method | Path | Query | Use In Vendor App |
| --- | --- | --- | --- |
| `GET` | `/api/settlements/merchant-payouts` | `merchantId`, optional `status` | Read merchant payout state when merchant role/scope allows it. |

## API Endpoints Missing Or Not Mobile-Ready

These are required for a complete seller app but should not be called until active, mobile-safe backend controllers exist.

### Seller Catalog Management

Current `CommerceApi.kt` exposes global product mutation routes:

- `POST /api/catalog/products`
- `PATCH /api/catalog/products/{productId}`
- `POST /api/catalog/products/{productId}/archive`

These are not currently safe enough for normal vendor mobile use because the controller does not enforce a merchant-owner route shape like the target `/merchant/products` contract in `API_SPEC.md`.

Needed mobile-ready seller endpoints:

- `GET /api/merchant/products`
- `POST /api/merchant/products`
- `PATCH /api/merchant/products/{productId}`
- `POST` or `PATCH /api/merchant/products/{productId}/archive`
- `PATCH /api/merchant/products/{productId}/availability`
- `PATCH /api/merchant/products/{productId}/stock`
- `PATCH /api/merchant/products/{productId}/bargaining`
- `POST /api/merchant/products/{productId}/photos`
- seller-scoped stock, option, variant, and return-eligibility updates
- seller-scoped validation that authenticated merchant owns the product
- seller-scoped `updatedAt`, stock/out-of-stock, draft, paused, and media verification fields in read responses

### Product Media And Real-Time Photo Evidence

Customer rules require seller-specific real-time camera photos. Missing vendor-ready contracts:

- create/upload live camera capture evidence
- attach approved generic catalog reference for generic sealed items
- read media verification state and freshness
- reject gallery/internet uploads server-side for seller-specific products

### Store Status And Hours

Hub opening-hours endpoints exist for relay/hub apps, but merchant store hours/status are not available as a seller contract.

Current SequoVendor implementation: the Store screen uses a `VendorStoreRepository` interface with a `LocalVendorStoreRepository` fake/local implementation. It lets a seller mark the store open/closed, enter a temporary closure reason, see a last-updated local marker, and preview the customer-facing availability message. It does not call `/api/hub/opening-hours` or `/api/hub/control-state` because those routes are relay/hub scoped, not merchant scoped.

Weekly opening hours and temporary exceptions are also local-only for now. The app supports one row per weekday, closed-day time omission, open-day `HH:mm` validation, "copy Monday to weekdays", and temporary date exceptions with closed/open override plus reason. This state is intentionally kept behind `VendorStoreRepository` until a merchant-owned backend contract exists.

Needed:

- `GET /api/merchant/store-availability?merchantId=...` or `GET /api/merchant/store-settings?merchantId=...`
- `PUT /api/merchant/store-availability` or `PUT /api/merchant/store-settings`
- open/closed state
- weekly hours and temporary exceptions
- prep time/closure note
- server-generated effective availability state
- merchant ownership/scope validation for owner and staff users

### Merchant Staff And Merchant Selection

Auth tokens include merchant scope IDs, but a vendor app still needs mobile-ready UX contracts for:

- listing the authenticated user's merchant memberships
- switching active merchant when a staff user belongs to multiple merchants
- inviting/removing staff
- role-specific permissions inside the merchant app

### Merchant Bargaining

Customer bargaining endpoints exist. Target merchant bargaining actions are listed in `API_SPEC.md`, but active merchant-side controller routes are not present in `CommerceApi.kt`.

Needed:

- read merchant bargaining threads/offers by merchant
- accept/reject/counter customer offers
- enforce attempt limits and 24-hour lock windows server-side

### Order Detail Completeness

Merchant sub-orders currently expose seller accounting and lifecycle fields, but vendor UI may need richer mobile-ready detail:

- line items belonging to the merchant sub-order
- product snapshots/options/variants
- safe customer delivery context where allowed
- package labels or pickup instructions
- rider/courier handoff summary without exposing private proof/PIN data

### Realtime

`MOBILE_API_GUIDE.md` explicitly lists WebSocket/STOMP realtime as not mobile-ready yet. Vendor app should use polling/refresh until realtime subscription contracts are finalized.

### Payment Initiation And Provider Operations

Provider webhooks and payment validation are server/provider concerns. Vendor app must not call payment webhook routes or expose provider-secret operations.

### Admin And Operations

Admin, SLA publishing, commission overrides, settlement ledger, dispatch, courier assignment, relay parcel administration, and webhook routes are not normal seller-mobile features.

## Implementation Phases

### Phase 0: Contract And Skeleton

- Keep `README.md` and API inventory current.
- Keep app modules building for Android and iOS targets.
- Establish repository interfaces for auth, merchant sub-orders, catalog read, store settings, notifications, and preferences.
- Add typed DTOs matching active controllers only.

### Phase 1: Auth And Session Safety

- Implement WhatsApp OTP login with `appSource=SEQUO_MERCHANT`.
- Add secure token storage per platform.
- Add refresh/logout/session handling.
- Add merchant-scope selection after login.
- Add user-safe handling for `401`, `403`, and `429`.

### Phase 2: Merchant Orders MVP

- List active merchant sub-orders.
- Filter by `MerchantSubOrderStatus`.
- Show SLA deadlines.
- Show order detail using currently available safe fields.
- Wire seller actions: accept, start preparation, mark packed, handoff, reject.
- Add duplicate-submit protection and clean retry/error states.

### Phase 3: Catalog Read And Local Drafts

- Read merchant products through `/api/catalog/products?merchantId=...`.
- Show camera verification, bargaining, status, category, and price fields.
- Provide local draft/editor UI only, clearly marked as non-persistent until seller-scoped write endpoints exist.

### Phase 4: Store Status, Hours, And Notifications

- Implement store status/hours repository against new merchant settings endpoints when available.
- Register FCM with `appFamily=SEQUO_MERCHANT`.
- Add notification inbox and preferences.
- Keep realtime behind an interface until WebSocket/STOMP mobile contract is ready.

### Phase 5: Seller Catalog Writes And Media

- Integrate seller-scoped product create/update/archive endpoints after backend authorization is ready.
- Add real-time camera capture workflow and media verification display.
- Add stock, variants/options, return eligibility, and bargaining controls.

### Phase 6: Merchant Finance And Staff

- Add scoped payout/settlement read views where allowed.
- Add merchant staff/member management after backend contracts exist.
- Keep admin/ledger/provider routes out of seller navigation.

## Build Commands

Exact commands used in the final integration pass:

```bash
./gradlew :androidApp:compileDebugKotlin
```

```bash
./gradlew :shared:testAndroidHostTest
```

```bash
./gradlew :androidApp:assembleDebug
```

Other useful commands:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

## Current Repository Notes

- This repository is a Kotlin Multiplatform app with `androidApp`, `iosApp`, and `shared` modules.
- Treat generated `build/` artifacts and local screenshots as development output, not source-of-truth product documentation.
- Do not call target/future backend routes until active controllers exist and enforce merchant scope.
