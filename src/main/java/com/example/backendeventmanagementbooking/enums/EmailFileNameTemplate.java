package com.example.backendeventmanagementbooking.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailFileNameTemplate {
    REGISTER_USER("register_email.html"),
    LOGIN_USER("login_email.html"),
    INVITE_PRIVATE_EVENT("invite_private_event.html"),
    CONFIRMATION_EVENT("confirmation_event.html");
    private final String value;
}
