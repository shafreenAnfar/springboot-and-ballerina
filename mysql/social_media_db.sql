CREATE TABLE social_media_database.user (
    id INT NOT NULL auto_increment PRIMARY KEY,
    birth_date DATE,
    name VARCHAR(255)
);
CREATE TABLE social_media_database.post (
    id INT NOT NULL auto_increment PRIMARY KEY,
    description VARCHAR(255),
    user_id INT
);
ALTER TABLE social_media_database.post ADD FOREIGN KEY (user_id) REFERENCES social_media_database.user(id) ON DELETE CASCADE;

INSERT INTO social_media_database.user (
        id,
        birth_date,
        name
    )
VALUES (
        1,
        Curdate(),
        "ranga"
    );
INSERT INTO social_media_database.user (
        id,
        birth_date,
        name
    )
VALUES (
        2,
        Curdate(),
        "ravi"
    );
INSERT INTO social_media_database.user (
        id,
        birth_date,
        name
    )
VALUES (
        3,
        Curdate(),
        "satish"
    );
INSERT INTO social_media_database.user (
        id,
        birth_date,
        name
    )
VALUES (
        4,
        Curdate(),
        "ayesh"
    );
INSERT INTO social_media_database.post (
        description,
        user_id
    )
VALUES (
        'I want to learn AWS',
        1
    );
INSERT INTO social_media_database.post (
        description,
        user_id
    )
VALUES (
        'I want to learn DevOps',
        1
    );
INSERT INTO social_media_database.post (
        description,
        user_id
    )
VALUES (
        'I want to learn GCP',
        2
    );
INSERT INTO social_media_database.post (
        description,
        user_id
    )
VALUES (
        'I want to learn multi cloud',
        3
    );


