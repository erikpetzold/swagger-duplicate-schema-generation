package de.epet.demo.annotations;

public class MyBrokenId extends AbstractId {
    public MyBrokenId(String value) {
        id = value;
    }

    int myBrokenIdProperty;
}
