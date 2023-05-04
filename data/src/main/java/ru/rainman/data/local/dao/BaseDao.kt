package ru.rainman.data.local.dao

import androidx.room.*

interface BaseDao<E> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(obj: E) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(obj: List<E>): List<Long>

    @Update
    fun update(obj: E) : Int

    @Update
    fun update(obj: List<E>) : Int

    @Delete
    fun delete(obj: E) : Int

    @Delete
    fun delete(obj: List<E>) : Int

}
