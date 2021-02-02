package org.shadowice.flocke.andotp.Tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Encapsulates a background task that needs to communicate back to the UI (on the main thread) to
 * provide a result. */
public abstract class UiBasedBackgroundTask<Result> {

    private final Result failedResult;
    private final ExecutorService executor;
    private final Handler mainThreadHandler;

    private final Object callbackLock = new Object();
    @Nullable
    private UiCallback<Result> callback;
    @Nullable
    private Result awaitedResult;

    /** @param failedResult The result to return if the task fails (throws an exception or returns null). */
    public UiBasedBackgroundTask(@NonNull Result failedResult) {
        this.failedResult = failedResult;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    /** @param callback If null, any results which may arrive from a currently executing task will
     *                   be stored until a new callback is set. */
    public void setCallback(@Nullable UiCallback<Result> callback) {
        synchronized (callbackLock) {
            this.callback = callback;
            // If we have an awaited result and are setting a new callback, publish the result immediately.
            if (awaitedResult != null && callback != null) {
                emitResultOnMainThread(callback, awaitedResult);
            }
        }
    }

    private void emitResultOnMainThread(@NonNull UiCallback<Result> callback, @NonNull Result result) {
        mainThreadHandler.post(() -> callback.onResult(result));
        this.callback = null;
        this.awaitedResult = null;
    }

    /** Executed the task on a background thread. Safe to call from the main thread. */
    @AnyThread
    public void execute() {
        executor.execute(this::runTask);
    }

    private void runTask() {
        Result result = failedResult;
        try {
            result = doInBackground();
        } catch (Exception e) {
            Log.e("UiBasedBackgroundTask", "Problem running background task", e);
        }

        synchronized (callbackLock) {
            if (callback != null) {
                emitResultOnMainThread(callback, result);
            } else {
                awaitedResult = result;
            }
        }
    }

    /** Work to be done in a background thread.
     * @return Return the result from this task's execution.
     * @throws Exception If an Exception is thrown from this task's execution, it will be logged
     *         and the provided default Result will be returned. */
    @NonNull
    protected abstract Result doInBackground() throws Exception;

    @FunctionalInterface
    public interface UiCallback<Result> {
        @MainThread
        void onResult(@NonNull Result result);
    }
}
