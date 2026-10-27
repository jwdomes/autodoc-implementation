package com.example.router.docs;

import com.example.flowcap.test.FlowcapSettings;
import com.example.router.Participants;

final class RouterFlowcap {

    static final FlowcapSettings SETTINGS = FlowcapSettings.of("com.example.router", Participants.ROUTER);

    private RouterFlowcap() {
    }
}
