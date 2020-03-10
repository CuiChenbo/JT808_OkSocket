package com.cuichen.jt808_sdk.oksocket.core.exceptions;

import android.annotation.SuppressLint;

/**
 * 读异常
 * Created by xuhao on 2017/5/16.
 */

public class ReadException extends RuntimeException {
    public ReadException() {
        super();
    }

    public ReadException(String message) {
        super(message);
    }

    public ReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadException(Throwable cause) {
        super(cause);
    }

    @SuppressLint("NewApi")
    protected ReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
