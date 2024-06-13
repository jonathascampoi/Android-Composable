package com.example.androidcomposable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.androidcomposable.presentation.draganddrop.CommonDragItem
import com.example.androidcomposable.presentation.draganddrop.MyState
import com.example.androidcomposable.presentation.draganddrop.ReorderWhenDrag
import com.example.androidcomposable.presentation.draganddrop.ReorderingFallingIndex
import com.example.androidcomposable.ui.theme.AndroidComposableTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidComposableTheme {
                val navController = rememberNavController()
                val currentScreenTitle = remember { mutableStateOf("Home") }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(text = currentScreenTitle.value) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.secondary,
                            ),
                            navigationIcon = {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route
                                if (currentRoute != "home") {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            }
                        )
                    },
                ) { innerPadding ->
                    Navigation(
                        navController = navController,
                        onTitleChange = { currentScreenTitle.value = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            onTitleChange("Home")
            HomeContent(navController, modifier)
        }
        composable("commonDragItem") {
            onTitleChange("Common Drag Item")
            CommonDragItem(modifier)
        }
        composable("reorderingFallingIndex") {
            onTitleChange("Reordering Falling Index")
            ReorderingFallingIndex(modifier)
        }
        composable("reorderWhenDrag") {
            onTitleChange("Reorder When Drag")
            ReorderWhenDrag(MyState(), modifier)
        }
    }
}

@Composable
fun HomeContent(navController: NavController, modifier: Modifier = Modifier) {
    Column(modifier= modifier) {
        NavigationItem(text = "Common Drag Item", onClick = { navController.navigate("commonDragItem") })
        NavigationItem(text = "Reordering Falling Index", onClick = { navController.navigate("reorderingFallingIndex") })
        NavigationItem(text = "Reorder When Drag", onClick = { navController.navigate("reorderWhenDrag") })
    }
}

@Composable
fun NavigationItem(text: String, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp)
    ) {
        Text(text = text)
        Spacer(modifier = Modifier.width(8.dp))
    }
}