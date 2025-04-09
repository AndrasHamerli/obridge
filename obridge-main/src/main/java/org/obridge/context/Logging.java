package org.obridge.context;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Logging {

    private String initializer;
    private String annotationBasedInitializer;
    private String method;
    private Boolean enabled;
}
