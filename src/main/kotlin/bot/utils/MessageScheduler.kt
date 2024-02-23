package org.example.bot.utils

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

    fun createScheduledTask(task: RunnableTask, executionTime: LocalDateTime) : ScheduledFuture<*>? {
        if (!MessageScheduler::scheduler.isInitialized) {
            throw RuntimeException("MessageScheduler object is not initialized. " +
                    "Call init() function once before database interaction")
        }

        val currentTime = LocalDateTime.now()

        if (currentTime > executionTime) {
            throw RuntimeException("$currentTime is more than $executionTime. Skipping task")
        }

        try {
            val scheduledTask = scheduler.schedule(
                task,
                Duration.between(
                    currentTime,
                    executionTime
                ).toMillis(),
                TimeUnit.MILLISECONDS
            )

            println("Got a new task: $scheduledTask. Execution Time: $executionTime. Context: ${task.getContext()}")
            task.setStatus(RunnableTask.TaskStatus.WAITING_FOR_EXECUTION)
            return scheduledTask
        } catch (e: Exception){
            println("Failed to schedule task with reason: $e")
            return null
        }


    }
}

class RunnableTask(val task: () -> Unit) : Runnable {

    private val taskName = task.javaClass
    private var status: Int
            by Delegates.observable(1) {
                    _,
                    oldValue,
                    newValue -> onChange(oldValue, newValue)
            }

    private fun onChange(oldValue: Int, newValue: Int) {
        println("Status changed from $oldValue to $newValue. Current status: ${getStatus()} ")
    }

    override fun run() {
        try {
            task()
            status = 3
        } catch (e: Exception) {
            println("Task $this was canceled because of exception: $e")
            status = 4
        }


    }

    fun setStatus(taskStatus: TaskStatus) {
        when (taskStatus) {
            TaskStatus.CREATED -> this.status = 1
            TaskStatus.WAITING_FOR_EXECUTION -> this.status = 2
            TaskStatus.DONE -> this.status = 3
            TaskStatus.CANCELED -> this.status = 4
        }
    }

    fun getStatus(): TaskStatus {
        return when (status) {
            1 -> TaskStatus.CREATED
            2 -> TaskStatus.WAITING_FOR_EXECUTION
            3 -> TaskStatus.DONE
            4 -> TaskStatus.CANCELED
            else -> {
                throw RuntimeException("Task status is incorrect. Only 1..4 integers allowed!")
            }
        }
    }

    fun getContext(): String {
        return taskName.toString()
    }

    enum class TaskStatus {
        CREATED,
        WAITING_FOR_EXECUTION,
        DONE,
        CANCELED
    }
}
