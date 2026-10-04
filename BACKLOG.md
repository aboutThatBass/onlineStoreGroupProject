# Viral Vault backlog

Everything we need to build, in roughly the order to build it. Put your name after **Owner:** when you take a feature. Check off each item as it works; a feature is done when every box is checked and its pull request has been merged.

### F01 - Pick frontend and database

Owner:

- [ ] Team agrees on the frontend (Svelte or Thymeleaf) and database (SQLite or other).
- [ ] The choice and the reason are written in the README.
- [ ] Everyone can run the app on their own computer.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F02 - Database setup with sample data

Owner:

- [ ] Tables exist for users, products, orders, and order items.
- [ ] Anyone can set up the database with sample products on their computer.
- [ ] Data is still there after restarting the app.
- [ ] No real passwords are committed.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F03 - Product catalog and product pages

Owner:

- [ ] The main page shows products from the database.
- [ ] Each product shows its image, name, price, and **available quantity on the main page** (required).
- [ ] Each product has a page with its description and sale info.
- [ ] Sample products fit the trending theme (Labubus, Stanley tumblers, etc.).

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F04 - Search and sorting

Owner:

- [ ] Search by product name or description.
- [ ] Sort by price and by availability.
- [ ] A message shows when nothing matches.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F05 - Featured trending products

Owner:

- [ ] Admins can choose featured products without changing code.
- [ ] Featured products show on the homepage.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F06 - Register, log in, edit profile

Owner:

- [ ] Customers can register, log in, and log out.
- [ ] Customers can change their account info, including their name.
- [ ] Passwords are stored securely, not as plain text.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F07 - Customer order history

Owner:

- [ ] Logged-in customers can see their past orders.
- [ ] Customers can't see other people's orders.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F08 - Shopping cart

Owner:

- [ ] Add items, change quantities, and remove items.
- [ ] Cart shows each item, quantity, price, and line total.
- [ ] Can't add more than what's in stock.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F09 - Tax, discount codes, sale prices

Owner:

- [ ] Cart shows subtotal, discount, tax (8.25%), and total.
- [ ] Shows which discount code was used and how much it saved.
- [ ] Sale prices are clearly marked.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F10 - Checkout and place order

Owner:

- [ ] Customer sees an order summary and can place the order.
- [ ] The order is saved and a confirmation is shown.
- [ ] Stock goes down after an order, and can't go below zero.
- [ ] No real payment needed.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F11 - Admin: manage products

Owner:

- [ ] Admin pages to add and edit products (image, price, description, stock).
- [ ] Regular customers can't use admin pages, even by typing the URL.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F12 - Admin: discount codes and sales

Owner:

- [ ] Admins can create discount codes and put items on sale.
- [ ] Changes show up in the store and the cart.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F13 - Admin: manage users

Owner:

- [ ] Admins can view and edit users.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F14 - Admin: view and sort orders

Owner:

- [ ] Admins can see current orders and order history.
- [ ] Orders can be sorted by date, customer, and dollar amount.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F15 - Look and feel, mobile friendly

Owner:

- [ ] Consistent colors and easy navigation.
- [ ] Works on phone screens (bonus points).

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F16 - Testing

Owner:

- [ ] Tests cover prices/tax, stock, orders, and admin-only access.
- [ ] Walk through the full customer and admin flows before the demo.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F17 - Final report

Owner:

- [ ] About 10 pages, with diagrams and workflows.
- [ ] Includes the feature list, how each feature was built, and the integration count.
- [ ] One person submits it for the group.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F18 - Demo and peer reviews

Owner:

- [ ] Practice the 15-minute demo with sample data.
- [ ] Everyone attends and shows what they built.
- [ ] Everyone submits their own peer review.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):

### F19 - Host on the cloud (bonus)

Owner:

- [ ] The app is online and working.
- [ ] Only after everything else works locally.

Critical (yes/no):

How built (LLM, existing component, or low-code):

Pull request merged (yes/no):
