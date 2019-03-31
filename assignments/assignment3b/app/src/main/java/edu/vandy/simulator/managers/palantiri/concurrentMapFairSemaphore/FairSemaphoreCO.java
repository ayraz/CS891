package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a fair semaphore using the Specific Notification pattern
 * (www.dre.vanderbilt.edu/~schmidt/PDF/specific-notification.pdf)
 * using ReentrantLock/ConditionObject.  Graduate students should
 * implement this class.
 */
public class FairSemaphoreCO
        implements FairSemaphore {
    /**
     * Debugging tag used by the Android logger.
     */
    private final static String TAG =
            FairSemaphore.class.getSimpleName();
    /**
     * Define a monitor lock (using a Lock) to protect critical sections.
     */
    // TODO -- you fill in here
    private final Lock lock;

    /**
     * Define a LinkedList "WaitQueue" that keeps track of the waiters in a FIFO
     * List to ensure "fair" semantics.
     */
    // TODO -- you fill in here.
    private final Queue<Waiter> waitQueue;

    /**
     * Define a count of the number of available permits.
     */
    // TODO -- you fill in here.  Make sure that this field will ensure
    // its values aren't cached by multiple threads..
    private volatile int mAvailablePermits;

    /**
     * Initialize the fields in the class.
     */
    public FairSemaphoreCO(int availablePermits) {
        // TODO -- you fill in here.
        this.mAvailablePermits = availablePermits;
        this.lock = new ReentrantLock();
        this.waitQueue = new LinkedList<>();
    }

    /**
     * Acquire one permit from the semaphore in a manner that cannot
     * be interrupted.
     */
    @Override
    public void acquireUninterruptibly() {
        // TODO -- you fill in here, using a loop to ignore InterruptedExceptions.
        while (!tryToGetPermit()) {
            try {
                waitForPermit();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Acquire one permit from the semaphore in a manner that can be
     * interrupted.
     */
    @Override
    public void acquire() throws InterruptedException {
        // Bail out quickly if we've been interrupted.
        if (Thread.interrupted()) {
            throw new InterruptedException();

            // Try to get a permit without blocking.
        } else if (!tryToGetPermit()) {
            // Block until a permit is available.
            waitForPermit();
        }
    }

    /**
     * Handle the case where we can get a permit without blocking.
     *
     * @return Returns true if the permit was obtained, else false.
     * If the return value is true then monitor lock has been
     * unlocked, otherwise it's still locked.
     */
    protected boolean tryToGetPermit() {
        // TODO -- first try the "fast path" where the method doesn't
        // need to block if there are no waiters in the queue or if
        // there are permits available.

        // TODO -- you fill in here (replace false with the
        // appropriate code).
        lock.lock();
        try {
            boolean obtained = tryToGetPermitUnlocked();
            if (obtained) {
                mAvailablePermits -= 1;
            }
            return obtained;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Factors out code that checks to see if a permit can be obtained
     * without blocking.  This method assumes the monitor lock
     * ("intrinsic lock") is held.
     *
     * @return Returns true if the permit was obtained, else false.
     */
    protected boolean tryToGetPermitUnlocked() {
        // We must wait if there are already conditions in the queue
        // or if there are no permits available.

        // TODO -- you fill in here (replace false with the
        // appropriate code).
        return waitQueue.isEmpty() && mAvailablePermits > 0;
    }

    /**
     * Constructs a new Waiter (required for test mocking).
     *
     * @return A new Waiter instance
     */
    protected Waiter createWaiter() {
        return new Waiter();
    }

    /**
     * Handle the case where we need to block since there are already
     * waiters in the queue or no permits are available.  If this
     * method is called the monitor lock is held.
     */
    protected void waitForPermit() throws InterruptedException {
        // Call createWaiter helper method to allocate a new Waiter that
        // acts as the "specific-notification lock".
        final Waiter waiter = createWaiter();

        // TODO -- implement "fair" semaphore acquire semantics using
        // the Specific Notification pattern.
        waiter.lock.lock();
        try {
            lock.lock();
            try {
                waitQueue.add(waiter);
            } finally {
                lock.unlock();
            }
            while (!waiter.mReleased) {
                waiter.condition.await();
            }
        } catch (InterruptedException ex) {
            lock.lock();
            try {
                boolean removed = waitQueue.remove(waiter);
                // other thread already removed the waiter, give back permit to match it
                if (!removed) {
                    release();
                }
                throw ex;
            } finally {
                lock.unlock();
            }
        } finally {
            waiter.lock.unlock();
        }
    }

    /**
     * Return one permit to the semaphore.
     */
    @Override
    public void release() {
        // TODO -- implement "fair" semaphore release semantics using
        // the Specific Notification pattern.
        lock.lock();
        try {
            if (!waitQueue.isEmpty()) {
                Waiter waiter = waitQueue.poll();
                waiter.lock.lock();
                try {
                    waiter.mReleased = true;
                    waiter.condition.signal();
                } finally {
                    waiter.lock.unlock();
                }
            } else {
                mAvailablePermits += 1;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return The number of available permits.
     */
    @Override
    public int availablePermits() {
        // @@ TODO -- you fill in here replacing 0 with the right
        // value.
        return mAvailablePermits;
    }

    /**
     * Define a class that can be used in the "WaitQueue" to wait for
     * a specific thread to be notified.
     */
    static class Waiter {
        /**
         * A lock used to synchronize access to the condition below.
         */
        // TODO -- you fill in here.
        private final Lock lock;

        /**
         * A condition that's used to wait in FIFO order.
         */
        // TODO -- you fill in here.
        private final Condition condition;

        /**
         * Keeps track of whether the Waiter was released or not to
         * detected and handle "spurious wakeups".
         */
        boolean mReleased = false;

        /**
         * Private constructor (only available to static create() method)
         * initializes the fields.
         */
        Waiter() {
            // TODO -- you fill in here to initialize the lock and condition fields.
            this.lock = new ReentrantLock();
            this.condition = lock.newCondition();
        }
    }
}

