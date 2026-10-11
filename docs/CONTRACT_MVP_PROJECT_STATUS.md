# Sequo Vendor MVP Status

Source contract: `/Users/muhirwagabooreste/AndroidStudioProjects/SequoService/sequo-api/docs/contracts/contrat_mvp_sequo.tex`

This file tracks only contract-listed MVP scope for the vendor app.

| Contract item | Status | Progress | Evidence | Remaining MVP work |
| --- | --- | ---: | --- | --- |
| Vendor login/account context | In progress | 90% | `shared/src/commonMain/kotlin/dev/orestegabo/sequovendor/core/auth`, `ui/account` | Replace temporary merchant selection when API exposes merchant context robustly. |
| Merchant receives orders | In progress | 85% | `core/fulfillment`, `ui/dashboard`, `ui/orders` | Validate with real merchant accounts and production data. |
| Merchant accepts/rejects/prepares/marks ready | In progress | 85% | `MerchantFulfillmentApiClient`, order detail actions | Continue duplicate-submit and localized error QA. |
| Merchant adds/edits/deactivates products | In progress | 65% | `core/catalog`, `ui/catalog` | Backend merchant catalog write/deactivate/upload routes are still required. |
| Product photos follow real-time policy | In progress | 50% | Product editor photo-status placeholder, API media policy | Needs merchant product photo endpoint and camera upload flow. |
| Food options/toppings/variants | In progress | 45% | API food customization domain, local editor roadmap | Needs vendor editor support and backend write route. |
| Seller can enable/disable bargaining | In progress | 35% | Catalog model/UI placeholders | Needs local editor controls and merchant backend field. |
| Store availability/opening hours | In progress | 65% | `ui/store`, repository interface/local store | Needs merchant-scoped store status/hours API. |
| Merchant follows sales/returns/payment statuses | In progress | 60% | Dashboard/order/account screens | Needs richer settlement/return visibility endpoints. |
| Notifications for merchant events | In progress | 80% | `core/notifications`, app family `SEQUO_MERCHANT` | Final device registration and production push QA. |

Recommended verification:

```bash
./gradlew :androidApp:compileDebugKotlin
./gradlew :shared:testAndroidHostTest
```
