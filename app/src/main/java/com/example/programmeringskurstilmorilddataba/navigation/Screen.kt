package com.example.programmeringskurstilmorilddataba.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("adminDashboard")
    object UserUI : Screen("userUI")
    object UserProfile : Screen("userProfile")
    object UserCourses : Screen("userCourses")
    object UserSettings : Screen("userSettings")
    object ChangeUsername : Screen("changeName")
    object ChangePassword : Screen("changePassword")

    object CourseScreen : Screen("courseScreen/{courseName}") {
        fun createRoute(courseName: String) = "courseScreen/$courseName"
    }

    object CourseModules : Screen("courseModules/{courseName}") {
        fun createRoute(courseName: String) = "courseModules/$courseName"
    }

    object ModuleEditorScreen : Screen("moduleEditor/{courseName}/{moduleId}") {
        fun createRoute(courseName: String, moduleId: String) = "moduleEditor/$courseName/$moduleId"
    }

    object ChapterViewScreen : Screen("chapterView/{courseId}/{moduleId}/{chapterId}") {
        fun createRoute(courseId: String, moduleId: String, chapterId: String) =
            "chapterView/$courseId/$moduleId/$chapterId"
    }
    object TaskOptionsScreen : Screen("taskOptions/{courseId}/{moduleId}/{chapterId}/{taskId}") {
        fun createRoute(
            courseId: String,
            moduleId: String,
            chapterId: String,
            taskId: String
        ) = "taskOptions/$courseId/$moduleId/$chapterId/$taskId"
    }
    object DropDownTaskOptionsScreen : Screen("dropDownTaskOptions/{courseId}/{moduleId}/{chapterId}/{taskId}") {
        fun createRoute(
            courseId: String,
            moduleId: String,
            chapterId: String,
            taskId: String
        ) = "dropDownTaskOptions/$courseId/$moduleId/$chapterId/$taskId"
    }
}