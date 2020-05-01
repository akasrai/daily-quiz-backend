package com.machpay.api.entity;

import com.machpay.api.common.enums.PrivilegeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Collection;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private PrivilegeType name;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "privileges")
    private Collection<Role> roles;

    public Privilege(PrivilegeType name) {
        this.name = name;
    }
}
