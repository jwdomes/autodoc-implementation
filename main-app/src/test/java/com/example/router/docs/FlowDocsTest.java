package com.example.router.docs;

import com.example.flowcap.test.StaticFlowcap;
import org.junit.jupiter.api.Test;

class FlowDocsTest {

    @Test
    void flowDocsAreCurrent() throws Exception {
        StaticFlowcap.verify(RouterFlowcap.SETTINGS);
    }
}
