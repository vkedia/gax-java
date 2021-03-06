/*
 * Copyright 2016, Google Inc. All rights reserved.
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
package com.google.api.gax.grpc;

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.batching.PartitionKey;
import com.google.api.gax.batching.RequestBuilder;
import com.google.api.gax.core.ApiFuture;
import com.google.api.gax.core.ApiFutures;
import com.google.api.gax.core.FakeApiClock;
import com.google.api.gax.core.FixedSizeCollection;
import com.google.api.gax.core.FlowControlSettings;
import com.google.api.gax.core.FlowController.LimitExceededBehavior;
import com.google.api.gax.core.Page;
import com.google.api.gax.core.RetrySettings;
import com.google.api.gax.core.TrackedFlowController;
import com.google.api.gax.protobuf.ValidationException;
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.UncheckedExecutionException;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Tests for {@link UnaryCallable}. */
@RunWith(JUnit4.class)
public class UnaryCallableTest {
  @SuppressWarnings("unchecked")
  private FutureCallable<Integer, Integer> callInt = Mockito.mock(FutureCallable.class);

  private static final RetrySettings FAST_RETRY_SETTINGS =
      RetrySettings.newBuilder()
          .setInitialRetryDelay(Duration.millis(2L))
          .setRetryDelayMultiplier(1)
          .setMaxRetryDelay(Duration.millis(2L))
          .setInitialRpcTimeout(Duration.millis(2L))
          .setRpcTimeoutMultiplier(1)
          .setMaxRpcTimeout(Duration.millis(2L))
          .setTotalTimeout(Duration.millis(10L))
          .build();

  static <V> ApiFuture<V> immediateFuture(V v) {
    return ApiFutures.<V>immediateFuture(v);
  }

  static <V> ApiFuture<V> immediateFailedFuture(Throwable t) {
    return ApiFutures.<V>immediateFailedFuture(t);
  }

  private FakeApiClock fakeClock;
  private RecordingScheduler executor;
  private ScheduledExecutorService batchingExecutor;

  @Before
  public void resetClock() {
    fakeClock = new FakeApiClock(System.nanoTime());
    executor = RecordingScheduler.create(fakeClock);
  }

  @Before
  public void startBatchingExecutor() {
    batchingExecutor = new ScheduledThreadPoolExecutor(1);
  }

  @After
  public void teardown() {
    executor.shutdownNow();
    batchingExecutor.shutdownNow();
  }

  @Rule public ExpectedException thrown = ExpectedException.none();

  private static class StashCallable<RequestT, ResponseT>
      implements FutureCallable<RequestT, ResponseT> {
    CallContext context;
    RequestT request;
    ResponseT result;

    public StashCallable(ResponseT result) {
      this.result = result;
    }

    @Override
    public ApiFuture<ResponseT> futureCall(RequestT request, CallContext context) {
      this.request = request;
      this.context = context;
      return immediateFuture(result);
    }
  }

  @Test
  public void simpleCall() throws Exception {
    StashCallable<Integer, Integer> stash = new StashCallable<>(1);

    UnaryCallable<Integer, Integer> callable = UnaryCallable.<Integer, Integer>create(stash);
    Integer response = callable.call(2, CallContext.createDefault());
    Truth.assertThat(response).isEqualTo(Integer.valueOf(1));
    Truth.assertThat(stash.context.getChannel()).isNull();
    Truth.assertThat(stash.context.getCallOptions()).isEqualTo(CallOptions.DEFAULT);
  }

  // Bind
  // ====
  @Test
  public void bind() {
    Channel channel = Mockito.mock(Channel.class);
    StashCallable<Integer, Integer> stash = new StashCallable<>(0);
    UnaryCallable.<Integer, Integer>create(stash).bind(channel).futureCall(0);
    Truth.assertThat(stash.context.getChannel()).isSameAs(channel);
  }

  @Test
  public void retryableBind() throws Exception {
    Channel channel = Mockito.mock(Channel.class);
    StashCallable<Integer, Integer> stash = new StashCallable<>(0);

    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of(Status.Code.UNAVAILABLE);
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(stash)
            .bind(channel)
            .retryableOn(retryable)
            .retrying(FAST_RETRY_SETTINGS, executor, fakeClock);
    callable.call(0);
    Truth.assertThat(stash.context.getChannel()).isSameAs(channel);
  }

