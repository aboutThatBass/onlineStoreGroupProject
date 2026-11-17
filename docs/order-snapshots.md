# Order snapshots

New orders save checkout contact/shipping details and a server-calculated receipt in `order_snapshots`, in the same transaction as the order, purchased prices, stock updates, and cart removal. Account and catalog edits do not rewrite these receipts. Checkout requires a customer object with firstName, lastName, email, address, city, state, and zipCode. Client-supplied money is not accepted.

Restart the Spring Boot backend before testing. Its existing startup SQL creates the additional table for both old and fresh databases without deleting or backfilling orders. Older orders retain their totals and item prices; their views explain that shipping and tax/discount details were not recorded. Do not reset the database to use this feature.

## Local review

1. Restart backend and frontend. Sign in as a customer and add a product.
2. Enter a shipping name/address different from your account profile. Apply LABUB00, then place the order.
3. Check the confirmation and Profile -> Orders for shipping, subtotal, code/discount amount, 8.25% tax, and total.
4. Refresh the browser, edit the account name/address, and verify the old receipt is unchanged.
5. Sign in as an administrator and open the same order's details. Confirm the saved recipient/address and receipt match the customer view.
6. Repeat with no code and with TUNGTUNG22. Check an older order displays the unavailable-details message while retaining its original total.

Automated coverage: `./mvnw verify` (Windows: `.\mvnw.cmd verify`). Snapshot integration tests use a separate in-memory SQLite database, not `viralVault.db`.
