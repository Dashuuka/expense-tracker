# Functional Requirements

## Use Case Diagram (text)

```
Actor: User
─────────────────────────────────────────
UC-01  Add a transaction
UC-02  Edit a transaction
UC-03  Delete a transaction (swipe)
UC-04  View transaction list (filter by period)
UC-05  View balance summary (income / expense)
UC-06  View statistics (by category, daily chart)
UC-07  Set a category budget
UC-08  Change display currency
UC-09  View live exchange rates
UC-10  Receive budget-exceeded notification
UC-11  Receive currency-rate-updated notification
```

---

## Text Scenarios

### UC-01 Add a Transaction

| Field | Value |
|---|---|
| **Trigger** | User taps FAB (+) on Home screen |
| **Pre-condition** | App open, Home screen visible |
| **Main flow** | 1. Screen "Add Transaction" opens. 2. User toggles Expense / Income. 3. User enters amount (decimal, up to 10 digits). 4. User selects a category from the grid. 5. User optionally changes date via date picker. 6. User optionally enters a note. 7. User taps Save. 8. Transaction saved to Room DB. 9. App navigates back to Home. |
| **Alternative** | 3a. Amount is empty or 0 → validation error shown, no save. 4a. No category selected → validation error shown. |
| **Post-condition** | New transaction appears in the list on Home. |

---

### UC-03 Delete a Transaction

| Field | Value |
|---|---|
| **Trigger** | User swipes a transaction card to the left |
| **Main flow** | 1. Delete icon appears behind card. 2. Confirmation dialog: "Delete transaction? This action cannot be undone." 3. User taps Delete. 4. Transaction removed from DB. |
| **Alternative** | 3a. User taps Cancel → card snaps back, nothing deleted. |

---

### UC-09 View Live Exchange Rates

| Field | Value |
|---|---|
| **Trigger** | User opens Settings screen (rates loaded automatically) |
| **Main flow** | 1. App calls NBRB API (`api.nbrb.by/exrates/rates`). 2. Rates for USD, EUR, RUB, CNY, GBP displayed in table. 3. User may tap Refresh to force-reload. |
| **Alternative** | Network unavailable → error message shown below the table. |
| **Background** | WorkManager runs `CurrencyRefreshWorker` every 6 hours automatically. |
