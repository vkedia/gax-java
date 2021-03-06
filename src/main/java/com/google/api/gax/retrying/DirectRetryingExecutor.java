/*
 * Copyright 2017, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.google.api.gax.retrying;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.gax.core.internal.ListenableFutureToApiFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.InterruptedIOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.Callable;
import org.joda.time.Duration;

/**
 * The retry executor which executes attempts in the current thread, potentially causing the current
 * thread to sleep for the specified amount of time before execution.
 *
 * <p>
 * This class is thread-safe.
 *
 * @param <ResponseT> response type
 */
public class DirectRetryingExecutor<ResponseT> implements RetryingExecutor<ResponseT> {

  private final RetryAlgorithm retryAlgorithm;

  /**
   * Creates a new direct retrying executor instance, which will be using {@code retryAlgorithm} to
   * determine retrying strategy.
   *
   * @param retryAlgorithm retry algorithm to use for attempts execution
   * @throws NullPointerException if {@code retryAlgorithm} is null
   */
  public DirectRetryingExecutor(RetryAlgorithm retryAlgorithm) {
    this.retryAlgorithm = checkNotNull(retryAlgorithm);
  }

  /**
   * Creates a {@link RetryingFuture}, which is a facade, returned to the client code to wait for
   * any retriable operation to complete. The future is bounded to {@code this} executor instance.
   *
   * @param callable the actual callable, which should be executed in a retriable context
   * @return retrying future facade
   */
  @Override
  public RetryingFuture<ResponseT> createFuture(Callable<ResponseT> callable) {
    return new RetryingFutureImpl<>(callable, retryAlgorithm, this);
  }

  /**
   * Submits an attempt for execution in the current thread, causing the current thread to sleep for
   * the specified by the {@link RetryingFuture#getAttemptSettings()} amount of time.
   *
   * @param retryingFuture the future previously returned by {@link #createFuture(Callable)}
   */
  @Override
  public void submit(RetryingFuture<ResponseT> retryingFuture) {
    ListenableFuture<ResponseT> attemptFuture;
    try {
      Duration delay = retryingFuture.getAttemptSettings().getRandomizedRetryDelay();
      if (Duration.ZERO.compareTo(delay) < 0) {
        Thread.sleep(delay.getMillis());
      }
      attemptFuture = Futures.immediateFuture(retryingFuture.getCallable().call());
    } catch (InterruptedException | InterruptedIOException | ClosedByInterruptException e) {
      Thread.currentThread().interrupt();
      attemptFuture = Futures.immediateFailedFuture(e);
    } catch (Throwable e) {
      attemptFuture = Futures.immediateFailedFuture(e);
    }

    retryingFuture.setAttemptFuture(new ListenableFutureToApiFuture<>(attemptFuture));
  }
}
