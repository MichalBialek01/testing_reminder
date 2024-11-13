package com.example.testing_reminder.payment.charger;

public class CardPaymentCharge {

    private final boolean isCardDebited;

    public CardPaymentCharge(boolean isCardDebited) {
        this.isCardDebited = isCardDebited;
    }

    public boolean isCardDebited() {
        return isCardDebited;
    }

    @Override
    public String toString() {
        return "CardPaymentChargerImpl{" +
                "isCardDebited=" + isCardDebited +
                '}';
    }
}
