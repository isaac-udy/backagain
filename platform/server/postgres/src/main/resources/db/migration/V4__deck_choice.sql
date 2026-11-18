-- The presenter chooses which version of the talk to give; every client maps the id to its slides.
ALTER TABLE deck_state ADD COLUMN deck_id text NOT NULL DEFAULT 'full';
