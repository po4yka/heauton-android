package com.po4yka.heauton.data.local.database.entities

import java.util.UUID

/**
 * Seed data for exercise library.
 *
 * Provides a curated collection of wellness exercises including:
 * - Breathing exercises with specific patterns
 * - Guided meditation exercises
 * - Visualization exercises
 * - Body scan exercises
 */
object ExercisesSeedData {

    /**
     * Returns the complete list of seed exercises.
     */
    fun getExercises(): List<ExerciseEntity> = listOf(
        // ========== BREATHING EXERCISES ==========

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Box Breathing",
            description = "A calming breathing technique used by Navy SEALs to reduce stress and improve focus. Inhale, hold, exhale, and hold for equal counts.",
            type = ExerciseType.BREATHING,
            duration = 240, // 8 cycles × 16 seconds each = 4 minutes
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Find a comfortable seated position",
                "Close your eyes or maintain a soft gaze",
                "Inhale slowly through your nose for 4 seconds",
                "Hold your breath for 4 seconds",
                "Exhale slowly through your mouth for 4 seconds",
                "Hold your breath for 4 seconds",
                "Repeat for 8 complete cycles",
                "Notice how you feel calmer and more centered"
            ),
            category = "Stress Relief",
            breathingInhale = 4,
            breathingHold1 = 4,
            breathingExhale = 4,
            breathingHold2 = 4,
            breathingCycles = 8
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "4-7-8 Breathing",
            description = "Dr. Andrew Weil's relaxation breathing technique. Promotes deep relaxation and can help with falling asleep.",
            type = ExerciseType.BREATHING,
            duration = 120, // 4 cycles × ~20 seconds each
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Sit comfortably with your back straight",
                "Place the tip of your tongue behind your upper front teeth",
                "Exhale completely through your mouth",
                "Close your mouth and inhale through your nose for 4 seconds",
                "Hold your breath for 7 seconds",
                "Exhale completely through your mouth for 8 seconds",
                "Repeat for 4 complete cycles",
                "Feel your body becoming deeply relaxed"
            ),
            category = "Relaxation",
            breathingInhale = 4,
            breathingHold1 = 7,
            breathingExhale = 8,
            breathingHold2 = 0,
            breathingCycles = 4
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Deep Breathing",
            description = "Classic deep breathing to activate the parasympathetic nervous system and promote calmness.",
            type = ExerciseType.BREATHING,
            duration = 300, // 6 cycles × ~14 seconds each = 5 minutes
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Sit or lie down in a comfortable position",
                "Place one hand on your chest, one on your belly",
                "Breathe in deeply through your nose for 5 seconds",
                "Feel your belly expand",
                "Hold briefly for 2 seconds",
                "Exhale slowly through your mouth for 5 seconds",
                "Hold briefly for 2 seconds",
                "Repeat for 6 cycles",
                "Notice the sense of calm washing over you"
            ),
            category = "Relaxation",
            breathingInhale = 5,
            breathingHold1 = 2,
            breathingExhale = 5,
            breathingHold2 = 2,
            breathingCycles = 6
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Energizing Breath",
            description = "Quick, rhythmic breathing to increase energy and alertness. Perfect for a midday boost.",
            type = ExerciseType.BREATHING,
            duration = 180, // 10 cycles × 6 seconds each = 3 minutes
            difficulty = Difficulty.INTERMEDIATE,
            instructions = listOf(
                "Sit up straight with alert posture",
                "Take a few normal breaths to prepare",
                "Breathe in quickly through your nose for 3 seconds",
                "Exhale forcefully through your mouth for 3 seconds",
                "Maintain a quick, rhythmic pace",
                "Continue for 10 cycles",
                "End with a few normal breaths",
                "Notice your increased energy and clarity"
            ),
            category = "Energy",
            breathingInhale = 3,
            breathingHold1 = 0,
            breathingExhale = 3,
            breathingHold2 = 0,
            breathingCycles = 10
        ),

        // ========== MEDITATION EXERCISES ==========

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Mindful Breathing Meditation",
            description = "Simple yet powerful meditation focusing on the breath. Builds awareness and presence.",
            type = ExerciseType.MEDITATION,
            duration = 600, // 10 minutes
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Find a quiet, comfortable place to sit",
                "Close your eyes and relax your body",
                "Bring your attention to your natural breathing",
                "Notice the sensation of breath entering and leaving",
                "When your mind wanders, gently return to the breath",
                "Don't judge yourself for wandering thoughts",
                "Continue observing your breath with kindness",
                "Gradually expand awareness to your whole body",
                "When ready, slowly open your eyes"
            ),
            category = "Mindfulness"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Loving-Kindness Meditation",
            description = "Cultivate compassion for yourself and others. Promotes emotional well-being and connection.",
            type = ExerciseType.MEDITATION,
            duration = 900, // 15 minutes
            difficulty = Difficulty.INTERMEDIATE,
            instructions = listOf(
                "Sit comfortably and close your eyes",
                "Take a few deep, settling breaths",
                "Bring to mind someone you love deeply",
                "Silently repeat: 'May you be happy, may you be healthy, may you be safe'",
                "Feel the warmth of these wishes",
                "Extend these wishes to yourself",
                "Then to a neutral person you know",
                "Finally, extend to all beings everywhere",
                "Rest in the feeling of loving-kindness",
                "Slowly return to the present moment"
            ),
            category = "Compassion"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Body Awareness Meditation",
            description = "Develop deeper connection with your physical sensations and embodied presence.",
            type = ExerciseType.MEDITATION,
            duration = 720, // 12 minutes
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Lie down or sit in a supported position",
                "Close your eyes and breathe naturally",
                "Scan through your body from head to toe",
                "Notice areas of tension or relaxation",
                "Observe sensations without trying to change them",
                "Bring curiosity to each body part",
                "If you find tension, breathe into it gently",
                "Appreciate your body's wisdom and intelligence",
                "Complete the practice with gratitude",
                "Slowly transition back to movement"
            ),
            category = "Awareness"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Stoic Contemplation",
            description = "Reflect on Stoic principles of wisdom, courage, justice, and temperance. Cultivate resilience.",
            type = ExerciseType.MEDITATION,
            duration = 900, // 15 minutes
            difficulty = Difficulty.ADVANCED,
            instructions = listOf(
                "Sit in a dignified, alert posture",
                "Take several centering breaths",
                "Reflect on Marcus Aurelius's words: 'The impediment to action advances action. What stands in the way becomes the way.'",
                "Consider a current challenge in your life",
                "Ask: What is within my control?",
                "Ask: How can I respond with wisdom?",
                "Contemplate the virtues: wisdom, courage, justice, temperance",
                "Imagine yourself embodying these virtues",
                "Set an intention for your day",
                "Return to awareness with renewed perspective"
            ),
            category = "Philosophy"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Gratitude Meditation",
            description = "Focus on appreciation and thankfulness. Shifts perspective toward abundance and joy.",
            type = ExerciseType.MEDITATION,
            duration = 600, // 10 minutes
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Sit comfortably and gently close your eyes",
                "Bring attention to your heart center",
                "Recall three things you're grateful for today",
                "Allow yourself to really feel the gratitude",
                "Notice how gratitude feels in your body",
                "Expand to gratitude for simple things: breath, shelter, food",
                "Include gratitude for challenges that helped you grow",
                "Send gratitude to people who've supported you",
                "Rest in the warmth of appreciation",
                "Carry this feeling into your day"
            ),
            category = "Gratitude"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Present Moment Awareness",
            description = "Practice being fully present with whatever arises. Foundation of mindfulness.",
            type = ExerciseType.MEDITATION,
            duration = 900, // 15 minutes
            difficulty = Difficulty.INTERMEDIATE,
            instructions = listOf(
                "Sit in a stable, comfortable position",
                "Allow your eyes to close or soften",
                "Simply be aware of this present moment",
                "Notice sounds, sensations, thoughts, emotions",
                "Don't try to change anything",
                "When you notice you're lost in thought, return to presence",
                "Be gentle and patient with yourself",
                "Observe the flow of experience without grasping",
                "Rest in the simplicity of being",
                "Gradually return to full awareness"
            ),
            category = "Mindfulness"
        ),

        // ========== VISUALIZATION EXERCISES ==========

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Inner Sanctuary Visualization",
            description = "Create and visit your personal inner sanctuary. A place of complete peace and safety.",
            type = ExerciseType.VISUALIZATION,
            duration = 720, // 12 minutes
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Close your eyes and take several deep breaths",
                "Imagine yourself in a beautiful, peaceful place",
                "This is your sanctuary—perfectly safe and calm",
                "Notice the colors, sounds, and sensations",
                "Perhaps there's water, trees, mountains, or a cozy room",
                "This place is yours alone",
                "Explore your sanctuary at your own pace",
                "Know you can return here anytime",
                "When ready, slowly return to the present",
                "Carry this sense of peace with you"
            ),
            category = "Relaxation"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Healing Light Visualization",
            description = "Visualize healing energy flowing through your body. Promotes well-being and recovery.",
            type = ExerciseType.VISUALIZATION,
            duration = 600, // 10 minutes
            difficulty = Difficulty.INTERMEDIATE,
            instructions = listOf(
                "Lie down in a comfortable position",
                "Close your eyes and breathe deeply",
                "Imagine a warm, healing light above you",
                "This light is your chosen color of healing",
                "Watch it slowly descend toward your body",
                "Feel it enter through the crown of your head",
                "The light flows through every cell, healing and renewing",
                "It dissolves any tension or discomfort",
                "Your whole body glows with this healing energy",
                "Rest in this state of wellness and vitality"
            ),
            category = "Healing"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Future Self Visualization",
            description = "Connect with your highest potential self. Clarifies goals and inspires positive action.",
            type = ExerciseType.VISUALIZATION,
            duration = 900, // 15 minutes
            difficulty = Difficulty.ADVANCED,
            instructions = listOf(
                "Sit comfortably and close your eyes",
                "Take several grounding breaths",
                "Imagine yourself one year from now",
                "You've achieved your most important goals",
                "See yourself clearly: How do you look? How do you carry yourself?",
                "What are you doing? Who are you with?",
                "How does it feel to be this version of yourself?",
                "Ask your future self for guidance",
                "Listen for their wisdom",
                "Thank them and slowly return to now",
                "Bring one insight back with you"
            ),
            category = "Motivation"
        ),

        // ========== BODY SCAN EXERCISES ==========

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Progressive Muscle Relaxation",
            description = "Systematically tense and release muscle groups. Excellent for releasing physical tension.",
            type = ExerciseType.BODY_SCAN,
            duration = 900, // 15 minutes
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Lie down in a comfortable position",
                "Take a few deep, settling breaths",
                "Start with your feet: tense them tightly for 5 seconds",
                "Release and notice the relaxation",
                "Move to your calves, then thighs",
                "Continue up through abdomen, chest, arms, hands",
                "Tense your shoulders up to your ears",
                "Finally, scrunch and release your face",
                "Scan your entire body, noticing the deep relaxation",
                "Rest in this state of complete ease"
            ),
            category = "Stress Relief"
        ),

        ExerciseEntity(
            id = UUID.randomUUID().toString(),
            title = "Mindful Body Scan",
            description = "Gentle awareness of body sensations from head to toe. Develops mindful attention.",
            type = ExerciseType.BODY_SCAN,
            duration = 1200, // 20 minutes
            difficulty = Difficulty.INTERMEDIATE,
            instructions = listOf(
                "Lie down in a comfortable, supported position",
                "Close your eyes and breathe naturally",
                "Bring attention to the top of your head",
                "Notice any sensations: tingling, warmth, pressure, or nothing at all",
                "Slowly move awareness down through your face, neck, shoulders",
                "Spend time with each body part",
                "If you find areas of tension, breathe into them",
                "Continue down through arms, torso, hips, legs, feet",
                "Notice how your whole body feels",
                "Rest in full-body awareness",
                "Slowly wiggle fingers and toes",
                "Gently return to the room"
            ),
            category = "Awareness"
        )
    )

    /**
     * Gets distinct categories from all exercises.
     */
    fun getCategories(): List<String> {
        return getExercises()
            .map { it.category }
            .distinct()
            .sorted()
    }
}
