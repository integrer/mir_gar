package org.mirgar.util.exceptions;

public class ContextUnreachableException extends IllegalStateException {
    public ContextUnreachableException() {
        super("Unable to get context.");
    }
}
