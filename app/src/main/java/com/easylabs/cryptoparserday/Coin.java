package com.easylabs.cryptoparserday;

/**
 * Created by Maxim on 25.01.2018.
 */

public class Coin {
    // Название
    private String name;
    // Стоимость в долларах
    private double value = -1;
    // Разница с пред. значением, выраженная в процентах
    private double div = 0;

    public Coin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double newValue) {
        if (this.value == -1) {
            div = 0;
            this.value = newValue;
        } else {
            div = ((newValue - this.value) / this.value) * 100;
        }
    }

    public double getDiv() {
        return div;
    }
}
