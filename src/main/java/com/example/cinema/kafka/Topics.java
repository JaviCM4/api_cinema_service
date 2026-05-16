package com.example.cinema.kafka;

public final class Topics {
    private Topics() {}

    public static final String ROOM_COMMENT_CREATED = "cinema.room-comment.created";
    public static final String ROOM_COMMENT_UPDATED = "cinema.room-comment.updated";
    public static final String ROOM_COMMENT_DELETED = "cinema.room-comment.deleted";

    public static final String ROOM_RATING_CREATED  = "cinema.room-rating.created";
    public static final String ROOM_RATING_UPDATED  = "cinema.room-rating.updated";

    public static final String SHOWTIME_CREATED     = "cinema.showtime.created";
    public static final String SHOWTIME_UPDATED     = "cinema.showtime.updated";

    public static final String AD_BLOCK_CREATED     = "cinema.ad-block.created";

    public static final String OPERATING_COST_CREATED = "cinema.operating-cost.created";
}
