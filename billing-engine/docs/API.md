# Billing Engine API Documentation

## Core Billing API

### BillingEngine
- `BillingEngine(config)`: Initialize billing engine with configuration
- `async initialize()`: Initialize database connections and external integrations
- `async shutdown()`: Gracefully shutdown all connections
- `getStatus()`: Get engine status and health information
- `getMetrics()`: Get billing metrics and statistics
- `getConfiguration()`: Get current billing configuration
- `updateConfiguration(config)`: Update billing configuration dynamically
- `registerWebhook(url, events)`: Register webhook endpoint
- `unregisterWebhook(webhookId)`: Unregister webhook endpoint
- `testWebhook(webhookId)`: Send test event to webhook
- `getWebhooks()`: List all registered webhooks
- `clearCache()`: Clear billing cache
- `reloadConfiguration()`: Reload configuration from database

### CustomerManager
- `CustomerManager(billingEngine)`: Initialize customer manager
- `async createCustomer(customerData)`: Create new customer record
- `async updateCustomer(customerId, updateData)`: Update customer information
- `async getCustomer(customerId)`: Retrieve customer details
- `async deleteCustomer(customerId)`: Delete customer and associated data
- `async listCustomers(filters, pagination)`: List customers with filtering
- `async searchCustomers(query)`: Search customers by various criteria
- `async getCustomerBalance(customerId)`: Get customer account balance
- `async addCredit(customerId, amount, description)`: Add credit to customer account
- `async deductCredit(customerId, amount, description)`: Deduct credit from customer account
- `async getPaymentMethods(customerId)`: Get customer payment methods
- `async addPaymentMethod(customerId, paymentMethodData)`: Add payment method
- `async removePaymentMethod(customerId, paymentMethodId)`: Remove payment method
- `async setDefaultPaymentMethod(customerId, paymentMethodId)`: Set default payment method
- `async getCustomerInvoices(customerId, filters)`: Get customer invoices
- `async getCustomerSubscriptions(customerId)`: Get customer subscriptions
- `async getCustomerTransactions(customerId, filters)`: Get transaction history
- `async calculateLifetimeValue(customerId)`: Calculate customer LTV

### SubscriptionManager
- `SubscriptionManager(billingEngine)`: Initialize subscription manager
- `async createPlan(planData)`: Create subscription plan
- `async updatePlan(planId, updateData)`: Update existing plan
- `async deletePlan(planId)`: Delete subscription plan
- `async getPlan(planId)`: Get plan details
- `async listPlans(filters)`: List all plans
- `async createSubscription(subscriptionData)`: Create customer subscription
- `async updateSubscription(subscriptionId, updateData)`: Update subscription
- `async cancelSubscription(subscriptionId, options)`: Cancel subscription
- `async pauseSubscription(subscriptionId, options)`: Pause subscription
- `async resumeSubscription(subscriptionId)`: Resume paused subscription
- `async getSubscription(subscriptionId)`: Get subscription details
- `async listSubscriptions(filters)`: List subscriptions
- `async changeSubscriptionPlan(subscriptionId, newPlanId, options)`: Change subscription plan
- `async addAddon(subscriptionId, addonData)`: Add subscription addon
- `async removeAddon(subscriptionId, addonId)`: Remove subscription addon
- `async applyCoupon(subscriptionId, couponCode)`: Apply discount coupon
- `async removeCoupon(subscriptionId)`: Remove applied coupon
- `async getUpcomingInvoice(subscriptionId)`: Get preview of next invoice
- `async scheduleSubscriptionChange(subscriptionId, changes, effectiveDate)`: Schedule future changes
- `async getSubscriptionHistory(subscriptionId)`: Get subscription change history

