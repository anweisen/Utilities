package net.anweisen.utilities.common.concurrent.task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public class CompletedTask<V> implements Task<V> {

	private final V value;
	private final Throwable failure;

	private CompletableFuture<V> future;

	public CompletedTask(@Nullable V value) {
		this.value = value;
		this.failure = null;
	}

	public CompletedTask(@Nonnull Throwable failure) {
		this.value = null;
		this.failure = failure;
	}

	@Nonnull
	@Override
	public Task<V> addListener(@Nonnull TaskListener<V> listener) {
		if (failure != null) {
			listener.onFailure(this, failure);
		} else {
			listener.onComplete(this, value);
		}

		return this;
	}

	@Nonnull
	@Override
	public Task<V> clearListeners() {
		return this;
	}

	@Nonnull
	@Override
	public <R> Task<R> map(@Nullable Function<? super V, ? extends R> mapper) {
		if (failure != null)
			return new CompletedTask<>(failure);

		return new CompletedTask<>(value == null || mapper == null ? null : mapper.apply(value));
	}

	@Override
	public V get(long timeout, @Nonnull TimeUnit unit, V def) {
		if (failure != null)
			return def;

		return value;
	}

	@Override
	public V call() throws Exception {
		return value;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return value;
	}

	@Override
	public V get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return value;
	}

	@Nonnull
	@Override
	public CompletionStage<V> stage() {
		if (future == null) {
			future = new CompletableFuture<>();

			if (failure != null) {
				future.completeExceptionally(failure);
			} else {
				future.complete(value);
			}
		}

		return future;
	}

}