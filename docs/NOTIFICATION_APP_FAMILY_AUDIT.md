# Notification App Family Audit

Source of truth: `/Users/muhirwagabooreste/AndroidStudioProjects/SequoService/sequo-api/src/main/kotlin/dev/orestegabo/sequo_api/domain/notification`.

This audit only covers notification app-family support that is present in the contract/API source and the current app code. It does not add new product features.

## Backend Support

The API supports these notification app families in `NotificationAppFamily`:

| App family | Intended app |
| --- | --- |
| `SEQUO_CUSTOMER` | Customer app |
| `SEQUO_MERCHANT` | Vendor / merchant app |
| `SEQUO_HUB` | Hub / relay app |
| `SEQUO_RIDER` | Rider / courier app |
| `SEQUO_ADMIN` | Admin / operations web app |

Available notification routes:

- `POST /api/notifications/devices/fcm`
- `DELETE /api/notifications/devices/{appFamily}/{deviceId}`
- `GET /api/notifications/inbox`
- `PATCH /api/notifications/inbox/{messageId}/read`
- `POST /api/notifications/inbox/{messageId}/archive`
- `DELETE /api/notifications/inbox/{messageId}/archive`
- `GET /api/notifications/preferences/{appFamily}/effective`
- `PUT /api/notifications/preferences/{appFamily}`

The routing service maps recipients to app families as follows:

| Recipient | App family | Status |
| --- | --- | --- |
| Customer | `SEQUO_CUSTOMER` | Supported by API routing |
| Merchant owner / staff | `SEQUO_MERCHANT` | Supported by API routing |
| Relay partner | `SEQUO_HUB` | Supported by API routing |
| Courier / rider | `SEQUO_RIDER` | Supported by API routing |
| Support / admin / super admin | `SEQUO_ADMIN` | Supported by API routing |

## App Implementation Status

| Project | Expected app family | Current status | Evidence |
| --- | --- | --- | --- |
| Sequo | `SEQUO_CUSTOMER` | Partial | Customer app has a notification screen and localized strings, but the scanned notification UI uses local/demo data and no notification API client was found in the customer app notification package. |
| SequoVendor | `SEQUO_MERCHANT` | Done | `core/notifications/NotificationDtos.kt` sets `SequoMerchantAppFamily = "SEQUO_MERCHANT"` and the client uses that value for device unregister, inbox filtering, and preferences. |
| SequoRider | `SEQUO_RIDER` | Missing | Backend supports `SEQUO_RIDER`, but the rider app currently has no notification client, inbox, device registration, or preferences implementation. |
| SequoHub | `SEQUO_HUB` | Partial | Hub has localized notification UI and settings callbacks, but the scanned code uses sample/local notification messages and empty callback wiring in `App.kt`; no hub notification API client was found. |
| SequoAdmin | `SEQUO_ADMIN` | Missing / scaffold | Backend supports admin routing, but the admin app is currently only a web template/scaffold and has no notification integration. |

## Gaps To Close

- Implement rider notification integration with `SEQUO_RIDER`.
- Add customer notification API wiring with `SEQUO_CUSTOMER` if the customer app should use the shared backend inbox instead of local/demo data.
- Add hub notification API wiring with `SEQUO_HUB` for relay pickup, return, hold, and blocked-flow alerts.
- Add admin notification API wiring with `SEQUO_ADMIN` once SequoAdmin leaves scaffold state.

## Notes

- The API already has the app-family enum values needed by all current apps.
- Apps must keep using `/api`, never `/api/v1`.
- Apps must not display raw backend errors or log FCM/device tokens.
- Apps must not ship Firebase server credentials; device registration sends client FCM tokens only to the API.
