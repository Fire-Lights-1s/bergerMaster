package com.itwillbs.entity.test;

import java.math.BigDecimal;

import com.itwillbs.entity.Item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "bom")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Bom {

    @Id
    @Column(name = "pp_code", length = 20, nullable = false)
    private String ppCode;

    @Column(name = "quantity", precision = 10, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "use_yn", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private char useYN;

    @ManyToOne
    @JoinColumn(name = "rm_code", referencedColumnName = "item_code")
    private Item rmCode;
}
