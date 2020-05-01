package com.machpay.api.user.password;

import com.machpay.api.entity.ResetPassword;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ResetPasswordRepository extends PagingAndSortingRepository<ResetPassword, Long> {
    ResetPassword findByToken(String token);

    ResetPassword findByUserIdAndExpiryDateIsAfter(Long userId, LocalDateTime localDateTime);
}
