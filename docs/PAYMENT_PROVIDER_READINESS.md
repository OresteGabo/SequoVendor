# Payment Provider Readiness

Source prompts and contract tracker:

- `/Users/muhirwagabooreste/AndroidStudioProjects/SequoService/sequo-api/docs/contracts/CONTRACT_MVP_TRACKER.md`
- `/Users/muhirwagabooreste/AndroidStudioProjects/SequoService/Sequo/PAYMENTS_AND_REFERRALS.md`
- `/Users/muhirwagabooreste/AndroidStudioProjects/SequoService/sequo-api/src/main/kotlin/dev/orestegabo/sequo_api/domain/payment`

This audit covers the contract MVP requirement to prepare Yas Togo and Moov Africa payments without adding live credentials to source control.

## Current Implementation

| Area | Current evidence | Status |
| --- | --- | --- |
| Provider identifiers | `SequoPaymentProviders.YasTogo`, `SequoPaymentProviders.MoovAfrica` in `PaymentProcessing.kt` | Present |
| Payment policy | `PaymentFeature`, `ConfigurablePaymentPolicy`, country/provider rules in `PaymentProcessing.kt` | Present |
| Payment attempt abstraction | `SequoPaymentMethod.createPaymentAttempt` and `PaymentAttemptResult` | Present, currently handler-based |
| Payment validation abstraction | `SequoPaymentMethod.validatePayment` and `PaymentValidationStatus` | Present, default app config keeps validation pending |
| Cancellation abstraction | `SequoPaymentMethod.cancelPayment` and `PaymentActionResult` | Present, currently handler-based |
| Refund abstraction | `SequoPaymentMethod.refundPayment` and `PaymentRefundResult` | Present, currently handler-based |
| Webhook endpoint | `POST /api/payments/webhooks/{provider}` in `PaymentWebhookController.kt` | Present |
| Webhook signature/idempotency | `PaymentWebhookService` verifies HMAC signature, replay window and duplicate event IDs | Present |
| Order payment dependency | `OrderProcessing.kt` rejects non-validated payment and creates pending checkout states | Partial until real provider status checks are wired |
| Merchant settlement dependency | `domain/settlement` records merchant payout accruals | Present for merchant accruals |
| Courier payout dependency | Contract requires courier payout visibility/execution | Partial; payout visibility still needs rider-scoped endpoint/app wiring |

## Missing Provider Credentials And Config

No real provider secrets should be committed. Production readiness needs environment-backed configuration for each provider.

Suggested placeholder environment variables:

| Provider | Placeholder variables |
| --- | --- |
| Yas Togo | `YAS_TOGO_API_BASE_URL`, `YAS_TOGO_API_KEY`, `YAS_TOGO_MERCHANT_ID`, `YAS_TOGO_WEBHOOK_SECRET`, `YAS_TOGO_CALLBACK_PUBLIC_URL` |
| Moov Africa | `MOOV_AFRICA_API_BASE_URL`, `MOOV_AFRICA_API_KEY`, `MOOV_AFRICA_MERCHANT_ID`, `MOOV_AFRICA_WEBHOOK_SECRET`, `MOOV_AFRICA_CALLBACK_PUBLIC_URL` |
| Shared payment safety | `SEQUO_PAYMENT_WEBHOOK_REPLAY_WINDOW_SECONDS`, `SEQUO_PAYMENT_STATUS_TIMEOUT_SECONDS`, `SEQUO_PAYMENT_IDEMPOTENCY_SECRET` |

Existing source already references API keys and webhook secrets in environment-backed properties. Production still needs provider base URLs, merchant account identifiers, callback URLs, and adapter configuration that fails closed when any required value is missing.

## Callback And Webhook Requirements

- Provider callbacks must post to `/api/payments/webhooks/{provider}` with `provider` equal to `yas_togo` or `moov_africa`.
- Requests must include `X-Sequo-Webhook-Timestamp` and `X-Sequo-Webhook-Signature`.
- The backend verifies a HMAC-SHA256 signature against the provider webhook secret.
- Events must be idempotent by provider and event ID.
- Reused event IDs with different payload hashes are rejected.
- Callback payloads must include `eventId`, `checkoutId`, `paymentReference`, `amountCfa`, `status`, and `occurredAt`.

Production provider adapters still need exact field mapping for Yas Togo and Moov Africa payloads.

## Order Validation Dependency

The customer order flow must not mark an order complete unless the payment status is validated. Existing `OrderProcessing.kt` already treats pending, failed, cancelled and blocked payment states as non-complete states.

Remaining work:

- Add provider adapters that create a provider payment reference instead of using local handler defaults.
- Add status-check calls that convert provider status into `Pending`, `Validated`, `Failed` or `Cancelled`.
- Ensure customer apps show pending/failure states and do not fake success.

## Refund Dependency

The contract requires refunds after SEQUO receives and checks returned products. The current payment domain has a refund abstraction, but production refunds need:

- Provider refund API mapping.
- Refund idempotency key.
- Safe state for pending refund instructions.
- Admin/support visibility for failed refund instructions.
- Tests proving no cash-refund path is exposed through relay or rider apps.

## Courier Payout Dependency

Courier payout execution is not the same as payout visibility. For MVP, riders need safe visibility of their own eligible or historical payout records; real payout execution can remain behind provider adapters until provider credentials and business approval are ready.

Production payout execution needs:

- Provider payout API mapping.
- Courier wallet account validation.
- Salaried courier exclusion from freelancer payable totals.
- Idempotent payout instruction records.
- Admin retry/failure handling.

## Readiness Summary

| Capability | Readiness | Notes |
| --- | ---: | --- |
| Policy and supported provider model | 85% | Domain model exists. |
| Customer payment validation rule | 70% | Backend rule exists; production provider adapters are missing. |
| Yas Togo live checkout | 30% | Placeholder config exists; real adapter/status mapping missing. |
| Moov Africa live checkout | 30% | Placeholder config exists; real adapter/status mapping missing. |
| Webhook safety | 75% | Generic signature/idempotency exists; provider payload mapping must be finalized. |
| Refunds | 35% | Domain abstraction exists; live provider refund execution missing. |
| Courier payouts | 25% | Settlement domain exists; rider visibility and provider payout execution need work. |

## MVP Blocking Items

- Add provider-neutral adapter interfaces with fail-closed Yas Togo and Moov Africa implementations.
- Add tests proving missing config returns safe pending/failed results, never fake success.
- Wire customer payment UI to pending/validated/failed/cancelled states.
- Expose rider payout visibility endpoints before wiring the rider payout screen.
- Keep live provider credentials out of Git and inject them through deployment environment variables.
