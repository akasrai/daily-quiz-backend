package com.machpay.api.redis;

import com.machpay.api.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private Logger logger = LoggerFactory.getLogger(AuthTokenService.class);

    public String create(String accessToken, Long userId) {
        AuthToken authToken = new AuthToken();
        String referenceToken = createReferenceToken();

        authToken.setUserId(userId);
        authToken.setJWTtoken(accessToken);
        authToken.setReferenceToken(referenceToken);
        logger.info("Creating auth token for userId: {}", userId);

        return authTokenRepository.save(authToken).getReferenceToken();
    }

    public AuthToken getAuthToken(String referenceToken) {
        return authTokenRepository.findByReferenceToken(referenceToken)
                .orElseThrow(() -> new ResourceNotFoundException("AuthToken", "reference_token", referenceToken));
    }

    public String getJWTtoken(String referenceToken) {
        Optional<AuthToken> authToken = authTokenRepository.findByReferenceToken(referenceToken);

        // returning null since JWT unauthorised case is handled in token filter class
        // for accessing unauthoriesd resources if token isn't available
        return authToken.isPresent() ? authToken.get().getJWTtoken() : null;
    }

    public void deleteAuthTokenByUserId(Long userId) {
        AuthToken authToken =
                authTokenRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("AuthToken",
                        "userId", userId));
        authTokenRepository.delete(authToken);
    }

    public void deleteAuthTokenByReferenceToken(String referenceToken) {
        Optional<AuthToken> authToken = authTokenRepository.findByReferenceToken(referenceToken);

        if (authToken.isPresent()) {
            logger.info("Deleting authToken with referenceId: {}", referenceToken);
            authTokenRepository.delete(authToken.get());
        } else {
            logger.error("Auth token not found with referenceId: {}", referenceToken);
            throw new ResourceNotFoundException("AuthToken", "reference_token", referenceToken);
        }
    }

    private String createReferenceToken() {
        return UUID.randomUUID().toString();
    }
}
