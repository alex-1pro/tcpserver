package il.ac.hit.project.part1;

import java.util.concurrent.*;
import java.util.concurrent.RunnableFuture;

public class TaskWrapper<V> implements RunnableFuture<V> {

    private FutureTask<V> task;
    private TaskType taskType;

    public TaskWrapper(Runnable runnable, V result, TaskType taskType) {
        this.task = new FutureTask(runnable, result);
        this.taskType = taskType;
    }

    public TaskWrapper(Callable<V> callable, TaskType taskType) {
        this.task = new FutureTask(callable);
        this.taskType = taskType;
    }

    @Override
    public void run() {
        
    }

    @Override
    public boolean cancel(boolean b) {
        return task.cancel(b);
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public boolean isDone() {
        return task.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return task.get();
    }

    @Override
    public V get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return task.get(l, timeUnit);
    }

    @Override
    public String toString() {
        return "TaskWrapper{" +
                "taskType=" + taskType +
                "taskPriority=" + taskType.getPriority() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskWrapper<?> that = (TaskWrapper<?>) o;
        return taskType.getPriority() == that.taskType.getPriority();
    }
}
