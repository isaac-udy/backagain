-- Every change to a poll's votes announces the poll's id on the poll_votes channel, whichever code
-- made it. A transaction that changes one poll many times, like a reset, sends it once.
CREATE OR REPLACE FUNCTION notify_poll_votes() RETURNS trigger AS
$$
BEGIN
    PERFORM pg_notify('poll_votes', COALESCE(NEW.poll_id, OLD.poll_id));
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER poll_votes_notify
    AFTER INSERT OR UPDATE OR DELETE
    ON poll_votes
    FOR EACH ROW
EXECUTE FUNCTION notify_poll_votes();
