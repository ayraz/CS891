package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.utils.Assignment;

/**
 * A PalantiriManager implemented using a SpinLock, a Semaphore, and a
 * HashMap.
 */
public class SpinLockHashMapMgr
        extends PalantiriManager {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
            SpinLockHashMapMgr.class.getSimpleName();
    /**
     * A map that associates the @a Palantiri key to the @a boolean
     * values that keep track of whether the key is available.
     */
    public HashMap<Palantir, Boolean> mPalantiriMap;
    /**
     * A "spin lock" used to ensure that threads serialize on a
     * critical section.
     */
    // TODO -- you fill in here.
    private CancellableLock lock;

    /**
     * A counting Semaphore that limits concurrent access to the fixed
     * number of available palantiri managed by the PalantiriManager.
     */
    // TODO -- you fill in here.
    private Semaphore semaphore;

    /**
     * @return The "spin lock" instance.
     */
    public CancellableLock getSpinLock() {
        // TODO -- you fill in here, replacing null with the proper
        // code.
        return lock;
    }

    /**
     * @return The available palantiri semaphore instance.
     */
    public Semaphore getAvailablePalantiri() {
        // TODO -- you fill in here, replacing null with the proper
        // code.
        return semaphore;
    }

    /**
     * @return The palantiri map.
     */
    public HashMap<Palantir, Boolean> getPalantiriMap() {
        // TODO -- you fill in here, replacing null with the proper
        // code.
        return mPalantiriMap;
    }

    /**
     * Called to allow subclass implementations the opportunity
     * to setup fields and initialize field values.
     */
    @Override
    protected void buildModel() {
        // Create a new HashMap.
        mPalantiriMap = new HashMap<>();

        // Iterate through the List of Palantiri returned via the
        // getPalantiri() factory method and initialize each key in
        // the mPalantiriMap with "true" to indicate it's available.
        // TODO -- you fill in here.
        for (Palantir p : getPalantiri()) {
            mPalantiriMap.put(p, true);
        }

        // Initialize the Semaphore to use a "fair" implementation
        // that mediates concurrent access to the given Palantiri.
        // TODO -- you fill in here.
        semaphore = new Semaphore(getPalantirCount(), true);

        if (Assignment.isUndergraduateTodo()) {
            // UNDERGRADUATES:
            //
            // Initialize the CancellableLock by replacing the
            // null value with your SpinLock implementation.
            //
            // NOTE: You also will need to set the assignment type
            // to UNDERGRADUATE in the edu.vandy.simulator.utils.Assignment.

            // TODO -- you fill in here.\
            lock = new SpinLock();
        } else if (Assignment.isGraduateTodo()) {
            // GRADUATES:
            //
            // Initialize the CancellableLock by replacing the
            // null value with your ReentrantSpinLock implementation.
            //
            // NOTE: You also will need to set the assignment type
            // to GRADUATE in the edu.vandy.simulator.utils.Assignment.

            // TODO -- you fill in here.
            lock = new ReentrantSpinLock();
        } else {
            throw new IllegalStateException("Invalid assignment type");
        }
    }

    /**
     * Get a Palantir from the PalantiriManager, blocking until one is
     * available.
     *
     * @return The first available Palantir.
     */
    @Override
    @NotNull
    protected Palantir acquire() throws InterruptedException {
        // Acquire the Semaphore interruptibly and then acquired the
        // spin-lock to ensure that finding the first key in the
        // HashMap whose value is "true" (which indicates it's
        // available for use) occurs in a thread-safe manner.  Replace
        // the value of this key with "false" to indicate the Palantir
        // isn't available, return that palantir to the client, and
        // release the spin-lock.
        // TODO -- you fill in here.
        semaphore.acquire();

        try {
            lock.lock(this::isCancelled);
            for (Map.Entry<Palantir, Boolean> entry : mPalantiriMap.entrySet()) {
                if (entry.getValue()) {
                    entry.setValue(false);
                    return entry.getKey();
                }
            }
        } finally {
            lock.unlock();
        }

        // This invariant should always hold for all acquire()
        // implementations if implemented correctly. That is the
        // purpose of enforcing the @NotNull along with the
        // CancellationException; It makes it clear that all
        // implementations should either be successful (if implemented
        // correctly) and return a Palantir, or fail because of
        // cancellation.
        throw new IllegalStateException("This method should either return a valid " +
                "Palantir or throw a CancellationException. " +
                "In either case, this statement should not be reached.");
    }

    /**
     * Returns the designated @code palantir to the PalantiriManager
     * so it's available for other beings to use.
     *
     * @param palantir The palantir to release back to the Palantiri pool
     */
    @Override
    protected void release(Palantir palantir) {
        // Put the "true" value back into HashMap for the palantir key
        // in a thread-safe manner and release the Semaphore if all
        // works properly.
        // TODO -- you fill in here.
        if (palantir == null) {
            return;
        }

        try {
            lock.lock(this::isCancelled);
            if (mPalantiriMap.put(palantir, true) != null) {
                semaphore.release();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method is just intended for use by the regression tests,
     * not by applications.
     *
     * @return the number of available permits on the semaphore.
     */
    @Override
    protected int availablePermits() {
        // TODO -- you fill in here, replacing -1 with the proper
        // code.
        return semaphore.availablePermits();
    }

    /**
     * Called when the simulation is being shutdown to allow model
     * components the opportunity to and release resources and to
     * reset field values.
     */
    @Override
    public void shutdownNow() {
        mPalantiriMap.clear();
        semaphore = null;
        lock = null;
    }
}
