package com.fpt.careermate.util;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String text) {
}
