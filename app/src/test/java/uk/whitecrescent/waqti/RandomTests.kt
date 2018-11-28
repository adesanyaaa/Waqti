package uk.whitecrescent.waqti

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.whitecrescent.waqti.model.at
import uk.whitecrescent.waqti.model.collections.Board
import uk.whitecrescent.waqti.model.collections.TaskList
import uk.whitecrescent.waqti.model.hours
import uk.whitecrescent.waqti.model.now
import uk.whitecrescent.waqti.model.persistence.Caches
import uk.whitecrescent.waqti.model.persistence.Database
import uk.whitecrescent.waqti.model.size
import uk.whitecrescent.waqti.model.sleep
import uk.whitecrescent.waqti.model.task.Task
import uk.whitecrescent.waqti.model.tomorrow
import uk.whitecrescent.waqti.persistence.BasePersistenceTest

@DisplayName("Random Tests")
class RandomTests : BasePersistenceTest() {

    @DisplayName("Task List")
    @Test
    fun testTaskList() {
        val board = Board("MY BOARD")
        board.add(TaskList("MY TASK LIST0", getTasks(100)))
        board.add(TaskList("MY TASK LIST1", getTasks(100)))
        board.add(TaskList("MY TASK LIST2", getTasks(100)))
        board.update()

        assertTrue(Database.boards.size == 1)
        assertTrue(Database.boards[1].size == 3)
        assertTrue(Database.boards[1][1].size == 100)
        assertTrue(Database.boards[1][2].size == 100)
        assertTrue(Database.boards[1][1] == board[0])
    }

    @DisplayName("Test")
    @Test
    fun test() {
        var task: Task? =
                Task("My Task")
                        .setTimeConstraintValue(now + 6.hours)
                        .setDeadlineConstraintValue(tomorrow at 11)
                        .setTargetConstraintValue("My Target!")

        println(task!!.getAllShowingProperties())

        assertEquals(3, task!!.getAllUnmetAndShowingConstraints().size)

        Caches.tasks.put(task!!)

        task = null

        System.gc()

        sleep(5)

        assertNull(task)

        task = Caches.tasks[1]

        // TODO: 23-Nov-18 Test the Cache and its relationship to the DB

        assertTrue(task!!.getAllUnmetAndShowingConstraints().size == 3)

    }

    @DisplayName("Test")
    @Test
    fun test1() {
        val tasks = getTasks(100)
        Caches.tasks.clearMap()

        assertTrue(Caches.tasks.isEmpty())
        assertTrue(Database.tasks.size == 100)

        Caches.testTaskCache.putAll(Database.tasks.all.map { it.id to it }.toMap())

        assertTrue(Caches.testTaskCache.keys().count() == 100)
        Caches.testTaskCache.asMap().values.forEach { assertTrue(it == Database.tasks[it.id]) }
        assertEquals(Caches.testTaskCache.keys().sorted().toList(), Database.tasks.all.map {
            it
                    .id
        }.sorted().toList())
    }

}