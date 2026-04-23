package com.HostelManagement.HM.service;

import com.HostelManagement.HM.dto.CustomerDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

class TwilioServiceTest {

    @Test
    void sendCustomizeMessageSuccess() {
        TwilioService twilioService = Mockito.spy(new TwilioService());
        CustomerDto customerDto = CustomerDto.builder()
                .phoneNo("+911234567890")
                .build();

        doNothing().when(twilioService).sendMessage(anyString(),anyString());

        twilioService.sendCustomizeMessage(customerDto,"Test Body");

        verify(twilioService,times(1)).sendMessage("Test Body","+911234567890");
    }
}
