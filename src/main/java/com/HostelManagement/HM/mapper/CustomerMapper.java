package com.HostelManagement.HM.mapper;

import com.HostelManagement.HM.dto.CustomerDto;
import com.HostelManagement.HM.model.CustomerModel;
import org.mapstruct.*;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper{

    CustomerDto toCustomerDto(CustomerModel CustomerEntity);

    CustomerModel toCustomerModel(CustomerDto customerDto);

    void updateToCustomerModel(CustomerDto customerDto, @MappingTarget CustomerModel customerModel);

    List<CustomerDto> toCustomerDtoList(List<CustomerModel> customerModelList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "userId",ignore = true)
    void patchToCustomerModel(CustomerDto customerDto, @MappingTarget CustomerModel customerModel);

}