### InvoiceGenerator
- `InvoiceGenerator(billingEngine)`: Initialize invoice generator
- `async generateInvoice(invoiceData)`: Generate new invoice
- `async generateFromSubscription(subscriptionId, billingPeriod)`: Generate subscription invoice
- `async updateInvoice(invoiceId, updateData)`: Update invoice details
- `async addLineItem(invoiceId, lineItem)`: Add line item to invoice
- `async removeLineItem(invoiceId, lineItemId)`: Remove line item from invoice
- `async updateLineItem(invoiceId, lineItemId, updateData)`: Update line item
- `async applyDiscount(invoiceId, discount)`: Apply discount to invoice
- `async removeDiscount(invoiceId, discountId)`: Remove discount from invoice
- `async applyTax(invoiceId, taxData)`: Apply tax to invoice
- `async finalizeInvoice(invoiceId)`: Finalize draft invoice
- `async sendInvoice(invoiceId, options)`: Send invoice to customer
- `async markInvoicePaid(invoiceId, paymentData)`: Mark invoice as paid
- `async markInvoicePartiallyPaid(invoiceId, paymentData)`: Mark partial payment
- `async voidInvoice(invoiceId, reason)`: Void invoice
- `async duplicateInvoice(invoiceId)`: Create duplicate invoice
- `async generatePDF(invoiceId, options)`: Generate PDF invoice
- `async getInvoice(invoiceId)`: Get invoice details
- `async listInvoices(filters)`: List invoices
- `async getInvoicePayments(invoiceId)`: Get invoice payment history
- `async scheduleInvoice(invoiceData, scheduleDate)`: Schedule future invoice

### PaymentProcessor
- `PaymentProcessor(billingEngine)`: Initialize payment processor
- `async processPayment(paymentData)`: Process payment transaction
- `async authorizePayment(paymentData)`: Authorize payment without capture
- `async capturePayment(paymentId, amount)`: Capture authorized payment
- `async voidPayment(paymentId)`: Void authorized payment
- `async createRefund(refundData)`: Create payment refund
- `async processPartialRefund(paymentId, amount, reason)`: Process partial refund
- `async getPaymentStatus(paymentId)`: Get payment status
- `async getPaymentDetails(paymentId)`: Get detailed payment information
- `async listPayments(filters, pagination)`: List payments with filtering
- `async retryFailedPayment(paymentId, options)`: Retry failed payment
- `async schedulePaymentRetry(paymentId, retrySchedule)`: Schedule automatic retry
- `async validatePaymentMethod(paymentMethodData)`: Validate payment method
- `async tokenizePaymentMethod(paymentMethodData)`: Tokenize payment information
- `async handleWebhook(webhookData, signature)`: Process payment webhook events
- `async getPaymentHistory(customerId, filters)`: Get customer payment history
- `async generatePaymentReport(filters)`: Generate payment analytics report
- `async cancelRecurringPayment(recurringPaymentId)`: Cancel recurring payment
- `async updateRecurringPayment(recurringPaymentId, updateData)`: Update recurring payment

### TaxCalculator
- `TaxCalculator(billingEngine)`: Initialize tax calculator
- `async calculateTax(taxData)`: Calculate tax for transaction
- `async calculateSalesTax(address, amount, productCategory)`: Calculate sales tax
- `async calculateVAT(fromCountry, toCountry, amount, vatNumber)`: Calculate VAT
- `async validateVATNumber(vatNumber, country)`: Validate VAT number
- `async getTaxRates(address)`: Get applicable tax rates
- `async exemptCustomer(customerId, exemptionData)`: Add tax exemption
- `async removeExemption(customerId, exemptionId)`: Remove tax exemption
- `async getExemptions(customerId)`: Get customer exemptions
- `async createTaxReport(period, jurisdiction)`: Generate tax report
- `async submitTaxFiling(filingData)`: Submit tax filing
- `async getTaxJurisdictions()`: Get supported tax jurisdictions
- `async updateTaxRates(jurisdiction, rates)`: Update tax rates
- `async calculateInclusiveTax(totalAmount, address)`: Calculate tax from inclusive amount
- `async getTaxHistory(customerId, period)`: Get customer tax history

