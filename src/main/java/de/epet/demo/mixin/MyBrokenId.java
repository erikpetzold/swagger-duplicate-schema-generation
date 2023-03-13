package de.epet.demo.mixin;

class MyBrokenId extends AbstractId {
    public MyBrokenId(String value) {
        id = value;
    }

    int myBrokenIdProperty;
}
