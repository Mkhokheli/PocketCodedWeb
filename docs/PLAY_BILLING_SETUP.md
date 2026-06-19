# PocketCodedPy Google Play Billing Setup

PocketCodedPy uses a one-time, non-consumable Google Play product.

## Product

- Product ID: `pocketcodedpy_lifetime`
- Product type: One-time product
- Suggested display name: `PocketCodedPy Premium`
- Base price: `R99.99` in South African rand

The Android app must be listed as free to install. Google Play does not support changing an app
from paid to free-trial-plus-IAP behavior automatically. Premium access is sold through the
one-time product above.

## Play Console

1. Upload a signed Android App Bundle with the Billing implementation to an internal testing track.
2. Open **Monetize with Play > Products > One-time products**.
3. Create `pocketcodedpy_lifetime` exactly. Product IDs cannot be changed later.
4. Add and activate a purchase option for a one-time, non-consumable purchase.
5. Set the South African base price to `R99.99`.
6. Review every regional price. Google Play converts the base price automatically. Manually adjust
   supported currencies to the closest locally valid price ending in `.99`. Some currencies, such
   as currencies without minor units, cannot use `.99`; use the closest valid local price there.
7. Activate the product.
8. Add tester Gmail accounts under **Settings > License testing** and include them in the internal track.
9. Install PocketCodedPy from the tester's Google Play opt-in link. Sideloaded APKs cannot reliably
   load or purchase Play products.

The paywall displays the localized price supplied by Google Play. It intentionally does not perform
currency conversion inside the app.

## Testing

Verify these cases with Play license testers:

- Successful purchase immediately unlocks Premium.
- A pending payment stays locked until Google Play changes it to `PURCHASED`.
- Cancelling checkout does not unlock Premium.
- **Restore Purchase** unlocks the app for the Google Play account which owns the product.
- Clearing app data and signing in again restores the purchase.
- Refunded or revoked purchases are removed after the next successful Play Billing reconciliation.

## Production Security

The current app acknowledges and checks purchases with Google Play Billing on-device. Before a
large production launch, add server-side purchase-token verification using the Google Play
Developer API and grant entitlements from a trusted backend. Never grant Premium from an editable
Firestore boolean supplied by the Android client.