### RevenueRecognition
- `RevenueRecognition(billingEngine)`: Initialize revenue recognition
- `async createSchedule(scheduleData)`: Create recognition schedule
- `async updateSchedule(scheduleId, updateData)`: Update recognition schedule
- `async deleteSchedule(scheduleId)`: Delete recognition schedule
- `async generateEntries(scheduleId, period)`: Generate recognition entries
- `async postEntries(entries)`: Post recognition entries
- `async reverseEntries(entryIds, reason)`: Reverse recognition entries
- `async getSchedule(scheduleId)`: Get schedule details
- `async listSchedules(filters)`: List recognition schedules
- `async calculateDeferred(period)`: Calculate deferred revenue
- `async generateReport(reportOptions)`: Generate recognition report
- `async exportToGL(period, glSystem)`: Export to general ledger
- `async reconcile(period)`: Reconcile recognition entries
- `async forecast(periods)`: Forecast future recognition
- `async auditTrail(scheduleId)`: Get audit trail for schedule

### UsageBillingManager
- `UsageBillingManager(billingEngine)`: Initialize usage billing manager
- `async createUsagePlan(planData)`: Create usage-based pricing plan
- `async updateUsagePlan(planId, updateData)`: Update usage plan
- `async recordUsage(usageData)`: Record usage event
- `async batchRecordUsage(usageEvents)`: Batch record usage events
- `async getUsage(subscriptionId, period)`: Get usage for period
- `async aggregateUsage(subscriptionId, aggregationType)`: Aggregate usage data
- `async generateUsageInvoice(subscriptionId, billingPeriod)`: Generate usage invoice
- `async createMeter(meterData)`: Create usage meter
- `async updateMeter(meterId, updateData)`: Update meter configuration
- `async listMeters()`: List all usage meters
- `async resetUsage(subscriptionId, meterId)`: Reset usage counter
- `async getUsageHistory(subscriptionId, meterId, period)`: Get usage history
- `async setUsageAlert(subscriptionId, threshold, action)`: Set usage alert
- `async calculateOverage(subscriptionId, period)`: Calculate overage charges

### CreditManager
- `CreditManager(billingEngine)`: Initialize credit manager
- `async createCreditNote(creditData)`: Create credit note
- `async applyCreditToInvoice(creditNoteId, invoiceId)`: Apply credit to invoice
- `async refundCredit(creditNoteId, refundData)`: Refund credit balance
- `async transferCredit(fromCustomerId, toCustomerId, amount)`: Transfer credit
- `async getCreditBalance(customerId)`: Get customer credit balance
- `async listCreditNotes(customerId)`: List customer credit notes
- `async voidCreditNote(creditNoteId, reason)`: Void credit note
- `async allocateCredit(customerId, allocationRules)`: Auto-allocate credits
- `async expireCredits(expiryDate)`: Expire old credits
- `async generateCreditReport(period)`: Generate credit report

### DiscountEngine
- `DiscountEngine(billingEngine)`: Initialize discount engine
- `async createCoupon(couponData)`: Create discount coupon
- `async updateCoupon(couponId, updateData)`: Update coupon
- `async deleteCoupon(couponId)`: Delete coupon
- `async validateCoupon(couponCode, context)`: Validate coupon
- `async applyCoupon(couponCode, targetId, targetType)`: Apply coupon
- `async removeCoupon(targetId, targetType)`: Remove applied coupon
- `async createPromotionRule(ruleData)`: Create promotion rule
- `async evaluatePromotions(context)`: Evaluate applicable promotions
- `async calculateDiscount(amount, discounts)`: Calculate total discount
- `async getCouponUsage(couponId)`: Get coupon usage statistics
- `async bulkGenerateCoupons(template, quantity)`: Bulk generate coupons
- `async exportCoupons(filters, format)`: Export coupon data

