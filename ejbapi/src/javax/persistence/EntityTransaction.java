package javax.persistence;

/**
 * Warning: this class is from the EJB 3.0 Persistence API Early Draft 2 published
 * on February 7, 2005, and as such is likely to change signifcantly in the future
 * as the specification nears finalization.
 */
public interface EntityTransaction {

    /**
     * Start a resource transaction.
     *
     * @throws IllegalStateException if isActive() is true.
     */
    public abstract void begin();

    /**
     * Commit the current transaction, writing any unflushed changes to the database.
     *
     * @throws IllegalStateException - if isActive() is false.
     */
    public abstract void commit();

    /**
     * Roll back the current transaction.
     *
     * @throws IllegalStateException - if isActive() is false.
     */
    public abstract void rollback();

    /**
     * Check to see if a transaction is in progress.
     */
    public abstract boolean isActive();
}
