package com.goomoong.room9backend.repository.room;

import com.goomoong.room9backend.domain.room.RoomLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomLikeRepository extends JpaRepository<RoomLike, Long>, RoomLikeRepositoryCustom {

    RoomLike findByRoomIdAndUserId(Long roomId, Long userId);
}
