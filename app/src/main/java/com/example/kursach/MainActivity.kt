@file:Suppress("DEPRECATION")

package com.example.kursach


import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


private const val NOTIFICATION_PERMISSION_CODE = 1001
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем состояние таймера
        val isTimerRunning =
            getSharedPreferences("timer_prefs", MODE_PRIVATE).getBoolean("isTimerRunning", false)
        if (isTimerRunning) {
            // Таймер еще работает, можно отображать экран с таймером или другое UI
            setContent {

                // Отображение UI, связанного с таймером, или просто переход на экран с таймером
                // Например, вы можете использовать другой Composable для отображения состояния
            }
        } else {
            setContent {
                val db = Room.databaseBuilder(
                    LocalContext.current, AppDatabase::class.java, "AppDB"
                )
                    .allowMainThreadQueries()
                    .build()
                /*Surface(color = MaterialTheme.colorScheme.background) {
                NavigationExample() // Ваше главное содержимое*/
                var controller = rememberNavController()
                NavHost(navController = controller, startDestination = "SplashScreen") {
                    composable("SplashScreen") { SplashScreen(controller = controller) }
                    composable("main_screen") { main_screen(controller = controller, db = db) }
                    composable("settings") { settings(controller = controller, db = db) }
                    composable("tasks") { tasks(controller = controller, db = db) }
                    composable("statistic") { statistic(controller = controller, db = db) }
                    composable("tasks_edit/{task_id}") { tasks_edit(controller = controller, db = db, it.arguments?.getString("task_id")) }


                }
            }
        }
    }
}



    //главная страница
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun main_screen(controller: NavHostController, db: AppDatabase) {
        val textfamilyInterMedium = FontFamily(Font(R.font.intermedium))
        val textfamilyInterBold = FontFamily(Font(R.font.interbold))
        var showDialog by remember { mutableStateOf(false) }
        var showDialogEditCategory by remember { mutableStateOf(false) }
        val context = LocalContext.current



        var newCategoryName by remember { mutableStateOf("") }

        var newCategoryColorRed by remember { mutableStateOf(true) }
        var newCategoryColorBlue by remember { mutableStateOf(false) }
        var newCategoryColorGreen by remember { mutableStateOf(false) }
        var newCategoryColorYellow by remember { mutableStateOf(false) }

        //var allUniqueDates = db.taskDao().allUniqueDate()
        var allUniqueDates by remember { mutableStateOf(db.taskDao().allUniqueDate()) }
        var allCategory = db.taskDao().allCategory()

        var selectedCategory by remember { mutableStateOf(-1) }


        fun getUniqueDateWithCategory(category_id: Int):List<String>
        {
            var result:List<String>

            if (category_id == -1)
            {
                result = db.taskDao().allUniqueDate()
            }
            else
            {
                result = db.taskDao().allUniqueDateWithCategory(category_id)
            }

            return result
        }

        fun getTaskWithDateAndCategory(category_id: Int, date: String):List<Task>
        {
            var result:List<Task>

            if (category_id == -1)
            {
                result = db.taskDao().allTaskFromDate(date)
            }
            else
            {
                result = db.taskDao().allTaskFromDateWithCategory(date, category_id)
            }

            return result
        }

        //allUniqueDates = getUniqueDateWithCategory(selectedCategory)

        /*LaunchedEffect(Unit) {
            tasksState.value =  // Замените на ваш метод получения задач
        }*/

        Scaffold(
            modifier = Modifier
                .background(Color.White),
            topBar = {
                Column(
                    Modifier .background(Color.White)
                ) {
                    Text(
                        text = "TimeCheck",
                        Modifier
                            .padding(top = 5.dp, start = 10.dp),
                        fontSize = 20.sp,
                        color = Color(5, 10, 126, 255),
                        fontFamily = textfamilyInterBold
                    )
                    LazyRow(
                        Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                            .border(2.dp, Color.Black, RoundedCornerShape(30.dp))
                            .padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(allCategory.size) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .combinedClickable(
                                        onClick = {
                                            // Логика для короткого нажатия
                                            if (selectedCategory == allCategory[it].id) {
                                                selectedCategory = -1
                                            } else {
                                                selectedCategory = allCategory[it].id
                                            }
                                        },
                                        onLongClick = {
                                            showDialogEditCategory = true // Открыть диалог при длительном нажатии
                                        }
                                    )
                                    .background(Color(allCategory[it].color))
                                    .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(20.dp))
                                    .padding(top= 10.dp,bottom=10.dp,start=20.dp,end=20.dp)
                            ) {
                                Text(
                                    text = allCategory[it].name,
                                    fontSize = 17.sp,
                                    fontFamily = textfamilyInterMedium,
                                    color = Color.White
                                )
                            }
                        }
                        item {
                            //кнопка для добавления новой категории, а вот выше уже новая категория которая добавляется при клике на кнопку добавления
                            Button(
                                onClick = { showDialog = true }, // Открываем диалог при нажатии
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color(0xFF000000),
                                    containerColor = Color(0xFFE7E6E6)
                                ),
                                border = BorderStroke(1.dp, Color.Black),
                            ) {
                                Text(
                                    text = "Добавить категорию +",
                                    fontSize = 17.sp,
                                    fontFamily = textfamilyInterMedium
                                )
                            }
                        }
                    }
                }

            },

            bottomBar = {
                BottomAppBar(
                    containerColor = Color.White,
                    modifier = Modifier
                        .padding(start = 15.dp, end = 15.dp, bottom = 5.dp)
                        .border(4.dp, Color(36, 41, 142, 255), RoundedCornerShape(40.dp))
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(painterResource(R.drawable.calendar), contentDescription = "",
                            modifier = Modifier
                                .clickable {
                                    controller.navigate("statistic")
                                })//статистика
                        Image(painterResource(R.drawable.add), contentDescription = "",
                            modifier = Modifier
                                .clickable { controller.navigate("tasks") }
                        )// добавление новой задачи
                        Image(painterResource(R.drawable.account),
                            contentDescription = "", //настройки
                            modifier = Modifier
                                .clickable { controller.navigate("settings") }
                        )
                    }
                }
            },


            content = { innerPadding ->
                Box(
                    Modifier
                        .padding(10.dp)
                        .background(Color.White)
                ) {
                    Image(
                        painterResource(R.drawable.background), "",

                        )

                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(allUniqueDates.size) {
                            var tasksForDate by remember { mutableStateOf(getTaskWithDateAndCategory(selectedCategory, allUniqueDates[it])) }
                            //var tasksForDate = getTaskWithDateAndCategory(selectedCategory, allUniqueDates[it])
                            Column {
                                Text(
                                    text = allUniqueDates[it],
                                    fontSize = 17.sp,
                                    fontFamily = textfamilyInterMedium,
                                    modifier = Modifier
                                        .background(Color.White)
                                        .border(2.dp, Color.Black, RoundedCornerShape(20.dp))
                                        .padding(
                                            start = 10.dp,
                                            end = 10.dp,
                                            top = 5.dp,
                                            bottom = 5.dp
                                        )
                                )
                                tasksForDate.forEach{ task ->
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = task.status_task, onCheckedChange =
                                                {
                                                    if (task.status_task == false )
                                                    {
                                                        db.taskDao().updateStatusTask(true, task.id)

                                                        val sdfTime = SimpleDateFormat("HH:mm")
                                                        var currentDateAndTime = sdfTime.format(Date())

                                                        var timeDifference = calculateTimeDifference(task.start_time, currentDateAndTime)

                                                        db.taskDao().updateEndTimeTask(currentDateAndTime, task.id)
                                                        db.taskDao().insert(Statistic(task.category, task.date, timeDifference.asString, task.id))


                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = Color(2, 190, 43, 255),
                                                    uncheckedColor = Color(5, 10, 126, 255),
                                                    checkmarkColor = Color.White
                                                ),
                                                modifier = Modifier.scale(1.5f)
                                            )
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 5.dp)
                                                    .background(Color.White)
                                                    .border(
                                                        2.dp,
                                                        Color.Black,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { if (!task.status_task) { controller.navigate("tasks_edit/${task.id}") } }
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Column {
                                                    Text(
                                                        text = task.start_time,
                                                        fontSize = 17.sp,
                                                        fontFamily = textfamilyInterMedium
                                                    )
                                                    Text(
                                                        text = task.end_time,
                                                        fontSize = 17.sp,
                                                        fontFamily = textfamilyInterMedium
                                                    )
                                                }
                                                Text(
                                                    text = task.heading,
                                                    fontSize = 17.sp,
                                                    fontFamily = textfamilyInterMedium,
                                                    maxLines = 2, // Ограничиваем количество строк до 2
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                }
                            }
                        }
                    }

                    // Диалоговое окно создания категории
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showDialog = false
                            }, // Закрытие при нажатии вне диалога
                            title = {
                                Text(text = "Добавить категорию",
                                    fontFamily = textfamilyInterMedium)
                            },
                            text = {
                                Column {
                                    Text("Название категории:",
                                        fontFamily = textfamilyInterMedium)
                                    TextField(
                                        value = newCategoryName,
                                        onValueChange = {
                                            if(it.length<=15) {
                                                newCategoryName = it
                                            } },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(25.dp)),
                                        textStyle = TextStyle(
                                            fontSize = 18.sp,
                                            color = Color.Black
                                        ),
                                        placeholder = {
                                            Text(
                                                text = "Категория",
                                                fontSize = 15.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center,
                                                fontFamily = textfamilyInterBold
                                            )
                                        },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                    Text(text = "Цвет категории:",
                                        fontFamily = textfamilyInterMedium)
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorGreen,
                                            onClick = {
                                                newCategoryColorGreen = true
                                                newCategoryColorRed = false
                                                newCategoryColorBlue = false
                                                newCategoryColorYellow = false

                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Green,
                                                unselectedColor = Color.Green,
                                            )
                                        )
                                        Text(text = "Зеленый",
                                            fontFamily = textfamilyInterMedium)
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorBlue,
                                            onClick = {
                                                newCategoryColorGreen = false
                                                newCategoryColorRed = false
                                                newCategoryColorBlue = true
                                                newCategoryColorYellow = false
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Blue,
                                                unselectedColor = Color.Blue,
                                            )
                                        )
                                        Text(text = "Синий",
                                            fontFamily = textfamilyInterMedium)
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorRed,
                                            onClick = {
                                                newCategoryColorGreen = false
                                                newCategoryColorRed = true
                                                newCategoryColorBlue = false
                                                newCategoryColorYellow = false
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Red,
                                                unselectedColor = Color.Red,
                                            )
                                        )
                                        Text(text = "Красный",
                                            fontFamily = textfamilyInterMedium)
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorYellow,
                                            onClick = {
                                                newCategoryColorGreen = false
                                                newCategoryColorRed = false
                                                newCategoryColorBlue = false
                                                newCategoryColorYellow = true
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Yellow,
                                                unselectedColor = Color.Yellow,
                                            )
                                        )
                                        Text(text = "Желтый",
                                            fontFamily = textfamilyInterMedium)
                                    }


                                }

                            },

                            confirmButton = {
                                Button(
                                    onClick = {
                                        // Проверка на пустое название или только пробелы
                                        if (newCategoryName.isBlank()) {
                                            Toast.makeText(context, "Название категории не может быть пустым и содержать только пробелы", Toast.LENGTH_SHORT).show()

                                        }
                                        else {
                                            var newColor: Long = 0

                                            if (newCategoryColorRed) {
                                                newColor = 0xFFFF0000
                                            } else if (newCategoryColorBlue) {
                                                newColor = 0xFF0055FF
                                            } else if (newCategoryColorGreen) {
                                                newColor = 0xFF00FF0D
                                            } else if (newCategoryColorYellow) {
                                                newColor = 0xFFFFEB3B
                                            }

                                            // Вставка новой категории в базу данных
                                            db.taskDao().insert(Category(newCategoryName, newColor))

                                            // Очистка имени новой категории
                                            newCategoryName = ""

                                            // Обновление списка категорий
                                            //allCategory = db.taskDao().allCategory()

                                            // Закрытие диалога, если необходимо
                                            showDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(2, 190, 43, 255) // Задайте нужный цвет контейнера
                                    )
                                ) {
                                    Text(
                                        "Сохранить",
                                        fontFamily = textfamilyInterBold // Убедитесь, что этот шрифт определен
                                    )
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = { showDialog = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(190, 2, 2, 255)
                                    )// Закрытие при отмене
                                ) {
                                    Text(
                                        "Отмена",
                                        fontFamily = textfamilyInterBold
                                    )
                                }
                            },
                            containerColor = Color.White
                        )
                    }
                    if (showDialogEditCategory) {
                        AlertDialog(
                            onDismissRequest = {
                                showDialogEditCategory = false
                            }, // Закрытие при нажатии вне диалога
                            title = {
                                Text(text = "Редактировать категорию",
                                    fontFamily = textfamilyInterMedium)
                            },
                            text = {
                                Column {
                                    Text("Изменить название:",
                                        fontFamily = textfamilyInterMedium)
                                    TextField(
                                        value = newCategoryName,
                                        onValueChange = { newCategoryName = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(25.dp)),
                                        textStyle = TextStyle(
                                            fontSize = 18.sp,
                                            color = Color.Black
                                        ),
                                        placeholder = {
                                            Text(
                                                text = "Категория",
                                                fontSize = 15.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center,
                                                fontFamily = textfamilyInterBold
                                            )
                                        },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                    Text(text = "Изменить цвет категории:",
                                        fontFamily = textfamilyInterMedium)
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorGreen,
                                            onClick = {
                                                newCategoryColorGreen = true
                                                newCategoryColorRed = false
                                                newCategoryColorBlue = false
                                                newCategoryColorYellow = false

                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Green,
                                                unselectedColor = Color.Green,
                                            )
                                        )
                                        Text(text = "Зеленый",
                                            fontFamily = textfamilyInterMedium)
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorBlue,
                                            onClick = {
                                                newCategoryColorGreen = false
                                                newCategoryColorRed = false
                                                newCategoryColorBlue = true
                                                newCategoryColorYellow = false
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Blue,
                                                unselectedColor = Color.Blue,
                                            )
                                        )
                                        Text(text = "Синий",
                                            fontFamily = textfamilyInterMedium)
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorRed,
                                            onClick = {
                                                newCategoryColorGreen = false
                                                newCategoryColorRed = true
                                                newCategoryColorBlue = false
                                                newCategoryColorYellow = false
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Red,
                                                unselectedColor = Color.Red,
                                            )
                                        )
                                        Text(text = "Красный",
                                            fontFamily = textfamilyInterMedium)
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = newCategoryColorYellow,
                                            onClick = {
                                                newCategoryColorGreen = false
                                                newCategoryColorRed = false
                                                newCategoryColorBlue = false
                                                newCategoryColorYellow = true
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Yellow,
                                                unselectedColor = Color.Yellow,
                                            )
                                        )
                                        Text(text = "Желтый",
                                            fontFamily = textfamilyInterMedium)
                                    }


                                }

                            },

                            confirmButton = {
                                Button(
                                    onClick = {
                                        // Логика для подтверждения

                                        var newColor: Long = 0

                                        if (newCategoryColorRed) {
                                            newColor = 0xFFFF0000
                                        } else if (newCategoryColorBlue) {
                                            newColor = 0xFF0055FF
                                        } else if (newCategoryColorGreen) {
                                            newColor = 0xFF00FF0D
                                        } else if (newCategoryColorYellow) {
                                            newColor = 0xFFFFEB3B
                                        }

                                        db.taskDao().insert(Category(newCategoryName, newColor))

                                        newCategoryName = ""

                                        //allCategory = db.taskDao().allCategory()

                                        showDialogEditCategory = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(2, 190, 43, 255)
                                    )
                                ) {
                                    Text(
                                        "Сохранить",
                                        fontFamily = textfamilyInterBold
                                    )
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = { showDialogEditCategory = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(190, 2, 2, 255)
                                    )// Закрытие при отмене
                                ) {
                                    Text(
                                        "Отмена",
                                        fontFamily = textfamilyInterBold
                                    )
                                }
                            },
                            containerColor = Color.White
                        )
                    }
                }
            }
        )
    }

    //страница настроек
    @Composable
    fun settings(controller: NavHostController, db: AppDatabase ) {
        var showDialog by remember { mutableStateOf(false) } //показ диалогового окна
        var selectedSound by remember { mutableStateOf(R.raw.alarm_sound) }// это мелодия уведомления, работает или нет не знаю. то что в скобках это по умолчаёнию
        var showDialog1 by remember { mutableStateOf(false) }
        val textfamilyInterMedium = FontFamily(Font(R.font.intermedium))
        val textfamilyInterBold = FontFamily(Font(R.font.interbold))

        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val context: Context = LocalContext.current

        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        DisposableEffect(Unit) {
            onDispose {
                // Освобождаем ресурсы при закрытии экрана
                mediaPlayer?.release()
            }
        }

        Box {
            Image(
                painterResource(R.drawable.background), "",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                Text(
                    text = "Настройки",
                    fontSize = 25.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = textfamilyInterBold
                )
                Divider(thickness = 2.dp, color = Color.Black)
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    //строка для редактирования мелодии уведомлений
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 10.dp, RoundedCornerShape(25.dp))
                            .background(Color.White)
                            .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                            .clickable { showDialog = true }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painterResource(R.drawable.notifications), "",
                            Modifier.border(2.dp, Color.Black, CircleShape)
                        )
                        Text(
                            text = "Мелодии уведомлений",
                            fontSize = 17.sp,
                            fontFamily = textfamilyInterBold
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 10.dp, RoundedCornerShape(25.dp))
                            .background(Color.White)
                            .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                            .clickable { showDialog1 = true }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painterResource(R.drawable.info), "",
                            Modifier.border(2.dp, Color.Black, CircleShape)
                        )
                        Text(
                            text = "О разработчике",
                            fontSize = 17.sp,
                            fontFamily = textfamilyInterBold
                        )
                    }
                    
                    
                }


                //диалоговое окно
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                            mediaPlayer?.stop() // Остановить звук при закрытии диалога
                        },
                        title = { Text(text = "Изменить мелодию", fontFamily = textfamilyInterBold) },
                        text = {
                            Column {
                                Text(text = "Мелодии", fontSize = 17.sp,fontFamily = textfamilyInterMedium)

                                // Ваши звуки
                                val sounds =
                                    listOf(
                                        R.raw.moments,
                                        R.raw.guitar,
                                        R.raw.evening_cafe,
                                        R.raw.dj
                                    )

                                sounds.forEach { sound ->
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (sound == selectedSound),
                                            onClick = {
                                                selectedSound = sound

                                                // Останавливаем текущий MediaPlayer, если он играет
                                                mediaPlayer?.stop()
                                                mediaPlayer?.release()

                                                // Инициализируем новый MediaPlayer для выбранного звука
                                                mediaPlayer = MediaPlayer.create(context, sound).apply {
                                                    start()
                                                }
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(5, 10, 126, 255),
                                                unselectedColor = Color(5, 10, 126, 255),
                                            )
                                        )
                                        //выбор музыки, поч так не ебу ии писала
                                        Text(
                                            text = when (sound) {
                                                R.raw.moments -> "Моменты"
                                                R.raw.guitar -> "Гитара"
                                                R.raw.evening_cafe -> "Вечер, кафе"
                                                R.raw.dj -> "Диджей"
                                                else -> ""
                                            }, fontSize = 17.sp, fontFamily = textfamilyInterMedium
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    mediaPlayer?.stop()
                                    // Передаем выбранный звук в TimerFinishedActivity
                                    val sharedPreferences = context.getSharedPreferences("AppPreferences", MODE_PRIVATE)
                                    sharedPreferences.edit().putInt("selectedSound", selectedSound).apply()
                                    showDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(2, 190, 43, 255)
                                )
                            ) {
                                Text("Сохранить", fontFamily = textfamilyInterBold)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showDialog = false
                                    mediaPlayer?.stop() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(190, 2, 2, 255)
                                )
                            ) {
                                Text("Отмена", fontFamily = textfamilyInterBold)
                            }
                        },
                        containerColor = Color.White
                    )
                }
                if (showDialog1) {
                    AlertDialog(onDismissRequest = { showDialog1 = false },
                        title = {
                            Text(text = "О разработчике",fontSize = 25.sp,fontFamily = textfamilyInterBold) },
                        text = {
                            Column(
                                Modifier
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(15.dp)
                            ) {
                                Text(text = "by Pelicanus",
                                    fontSize = 20.sp,
                                    fontFamily = textfamilyInterMedium)
                                Image(painterResource(R.drawable.by), contentDescription = "",
                                    Modifier
                                        .size(200.dp,200.dp))
                                Text(text="Почта для обратной связи",
                                    fontSize = 20.sp,
                                    fontFamily = textfamilyInterMedium)
                                Text(text="pelicanus.info@mail.ru",
                                    fontSize = 20.sp,
                                    color=Color.Blue,
                                    textDecoration = TextDecoration.Underline,
                                    fontFamily = textfamilyInterMedium,
                                    modifier = Modifier
                                        .clickable {
                                            // Копируем текст в буфер обмена
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("pelicanus.info@mail.ru"))

                                            // Показываем Toast уведомление
                                            Toast.makeText(context, "Текст скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
                                        }
                                    )

                            }
                        },
                        confirmButton = {

                        },
                        dismissButton = {

                        },
                        containerColor = Color.White
                    )
                }
            }
        }
    }

    // Composable для выбора времени для страницы заметки
    @Composable
    fun TimePicker(label: String, onValueChange: (Int) -> Unit) {
        var selectedValue by remember { mutableStateOf(0) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label)
            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 0
                        maxValue = if (label == "Часы") 23 else 59
                        setOnValueChangedListener { _, _, newVal ->
                            selectedValue = newVal
                            onValueChange(newVal)
                        }
                    }
                },
                modifier = Modifier.width(80.dp)
            )
        }
    }

    data class TimeDifference(
        val asString: String,    // Разница во времени в формате "HH:mm"
        val inMilliseconds: Long // Разница во времени в миллисекундах
    )

    fun calculateTimeDifference(startTime: String, endTime: String): TimeDifference {
        // Определяем формат времени
        val dateFormat = SimpleDateFormat("HH:mm")

        // Преобразуем строки времени в объекты Date
        val startDate: Date = dateFormat.parse(startTime)
        val endDate: Date = dateFormat.parse(endTime)

        // Вычисляем разницу в миллисекундах
        val differenceInMillis: Long = endDate.time - startDate.time

        // Преобразуем разницу в часы и минуты
        val hours = TimeUnit.MILLISECONDS.toHours(differenceInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) % 60

        // Форматируем результат в строку "HH:mm"
        val formattedDifference = String.format("%02d:%02d", hours, minutes)

        return TimeDifference(
            asString = formattedDifference,
            inMilliseconds = differenceInMillis
        )
    }

    fun timeStringToMilliseconds(time: String): Long {
        // Проверяем, что строка имеет корректный формат
        val timeParts = time.split(":")
        if (timeParts.size != 2) {
            throw IllegalArgumentException("Неверный формат времени. Ожидалось HH:mm.")
        }

        // Извлекаем часы и минуты
        val hours = timeParts[0].toLongOrNull() ?: throw IllegalArgumentException("Некорректное значение для часов.")
        val minutes = timeParts[1].toLongOrNull() ?: throw IllegalArgumentException("Некорректное значение для минут.")

        // Вычисляем общее количество миллисекунд
        return (hours * 3600 + minutes * 60) * 1000
    }

    //страница добавления задачи
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun tasks(controller: NavHostController, db: AppDatabase) {
        val textfamilyInterMedium = FontFamily(Font(R.font.intermedium))
        val textfamilyInterBold = FontFamily(Font(R.font.interbold))
        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState()
        var taskText by remember { mutableStateOf("") }
        var taskDate by remember { mutableStateOf("") }
        var showDialog by remember { mutableStateOf(false) }
        var showDialogTime by remember { mutableStateOf(false) }
        var showDialogCategory by remember { mutableStateOf(false) }
        var hours by remember { mutableStateOf(0) }
        var minutes by remember { mutableStateOf(0) }
        var seconds by remember { mutableStateOf(0) }
        var isTimerStopped by remember { mutableStateOf(false) }
        var countDownTimer: CountDownTimer? = null
        var expanded by remember { mutableStateOf(false) }
        var isInputValid by remember { mutableStateOf(true) }
        val navController = rememberNavController()


        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date()) // Форматируем текущую дату
        var selectedDate by remember { mutableStateOf(currentDate) } // Инициализируем выбранную дату

        // Получаем текущее время в миллисекундах
        val currentTimeMillis = System.currentTimeMillis()

        var allCategories by remember { mutableStateOf(db.taskDao().allCategory()) }

        var selectedCategory = -1

        val context = LocalContext.current
        val notificationManager = remember {
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        val CHANNEL_ID = "timer_channel"
        val notificationId = 123

        val sdfTime = SimpleDateFormat("HH:mm")
        var currentDateAndTime = ""

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        taskDate = sdfDate.format(Date())



        // Запрос разрешения на отправку уведомлений
        requestNotificationPermission(context)

        // Создаем канал уведомлений (если Android O или выше)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Таймер",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления таймера"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Функция для создания уведомлений
        fun createBuilder(text: String, title: String): NotificationCompat.Builder {
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.avatar)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        }

        // Настройка ActivityResultLauncher для получения результата из TimerInfoActivity
        val activityLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                isTimerStopped = result.data?.getBooleanExtra("isTimerStopped", false) ?: false
                if (isTimerStopped) {
                    countDownTimer?.cancel() // Остановка таймера, если он был остановлен в TimerInfoActivity
                    notificationManager.cancel(notificationId) // Удаление уведомления
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Создание задачи",
                            fontFamily = textfamilyInterBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }

                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            modifier = Modifier
                                .background(Color.White),
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                modifier = Modifier
                                    .background(Color.White),
                                text = { Text("Выбрать дату", fontFamily = textfamilyInterMedium, fontSize = 17.sp) },
                                onClick = {
                                    showDatePicker = true
                                    expanded = false
                                }
                            )

                            DropdownMenuItem(
                                modifier = Modifier
                                    .background(Color.White),
                                text = { Text("Выбрать время",fontFamily = textfamilyInterMedium,fontSize = 17.sp) },
                                onClick = {
                                    showDialogTime = true
                                    expanded = false
                                }
                            )

                            DropdownMenuItem(
                            modifier = Modifier
                                .background(Color.White),
                                text = { Text("Выбрать категорию",fontFamily = textfamilyInterMedium,fontSize = 17.sp) },
                                onClick = {
                                    showDialogCategory = true
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                modifier = Modifier
                                    .background(Color.White),
                                text = { Text("Добавить таймер",fontFamily = textfamilyInterMedium,fontSize = 17.sp) },
                                onClick = {
                                    showDialog = true
                                    expanded = false
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    content = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //кнопка для сохранения задачи
                            Button(
                                onClick =
                                {
                                    if (taskText.isBlank()) {
                                        Toast.makeText(context, "Поле не может быть пустым или содержать только пробелы", Toast.LENGTH_SHORT).show()
                                    }
                                    else {
                                        // Устанавливаем текущее время, если оно пустое
                                        if (currentDateAndTime == "") {
                                            currentDateAndTime = sdfTime.format(Date())
                                        }
                                        // Вставляем задачу в базу данных
                                        db.taskDao().insert(Task(selectedCategory, taskText, taskDate, currentDateAndTime, "", false))
                                        Toast.makeText(context, "Задача сохранена", Toast.LENGTH_SHORT).show()
                                        controller.navigate("main_screen")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(2, 190, 43, 255)
                                )
                            ) {
                                Text(
                                    text = "Сохранить",
                                    color = Color.White,
                                    fontFamily = textfamilyInterBold
                                )
                            }
                            Button(
                                onClick = { controller.navigate("main_screen") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(190, 2, 2, 255)
                                )
                            ) {
                                Text(
                                    text = "Отмена",
                                    color = Color.White,
                                    fontFamily = textfamilyInterBold
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp) // Фиксированная высота
                        .background(Color.White, RoundedCornerShape(25.dp))
                        .verticalScroll(scrollState) // Применяем вертикальную прокрутку
                ) {
                    TextField(
                        value = taskText,
                        onValueChange = { newText ->
                            // Ограничиваем текст до 4 строк и до 100 символов
                            val lines = newText.lines() // Получаем строки из нового текста
                            if (lines.size <= 4 && newText.length <= 100) {
                                taskText = newText // Если все условия выполнены, обновляем текст
                            } else if (lines.size > 4) {
                                // Если количество строк превышает 4, берем только первые 4 строки
                                taskText =
                                    lines.take(4).joinToString("\n") // Объединяем строки обратно
                            } else if (newText.length > 100) {
                                // Если длина текста превышает 100 символов, обрезаем его
                                taskText = newText.take(100) // Берем только первые 100 символов
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 8.dp,
                                vertical = 8.dp
                            ), // Отступы внутри TextField
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            color = Color.Black
                        ),
                        placeholder = {
                            Text(
                                text = "Задача",
                                fontSize = 20.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontFamily = textfamilyInterBold
                            )
                        },
                        singleLine = false, // Позволяем многострочный ввод
                        maxLines = 4, // Ограничение на 4 строки
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }
            //показ календаря
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { selectedMillis ->
                                val currentTimeMillis = System.currentTimeMillis() // Получаем текущее время в миллисекундах

                                // Проверяем, что выбранная дата не меньше текущей
                                if (selectedMillis >= currentTimeMillis) {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    taskDate = sdf.format(Date(selectedMillis)) // Форматируем и сохраняем выбранную дату
                                    onDateSelected(selectedMillis) // Вызываем метод, если выбранная дата корректна
                                } else {
                                    // Здесь можно добавить уведомление о том, что дата выбрана неверно
                                    Toast.makeText(context, "Нельзя выбирать предыдущие даты", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showDatePicker = false // Закрываем диалог
                        }) {
                            Text("Ок", fontFamily = textfamilyInterMedium)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Отмена", fontFamily = textfamilyInterMedium)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState) // Отображаем DatePicker
                }
            }
            //диалоговое окно с таймеров
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Добавить таймер",fontFamily = textfamilyInterMedium) },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TimePicker("Часы",) { hours = it }
                            TimePicker("Минуты") { minutes = it }
                            TimePicker("Секунды") { seconds = it }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L
                                val endTime = System.currentTimeMillis() + totalMillis

                                // Intent для запуска TimerInfoActivity по нажатию на уведомление
                                val timerInfoIntent =
                                    Intent(context, TimerInfoActivity::class.java).apply {
                                        putExtra("endTime", endTime)

                                    }
                                val timerInfoPendingIntent = PendingIntent.getActivity(
                                    context,
                                    0,
                                    timerInfoIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                // Создаем уведомление о запущенном таймере
                                val notification = createBuilder(
                                    "Таймер завершится в ${
                                        SimpleDateFormat(
                                            "HH:mm",
                                            Locale.getDefault()
                                        ).format(Date(endTime))
                                    }",
                                    "Таймер запущен"
                                )
                                    .setContentIntent(timerInfoPendingIntent)
                                    .build()

                                notificationManager.notify(notificationId, notification)

                                // Сохраняем состояние таймера
                                val sharedPreferences = context.getSharedPreferences(
                                    "timer_prefs",
                                    Context.MODE_PRIVATE
                                )
                                sharedPreferences.edit().putBoolean("isTimerStopped", false)
                                    .apply() // Таймер запущен

                                // Запускаем таймер
                                countDownTimer = object : CountDownTimer(totalMillis, 1000) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        // Обновление уведомления или UI по мере необходимости
                                    }

                                    override fun onFinish() {
                                        val isStopped =
                                            sharedPreferences.getBoolean("isTimerStopped", false)

                                        Log.d("Timer", "Таймер завершен. isStopped: $isStopped")

                                        if (!isStopped) {
                                            // Уведомление о завершении таймера
                                            val finishNotification = createBuilder(
                                                "Таймер завершён",
                                                "Таймер завершен"
                                            ).build()
                                            notificationManager.notify(
                                                notificationId + 1,
                                                finishNotification
                                            )

                                            // Переход на TimerFinishedActivity
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    TimerFinishedActivity::class.java
                                                ).apply {
                                                    flags =
                                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                            )
                                        }
                                    }
                                }.start()

                                showDialog = false
                            }, colors = ButtonDefaults.buttonColors(
                                containerColor = Color(2, 190, 43, 255)
                            )
                        ) {
                            Text("Запустить таймер",fontFamily = textfamilyInterMedium)
                        }
                        // все что выше это логика одной кнопки!!!!
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(190, 2, 2, 255)
                            )
                        ) {
                            Text("Отмена",fontFamily = textfamilyInterMedium)
                        }
                    }
                )
            }
            if (showDialogTime) {
                AlertDialog(
                    onDismissRequest = { showDialogTime = false },
                    title = { Text("Время",fontFamily = textfamilyInterMedium) },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TimePicker("Часы",) { hours = it }
                            TimePicker("Минуты") { minutes = it }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val sdfTime = SimpleDateFormat("HH:mm")

                                val customCalendar = Calendar.getInstance()
                                customCalendar.set(Calendar.HOUR_OF_DAY, hours)
                                customCalendar.set(Calendar.MINUTE, minutes)

                                currentDateAndTime = sdfTime.format(customCalendar.time)

                                showDialogTime = false

                                }, colors = ButtonDefaults.buttonColors(
                                containerColor = Color(2, 190, 43, 255)
                            )
                        ) {
                            Text("Сохранить",fontFamily = textfamilyInterMedium)
                        }
                        // все что выше это логика одной кнопки!!!!
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialogTime = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(190, 2, 2, 255)
                            )
                        ) {
                            Text("Отмена",fontFamily = textfamilyInterMedium)
                        }
                    }
                )
            }
            if (showDialogCategory) {
                AlertDialog(
                    onDismissRequest = { showDialogCategory = false },
                    title = { Text("Категории",fontFamily = textfamilyInterMedium) },
                    text = {
                        LazyColumn {
                            items(allCategories.size){
                                Button(
                                    onClick =
                                    {
                                            selectedCategory = allCategories[it].id
                                            showDialogCategory = false

                                    }, colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(allCategories[it].color)
                                    )
                                ) {
                                    Text(allCategories[it].name,fontFamily = textfamilyInterMedium)
                                }

                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {


                            }, colors = ButtonDefaults.buttonColors(
                                containerColor = Color(2, 190, 43, 255)
                            )
                        ) {
                            Text("Сохранить",fontFamily = textfamilyInterMedium)
                        }
                        // все что выше это логика одной кнопки!!!!
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialogCategory= false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(190, 2, 2, 255)
                            )
                        ) {
                            Text("Отмена",fontFamily = textfamilyInterMedium)
                        }
                    }
                )
            }
        }
    }
    // Функция для запроса разрешения на отправку уведомлений
    private fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun tasks_edit(controller: NavHostController, db: AppDatabase, task_id:String?) {

    var context1 = LocalContext.current

    var current_task_id:Int = task_id?.toIntOrNull()?:0

    var current_task = db.taskDao().getTaskById(current_task_id)


    val textfamilyInterMedium = FontFamily(Font(R.font.intermedium))
    val textfamilyInterBold = FontFamily(Font(R.font.interbold))
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var taskText by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf("") }
   // var showDialog by remember { mutableStateOf(false) }
    var showDialogTime by remember { mutableStateOf(false) }
    var showDialogCategory by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(0) }
   // var seconds by remember { mutableStateOf(0) }
    var isTimerStopped by remember { mutableStateOf(false) }
    var countDownTimer: CountDownTimer? = null

    var allCategories by remember { mutableStateOf(db.taskDao().allCategory()) }

    var selectedCategory = -1

    val context = LocalContext.current
    val notificationManager = remember {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    val CHANNEL_ID = "timer_channel"
    val notificationId = 123

    val sdfTime = SimpleDateFormat("HH:mm")
    var currentDateAndTime = ""

    taskDate = current_task.date

    taskText = current_task.heading

    // Создаем канал уведомлений (если Android O или выше)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Таймер",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления таймера"
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Функция для создания уведомлений
    fun createBuilder(text: String, title: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.avatar)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    // Настройка ActivityResultLauncher для получения результата из TimerInfoActivity
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isTimerStopped = result.data?.getBooleanExtra("isTimerStopped", false) ?: false
            if (isTimerStopped) {
                countDownTimer?.cancel() // Остановка таймера, если он был остановлен в TimerInfoActivity
                notificationManager.cancel(notificationId) // Удаление уведомления
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Редактирование задачи",
                        fontFamily = textfamilyInterBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        modifier = Modifier
                            .background(Color.White),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            modifier = Modifier
                                .background(Color.White),
                            text = { Text("Изменить дату", fontFamily = textfamilyInterMedium, fontSize = 17.sp) },
                            onClick = {
                                showDatePicker = true
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            modifier = Modifier
                                .background(Color.White),
                            text = { Text("Изменить время",fontFamily = textfamilyInterMedium,fontSize = 17.sp) },
                            onClick = {
                                showDialogTime = true
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            modifier = Modifier
                                .background(Color.White),
                            text = { Text("Изменить категорию",fontFamily = textfamilyInterMedium,fontSize = 17.sp) },
                            onClick = {
                                showDialogCategory = true
                                expanded = false
                            }
                        )

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                content = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //кнопка для сохранения задачи
                        Button(
                            onClick =
                            {
                                if (taskText.isBlank()) {
                                    Toast.makeText(context, "Поле не может быть пустым или содержать только пробелы", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    // Устанавливаем текущее время, если оно пустое
                                    if (currentDateAndTime == "") {
                                        currentDateAndTime = sdfTime.format(Date())
                                    }
                                    // Вставляем задачу в базу данных
                                    db.taskDao().updateTaskNameCategory(taskText, selectedCategory, current_task.id)
                                    Toast.makeText(context, "Задача изменена", Toast.LENGTH_SHORT).show()
                                    controller.navigate("main_screen")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(2, 190, 43, 255)
                            )
                        ) {
                            Text(
                                text = "Сохранить",
                                color = Color.White,
                                fontFamily = textfamilyInterBold
                            )
                        }
                        Button(
                            onClick =
                            {
                                db.taskDao().deleteTask(current_task.id)
                                //controller.navigate("main_screen")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(190, 2, 2, 255)
                            )
                        ) {
                            Text(
                                text = "Удалить",
                                color = Color.White,
                                fontFamily = textfamilyInterBold
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ){
                Text(text=current_task.date)
                Text(text=current_task.start_time)
                if (current_task.category == -1)
                {
                    Text(
                        text="Без категории"
                    )
                }
                else
                {
                    Text(
                        text=db.taskDao().categoryWithId(current_task.category)
                    )
                }
            }
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Фиксированная высота
                    .background(Color.White, RoundedCornerShape(25.dp))
                    .verticalScroll(scrollState) // Применяем вертикальную прокрутку
            ) {

                TextField(
                    value = taskText,
                    onValueChange = { newText ->
                        // Ограничиваем текст до 4 строк и до 100 символов
                        val lines = newText.lines() // Получаем строки из нового текста
                        if (lines.size <= 4 && newText.length <= 100) {
                            taskText = newText // Если все условия выполнены, обновляем текст
                        } else if (lines.size > 4) {
                            // Если количество строк превышает 4, берем только первые 4 строки
                            taskText =
                                lines.take(4).joinToString("\n") // Объединяем строки обратно
                        } else if (newText.length > 100) {
                            // Если длина текста превышает 100 символов, обрезаем его
                            taskText = newText.take(100) // Берем только первые 100 символов
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        ), // Отступы внутри TextField
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = Color.Black
                    ),
                    placeholder = {
                        Text(
                            text = "Задача",
                            fontSize = 20.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontFamily = textfamilyInterBold
                        )
                    },
                    singleLine = false, // Позволяем многострочный ввод
                    maxLines = 4, // Ограничение на 4 строки
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }
        //показ календаря
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            val currentTimeMillis = System.currentTimeMillis() // Получаем текущее время в миллисекундах

                            // Проверяем, что выбранная дата не меньше текущей
                            if (selectedMillis >= currentTimeMillis) {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                taskDate = sdf.format(Date(selectedMillis)) // Форматируем и сохраняем выбранную дату
                                onDateSelected(selectedMillis) // Вызываем метод, если выбранная дата корректна
                            } else {
                                // Здесь можно добавить уведомление о том, что дата выбрана неверно
                                Toast.makeText(context, "Нельзя выбирать предыдущие даты", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDatePicker = false // Закрываем диалог
                    }) {
                        Text("Ок", fontFamily = textfamilyInterMedium)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Отмена", fontFamily = textfamilyInterMedium)
                    }
                }
            ) {
                DatePicker(state = datePickerState) // Отображаем DatePicker
            }
        }

        if (showDialogTime) {
            AlertDialog(
                onDismissRequest = { showDialogTime = false },
                title = { Text("Время",fontFamily = textfamilyInterMedium) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimePicker("Часы",) { hours = it }
                        TimePicker("Минуты") { minutes = it }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val sdfTime = SimpleDateFormat("HH:mm")

                            val customCalendar = Calendar.getInstance()
                            customCalendar.set(Calendar.HOUR_OF_DAY, hours)
                            customCalendar.set(Calendar.MINUTE, minutes)

                            currentDateAndTime = sdfTime.format(customCalendar.time)

                            showDialogTime = false

                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = Color(2, 190, 43, 255)
                        )
                    ) {
                        Text("Сохранить",fontFamily = textfamilyInterMedium)
                    }
                    // все что выше это логика одной кнопки!!!!
                },
                dismissButton = {
                    Button(
                        onClick = { showDialogTime = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(190, 2, 2, 255)
                        )
                    ) {
                        Text("Отмена",fontFamily = textfamilyInterMedium)
                    }
                }
            )
        }
        if (showDialogCategory) {
            AlertDialog(
                onDismissRequest = { showDialogCategory = false },
                title = { Text("Категории",fontFamily = textfamilyInterMedium) },
                text = {
                    LazyColumn {
                        items(allCategories.size){
                            Button(
                                onClick =
                                {
                                    selectedCategory = allCategories[it].id
                                    showDialogCategory = false

                                }, colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(allCategories[it].color)
                                )
                            ) {
                                Text(allCategories[it].name,fontFamily = textfamilyInterMedium)
                            }

                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {


                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = Color(2, 190, 43, 255)
                        )
                    ) {
                        Text("Сохранить",fontFamily = textfamilyInterMedium)
                    }
                    // все что выше это логика одной кнопки!!!!
                },
                dismissButton = {
                    Button(
                        onClick = { showDialogCategory= false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(190, 2, 2, 255)
                        )
                    ) {
                        Text("Отмена",fontFamily = textfamilyInterMedium)
                    }
                }
            )
        }
    }
}

    data class statisticClass(
        val categoryName: String,
        val categoryId: Int,
        val spentTimeString: String,
        val spentTimeLong: Long,
        var pieceOfCake: Float)

