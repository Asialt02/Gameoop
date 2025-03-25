package com.example.programmeringskurstilmorilddataba

import com.google.firebase.firestore.FirebaseFirestore

fun loadCourseDataFromDatabase() {
    val db = FirebaseFirestore.getInstance()

    var data = "If you see this something fucked up"

    db.collection("courses_v2_(Testing)").document("courseID1Testing").get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                data = document.get("courseName").toString()
                println(data)
            }

        }
        .addOnFailureListener { e ->
            println(e.message)
        }

    println(data)
}

data class CourseInformation(
    val id: Int = 0,
    val name: String = "Placeholder",
    val description: String = "Placeholder",
    val noOfModules: Int = 0,

    var completedModules: Int = 0,

    var unlocked: Boolean = false,

    val containedModules: List<ModuleInformation> = listOf()
)

data class ModuleInformation(
    val id: Int = 0,
    val name: String = "Placeholder",
    val description: String = "Placeholder",
    val difficulty: String = "Placeholder",
    val noOfChapters: Int = 0,

    var completedChapters: Int = 0
)

class DummyCourse {
    fun loadFirstCourseInformation(): CourseInformation {
        return CourseInformation(
            id = 1,
            name = "Test course",
            description = "For demonstrating UI",
            noOfModules = 4,
            unlocked = true,
            containedModules = listOf(
                ModuleInformation(
                    id = 1,
                    name = "Variables",
                    description = "Introduction to use of variables",
                    difficulty = "Easy",
                    noOfChapters = 6
                ),
                ModuleInformation(
                    id = 2,
                    name = "Loops",
                    description = "Introduction to use of loops",
                    difficulty = "Easy",
                    noOfChapters = 8
                ),
                ModuleInformation(
                    id = 3,
                    name = "The rest",
                    description = "Every other programming skill",
                    difficulty = "Very hard",
                    noOfChapters = 3981
                )
            )
        )
    }

    fun loadMultiCoursesInformation(): List<CourseInformation> {
        return listOf(
            CourseInformation(
                id = 1,
                name = "First Course",
                description = "Collection of some basic coding concepts.",
                noOfModules = 4,
                unlocked = true,
                containedModules = listOf(
                    ModuleInformation(
                        id = 1,
                        name = "Variables",
                        description = "Introduction to use of variables",
                        difficulty = "Beginner",
                        noOfChapters = 6
                    ),
                    ModuleInformation(
                        id = 2,
                        name = "Loops",
                        description = "Introduction to use of loops",
                        difficulty = "Intermediate",
                        noOfChapters = 8
                    ),
                    ModuleInformation(
                        id = 3,
                        name = "Objects and Classes",
                        description = "Introduction to objects as well as some simple classes.",
                        difficulty = "Advanced",
                        noOfChapters = 14
                    )
                )
            ),
            CourseInformation(
                id = 2,
                name = "Second test course",
                description = "Lorem ipsum",
                noOfModules = 2,
                containedModules = listOf(
                    ModuleInformation(
                        id = 4,
                    ),
                    ModuleInformation(
                        id = 5
                    ),
                    ModuleInformation(
                        id = 6
                    )
                )
            ),
            CourseInformation(
                containedModules = listOf(
                    ModuleInformation(
                        id = 7
                    ),
                    ModuleInformation(
                        id = 8
                    ),
                    ModuleInformation(
                        id = 9
                    )
                )
            )
        )
    }
}

