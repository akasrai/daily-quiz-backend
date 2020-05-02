package com.machpay.api.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import java.util.UUID;

@Getter
@Setter
public class QuizQuestion extends AuditModel {
    @Column(unique = true)
    @Type(type = "uuid-char")
    private UUID referenceId;

    @Column
    private String question;

    @Column
    private int point;

    @Column(nullable = false)
    private boolean active;
}
