package com.offshore.platform.common.context;

import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;

public final class CurrentUserContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(CurrentUser currentUser) {
        HOLDER.set(currentUser);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static CurrentUser require() {
        CurrentUser currentUser = HOLDER.get();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
