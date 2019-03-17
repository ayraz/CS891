package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class defines a counting semaphore with "fair" semantics that
 * are implemented using a Java ReentrantLock and ConditionObject.
 */
public class SimpleSemaphore {
    /**
     * Define a count of the number of available mPermits.
     */
    // TODO - you fill in here.  Ensure that this field will ensure
    // its values aren't cached by multiple threads..
    public int mPermits;

    /**
     * Define a ReentrantLock to protect critical sections.
     */
    // TODO - you fill in here
    private final Lock lock;

    /**
     * Define a Condition that's used to wait while the number of
     * mPermits is 0.
     */
    // TODO - you fill in here
    private final Condition condition;

    /**
     * Default constructor used for regression tests.
     */
    public SimpleSemaphore() {
        this.mPermits = 1;
        this.lock = new ReentrantLock(true);
        this.condition = lock.newCondition();
    }

    /**
     * Constructor initialize the fields.
     */
    public SimpleSemaphore(int mPermits) {
        // TODO -- you fill in here making sure the ReentrantLock has
        // "fair" semantics.
        this.mPermits = mPermits;
        this.lock = new ReentrantLock(true);
        this.condition = lock.newCondition();
    }

    /**
     * Acquire one permit from the semaphore in a manner that can be
     * interrupted.
     */
    public void acquire()
            throws InterruptedException {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
        try {
            lock.lock();
            while (mPermits <= 0) {
                condition.await();
            }
            mPermits -= 1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Acquire one permit from the semaphore in a manner that cannot
     * be interrupted.  If an interrupt occurs while this method is
     * running make sure the interrupt flag is reset when the method
     * returns.
     */
    public void acquireUninterruptibly() {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
        try {
            lock.lock();
            while (mPermits <= 0) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mPermits -= 1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Return one permit to the semaphore.
     */
    public void release() {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
        try {
            lock.lock();
            mPermits += 1;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current number of mPermits.
     */
    protected int availablePermits() {
        // TODO -- you fill in here, replacing 0 with the
        // appropriate field.
        try {
            lock.lock();
            return mPermits;
        } finally {
            lock.unlock();
        }
    }
}