### ReportingEngine
- `ReportingEngine(billingEngine)`: Initialize reporting engine
- `async generateMRRReport(options)`: Generate Monthly Recurring Revenue report
- `async generateARRReport(options)`: Generate Annual Recurring Revenue report
- `async generateChurnReport(options)`: Generate churn analysis report
- `async generateRevenueWaterfall(options)`: Generate revenue waterfall
- `async generateCohortAnalysis(options)`: Generate cohort analysis
- `async generateCustomReport(reportDefinition)`: Generate custom report
- `async scheduleReport(reportId, schedule)`: Schedule recurring report
- `async exportReport(reportId, format, destination)`: Export report
- `async getReport(reportId)`: Get report details
- `async listReports(filters)`: List available reports
- `async shareReport(reportId, recipients)`: Share report with users
- `async createDashboard(dashboardData)`: Create billing dashboard
- `async updateDashboard(dashboardId, updateData)`: Update dashboard
- `async generateForecast(forecastOptions)`: Generate revenue forecast

### WebhookManager
- `WebhookManager(billingEngine)`: Initialize webhook manager
- `async registerEndpoint(url, events, options)`: Register webhook endpoint
- `async updateEndpoint(endpointId, updateData)`: Update webhook endpoint
- `async deleteEndpoint(endpointId)`: Delete webhook endpoint
- `async listEndpoints()`: List webhook endpoints
- `async sendEvent(event, data)`: Send webhook event
- `async retryFailedEvent(eventId)`: Retry failed webhook
- `async getEventLog(filters)`: Get webhook event log
- `async verifySignature(payload, signature)`: Verify webhook signature
- `async testEndpoint(endpointId)`: Test webhook endpoint
- `on(event, handler)`: Register event handler
- `off(event, handler)`: Unregister event handler
- `emit(event, data)`: Emit webhook event

### NotificationClient
- `NotificationClient(billingEngine)`: Initialize notification client
- `async sendInvoiceNotification(invoiceId, template)`: Send invoice notification
- `async sendPaymentNotification(paymentId, template)`: Send payment notification
- `async sendSubscriptionNotification(subscriptionId, template)`: Send subscription notification
- `async sendReminderNotification(customerId, reminderType)`: Send reminder
- `async scheduleNotification(notificationData, sendAt)`: Schedule notification
- `async cancelScheduledNotification(notificationId)`: Cancel scheduled notification
- `async getNotificationHistory(customerId)`: Get notification history
- `async updateNotificationPreferences(customerId, preferences)`: Update preferences
- `async createNotificationTemplate(templateData)`: Create notification template
- `async updateNotificationTemplate(templateId, updateData)`: Update template

### CurrencyConverter
- `CurrencyConverter(billingEngine)`: Initialize currency converter
- `async convert(amount, fromCurrency, toCurrency, date)`: Convert currency
- `async getExchangeRate(fromCurrency, toCurrency, date)`: Get exchange rate
- `async updateExchangeRates()`: Update exchange rates from provider
- `async listSupportedCurrencies()`: List supported currencies
- `async setBaseCurrency(currency)`: Set base currency
- `async getHistoricalRates(currency, period)`: Get historical rates
- `async convertInvoice(invoiceId, targetCurrency)`: Convert invoice currency
- `async createMultiCurrencyPrice(basePrice, currencies)`: Create multi-currency pricing

## Event Types

### Customer Events
- `customer.created`: New customer created
- `customer.updated`: Customer information updated
- `customer.deleted`: Customer deleted
- `customer.payment_method.added`: Payment method added
- `customer.payment_method.removed`: Payment method removed
- `customer.credit.added`: Credit added to account
- `customer.credit.deducted`: Credit deducted from account

### Subscription Events
- `subscription.created`: New subscription created
- `subscription.updated`: Subscription updated
- `subscription.cancelled`: Subscription cancelled
- `subscription.paused`: Subscription paused
- `subscription.resumed`: Subscription resumed
- `subscription.trial.ending`: Trial period ending soon
- `subscription.trial.ended`: Trial period ended
- `subscription.renewed`: Subscription renewed
- `subscription.plan.changed`: Subscription plan changed

