# PocketCodedWeb Google Play Billing Setup

PocketCodedWeb uses a one-time, non-consumable Google Play product.

## Product

- Product ID: `pocketcodedweb_lifetime`
- Product type: One-time product
- Suggested display name: `PocketCodedWeb Premium`
- Base price: keep the price already configured in Google Play Console

The Android app must be listed as free to install. Google Play does not support changing an app
from paid to free-trial-plus-IAP behavior automatically. Premium access is sold through the
one-time product above.

## Play Console

1. Upload a signed Android App Bundle with the Billing implementation to an internal testing track.
2. Open **Monetize with Play > Products > One-time products**.
3. Create `pocketcodedweb_lifetime` exactly. Product IDs cannot be changed later.
4. Add and activate a purchase option for a one-time, non-consumable purchase.
5. Keep the existing South African base price unless you intentionally want to change the product price.
6. Review every regional price. Google Play converts the base price automatically. Manually adjust
   supported currencies to the closest locally valid price ending in `.99`. Some currencies, such
   as currencies without minor units, cannot use `.99`; use the closest valid local price there.
7. Activate the product.
8. Add tester Gmail accounts under **Settings > License testing** and include them in the internal track.
9. Install PocketCodedWeb from the tester's Google Play opt-in link. Sideloaded APKs cannot reliably
   load or purchase Play products.

The paywall displays the localized price supplied by Google Play. It intentionally does not perform
currency conversion inside the app.

## Testing

Verify these cases with Play license testers:

- Successful purchase immediately unlocks Premium after Google Play reports `PURCHASED`.
- A pending payment stays locked until Google Play changes it to `PURCHASED`.
- Cancelling checkout does not unlock Premium.
- **Restore Purchase** unlocks the app for the Google Play account which owns the product.
- Clearing app data and tapping **Restore Purchase** restores the purchase.
- Refunded or revoked purchases are removed after the next successful Play Billing reconciliation.
- A purchase made under another signed-in PocketCodedWeb account does not unlock this account.

## Production Security

The app validates the Google Play product ID, Google Play order ID prefix, obfuscated app account
ID, and purchase timing before unlocking Premium. Premium state is cached locally for offline
continuity and mirrored to `users/{uid}.isPremium` in Firestore for backend quota decisions.

Purchases are verified by the `verifyPlayPurchase` Firebase Function using the Google Play
Developer API before Premium is granted. Firestore billing fields and complete `playPurchases`
records are server-only. Real-time developer notifications and scheduled voided-purchase
reconciliation remove refunded or revoked entitlements.

The paywall never treats this document's base-price description as a price source. It displays the
localized `formattedPrice` returned by Google Play for the active tester, country, and purchase
option. See [FIREBASE_PREMIUM_DEPLOYMENT.md](FIREBASE_PREMIUM_DEPLOYMENT.md) for the complete
backend, Firebase, App Check, Pub/Sub, and deployment procedure.

