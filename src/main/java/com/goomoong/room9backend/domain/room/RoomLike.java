package com.goomoong.room9backend.domain.room;

import com.goomoong.room9backend.domain.base.BaseEntity;
import com.goomoong.room9backend.domain.user.Role;
import com.goomoong.room9backend.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RoomLike extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "roomLike_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_Id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Room_Id", nullable = false)
    private Room room;

    private Boolean likeStatus;

    public void setLikeStatus(Boolean currentStatus) {
        this.likeStatus = (currentStatus == true) ? false : true;
    }
}
