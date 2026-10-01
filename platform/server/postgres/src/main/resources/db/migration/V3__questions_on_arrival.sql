-- Questions go on screen as they're asked; the presenter hides the ones that shouldn't stay.
DROP INDEX questions_on_screen_by_approved_at;
ALTER TABLE questions DROP COLUMN approved_at;
CREATE INDEX questions_on_screen_by_asked_at ON questions (asked_at DESC) WHERE hidden_at IS NULL;