fun getPieChartData(statistics: List<statisticClass>): PieChartData {
    // Создаем список срезов для круговой диаграммы
    val slices = statistics.map { stat ->
        PieChartData.Slice(
            label = stat.categoryName,     // Название категории
            value = stat.pieceOfCake,      // Доля времени от общего (в процентах)
            color = Color(0xFF666A86)      // Временный цвет, который можно изменить позже
        )
    }

    // Создаем и возвращаем объект PieChartData
    return PieChartData(
        slices = slices,
        plotType = PlotType.Pie
    )
}

fun getCategoryTimeStrings(statistics: List<statisticClass>): List<Pair<String, String>> {
    val sdf = SimpleDateFormat("HH 'часов' mm 'минут'", Locale.getDefault())

    return statistics.map { stat ->
        // Преобразуем `spentTimeLong` (в миллисекундах) в `Date`, чтобы отформатировать его в `HH:mm`
        val timeSpentString = sdf.format(Date(stat.spentTimeLong))
        stat.categoryName to timeSpentString
    }
}

//страница статистики
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun statistic(controller: NavHostController, db: AppDatabase) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val textfamilyInterMedium = FontFamily(Font(R.font.intermedium))
    val textfamilyInterBold = FontFamily(Font(R.font.interbold))

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDate = sdf.format(Date())

    var selectedDate by remember { mutableStateOf(currentDate) }
    var statistic by remember { mutableStateOf(emptyList<statisticClass>()) }
    var lazyStatistic by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

    // Функция для получения статистики на основе выбранной даты
    fun getAllStatistic(date1: String): List<statisticClass> {
        val statistics = db.taskDao().getStatisticWithDate(date1)
        val groupedStatistics = statistics
            .groupBy { it.category }
            .map { (categoryId, statList) ->
                val totalSpentTimeForCategory = statList.sumOf { statistic ->
                    timeStringToMilliseconds(statistic.time_spent)
                }

                val categoryName = if (categoryId == -1) {
                    "Нет категории"
                } else {
                    db.taskDao().categoryWithId(categoryId) ?: "Неизвестная категория"
                }

                statisticClass(
                    categoryName = categoryName,
                    categoryId = categoryId,
                    spentTimeString = "",
                    spentTimeLong = totalSpentTimeForCategory,
                    pieceOfCake = 0f
                )
            }

        val totalSpentTime = groupedStatistics.sumOf { it.spentTimeLong }

        return groupedStatistics.map { stat ->
            val pieceOfCake = if (totalSpentTime > 0) {
                (stat.spentTimeLong.toFloat() / totalSpentTime * 100)
            } else {
                0f
            }

            stat.copy(pieceOfCake = pieceOfCake)
        }
    }

    // Обновляем данные после изменения выбранной даты
    LaunchedEffect(selectedDate) {
        statistic = getAllStatistic(selectedDate)
        lazyStatistic = getCategoryTimeStrings(statistic)
    }

    // Данные для графика
    val pieChartData = getPieChartData(statistic)

    // Конфигурация графика
    val pieChartConfig = PieChartConfig(
        isAnimationEnable = false,
        showSliceLabels = true,
        animationDuration = 1500,
        labelType = PieChartConfig.LabelType.PERCENTAGE
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Затраченное время",
                fontSize = 25.sp,
                fontFamily = textfamilyInterBold
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { selectedMillis ->
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                selectedDate = sdf.format(Date(selectedMillis))
                            }
                            showDatePicker = false
                        }) {
                            Text("OK", fontFamily = textfamilyInterMedium)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Отмена", fontFamily = textfamilyInterMedium)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            Divider(thickness = 2.dp, color = Color.Black)
            Row(
                Modifier
                    .clickable { showDatePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate,
                    fontSize = 20.sp,
                    fontFamily = textfamilyInterBold
                )
                Icon(
                    Icons.Default.ArrowDropDown, contentDescription = "",
                    Modifier
                        .size(40.dp)
                )
            }

            if (statistic.isEmpty()) {
                // Если статистики нет, показываем сообщение
                Text(
                    text = "В этот день не было выполненых задач",
                    fontSize = 20.sp,
                    fontFamily = textfamilyInterBold,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Иначе показываем график и список категорий
                PieChart(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(400.dp),
                    pieChartData,
                    pieChartConfig
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lazyStatistic.size) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color.Black, shape = RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lazyStatistic[it].first,
                                fontSize = 17.sp, fontFamily = textfamilyInterMedium
                            )
                            Text(
                                text = lazyStatistic[it].second,
                                fontSize = 17.sp, fontFamily = textfamilyInterMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// в душе не ебу че эт
fun onDateSelected(selectedDateMillis: Long?) {
    // Проверьте, что выбранная дата не равна null
    selectedDateMillis?.let {
        // Здесь можно обработать выбранную дату
        // Например, преобразовать в человекочитаемый формат или сохранить в состоянии
        val selectedDate = java.util.Date(it)
        // Вывод или обработка даты
        println("Выбрана дата: $selectedDate")
    }
}


//все что ниже это для стартовой страницы с иконкой приложения!!!
@Composable
fun SplashScreen(controller: NavHostController) {

    val textfamilyInterBold = FontFamily(Font(R.font.interbold))
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2000,
            easing = LinearEasing
        )
    )

    LaunchedEffect(Unit) {
        isVisible = true
        delay(5000)
        controller.navigate("main_screen") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Центрируем изображение и текст "TimeCheck"
        Column(
            modifier = Modifier.align(Alignment.Center), // Центрирование колонки
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.icon),
                contentDescription = "Логотип",
                modifier = Modifier
                    .size(500.dp)
                    .graphicsLayer(alpha = alpha)
                    .padding(16.dp)
            )
            Text(
                text = "TimeCheck",
                fontSize = 45.sp,
                fontFamily = textfamilyInterBold,
                color = Color(5, 10, 126, 255),
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
        }

        // Закрепляем "by Pelicanus" внизу экрана
        Text(
            text = "by Pelicanus",
            fontSize = 30.sp,
            fontFamily = textfamilyInterBold,
            color = Color(5, 10, 126, 255),
            modifier = Modifier
                .align(Alignment.BottomCenter) // Закрепление внизу
                .padding(bottom = 20.dp) // Отступ от нижнего края
                .graphicsLayer(alpha = alpha)
        )
    }
}
