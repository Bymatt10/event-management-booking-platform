package com.example.backendeventmanagementbooking.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

public interface PayPalService {
    /**
     * Creates a payment in PayPal.
     *
     * @param total       The total amount of the payment.
     * @param currency    The currency of the payment (e.g., USD).
     * @param method      The payment method (e.g., PayPal).
     * @param intent      The intent of the payment (e.g., sale).
     * @param description A brief description of the payment.
     * @param cancelUrl   URL to redirect to if the payment is canceled.
     * @param successUrl  URL to redirect to if the payment is successfully completed.
     * @return An instance of `Payment` representing the created payment.
     * @throws PayPalRESTException If an error occurs during the payment creation.
     */
    Payment createPayment(Double total, String currency, String method, String intent,
                          String description, String cancelUrl, String successUrl)
            throws PayPalRESTException;

    /**
     * Executes a previously approved payment in PayPal.
     *
     * @param paymentId The ID of the payment to be executed.
     * @param payerId   The ID of the payer who approved the payment.
     * @return An instance of `Payment` representing the executed payment.
     * @throws PayPalRESTException If an error occurs during the payment execution.
     */
    Payment executePayment(String paymentId, String payerId) throws PayPalRESTException;
}
