package com.canvara.app.enums;

public enum PurchaseRequestStatus {
    PENDING,    // visitor submitted; supplier not yet responded
    CONFIRMED,  // supplier accepted; sale in progress
    REJECTED,   // supplier declined
    CANCELLED   // auto-cancelled when artwork marked SOLD via another request
}
