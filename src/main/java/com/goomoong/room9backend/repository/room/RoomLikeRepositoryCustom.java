package com.goomoong.room9backend.repository.room;

import com.goomoong.room9backend.domain.room.Room;
import com.goomoong.room9backend.domain.room.RoomLike;
import com.goomoong.room9backend.domain.user.User;

import java.util.List;

public interface RoomLikeRepositoryCustom {

    List<RoomLike> findRoomWithGood(User user);
}
