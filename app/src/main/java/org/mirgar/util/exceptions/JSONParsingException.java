package org.mirgar.util.exceptions;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

/**
 * Created by n.bibik on 23.06.2018.
 */

public class JSONParsingException extends JSONException {
    public JSONParsingException(String s, @Nullable Throwable clause) {
        super(s);
        if (clause != null)
            addSuppressed(clause);
    }

    public JSONParsingException(String s) {
        this(s, null);
    }
}
