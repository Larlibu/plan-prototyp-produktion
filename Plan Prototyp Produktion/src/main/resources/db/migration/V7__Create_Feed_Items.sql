CREATE TABLE feed_items (
    id           CHAR(36)     NOT NULL,
    title        VARCHAR(500) NOT NULL,
    description  TEXT,
    link         VARCHAR(500) NOT NULL,
    pub_date     DATETIME     NOT NULL,
    guid         VARCHAR(500) NOT NULL,
    blog_post_id CHAR(36)     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_feed_items_guid         (guid),
    UNIQUE KEY uq_feed_items_blog_post_id (blog_post_id)
);
