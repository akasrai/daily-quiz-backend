package com.machpay.api.user.admin;

import com.machpay.api.entity.Admin;
import com.machpay.api.user.auth.dto.CurrentUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AdminMapper {
    @Mapping(target = "roles", ignore = true)
    AdminResponse toAdminResponse(Admin admin);

    @Mapping(target = "roles", ignore = true)
    CurrentUserResponse toCurrentUserResponse(Admin admin);
}
