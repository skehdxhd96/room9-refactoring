package com.goomoong.room9backend.repository.room;

import com.goomoong.room9backend.domain.room.Room;
import com.goomoong.room9backend.domain.room.RoomLike;
import com.goomoong.room9backend.domain.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.goomoong.room9backend.domain.room.QRoom.room;
import static com.goomoong.room9backend.domain.room.QRoomLike.roomLike;

import java.util.List;

@RequiredArgsConstructor
public class RoomLikeRepositoryImpl implements RoomLikeRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RoomLike> findRoomWithGood(User user) {
        return queryFactory.select(roomLike)
                .from(roomLike)
                .join(roomLike.user).fetchJoin()
                .join(roomLike.room).fetchJoin()
                .where(roomLike.user.id.eq(user.getId()),
                        roomLike.likeStatus.eq(true))
                .fetch();
    }
}
