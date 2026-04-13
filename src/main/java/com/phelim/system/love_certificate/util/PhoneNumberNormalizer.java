package com.phelim.system.love_certificate.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.phelim.system.love_certificate.enums.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PhoneNumberNormalizer {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    /** Cmt by Phelim (13/04/2026)
     * Normalize phone number to E.164 format
     * Example: Vietnam region: 0987123456 => +84987123456
     */
    // rawPhone => E.164 (Store)
    public String toE164(String rawPhone, Region region) {
        log.info("[PhoneNumberNormalizer][toE164] Start. phone={}, region={}", rawPhone, region);
        return format(rawPhone, region, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    // E.164 => rawPhone (Display only) // Ex - Vietnam region: +84987123456 => 0987123456
    public String toNational(String e164Phone, Region region) {
        log.info("[PhoneNumberNormalizer][toNational] Start. phoneE164Format={}, region={}", e164Phone, region);
        return format(e164Phone, region, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
    }

    private String format(String phone, Region region, PhoneNumberUtil.PhoneNumberFormat formatType) {
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phone, region.name());

            if (!phoneUtil.isValidNumber(number)) {
                throw new IllegalArgumentException("[PhoneNumberNormalizer][format] Invalid phone number");
            }

            return phoneUtil.format(number, formatType);

        } catch (NumberParseException e) {
            throw new IllegalArgumentException("[PhoneNumberNormalizer][format] Phone parse error", e);
        }
    }

}
