package com.example.programmeringskurstilmorilddataba.ui.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.example.programmeringskurstilmorilddataba.navigation.BottomNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun FriendsScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var allFriends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("friends")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    snapshot?.let {
                        allFriends = it.documents.map { doc ->
                            Friend(
                                userId = doc.getString("userId") ?: "",
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                levelInfo = doc.getString("level") ?: ""
                            )
                        }
                    }
                }
        }
    }

    fun searchAllUsers(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }

        isLoading = true
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                searchResults = snapshot.documents
                    .filter { doc ->
                        doc.id != currentUser?.uid && (
                                (doc.getString("name") ?: "").contains(query, ignoreCase = true) ||
                                        (doc.getString("email") ?: "").contains(query, ignoreCase = true)
                                )
                    }
                    .map { doc ->
                        User(
                            id = doc.id,
                            name = doc.getString("name") ?: "No name",
                            email = doc.getString("email") ?: ""
                        )
                    }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Search failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun addFriend(friend: User) {
        currentUser?.uid?.let { userId ->
            val friendData = hashMapOf(
                "userId" to friend.id,
                "name" to friend.name,
                "email" to friend.email,
                "addedAt" to FieldValue.serverTimestamp()
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("friends")
                .document(friend.id)
                .set(friendData)
                .addOnSuccessListener {
                    Toast.makeText(context, "${friend.name} added!", Toast.LENGTH_SHORT).show()
                    searchQuery = ""
                    searchResults = emptyList()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to add friend", Toast.LENGTH_SHORT).show()
                    Log.e("AddFriend", "Error adding friend", e)
                }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Search for friends",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        searchQuery = newValue
                        if (newValue.isBlank()) {
                            searchResults = emptyList()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { searchAllUsers(searchQuery) },
                    modifier = Modifier.height(56.dp),
                    enabled = !isLoading && searchQuery.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Search")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                searchResults.isNotEmpty() -> {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Search Results",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(searchResults) { user ->
                                UserSearchResultItem(
                                    user = user,
                                    currentUserId = currentUser?.uid ?: "",
                                    onAddFriend = { addFriend(user) }
                                )
                                Divider()
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Friends",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (allFriends.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No friends yet. Search for friends to add!")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(allFriends) { friend ->
                                    FriendItem(friend = friend)
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun UserSearchResultItem(
    user: User,
    currentUserId: String,
    onAddFriend: () -> Unit
) {
    var isFriend by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(user.id)
            .get()
            .addOnSuccessListener { snapshot ->
                isFriend = snapshot.exists()
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        if (isFriend) {
            Text("Friends", color = Color.Green)
        } else {
            Button(onClick = {
                onAddFriend()
                isFriend = true
            }) {
                Text("Add Friend")
            }
        }
    }
}

@Composable
fun FriendItem(friend: Friend) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray))

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = friend.levelInfo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            if (friend.email.isNotBlank()) {
                Text(
                    text = friend.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SearchResultsScreen(navController: NavController, searchQuery: String) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var searchResults by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereGreaterThanOrEqualTo("name", searchQuery)
                .whereLessThanOrEqualTo("name", searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener { snapshot ->
                    searchResults = snapshot.documents.map { doc ->
                        User(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    LazyColumn {
        items(searchResults) { user ->
            UserSearchResultItem(
                user = user,
                currentUserId = currentUser?.uid ?: "",
                onAddFriend = {
                    currentUser?.uid?.let { userId ->
                        addFriend(userId, user) {
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

fun addFriend(
    currentUserId: String,
    friend: User,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    val friendData = hashMapOf(
        "userId" to friend.id,
        "name" to friend.name,
        "email" to friend.email,
        "addedAt" to FieldValue.serverTimestamp()
    )

    db.collection("users")
        .document(currentUserId)
        .collection("friends")
        .document(friend.id)
        .set(friendData)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->

            Log.e("AddFriend", "Error adding friend", e)
        }
}

data class Friend(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val levelInfo: String = ""
)

data class User(
    val id: String,
    val name: String,
    val email: String = ""
)