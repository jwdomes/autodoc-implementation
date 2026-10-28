package com.example.router.docs;

import com.example.autodoc.test.StaticAutodoc;
import org.junit.jupiter.api.Test;

class FlowDocsTest {

    @Test
    void flowDocsAreCurrent() throws Exception {
        StaticAutodoc.verify(RouterAutodoc.SETTINGS);
    }
}
