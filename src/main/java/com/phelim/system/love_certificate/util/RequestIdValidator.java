package com.phelim.system.love_certificate.util;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.HasRequestId;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;

public class RequestIdValidator {

    public static void sync(HttpServletRequest request, HasRequestId req) {

        String headerId = request.getHeader(BaseConstants.REQUEST_ID_HEADER);
        if (headerId == null || headerId.isBlank()) {
            return;
        }

        String bodyId = req.getRequestId();
        if (bodyId == null || bodyId.isBlank()) {
            req.setRequestId(headerId);
            return;
        }

        if (!bodyId.equals(headerId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "requestId mismatch",
                    String.format("header=%s, body=%s", headerId, bodyId));
        }
    }
}
