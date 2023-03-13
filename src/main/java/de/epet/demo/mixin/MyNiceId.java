package de.epet.demo.mixin;

class MyNiceId extends AbstractId {
    public MyNiceId(String value) {
        id = value;
    }

    int myNiceIdProperty;
}
