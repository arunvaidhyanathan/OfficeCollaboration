package com.officecollab.documentcore.security;

import java.util.UUID;

/** POC has no user auth -- every request acts as this single fixed local user. */
public final class LocalUser {

    public static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String NAME = "Local User";

    private LocalUser() {
    }
}