  @Test
  public void pagedBind() {
    Channel channel = Mockito.mock(Channel.class);
    StashCallable<Integer, List<Integer>> stash =
        new StashCallable<Integer, List<Integer>>(new ArrayList<Integer>());

    UnaryCallable.<Integer, List<Integer>>create(stash)
        .bind(channel)
        .paged(new PagedFactory())
        .call(0);

    Truth.assertThat(stash.context.getChannel()).isSameAs(channel);
  }

  private static BatchingDescriptor<List<Integer>, List<Integer>> STASH_BATCHING_DESC =
      new BatchingDescriptor<List<Integer>, List<Integer>>() {

        @Override
        public PartitionKey getBatchPartitionKey(List<Integer> request) {
          return new PartitionKey();
        }

        @Override
        public RequestBuilder<List<Integer>> getRequestBuilder() {
          return new RequestBuilder<List<Integer>>() {

            List<Integer> list = new ArrayList<>();

            @Override
            public void appendRequest(List<Integer> request) {
              list.addAll(request);
            }

            @Override
            public List<Integer> build() {
              return list;
            }
          };
        }

        @Override
        public void splitResponse(
            List<Integer> batchResponse,
            Collection<? extends BatchedRequestIssuer<List<Integer>>> batch) {
          for (BatchedRequestIssuer<List<Integer>> responder : batch) {
            responder.setResponse(new ArrayList<Integer>());
          }
        }

        @Override
        public void splitException(
            Throwable throwable, Collection<? extends BatchedRequestIssuer<List<Integer>>> batch) {}

        @Override
        public long countElements(List<Integer> request) {
          return request.size();
        }

        @Override
        public long countBytes(List<Integer> request) {
          return 0;
        }
      };

  @Test
  public void batchingBind() throws Exception {
    BatchingSettings batchingSettings =
        BatchingSettings.newBuilder()
            .setDelayThreshold(Duration.standardSeconds(1))
            .setElementCountThreshold(2L)
            .build();
    BatcherFactory<List<Integer>, List<Integer>> batcherFactory =
        new BatcherFactory<List<Integer>, List<Integer>>(
            STASH_BATCHING_DESC, batchingSettings, batchingExecutor);

    Channel channel = Mockito.mock(Channel.class);
    StashCallable<List<Integer>, List<Integer>> stash =
        new StashCallable<List<Integer>, List<Integer>>(new ArrayList<Integer>());
    UnaryCallable<List<Integer>, List<Integer>> callable =
        UnaryCallable.<List<Integer>, List<Integer>>create(stash)
            .bind(channel)
            .batching(STASH_BATCHING_DESC, batcherFactory);
    List<Integer> request = new ArrayList<Integer>();
    request.add(0);
    ApiFuture<List<Integer>> future = callable.futureCall(request);
    future.get();
    Truth.assertThat(stash.context.getChannel()).isSameAs(channel);
  }

  // Retry
  // =====

