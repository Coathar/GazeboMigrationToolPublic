package com.coathar;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Utils
{
    public static int generateHash(Object... objects)
    {
        return new HashCodeBuilder(17, 31)
                .append(objects)
                .toHashCode();
    }
}