### Invoice Events
- `invoice.created`: New invoice created
- `invoice.updated`: Invoice updated
- `invoice.finalized`: Invoice finalized
- `invoice.sent`: Invoice sent to customer
- `invoice.paid`: Invoice fully paid
- `invoice.partially_paid`: Invoice partially paid
- `invoice.payment_failed`: Invoice payment failed
- `invoice.voided`: Invoice voided
- `invoice.overdue`: Invoice overdue

### Payment Events
- `payment.succeeded`: Payment successful
- `payment.failed`: Payment failed
- `payment.processing`: Payment processing
- `payment.refunded`: Payment refunded
- `payment.partially_refunded`: Payment partially refunded
- `payment.dispute.created`: Payment dispute created
- `payment.dispute.resolved`: Payment dispute resolved

### Tax Events
- `tax.calculated`: Tax calculated for transaction
- `tax.exemption.added`: Tax exemption added
- `tax.exemption.removed`: Tax exemption removed
- `tax.filing.due`: Tax filing due soon
- `tax.filing.submitted`: Tax filing submitted

### Usage Events
- `usage.recorded`: Usage event recorded
- `usage.threshold.reached`: Usage threshold reached
- `usage.limit.exceeded`: Usage limit exceeded
- `usage.reset`: Usage counter reset

## Error Codes

### General Errors
- `BILLING_ENGINE_NOT_INITIALIZED`: Billing engine not initialized
- `INVALID_CONFIGURATION`: Invalid configuration provided
- `DATABASE_CONNECTION_ERROR`: Database connection failed
- `EXTERNAL_SERVICE_ERROR`: External service integration error

### Customer Errors
- `CUSTOMER_NOT_FOUND`: Customer not found
- `CUSTOMER_ALREADY_EXISTS`: Customer already exists
- `INVALID_CUSTOMER_DATA`: Invalid customer data provided
- `CUSTOMER_DELETED`: Customer has been deleted

### Subscription Errors
- `PLAN_NOT_FOUND`: Subscription plan not found
- `SUBSCRIPTION_NOT_FOUND`: Subscription not found
- `SUBSCRIPTION_ALREADY_CANCELLED`: Subscription already cancelled
- `INVALID_SUBSCRIPTION_STATE`: Invalid subscription state for operation
- `PLAN_CHANGE_NOT_ALLOWED`: Plan change not allowed

### Invoice Errors
- `INVOICE_NOT_FOUND`: Invoice not found
- `INVOICE_ALREADY_PAID`: Invoice already paid
- `INVOICE_ALREADY_VOIDED`: Invoice already voided
- `INVALID_INVOICE_STATE`: Invalid invoice state for operation
- `INVOICE_FINALIZED`: Cannot modify finalized invoice

### Payment Errors
- `PAYMENT_METHOD_INVALID`: Invalid payment method
- `PAYMENT_FAILED`: Payment processing failed
- `INSUFFICIENT_FUNDS`: Insufficient funds
- `CARD_DECLINED`: Card declined
- `PAYMENT_METHOD_EXPIRED`: Payment method expired
- `REFUND_EXCEEDS_PAYMENT`: Refund amount exceeds payment

### Tax Errors
- `TAX_CALCULATION_FAILED`: Tax calculation failed
- `INVALID_TAX_JURISDICTION`: Invalid tax jurisdiction
- `VAT_NUMBER_INVALID`: Invalid VAT number
- `TAX_EXEMPTION_INVALID`: Invalid tax exemption

### Usage Errors
- `USAGE_PLAN_NOT_FOUND`: Usage plan not found
- `INVALID_USAGE_DATA`: Invalid usage data
- `USAGE_LIMIT_EXCEEDED`: Usage limit exceeded
- `METER_NOT_FOUND`: Usage meter not found