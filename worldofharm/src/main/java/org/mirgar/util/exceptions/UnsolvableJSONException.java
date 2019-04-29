package org.mirgar.util.exceptions;

import org.json.JSONException;

/**
 * Created by n.bibik on 23.06.2018.
 */

public class UnsolvableJSONException extends JSONException {
    public UnsolvableJSONException(String s) {
        super(s);
    }
}
