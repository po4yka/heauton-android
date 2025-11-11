package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Database entity for wellness exercises.
 *
 * Represents a single exercise with metadata, instructions, and configuration.
 * Exercises can be meditation, breathing, visualization, or body scan types.
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,

    val description: String,

    val type: ExerciseType,

    /**
     * Duration in seconds.
     */
    val duration: Int,

    val difficulty: Difficulty,

    /**
     * Step-by-step instructions for the exercise.
     */
    val instructions: List<String>,

    val category: String,

    /**
     * Optional related quote ID for inspiration.
     */
    val relatedQuoteId: String? = null,

    val isFavorite: Boolean = false,

    /**
     * For breathing exercises: inhale duration in seconds.
     */
    val breathingInhale: Int? = null,

    /**
     * For breathing exercises: hold duration after inhale in seconds.
     */
    val breathingHold1: Int? = null,

    /**
     * For breathing exercises: exhale duration in seconds.
     */
    val breathingExhale: Int? = null,

    /**
     * For breathing exercises: hold duration after exhale in seconds.
     */
    val breathingHold2: Int? = null,

    /**
     * For breathing exercises: number of cycles to complete.
     */
    val breathingCycles: Int? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
