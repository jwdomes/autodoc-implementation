package com.example.router.partner;

import com.example.autodoc.FlowStep;
import com.example.partnerclients.PartnerBClient;
import com.example.partnerclients.PartnerBRequest;
import com.example.router.Participants;
import com.example.router.Steps;
import com.example.router.dto.DocumentType;
import com.example.router.dto.PartnerResult;
import com.example.router.dto.Person;
import com.example.router.dto.PersonMatch;
import com.example.router.dto.RequestDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PartnerBService {

    private final PartnerBClient client;

    public PartnerBService(PartnerBClient client) {
        this.client = client;
    }

    public boolean shouldCall(RequestDto request) {
        return StringUtils.hasText(request.person().pNumber())
                && request.firstValidNumber(DocumentType.DRIVER_LICENSE).isPresent();
    }

    @FlowStep(action = Steps.CALL_PARTNER_B, participant = Participants.PARTNER_B,
            note = "Using driver license #{#request.firstValidNumber(T(com.example.router.dto.DocumentType).DRIVER_LICENSE).orElse('none')}")
    public PartnerResult call(RequestDto request) {
        Person person = request.person();
        PartnerBRequest partnerRequest = new PartnerBRequest(
                person.first(),
                person.last(),
                person.birthDate(),
                request.firstValidNumber(DocumentType.DRIVER_LICENSE).orElseThrow(),
                person.pNumber());
        List<PersonMatch> matches = client.match(request.header().requestId(), partnerRequest).matches().stream()
                .map(PersonMatch::from)
                .toList();
        return new PartnerResult("PARTNER_B", matches);
    }
}
