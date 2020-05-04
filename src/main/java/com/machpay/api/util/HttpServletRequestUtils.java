package com.machpay.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.machpay.api.cachedrequest.CachedBodyServletInputStream;
import com.machpay.api.common.Constants;
import com.machpay.api.user.auth.dto.SignInRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpServletRequestUtils {
    private static final Logger logger = LoggerFactory.getLogger(CachedBodyServletInputStream.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HttpServletRequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static byte[] getRequestReaderByte(HttpServletRequest request) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(request.getReader(), byteArrayOutputStream, "UTF-8");
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error(Constants.PARSE_ERROR, e);
            throw new InternalAuthenticationServiceException(Constants.PARSE_ERROR, e);
        }
    }

    public static SignInRequest getAuthRequest(byte[] bytes) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            String requestBody = IOUtils.toString(byteArrayInputStream, "UTF-8");

            return objectMapper.readValue(requestBody, SignInRequest.class);
        } catch (IOException e) {
            logger.error(Constants.PARSE_ERROR, e);
            throw new InternalAuthenticationServiceException(Constants.PARSE_ERROR, e);
        }
    }
}
