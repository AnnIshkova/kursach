package com.example.kursach

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase


@Entity(tableName = "tasks")
class Task(
    var category:Int,
    var heading:String,
    var date: String,
    var end_date: String,
    var start_time : String,
    var end_time : String,
    var status_task : Boolean
)
{
    @PrimaryKey(autoGenerate = true)
    var id = 0;
}

@Entity(tableName = "categories")
class Category(
    var name:String,
    var color:Long

)
{
    @PrimaryKey(autoGenerate = true)
    var id = 0;
}
@Entity(
    tableName = "statistics",

)
class Statistic(
    var category: Int,
    var date: String,
    var time_spent: String,
    var task_id: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Dao
interface TasksDao {

    @Insert
    fun insert(task:Task)

    @Query("delete from tasks where id = :id1")
    fun deleteTask(id1:Int)

    @Query("select distinct date from tasks")
    fun allUniqueDate():List<String>

    @Query("select * from tasks where date = :date1")
    fun allTaskFromDate(date1:String):List<Task>

    @Query("select * from tasks where date = :date1 and category = :category_id")
    fun allTaskFromDateWithCategory(date1:String, category_id: Int):List<Task>

    @Query("UPDATE tasks SET heading = :heading_new, category = :category_new where id = :task_id")
    fun updateTaskNameCategory(heading_new: String, category_new: Int, task_id: Int)

    @Insert
    fun insert(category:Category)

    @Query("select * from categories")
    fun allCategory():List<Category>

    @Query("select name from categories where id = :id1")
    fun categoryWithId(id1: Int):String

    @Query("select color from categories where id = :id1")
    fun categoryColorWithId(id1: Int):Long

    @Query("update categories set name = :name_new, color = :color_new where id = :id_old")
    fun updateFullCategory(name_new:String, color_new:Long, id_old:Int)

    @Query("delete from categories where id=:id1")
    fun deleteCategory(id1:Int)

    @Query("select * from statistics where date = :date1")
    fun getStatisticWithDate(date1:String):List<Statistic>

    @Query("select * from statistics")
    fun getStatistic():List<Statistic>

    @Query("update tasks set status_task = :status_new where id = :id1")
    fun updateStatusTask(status_new:Boolean, id1:Int)

    @Query("update tasks set end_time = :end_time_new, end_date = :end_date_new where id = :id1")
    fun updateEndTimeTask(end_time_new:String, end_date_new: String, id1:Int)

    @Insert
    fun insert(statistic: Statistic)

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    fun getTaskById(taskId: Int): Task

    @Query("update tasks set category = -1 where category = :category_old")
    fun updateTasksCategoryOnNull(category_old: Int)

    @Query("delete from statistics where task_id = :taskId")
    fun deleteTaskFromStatistics(taskId: Int)
}

@Database(entities = [Task::class, Category::class, Statistic::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao
}