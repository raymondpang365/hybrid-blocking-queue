# Custom Hybrid Blocking Queue



Tthis repository showcases a unique implementation of thread-safe BlockingQueue which leverages both Atomic Operations and Locks.
It is uncommon to require both atomic operation and locks in concurrent system design. However, in this situation,
both CAS and Lock have their pros and cons, and I find that having both of them in this same time can provide the best result.

> This projects showcase even if we cannot predict the activities of the queue, we can still have a balanced design by combining both CAS and traiditional lock and conditions.

To illustrate why it is the best to leverage both of them to implement blocking queue, let's first understand the pros and cons between them for implementing a blocking queue.

|                                                                   | CAS               / Atomic operation s                                                                                                                | Locks & Conditions                                                                                |
|-------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| Performance of push/pop operation when the queue is full or empty | Worse, as there is busy waiting (atomic operation in a while loop )                                                                                   | Better, as this can put thread to sleep to avoid busy waiting                                     |
| Performance of push/pop operation when queue is NOT full or empty | Better, as no locks are required, avoiding the latency of context-switch                                                                         | Worse, as locks are required, causing high frequency of context-switches in high traffic use case |
 | Therefore, the suitable scenarios are:                            | frequency accessed queues that rarely hits the max capacity and rarely gets empty. | queue that always become full or empty                                                            |



### when should we use CAS for implementing a thread-safe queue?

when the condition (such as empty or full) rarely happens.  If the queue are usually in a state that there are some numbers of elements, then for those put and take operations, CAS operation does not require locks

### when should use condition for implementing a thread-safe queue?

If the majority of times the queue is empty or full, the thread must wait for another thread with an opposite action to proceed. Therefore, the impact of busy waiting would be more significant.
if that is the case, condition would be more suitable

### Conclusion

From the above discussion, we can see the benefits of applying both CAS and locks in the design of a custom BlockingQueue.

### Implementation of Hybrid Blocking Queue

The real benefit of CAS (Compare-And-Swap) is best realized when used outside of locks to avoid context switches and locking overhead. Therefore, in a hybrid implementation, the queue only fallbacks to the use of lock and condition only when the queue becomes full or empty.
