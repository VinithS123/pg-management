package com.HostelManagement.HM.service;

import com.HostelManagement.HM.dto.CustomerDto;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    @Value("${media.twilio.auth-token}")
    private String AUTH_TOKEN;

    @Value("${media.twilio.account-sid}")
    private String ACCOUNT_SID;

    public void sendMessage(String body, String phone_no) {

        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);

        Message message = Message
                //to 917483969238
                .creator(new com.twilio.type.PhoneNumber(phone_no),
                //from +12294952281
                        new com.twilio.type.PhoneNumber("+12294952281"),
                        body)
                .create();
        System.out.println(message.getStatus());

    }

    public void sendCustomizeMessage(CustomerDto customer, String body) {
        sendMessage(body, customer.getPhoneNo());
    }
}
