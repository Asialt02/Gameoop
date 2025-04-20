package com.example.programmeringskurstilmorilddataba.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.programmeringskurstilmorilddataba.ChangeDisplayNameScreen
import com.example.programmeringskurstilmorilddataba.ChangePasswordScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.SettingsScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.CourseModules
import com.example.programmeringskurstilmorilddataba.ui.ui.UserCourses
import com.example.programmeringskurstilmorilddataba.ui.ui.UserProfile
import com.example.programmeringskurstilmorilddataba.ui.ui.UserUIScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.admin.AdminDashboard
import com.example.programmeringskurstilmorilddataba.ui.ui.admin.ChapterViewScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.admin.ModuleEditorScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.admin.TaskOptionsScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.admin.DropDownTaskOptionsScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.auth.LoginScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.auth.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Authentication
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        // Admin
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(navController)
        }

        // User
        composable(Screen.UserUI.route) {
            UserUIScreen(navController)
        }
        composable(Screen.UserProfile.route) {
            UserProfile(navController)
        }
        composable(Screen.UserCourses.route) {
            UserCourses(navController)
        }
        composable(Screen.UserSettings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.ChangeUsername.route) {
            ChangeDisplayNameScreen(navController)
        }
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(navController)
        }

        // Course Navigation
        composable(Screen.CourseScreen.route) { backStackEntry ->
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            com.example.programmeringskurstilmorilddataba.ui.ui.admin.CourseScreen(
                navController,
                courseName
            )
        }

        composable(Screen.CourseModules.route) { backStackEntry ->
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            CourseModules(navController, courseName)
        }

        composable(Screen.ModuleEditorScreen.route) { backStackEntry ->
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            ModuleEditorScreen(navController, courseName, moduleId)
        }

        composable(Screen.ChapterViewScreen.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            ChapterViewScreen(navController, courseId, moduleId, chapterId)
        }
        composable(Screen.TaskOptionsScreen.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskOptionsScreen(
                navController = navController,
                courseId = courseId,
                moduleId = moduleId,
                chapterId = chapterId,
                taskId = taskId
            )
        }
        composable(Screen.DropDownTaskOptionsScreen.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            DropDownTaskOptionsScreen(
                navController = navController,
                courseId = courseId,
                moduleId = moduleId,
                chapterId = chapterId,
                taskId = taskId
            )
        }
    }
}