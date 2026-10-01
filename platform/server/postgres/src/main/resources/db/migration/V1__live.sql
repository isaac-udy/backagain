CREATE TABLE presenter_sessions
(
    token_hash text PRIMARY KEY,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL
);

-- One row: where the presenter is. Written on every slide change, read on every connect.
CREATE TABLE deck_state
(
    id         integer PRIMARY KEY CHECK (id = 1),
    slide_id   text        NOT NULL,
    step       integer     NOT NULL,
    is_live    boolean     NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE wall_messages
(
    id        uuid PRIMARY KEY     DEFAULT gen_random_uuid(),
    client_id text        NOT NULL,
    text      text        NOT NULL CHECK (char_length(text) BETWEEN 1 AND 140),
    posted_at timestamptz NOT NULL DEFAULT now(),
    hidden_at timestamptz
);

CREATE INDEX wall_messages_visible_by_posted_at ON wall_messages (posted_at DESC) WHERE hidden_at IS NULL;

CREATE TABLE poll_votes
(
    poll_id   text        NOT NULL,
    client_id text        NOT NULL,
    option_id text        NOT NULL,
    voted_at  timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (poll_id, client_id)
);

CREATE TABLE reaction_totals
(
    reaction text PRIMARY KEY,
    total    bigint NOT NULL
);
