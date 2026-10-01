-- The wall becomes questions, which wait for the presenter before anyone else sees them.
ALTER TABLE wall_messages RENAME TO questions;
ALTER TABLE questions RENAME COLUMN posted_at TO asked_at;
ALTER TABLE questions ADD COLUMN approved_at timestamptz;
ALTER TABLE questions RENAME CONSTRAINT wall_messages_pkey TO questions_pkey;
ALTER TABLE questions RENAME CONSTRAINT wall_messages_text_check TO questions_text_check;

DROP INDEX wall_messages_visible_by_posted_at;
CREATE INDEX questions_on_screen_by_approved_at ON questions (approved_at DESC)
    WHERE approved_at IS NOT NULL AND hidden_at IS NULL;