  @Test
  public void retry() {
    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of(Status.Code.UNAVAILABLE);
    Throwable throwable = Status.UNAVAILABLE.asException();
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(immediateFuture(2));
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt)
            .retryableOn(retryable)
            .retrying(FAST_RETRY_SETTINGS, executor, fakeClock);
    Truth.assertThat(callable.call(1)).isEqualTo(2);
  }

  @Test(expected = ApiException.class)
  public void retryTotalTimeoutExceeded() {
    ImmutableSet<Status.Code> retryable = ImmutableSet.of(Status.Code.UNAVAILABLE);
    Throwable throwable = Status.UNAVAILABLE.asException();
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(immediateFuture(2));

    RetrySettings retrySettings =
        FAST_RETRY_SETTINGS
            .toBuilder()
            .setInitialRetryDelay(Duration.millis(Integer.MAX_VALUE))
            .setMaxRetryDelay(Duration.millis(Integer.MAX_VALUE))
            .build();
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.create(callInt)
            .retryableOn(retryable)
            .retrying(retrySettings, executor, fakeClock);
    callable.call(1);
  }

  @Test(expected = ApiException.class)
  public void retryMaxAttemptsExeeded() {
    ImmutableSet<Status.Code> retryable = ImmutableSet.of(Status.Code.UNAVAILABLE);
    Throwable throwable = Status.UNAVAILABLE.asException();
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(immediateFuture(2));

    RetrySettings retrySettings = FAST_RETRY_SETTINGS.toBuilder().setMaxAttempts(2).build();
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.create(callInt)
            .retryableOn(retryable)
            .retrying(retrySettings, executor, fakeClock);
    callable.call(1);
  }

  @Test
  public void retryWithinMaxAttempts() {
    ImmutableSet<Status.Code> retryable = ImmutableSet.of(Status.Code.UNAVAILABLE);
    Throwable throwable = Status.UNAVAILABLE.asException();
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(immediateFuture(2));

    RetrySettings retrySettings = FAST_RETRY_SETTINGS.toBuilder().setMaxAttempts(3).build();
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.create(callInt)
            .retryableOn(retryable)
            .retrying(retrySettings, executor, fakeClock);
    callable.call(1);
    Truth.assertThat(callable.call(1)).isEqualTo(2);
  }

  @Test
  public void retryOnStatusUnknown() {
    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of(Status.Code.UNKNOWN);
    Throwable throwable = Status.UNKNOWN.asException();
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable))
        .thenReturn(immediateFuture(2));
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt)
            .retryableOn(retryable)
            .retrying(FAST_RETRY_SETTINGS, executor, fakeClock);
    Truth.assertThat(callable.call(1)).isEqualTo(2);
  }

  @Test
  public void retryOnUnexpectedException() {
    thrown.expect(ApiException.class);
    thrown.expectMessage("foobar");
    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of(Status.Code.UNKNOWN);
    Throwable throwable = new RuntimeException("foobar");
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(UnaryCallableTest.<Integer>immediateFailedFuture(throwable));
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt)
            .retryableOn(retryable)
            .retrying(FAST_RETRY_SETTINGS, executor, fakeClock);
    callable.call(1);
  }

  @Test
  public void retryNoRecover() {
    thrown.expect(ApiException.class);
    thrown.expectMessage("foobar");
    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of(Status.Code.UNAVAILABLE);
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(
            UnaryCallableTest.<Integer>immediateFailedFuture(
                Status.FAILED_PRECONDITION.withDescription("foobar").asException()))
        .thenReturn(immediateFuture(2));
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt)
            .retryableOn(retryable)
            .retrying(FAST_RETRY_SETTINGS, executor, fakeClock);
    callable.call(1);
  }

  @Test
  public void retryKeepFailing() {
    thrown.expect(UncheckedExecutionException.class);
    thrown.expectMessage("foobar");
    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of(Status.Code.UNAVAILABLE);
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(
            UnaryCallableTest.<Integer>immediateFailedFuture(
                Status.UNAVAILABLE.withDescription("foobar").asException()));
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt)
            .retryableOn(retryable)
            .retrying(FAST_RETRY_SETTINGS, executor, fakeClock);
    // Need to advance time inside the call.
    ApiFuture<Integer> future = callable.futureCall(1);
    Futures.getUnchecked(future);
  }

  @Test
  public void noSleepOnRetryTimeout() {
    ImmutableSet<Status.Code> retryable =
        ImmutableSet.<Status.Code>of(Status.Code.UNAVAILABLE, Status.Code.DEADLINE_EXCEEDED);
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(
            UnaryCallableTest.<Integer>immediateFailedFuture(
                Status.DEADLINE_EXCEEDED.withDescription("DEADLINE_EXCEEDED").asException()))
        .thenReturn(immediateFuture(2));

    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt)
            .retryableOn(retryable)
            .retrying(FAST_RETRY_SETTINGS, executor, fakeClock);
    callable.call(1);
    Truth.assertThat(executor.getSleepDurations().size()).isEqualTo(1);
    Truth.assertThat(executor.getSleepDurations().get(0))
        .isEqualTo(RetryingCallable.DEADLINE_SLEEP_DURATION);
  }

  // PagedList
  // ==============
  @SuppressWarnings("unchecked")
  FutureCallable<Integer, List<Integer>> callIntList = Mockito.mock(FutureCallable.class);

  private class StreamingDescriptor
      implements PagedListDescriptor<Integer, List<Integer>, Integer> {
    @Override
    public Object emptyToken() {
      return 0;
    }

    @Override
    public Integer injectToken(Integer payload, Object token) {
      return (Integer) token;
    }

    @Override
    public Object extractNextToken(List<Integer> payload) {
      int size = payload.size();
      return size == 0 ? emptyToken() : payload.get(size - 1);
    }

    @Override
    public Iterable<Integer> extractResources(List<Integer> payload) {
      return payload;
    }

    @Override
    public Integer injectPageSize(Integer payload, int pageSize) {
      return payload;
    }

    @Override
    public Integer extractPageSize(Integer payload) {
      return 3;
    }
  }

  private class PagedListResponse extends PagedListResponseImpl<Integer, List<Integer>, Integer> {
    public PagedListResponse(
        UnaryCallable<Integer, List<Integer>> callable,
        PagedListDescriptor<Integer, List<Integer>, Integer> pageDescriptor,
        Integer request,
        CallContext context) {
      super(callable, pageDescriptor, request, context);
    }
  }

  private class PagedFactory
      implements PagedListResponseFactory<Integer, List<Integer>, PagedListResponse> {

    private final StreamingDescriptor streamingDescriptor = new StreamingDescriptor();

    @Override
    public PagedListResponse createPagedListResponse(
        UnaryCallable<Integer, List<Integer>> callable, Integer request, CallContext context) {
      return new PagedListResponse(callable, streamingDescriptor, request, context);
    }
  }

  @Test
  public void paged() {
    Mockito.when(callIntList.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(immediateFuture(Arrays.asList(0, 1, 2)))
        .thenReturn(immediateFuture(Arrays.asList(3, 4)))
        .thenReturn(immediateFuture(Collections.<Integer>emptyList()));
    Truth.assertThat(
            UnaryCallable.<Integer, List<Integer>>create(callIntList)
                .paged(new PagedFactory())
                .call(0)
                .iterateAllElements())
        .containsExactly(0, 1, 2, 3, 4)
        .inOrder();
  }

  @Test
  public void pagedByPage() {
    Mockito.when(callIntList.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(immediateFuture(Arrays.asList(0, 1, 2)))
        .thenReturn(immediateFuture(Arrays.asList(3, 4)))
        .thenReturn(immediateFuture(Collections.<Integer>emptyList()));
    Page<Integer, List<Integer>, Integer> page =
        UnaryCallable.<Integer, List<Integer>>create(callIntList)
            .paged(new PagedFactory())
            .call(0)
            .getPage();

    Truth.assertThat(page).containsExactly(0, 1, 2).inOrder();
    Truth.assertThat(page.getNextPage()).containsExactly(3, 4).inOrder();
  }

  @Test
  public void pagedByFixedSizeCollection() {
    Mockito.when(callIntList.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(immediateFuture(Arrays.asList(0, 1, 2)))
        .thenReturn(immediateFuture(Arrays.asList(3, 4)))
        .thenReturn(immediateFuture(Arrays.asList(5, 6, 7)))
        .thenReturn(immediateFuture(Collections.<Integer>emptyList()));
    FixedSizeCollection<Integer> fixedSizeCollection =
        UnaryCallable.<Integer, List<Integer>>create(callIntList)
            .paged(new PagedFactory())
            .call(0)
            .expandToFixedSizeCollection(5);

    Truth.assertThat(fixedSizeCollection).containsExactly(0, 1, 2, 3, 4).inOrder();
    Truth.assertThat(fixedSizeCollection.getNextCollection()).containsExactly(5, 6, 7).inOrder();
  }

  @Test(expected = ValidationException.class)
  public void pagedFixedSizeCollectionTooManyElements() {
    Mockito.when(callIntList.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(immediateFuture(Arrays.asList(0, 1, 2)))
        .thenReturn(immediateFuture(Arrays.asList(3, 4)))
        .thenReturn(immediateFuture(Collections.<Integer>emptyList()));

    UnaryCallable.<Integer, List<Integer>>create(callIntList)
        .paged(new PagedFactory())
        .call(0)
        .expandToFixedSizeCollection(4);
  }

  @Test(expected = ValidationException.class)
  public void pagedFixedSizeCollectionTooSmallCollectionSize() {
    Mockito.when(callIntList.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(immediateFuture(Arrays.asList(0, 1)))
        .thenReturn(immediateFuture(Collections.<Integer>emptyList()));

    UnaryCallable.<Integer, List<Integer>>create(callIntList)
        .paged(new PagedFactory())
        .call(0)
        .expandToFixedSizeCollection(2);
  }

  // Batching
  // ========
  private static class LabeledIntList {
    public String label;
    public List<Integer> ints;

    public LabeledIntList(String label, Integer... numbers) {
      this(label, new ArrayList<>(Arrays.asList(numbers)));
    }

    public LabeledIntList(String label, List<Integer> ints) {
      this.label = label;
      this.ints = ints;
    }
  }

  private static FutureCallable<LabeledIntList, List<Integer>> callLabeledIntSquarer =
      new FutureCallable<LabeledIntList, List<Integer>>() {
        @Override
        public ApiFuture<List<Integer>> futureCall(LabeledIntList request, CallContext context) {
          List<Integer> result = new ArrayList<>();
          for (Integer i : request.ints) {
            result.add(i * i);
          }
          return immediateFuture(result);
        }
      };

  private static BatchingDescriptor<LabeledIntList, List<Integer>> SQUARER_BATCHING_DESC =
      new BatchingDescriptor<LabeledIntList, List<Integer>>() {

        @Override
        public PartitionKey getBatchPartitionKey(LabeledIntList request) {
          return new PartitionKey(request.label);
        }

        @Override
        public RequestBuilder<LabeledIntList> getRequestBuilder() {
          return new RequestBuilder<LabeledIntList>() {

            LabeledIntList list;

            @Override
            public void appendRequest(LabeledIntList request) {
              if (list == null) {
                list = request;
              } else {
                list.ints.addAll(request.ints);
              }
            }

            @Override
            public LabeledIntList build() {
              return list;
            }
          };
        }

        @Override
        public void splitResponse(
            List<Integer> batchResponse,
            Collection<? extends BatchedRequestIssuer<List<Integer>>> batch) {
          int batchMessageIndex = 0;
          for (BatchedRequestIssuer<List<Integer>> responder : batch) {
            List<Integer> messageIds = new ArrayList<>();
            long messageCount = responder.getMessageCount();
            for (int i = 0; i < messageCount; i++) {
              messageIds.add(batchResponse.get(batchMessageIndex));
              batchMessageIndex += 1;
            }
            responder.setResponse(messageIds);
          }
        }

        @Override
        public void splitException(
            Throwable throwable, Collection<? extends BatchedRequestIssuer<List<Integer>>> batch) {
          for (BatchedRequestIssuer<List<Integer>> responder : batch) {
            responder.setException(throwable);
          }
        }

        @Override
        public long countElements(LabeledIntList request) {
          return request.ints.size();
        }

        @Override
        public long countBytes(LabeledIntList request) {
          long counter = 0;
          for (Integer i : request.ints) {
            counter += i;
          }
          // Limit the byte size to simulate merged messages having smaller serialized size that the
          // sum of their components
          return Math.min(counter, 5);
        }
      };

  @Test
  public void batching() throws Exception {
    BatchingSettings batchingSettings =
        BatchingSettings.newBuilder()
            .setDelayThreshold(Duration.standardSeconds(1))
            .setElementCountThreshold(2L)
            .build();
    BatcherFactory<LabeledIntList, List<Integer>> batcherFactory =
        new BatcherFactory<>(SQUARER_BATCHING_DESC, batchingSettings, batchingExecutor);

    UnaryCallable<LabeledIntList, List<Integer>> callable =
        UnaryCallable.<LabeledIntList, List<Integer>>create(callLabeledIntSquarer)
            .batching(SQUARER_BATCHING_DESC, batcherFactory);
    ApiFuture<List<Integer>> f1 = callable.futureCall(new LabeledIntList("one", 1, 2));
    ApiFuture<List<Integer>> f2 = callable.futureCall(new LabeledIntList("one", 3, 4));
    Truth.assertThat(f1.get()).isEqualTo(Arrays.asList(1, 4));
    Truth.assertThat(f2.get()).isEqualTo(Arrays.asList(9, 16));
  }

  @Test
  public void batchingWithFlowControl() throws Exception {
    BatchingSettings batchingSettings =
        BatchingSettings.newBuilder()
            .setDelayThreshold(Duration.standardSeconds(1))
            .setElementCountThreshold(4L)
            .setFlowControlSettings(
                FlowControlSettings.newBuilder()
                    .setLimitExceededBehavior(LimitExceededBehavior.Block)
                    .setMaxOutstandingElementCount(10)
                    .setMaxOutstandingRequestBytes(10)
                    .build())
            .build();
    TrackedFlowController trackedFlowController =
        new TrackedFlowController(batchingSettings.getFlowControlSettings());
    BatcherFactory<LabeledIntList, List<Integer>> batcherFactory =
        new BatcherFactory<>(
            SQUARER_BATCHING_DESC, batchingSettings, batchingExecutor, trackedFlowController);

    Truth.assertThat(trackedFlowController.getElementsReserved()).isEqualTo(0);
    Truth.assertThat(trackedFlowController.getElementsReleased()).isEqualTo(0);
    Truth.assertThat(trackedFlowController.getBytesReserved()).isEqualTo(0);
    Truth.assertThat(trackedFlowController.getBytesReleased()).isEqualTo(0);
    Truth.assertThat(trackedFlowController.getCallsToReserve()).isEqualTo(0);
    Truth.assertThat(trackedFlowController.getCallsToRelease()).isEqualTo(0);

    LabeledIntList requestA = new LabeledIntList("one", 1, 2);
    LabeledIntList requestB = new LabeledIntList("one", 3, 4);

    UnaryCallable<LabeledIntList, List<Integer>> callable =
        UnaryCallable.create(callLabeledIntSquarer).batching(SQUARER_BATCHING_DESC, batcherFactory);
    ApiFuture<List<Integer>> f1 = callable.futureCall(requestA);
    ApiFuture<List<Integer>> f2 = callable.futureCall(requestB);
    Truth.assertThat(f1.get()).isEqualTo(Arrays.asList(1, 4));
    Truth.assertThat(f2.get()).isEqualTo(Arrays.asList(9, 16));

    batcherFactory
        .getPushingBatcher(SQUARER_BATCHING_DESC.getBatchPartitionKey(requestA))
        .pushCurrentBatch()
        .get();

    // Check that the number of bytes is correct even when requests are merged, and the merged
    // request consumes fewer bytes.
    Truth.assertThat(trackedFlowController.getElementsReserved()).isEqualTo(4);
    Truth.assertThat(trackedFlowController.getElementsReleased()).isEqualTo(4);
    Truth.assertThat(trackedFlowController.getBytesReserved()).isEqualTo(8);
    Truth.assertThat(trackedFlowController.getBytesReleased()).isEqualTo(8);
    Truth.assertThat(trackedFlowController.getCallsToReserve()).isEqualTo(2);
    Truth.assertThat(trackedFlowController.getCallsToRelease()).isEqualTo(1);
  }

  private static BatchingDescriptor<LabeledIntList, List<Integer>> DISABLED_BATCHING_DESC =
      new BatchingDescriptor<LabeledIntList, List<Integer>>() {

        @Override
        public PartitionKey getBatchPartitionKey(LabeledIntList request) {
          Assert.fail("getBatchPartitionKey should not be invoked while batching is disabled.");
          return null;
        }

        @Override
        public RequestBuilder<LabeledIntList> getRequestBuilder() {
          Assert.fail("getRequestBuilder should not be invoked while batching is disabled.");
          return null;
        }

        @Override
        public void splitResponse(
            List<Integer> batchResponse,
            Collection<? extends BatchedRequestIssuer<List<Integer>>> batch) {
          Assert.fail("splitResponse should not be invoked while batching is disabled.");
        }

        @Override
        public void splitException(
            Throwable throwable, Collection<? extends BatchedRequestIssuer<List<Integer>>> batch) {
          Assert.fail("splitException should not be invoked while batching is disabled.");
        }

        @Override
        public long countElements(LabeledIntList request) {
          Assert.fail("countElements should not be invoked while batching is disabled.");
          return 0;
        }

        @Override
        public long countBytes(LabeledIntList request) {
          Assert.fail("countBytes should not be invoked while batching is disabled.");
          return 0;
        }
      };

  @Test
  public void batchingDisabled() throws Exception {
    BatchingSettings batchingSettings = BatchingSettings.newBuilder().setIsEnabled(false).build();
    BatcherFactory<LabeledIntList, List<Integer>> batcherFactory =
        new BatcherFactory<>(DISABLED_BATCHING_DESC, batchingSettings, batchingExecutor);

    UnaryCallable<LabeledIntList, List<Integer>> callable =
        UnaryCallable.<LabeledIntList, List<Integer>>create(callLabeledIntSquarer)
            .batching(DISABLED_BATCHING_DESC, batcherFactory);
    ApiFuture<List<Integer>> f1 = callable.futureCall(new LabeledIntList("one", 1, 2));
    ApiFuture<List<Integer>> f2 = callable.futureCall(new LabeledIntList("one", 3, 4));
    Truth.assertThat(f1.get()).isEqualTo(Arrays.asList(1, 4));
    Truth.assertThat(f2.get()).isEqualTo(Arrays.asList(9, 16));
  }

  public void batchingWithBlockingCallThreshold() throws Exception {
    BatchingSettings batchingSettings =
        BatchingSettings.newBuilder()
            .setDelayThreshold(Duration.standardSeconds(1))
            .setElementCountThreshold(2L)
            .build();
    BatcherFactory<LabeledIntList, List<Integer>> batcherFactory =
        new BatcherFactory<>(SQUARER_BATCHING_DESC, batchingSettings, batchingExecutor);

    UnaryCallable<LabeledIntList, List<Integer>> callable =
        UnaryCallable.<LabeledIntList, List<Integer>>create(callLabeledIntSquarer)
            .batching(SQUARER_BATCHING_DESC, batcherFactory);
    ApiFuture<List<Integer>> f1 = callable.futureCall(new LabeledIntList("one", 1));
    ApiFuture<List<Integer>> f2 = callable.futureCall(new LabeledIntList("one", 3));
    Truth.assertThat(f1.get()).isEqualTo(Arrays.asList(1));
    Truth.assertThat(f2.get()).isEqualTo(Arrays.asList(9));
  }

  private static FutureCallable<LabeledIntList, List<Integer>> callLabeledIntExceptionThrower =
      new FutureCallable<LabeledIntList, List<Integer>>() {
        @Override
        public ApiFuture<List<Integer>> futureCall(LabeledIntList request, CallContext context) {
          return UnaryCallableTest.<List<Integer>>immediateFailedFuture(
              new IllegalArgumentException("I FAIL!!"));
        }
      };

  @Test
  public void batchingException() throws Exception {
    BatchingSettings batchingSettings =
        BatchingSettings.newBuilder()
            .setDelayThreshold(Duration.standardSeconds(1))
            .setElementCountThreshold(2L)
            .build();
    BatcherFactory<LabeledIntList, List<Integer>> batcherFactory =
        new BatcherFactory<>(SQUARER_BATCHING_DESC, batchingSettings, batchingExecutor);

    UnaryCallable<LabeledIntList, List<Integer>> callable =
        UnaryCallable.<LabeledIntList, List<Integer>>create(callLabeledIntExceptionThrower)
            .batching(SQUARER_BATCHING_DESC, batcherFactory);
    ApiFuture<List<Integer>> f1 = callable.futureCall(new LabeledIntList("one", 1, 2));
    ApiFuture<List<Integer>> f2 = callable.futureCall(new LabeledIntList("one", 3, 4));
    try {
      f1.get();
      Assert.fail("Expected exception from batching call");
    } catch (ExecutionException e) {
      // expected
    }
    try {
      f2.get();
      Assert.fail("Expected exception from batching call");
    } catch (ExecutionException e) {
      // expected
    }
  }

  // ApiException
  // ============

  @Test
  public void testKnownStatusCode() {
    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of(Status.Code.UNAVAILABLE);
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(
            UnaryCallableTest.<Integer>immediateFailedFuture(
                Status.FAILED_PRECONDITION.withDescription("known").asException()));
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt).retryableOn(retryable);
    try {
      callable.call(1);
    } catch (ApiException exception) {
      Truth.assertThat(exception.getStatusCode()).isEqualTo(Status.Code.FAILED_PRECONDITION);
      Truth.assertThat(exception.getMessage())
          .isEqualTo("io.grpc.StatusException: FAILED_PRECONDITION: known");
    }
  }

  @Test
  public void testUnknownStatusCode() {
    ImmutableSet<Status.Code> retryable = ImmutableSet.<Status.Code>of();
    Mockito.when(callInt.futureCall((Integer) Mockito.any(), (CallContext) Mockito.any()))
        .thenReturn(
            UnaryCallableTest.<Integer>immediateFailedFuture(new RuntimeException("unknown")));
    UnaryCallable<Integer, Integer> callable =
        UnaryCallable.<Integer, Integer>create(callInt).retryableOn(retryable);
    try {
      callable.call(1);
    } catch (ApiException exception) {
      Truth.assertThat(exception.getStatusCode()).isEqualTo(Status.Code.UNKNOWN);
      Truth.assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: unknown");
    }
  }
}
