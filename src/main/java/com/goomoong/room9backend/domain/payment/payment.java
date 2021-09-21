package com.goomoong.room9backend.domain.payment;

import com.goomoong.room9backend.domain.base.BaseEntity;
import com.goomoong.room9backend.domain.reservation.roomReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class payment extends BaseEntity {

    @Id
    @Column(name = "payment_Id")
    /**
     * import : merchant_uid
     */
    private String Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Reservation_Id")
    private roomReservation roomReservation;

    private String payMethod;
    private Integer totalPrice;
    private Boolean paymentStatus;
}
