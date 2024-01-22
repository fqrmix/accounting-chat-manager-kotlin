package org.example.bot

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

object MessageScheduler {

    private lateinit var scheduler : ScheduledExecutorService

    fun init() {
        scheduler = Executors.newScheduledThreadPool(1)
    }

    fun createScheduledTask(task: RunnableTask, executionTime: LocalDateTime) : ScheduledFuture<*> {
        if (!MessageScheduler::scheduler.isInitialized) {
            throw RuntimeException("MessageScheduler object is not initialized. " +
                    "Call init() function once before database interaction")
        }

        val currentTime = LocalDateTime.now()

        if (currentTime > executionTime) {
            throw RuntimeException("$currentTime is more than $executionTime. Skipping task")
        }

        val scheduledTask = scheduler.schedule(
            task,
            Duration.between(
                currentTime,
                executionTime
            ).seconds,
            TimeUnit.SECONDS
        )
        println("Got a new task: $scheduledTask. Execution Time: $executionTime. Context: ${task.getContext()}")

        task.setStatus(RunnableTask.TaskStatus.WAITING_FOR_EXECUTION)
        return scheduledTask
    }
}

class RunnableTask(val task: () -> Unit) : Runnable {

    private val taskName = task.javaClass
    private var status: Int
            by Delegates.observable(1) {
                    property,
                    oldValue,
                    newValue -> onChange(oldValue, newValue)
            }

    private fun onChange(oldValue: Int, newValue: Int) {
        println("Status changed from $oldValue to $newValue. Current status: ${getStatus()} ")
    }

    override fun run() {
        task()
        status = 3
    }

    fun setStatus(taskStatus: TaskStatus) {
        when (taskStatus) {
            TaskStatus.CREATED -> this.status = 1
            TaskStatus.WAITING_FOR_EXECUTION -> this.status = 2
            TaskStatus.DONE -> this.status = 3
            TaskStatus.CANCELED -> this.status = 4
            TaskStatus.UNKNOWN -> this.status = 0
        }
    }

    fun getStatus(): TaskStatus {
        return when (status) {
            1 -> TaskStatus.CREATED
            2 -> TaskStatus.WAITING_FOR_EXECUTION
            3 -> TaskStatus.DONE
            4 -> TaskStatus.CANCELED
            else -> {TaskStatus.UNKNOWN}
        }
    }

    fun getContext(): String {
        return taskName.toString()
    }

    enum class TaskStatus {
        CREATED,
        WAITING_FOR_EXECUTION,
        DONE,
        CANCELED,
        UNKNOWN
    }
}
