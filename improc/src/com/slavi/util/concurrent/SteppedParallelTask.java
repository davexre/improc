package com.slavi.util.concurrent;

import java.util.Queue;
import java.util.concurrent.Callable;

public interface SteppedParallelTask<V> {
	/**
	 * Invoked once before the first call to {@link #getNextStepTasks()}.
	 */
	void onPrepare() throws Exception;
	
	/**
	 * Called after completing the last sub-tasks (Callable) in the queue of
	 * tasks, returned by the previous call to this method. If no more steps
	 * are available, i.e. the task is completed this method returns an empty
	 * queue or null. The queue returned by this method must NOT be blockable.
	 * <p>
	 * The method is guaranteed to be called from only one thread.
	 */
	Queue<Callable<V>> getNextStepTasks() throws Exception;
	
	/**
	 * Invoked upon an exception in some of the sub-tasks. The method MUST BE
	 * thread safe, i.e. may be invoked multiple time from different threads. 
	 */
	void onError(Callable<V> task, Exception e);
	
	/**
	 * Invoked upon an successful execution of a sub-task. The method MUST BE
	 * thread safe, i.e. may be invoked multiple time from different threads. 
	 */
	void onSubtaskFinished(Callable<V> subtask, V result) throws Exception;
	
	/**
	 * Invoked once after the task has been completed. 
	 * <p>The method is invoked after:
	 * <ul>
	 * <li>the last sub-task has completed and the call to
	 * {@link #getNextStepTasks()} has returned no more sub-tasks</li>
	 * <li>the task has been canceled and all running sub-task have
	 * been aborted or finished</li>
	 * <li>an exception has been thrown by some sub-task or a method
	 * like {@link #getNextStepTasks()} and all running sub-taks have
	 * been aborted or complete</li>
	 * </ul>
	 */
	void onFinally() throws Exception;
}
